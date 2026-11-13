import base64
import ipaddress
import json
import math
import os
import re
import sys
import urllib.parse

BUDGETS = {
    "postgres": (256, 0.10), "redis": (64, 0.05),
    "ml-service": (512, 0.40), "backend": (768, 0.40), "frontend": (64, 0.05),
}
SERVICE_FIELDS = {
    "image", "command", "entrypoint", "environment", "healthcheck", "cpus",
    "mem_limit", "networks", "restart", "depends_on", "ports", "volumes",
}


class ConfigError(ValueError):
    pass


def require(condition, field, reason):
    if not condition:
        raise ConfigError(field + ": " + reason)


def image_prefix(repository):
    require(isinstance(repository, str) and re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository),
            "GITHUB_REPOSITORY", "expected owner/repository")
    return "ghcr.io/" + repository.lower() + "-preview"


def validate_origin(origin):
    require(isinstance(origin, str), "PREVIEW_URL", "HTTPS origin required")
    parsed = urllib.parse.urlsplit(origin)
    require(parsed.scheme == "https" and parsed.hostname and parsed.netloc == parsed.hostname
            and re.fullmatch(r"[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?", parsed.hostname)
            and "." in parsed.hostname and not parsed.path and not parsed.query and not parsed.fragment,
            "PREVIEW_URL", "expected https://hostname without a path, port or credentials")


def validate(model, project, sha, *, prefix=None, origin=None):
    require(re.fullmatch(r"cardio-preview-pr-[1-9][0-9]*", project), "project", "invalid PR project")
    require(re.fullmatch(r"[0-9a-f]{40}", sha), "commit", "invalid SHA")
    prefix = prefix or image_prefix(os.environ.get("GITHUB_REPOSITORY", ""))
    origin = origin or os.environ.get("PREVIEW_URL", "")
    validate_origin(origin)
    images = {name: prefix + "-" + suffix + ":" + sha
              for name, suffix in (("ml-service", "ml"), ("backend", "backend"), ("frontend", "frontend"))}
    _validate(model, project, images, origin)


def validate_manual(model, sha, images, *, project, origin, prefix):
    validate_origin(origin)
    require(re.fullmatch(r"[0-9a-f]{40}", sha), "commit", "invalid SHA")
    require(isinstance(images, dict) and set(images) == {"ml-service", "backend", "frontend"},
            "images", "expected three release images")
    for service, suffix in (("ml-service", "ml"), ("backend", "backend"), ("frontend", "frontend")):
        require(isinstance(images[service], str) and re.fullmatch(
            re.escape(prefix + "-" + suffix + "@sha256:") + r"[0-9a-f]{64}", images[service]),
            service + ".image", "expected an approved image pinned by digest")
    _validate(model, project, images, origin)


def _validate(model, project, images, origin):
    require(isinstance(model, dict), "model", "expected object")
    require(set(model) <= {"name", "services", "networks", "volumes"}, "model", "unsupported top-level fields")
    require(model.get("name") == project, "name", "must match the requested project")
    services = model.get("services")
    require(isinstance(services, dict) and set(services) == set(BUDGETS), "services", "expected the five preview services")
    for name, (memory_mib, cpu_max) in BUDGETS.items():
        service = services[name]
        require(isinstance(service, dict) and set(service) <= SERVICE_FIELDS, name, "unsupported service options")
        require(service.get("entrypoint") is None, name + ".entrypoint", "override is not supported")
        require(name == "redis" or service.get("command") is None, name + ".command", "override is not supported")
        expected = {"postgres": "postgres:16-alpine", "redis": "redis:7-alpine"}.get(name, images.get(name))
        require(service.get("image") == expected, name + ".image", "must use the approved image and requested SHA")
        try:
            memory = int(service.get("mem_limit", 0))
            cpu = float(service.get("cpus", 0))
        except (ValueError, TypeError, OverflowError):
            raise ConfigError(name + ": invalid resource limits") from None
        require(0 < memory <= memory_mib * 1024 * 1024, name + ".mem_limit", "outside the preview budget")
        require(math.isfinite(cpu) and 0 < cpu <= cpu_max, name + ".cpus", "outside the preview budget")
        health = service.get("healthcheck") or {}
        test = health.get("test", [])
        require(not health.get("disable") and isinstance(test, list) and len(test) >= 2 and test[0] in ("CMD", "CMD-SHELL"),
                name + ".healthcheck", "working healthcheck required")
        require(service.get("restart") == "unless-stopped", name + ".restart", "unexpected policy")
        networks = service.get("networks")
        require(isinstance(networks, dict) and set(networks) == {"default"} and networks["default"] in (None, {}),
                name + ".networks", "only the project default network is supported")
        if name != "frontend":
            require(not service.get("ports"), name + ".ports", "must remain internal")
        expected_mount = {
            "postgres": ("postgres_data", "/var/lib/postgresql/data"),
            "redis": ("redis_data", "/data"),
            "ml-service": ("ml_models", "/app/app/models"),
        }.get(name)
        mounts = service.get("volumes", [])
        if expected_mount:
            require(isinstance(mounts, list) and len(mounts) == 1, name + ".volumes", "one project data volume required")
            mount = mounts[0]
            require(set(mount) <= {"type", "source", "target", "volume", "read_only"}
                    and mount.get("type") == "volume" and mount.get("source") == expected_mount[0]
                    and mount.get("target") == expected_mount[1] and not mount.get("read_only")
                    and mount.get("volume", {}) == {}, name + ".volumes", "unexpected data mount")
        else:
            require(not mounts, name + ".volumes", "mounts are not supported")
        dependencies = {"backend": {"postgres", "redis", "ml-service"}, "frontend": {"backend"}}.get(name, set())
        depends = service.get("depends_on") or {}
        require(set(depends) == dependencies and all(
            isinstance(value, dict) and value.get("condition") == "service_healthy" and value.get("required", True)
            for value in depends.values()), name + ".depends_on", "healthy dependencies required")

    ports = services["frontend"].get("ports", [])
    require(isinstance(ports, list) and len(ports) == 1, "frontend.ports", "one loopback port required")
    port = ports[0]
    require(set(port) <= {"mode", "host_ip", "target", "published", "protocol"}
            and port.get("host_ip") == "127.0.0.1" and port.get("target") == 80
            and port.get("protocol", "tcp") == "tcp" and port.get("mode", "ingress") == "ingress",
            "frontend.ports", "expected 127.0.0.1:<port>:80/tcp")
    published = str(port.get("published", ""))
    require(published.isdecimal() and 1024 <= int(published) <= 65535 and int(published) not in (3000, 3001),
            "frontend.ports", "invalid or reserved host port")

    networks = model.get("networks")
    require(isinstance(networks, dict) and set(networks) == {"default"}, "networks", "only one project network supported")
    network = networks["default"]
    require(isinstance(network, dict) and set(network) <= {"name", "ipam"}
            and network.get("name") == project + "_default" and network.get("ipam", {}) == {},
            "networks.default", "must be a project-owned network without custom IPAM")

    volumes = model.get("volumes")
    require(isinstance(volumes, dict) and set(volumes) == {"postgres_data", "redis_data", "ml_models"}, "volumes", "unexpected data volumes")
    for name, volume in volumes.items():
        require(isinstance(volume, dict) and set(volume) <= {"name"}
                and volume.get("name") == project + "_" + name,
                "volumes." + name, "must use the project-owned local volume")

    backend = services["backend"].get("environment", {})
    postgres = services["postgres"].get("environment", {})
    redis = services["redis"].get("environment", {})
    frontend = services["frontend"].get("environment", {})
    expected_fields = {
        "SPRING_PROFILES_ACTIVE": "prod,preview", "DB_HOST": "postgres", "DB_PORT": "5432",
        "REDIS_HOST": "redis", "REDIS_PORT": "6379", "ML_SERVICE_URL": "http://ml-service:8000",
        "APP_AUTHCOOKIE_SECURE": "true", "ALLOWED_ORIGINS": origin,
        "JAVA_TOOL_OPTIONS": "-Xms128m -Xmx384m",
    }
    secret_fields = {"DB_NAME", "DB_USER", "DB_PASSWORD", "REDIS_PASSWORD", "JWT_SECRET",
                     "BOOTSTRAP_SUPER_ADMIN_EMAIL", "BOOTSTRAP_SUPER_ADMIN_PASSWORD"}
    require(set(backend) <= set(expected_fields) | secret_fields | {"TRUSTED_PROXY_REGEX"}, "backend.environment", "unsupported environment options")
    for name, expected in expected_fields.items():
        require(backend.get(name) == expected, "backend.environment." + name, "unexpected preview setting")
    for name in secret_fields:
        require(isinstance(backend.get(name), str) and bool(backend[name].strip()), "backend.environment." + name, "nonempty value required")
    require(set(postgres) == {"POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD"}, "postgres.environment", "unexpected options")
    for source, target in (("DB_NAME", "POSTGRES_DB"), ("DB_USER", "POSTGRES_USER"), ("DB_PASSWORD", "POSTGRES_PASSWORD")):
        require(backend[source] == postgres[target], "postgres.environment", "database settings must match Backend")
    require(set(redis) == {"REDISCLI_AUTH"} and redis["REDISCLI_AUTH"] == backend["REDIS_PASSWORD"], "redis.environment", "authentication settings must match")
    require(services["redis"].get("command") == [
        "redis-server", "--requirepass", redis["REDISCLI_AUTH"], "--maxmemory", "32mb", "--maxmemory-policy", "noeviction"],
        "redis.command", "unexpected Redis configuration")
    require(not services["ml-service"].get("environment"), "ml-service.environment", "unexpected options")
    require(set(frontend) == {"PREVIEW_PROXY_IP"}, "frontend.environment", "unexpected options")
    try:
        address = ipaddress.IPv4Address(frontend["PREVIEW_PROXY_IP"])
        require(not address.is_unspecified and not address.is_multicast, "frontend.PREVIEW_PROXY_IP", "a single peer address required")
    except ipaddress.AddressValueError:
        raise ConfigError("frontend.PREVIEW_PROXY_IP: a single IPv4 peer address required") from None
    trust = backend.get("TRUSTED_PROXY_REGEX", "(?!)").replace("$$", "$")
    if trust != "(?!)":
        try:
            address = ipaddress.IPv4Address(trust.removeprefix("^").removesuffix("$").replace(r"\.", "."))
        except ipaddress.AddressValueError:
            raise ConfigError("backend.TRUSTED_PROXY_REGEX: only disabled or exact IPv4 trust supported") from None
        require(trust == "^" + re.escape(str(address)) + "$", "backend.TRUSTED_PROXY_REGEX", "only exact address trust supported")
    try:
        jwt = base64.b64decode(backend["JWT_SECRET"], validate=True)
    except ValueError:
        raise ConfigError("backend.JWT_SECRET: valid Base64 required") from None
    require(len(jwt) >= 32, "backend.JWT_SECRET", "at least 32 decoded bytes required")


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: validate-config.py <project> <commit-sha>")
    try:
        validate(json.load(sys.stdin), sys.argv[1], sys.argv[2])
    except ConfigError as error:
        raise SystemExit("Preview configuration rejected: " + str(error)) from None
    except (json.JSONDecodeError, TypeError, KeyError, AttributeError):
        raise SystemExit("Preview configuration rejected: invalid Compose model") from None
    print("Preview configuration validated.")


if __name__ == "__main__":
    main()
