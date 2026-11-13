import argparse
import contextlib
import datetime
import email.parser
import importlib.util
import io
import ipaddress
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

APP_SERVICES = ("ml-service", "backend", "frontend")
IMAGE_KEYS = {"ml-service": "ML_IMAGE", "backend": "BACKEND_IMAGE", "frontend": "FRONTEND_IMAGE"}
SPEC = importlib.util.spec_from_file_location("preview_validator", Path(__file__).with_name("validate-config.py"))
VALIDATOR = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VALIDATOR)


class DeployError(RuntimeError):
    pass


def require(condition, message):
    if not condition:
        raise DeployError(message)


class Settings:
    KEYS = ("PREVIEW_URL", "PREVIEW_BASE_DIR", "PREVIEW_COMPOSE_PROJECT", "PREVIEW_FRONTEND_PORT", "GITHUB_REPOSITORY")

    def __init__(self, values):
        require(isinstance(values, dict) and set(values) == set(self.KEYS), "Invalid deployment settings")
        require(all(isinstance(values[k], str) and values[k] and values[k] == values[k].strip()
                    for k in self.KEYS), "Required deployment settings are missing or invalid")
        self.values = values.copy()
        self.origin = values["PREVIEW_URL"]
        VALIDATOR.validate_origin(self.origin)
        self.base_dir = values["PREVIEW_BASE_DIR"]
        require(re.fullmatch(r"/[A-Za-z0-9_./-]+", self.base_dir)
                and all(part not in ("", ".", "..") for part in self.base_dir.split("/")[1:])
                and len(PurePosixPath(self.base_dir).parts) >= 3, "PREVIEW_BASE_DIR must be an absolute directory below the SSH user's home")
        self.project = values["PREVIEW_COMPOSE_PROJECT"]
        require(re.fullmatch(r"[a-z0-9][a-z0-9_-]{0,62}", self.project), "Invalid PREVIEW_COMPOSE_PROJECT")
        port = values["PREVIEW_FRONTEND_PORT"]
        require(re.fullmatch(r"[0-9]{1,5}", port) and 1024 <= int(port) <= 65535
                and int(port) not in (3000, 3001), "Invalid or reserved PREVIEW_FRONTEND_PORT")
        self.frontend_port = str(int(port))
        self.prefix = VALIDATOR.image_prefix(values["GITHUB_REPOSITORY"])
        self.source = "https://github.com/" + values["GITHUB_REPOSITORY"]

    @classmethod
    def from_env(cls):
        return cls({key: os.environ.get(key, "") for key in cls.KEYS})

    @property
    def validation_args(self):
        return {"project": self.project, "origin": self.origin, "prefix": self.prefix}


def check_path(path, *, private=False):
    require(path.exists() and not path.is_symlink(), "Missing or symlinked deployment path: " + path.name)
    require(path.stat().st_uid == getattr(os, "getuid", lambda: 0)(), "Deployment paths must belong to the current user")
    if private:
        require(path.is_file() and path.stat().st_mode & 0o077 == 0, "Private deployment files must have mode 0600")


def atomic_write(path, content):
    temporary = path.with_name(path.name + ".deploy-tmp")
    fd = os.open(temporary, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as output:
            output.write(content)
        temporary.replace(path)
    finally:
        if temporary.exists():
            temporary.unlink()


def replace_env(text, changes):
    lines = text.splitlines()
    for key, value in changes.items():
        positions = [i for i, line in enumerate(lines) if line.startswith(key + "=")]
        require(len(positions) <= 1, "Duplicate deployment env key: " + key)
        if positions:
            lines[positions[0]] = key + "=" + value
        else:
            lines.append(key + "=" + value)
    return "\n".join(lines) + "\n"


def validate_release(metadata, release, settings):
    require(isinstance(metadata, dict) and set(metadata) == {"commit", "images"}, "Invalid release manifest")
    sha, images = metadata["commit"], metadata["images"]
    require(isinstance(sha, str) and re.fullmatch(r"[0-9a-f]{40}", sha), "Invalid release commit")
    require(re.fullmatch(r"actions-[0-9]+-[0-9]+-" + sha, release), "Release and commit do not match")
    require(isinstance(images, dict) and set(images) == set(APP_SERVICES), "Expected three release images")
    for service in APP_SERVICES:
        suffix = "ml" if service == "ml-service" else service
        require(isinstance(images[service], str) and re.fullmatch(
            re.escape(settings.prefix + "-" + suffix + "@sha256:") + r"[0-9a-f]{64}", images[service]),
            "Invalid pinned release image: " + service)
    return sha, images


class LoopbackHTTPSClient:
    def __init__(self, command, origin):
        self.command = command
        self.origin = origin

    def open(self, request, *, timeout):
        url = request if isinstance(request, str) else request.full_url
        require(url.startswith(self.origin + "/"), "Unexpected smoke-check origin")
        with tempfile.TemporaryDirectory(prefix="cardio-preview-http-") as directory:
            headers_path, body_path = Path(directory) / "headers", Path(directory) / "body"
            args = ["curl", "--silent", "--show-error", "--noproxy", "*", "--max-time", str(timeout),
                    "--resolve", urllib.parse.urlsplit(self.origin).hostname + ":443:127.0.0.1",
                    "--dump-header", str(headers_path), "--output", str(body_path), "--write-out", "%{http_code}"]
            if not isinstance(request, str):
                for key, value in request.header_items():
                    args += ["--header", key + ": " + value]
                if request.data is not None:
                    args += ["--data-binary", request.data.decode("utf-8")]
            status = int(self.command(args + [url], timeout=timeout + 5).strip())
            blocks = headers_path.read_bytes().strip().split(b"\r\n\r\n")
            headers = email.parser.BytesParser().parsebytes(blocks[-1].partition(b"\r\n")[2])
            response = io.BytesIO(body_path.read_bytes())
        if status >= 400:
            raise urllib.error.HTTPError(url, status, "Preview HTTP check failed", headers, response)
        response.status, response.headers = status, headers
        return response


class Deployment:
    def __init__(self, release, *, settings=None, home=None, base_dir=None):
        self.settings = settings if settings is not None else Settings.from_env()
        self.home = Path.home() if home is None else Path(home)
        self.base = Path(self.settings.base_dir) if base_dir is None else Path(base_dir)
        self.incoming = self.base / "incoming" / release
        self.release = release
        self.archive = self.base / "releases" / release
        self.env_file = self.base / ".env.preview"
        self.compose_file = self.base / "docker-compose.preview.yaml"
        self.cli_env = {k: v for k, v in os.environ.items() if k in {
            "PATH", "HOME", "DOCKER_CONFIG", "XDG_RUNTIME_DIR", "SSH_AUTH_SOCK", "LANG", "SYSTEMROOT",
        }}

    def check_directories(self):
        require(self.base.is_absolute() and self.base.is_relative_to(self.home) and self.base != self.home,
                "PREVIEW_BASE_DIR must be below the SSH user's home")
        current = self.home
        check_path(current)
        for part in self.base.relative_to(self.home).parts:
            require(part not in (".", ".."), "Invalid deployment directory")
            current = current / part
            check_path(current)

    def command(self, args, *, timeout=180, destination=None):
        try:
            result = subprocess.run(args, env=self.cli_env, stdout=destination or subprocess.PIPE,
                                    stderr=subprocess.PIPE, timeout=timeout, check=False)
        except (OSError, subprocess.TimeoutExpired):
            raise DeployError("Deployment command could not complete; inspect the preview on the VPS") from None
        require(result.returncode == 0, "Deployment command failed; inspect the preview on the VPS")
        return result.stdout.decode("utf-8") if destination is None else ""

    def compose(self, *args, candidate=False, images=None, timeout=240):
        compose_file = self.incoming / "docker-compose.preview.yaml" if candidate else self.compose_file
        previous = self.cli_env.copy()
        self.cli_env.update({IMAGE_KEYS[k]: v for k, v in (images or {}).items()})
        try:
            return self.command(["docker", "compose", "--env-file", str(self.env_file), "-p", self.settings.project,
                                 "-f", str(compose_file), *args], timeout=timeout)
        finally:
            self.cli_env = previous

    def inspect(self, name):
        return json.loads(self.command(["docker", "inspect", name]))[0]

    def container(self, service):
        container = self.inspect(self.settings.project + "-" + service + "-1")
        labels = container["Config"]["Labels"]
        require(labels.get("com.docker.compose.project") == self.settings.project
                and labels.get("com.docker.compose.service") == service, "Unexpected container ownership")
        return container

    def other_containers(self):
        names = self.command(["docker", "ps", "-a", "--format", "{{.Names}}"])
        return {name: self.inspect(name)["Id"] for name in names.splitlines()
                if not name.startswith(self.settings.project + "-")}

    @staticmethod
    def environment(container):
        return dict(item.split("=", 1) for item in container["Config"]["Env"] if "=" in item)

    def preflight(self):
        require(re.fullmatch(r"actions-[0-9]+-[0-9]+-[0-9a-f]{40}", self.release), "Invalid release directory")
        self.check_directories()
        for path in (self.base / "incoming", self.incoming):
            check_path(path)
        for name in ("release.json", "docker-compose.preview.yaml", "deploy-manual.py", "validate-config.py"):
            check_path(self.incoming / name)
        check_path(self.env_file, private=True)
        check_path(self.compose_file)
        require(shutil.disk_usage(self.base).free >= 4 * 1024 ** 3, "At least 4 GiB free disk space required before pulling images")
        require(not self.archive.exists(), "Release already attempted; use a new run attempt")
        self.metadata = json.loads((self.incoming / "release.json").read_text(encoding="utf-8"))
        self.sha, self.images = validate_release(self.metadata, self.release, self.settings)
        candidate = json.loads(self.compose("config", "--format", "json", candidate=True, images=self.images))
        VALIDATOR.validate_manual(candidate, self.sha, self.images, **self.settings.validation_args)
        require(str(candidate["services"]["frontend"]["ports"][0]["published"]) == self.settings.frontend_port,
                "Manual preview must keep the configured frontend port")
        self.old = {s: self.container(s) for s in VALIDATOR.BUDGETS}
        for service in ("postgres", "redis", "ml-service"):
            target = {"postgres": "/var/lib/postgresql/data", "redis": "/data", "ml-service": "/app/app/models"}[service]
            volume = {"postgres": "postgres_data", "redis": "redis_data", "ml-service": "ml_models"}[service]
            mounts = self.old[service]["Mounts"]
            require(len(mounts) == 1 and mounts[0].get("Type") == "volume"
                    and mounts[0].get("Name") == self.settings.project + "_" + volume
                    and mounts[0].get("Destination") == target, "Existing preview data mount is unexpected")
        for service in self.old:
            require(self.old[service]["State"].get("Health", {}).get("Status") == "healthy",
                    "Existing preview is not healthy: " + service)
        for service, keys in (("postgres", ("POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD")),
                              ("redis", ("REDISCLI_AUTH",))):
            existing = self.environment(self.old[service])
            desired = candidate["services"][service]["environment"]
            require(all(existing.get(k) == desired[k] for k in keys), "Existing database/Redis settings differ from env")
        self.others = self.other_containers()
        self.model_version = self.command(["docker", "exec", self.old["ml-service"]["Id"], "python", "-c",
            "from pathlib import Path; print(Path('/app/app/models/CURRENT').read_text().strip())"]).strip()
        require(re.fullmatch(r"[A-Za-z0-9._-]{1,128}", self.model_version), "Invalid current model version")
        self.smoke(loopback=True)
        print("Preflight passed: existing manual preview, three project data volumes, HTTPS configuration.", flush=True)

    @contextlib.contextmanager
    def lock(self):
        import fcntl
        self.check_directories()
        lock_path = self.base / ".deploy.lock"
        require(not lock_path.is_symlink(), "Deployment lock must not be a symlink")
        with lock_path.open("a") as lock:
            try:
                fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError:
                raise DeployError("Another manual preview deployment is running") from None
            yield

    def pull(self):
        for service, ref in self.images.items():
            print("Pulling verified release image: " + service, flush=True)
            self.command(["docker", "pull", ref], timeout=600)
            image = self.inspect(ref)
            labels = image["Config"].get("Labels") or {}
            require(labels.get("org.opencontainers.image.revision") == self.sha
                    and labels.get("org.opencontainers.image.source", "").lower() == self.settings.source.lower(),
                    "Image provenance does not match the selected commit: " + service)
            require(image.get("Os") == "linux" and image.get("Architecture") == "amd64", "Unsupported image platform")

    def snapshot(self):
        require(shutil.disk_usage(self.base).free >= 2 * 1024 ** 3, "At least 2 GiB free disk space required")
        releases = self.base / "releases"
        releases.mkdir(mode=0o700, exist_ok=True)
        check_path(releases)
        self.archive.mkdir(mode=0o700)
        shutil.copyfile(self.env_file, self.archive / "previous.env")
        (self.archive / "previous.env").chmod(0o600)
        shutil.copyfile(self.compose_file, self.archive / "previous-compose.yaml")
        self.rollback_images = {}
        for service in APP_SERVICES:
            tag = "cardio-preview-rollback:" + self.release + "-" + service
            self.command(["docker", "tag", self.old[service]["Image"], tag])
            self.rollback_images[service] = tag
        with (self.archive / "postgres.sql").open("wb") as output:
            self.command(["docker", "exec", self.old["postgres"]["Id"], "sh", "-c",
                          'exec pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-acl'], destination=output)
        self.command(["docker", "exec", self.old["redis"]["Id"], "redis-cli", "SAVE"])
        self.command(["docker", "cp", self.old["redis"]["Id"] + ":/data/dump.rdb", str(self.archive / "redis.rdb")])
        with (self.archive / "models.tar").open("wb") as output:
            self.command(["docker", "exec", self.old["ml-service"]["Id"], "tar", "-C", "/app/app/models", "-cf", "-", "."], destination=output)
        print("Preview configuration, database, Redis and models backed up.", flush=True)

    def install(self):
        changes = {IMAGE_KEYS[s]: ref for s, ref in self.images.items()}
        atomic_write(self.env_file, replace_env(self.env_file.read_text(encoding="utf-8"), changes))
        atomic_write(self.compose_file, (self.incoming / "docker-compose.preview.yaml").read_text(encoding="utf-8"))

    def start(self):
        self.compose("up", "-d", "--no-deps", "--wait", "--wait-timeout", "180", "ml-service", "backend", timeout=420)
        self.compose("up", "-d", "--no-deps", "--wait", "--wait-timeout", "90", "frontend", timeout=180)
        self.reconcile_proxy()

    def reconcile_proxy(self):
        networks = self.container("frontend")["NetworkSettings"]["Networks"]
        require(set(networks) == {self.settings.project + "_default"}, "Unexpected Frontend network")
        address = str(ipaddress.IPv4Address(networks[self.settings.project + "_default"]["IPAddress"]))
        trust = "^" + re.escape(address) + "$"
        if self.environment(self.container("backend")).get("TRUSTED_PROXY_REGEX") != trust:
            atomic_write(self.env_file, replace_env(self.env_file.read_text(encoding="utf-8"), {"TRUSTED_PROXY_REGEX": trust}))
            self.compose("up", "-d", "--no-deps", "--wait", "--wait-timeout", "180", "backend", timeout=240)
        self.compose("exec", "-T", "frontend", "nginx", "-s", "reload")

    def ensure_preserved(self):
        require(self.other_containers() == self.others, "A container outside the manual preview changed")
        for service in ("postgres", "redis"):
            require(self.container(service)["Id"] == self.old[service]["Id"], "A data service was recreated")

    def smoke(self, *, loopback=False):
        origin = self.settings.origin
        client = LoopbackHTTPSClient(self.command, origin) if loopback else urllib.request.build_opener(urllib.request.ProxyHandler({}))
        with client.open(origin + "/", timeout=20) as response:
            require(response.status == 200, "Preview HTTPS page failed")
        for kind in ("city", "region"):
            data = {"modelKind": kind, "year": 2021 if kind == "city" else None, "chronicLungDisease": 0,
                    "genderId": 1, "age": 55, "height": 175, "weight": 80,
                    "alcoholId": 1, "professionId": 2, "regionId": 5}
            request = urllib.request.Request(origin + "/api/survey/submit", data=json.dumps(data).encode(),
                headers={"Content-Type": "application/json", "Origin": origin}, method="POST")
            with client.open(request, timeout=30) as response:
                body = json.load(response)
                require(isinstance(body, dict), "Unexpected preview response")
                predictions = body.get("predictions", [])
                require(isinstance(predictions, list) and len(predictions) == 1
                        and isinstance(predictions[0], dict), "Unexpected preview prediction list")
                require(response.status == 200
                        and predictions[0].get("modelKind") == kind
                        and type(predictions[0].get("probability")) is int
                        and 0 <= predictions[0]["probability"] <= 100
                        and body.get("modelVersion") == self.model_version, "Unexpected preview prediction")
                require(bool(response.headers.get("Strict-Transport-Security")), "HTTPS proxy trust is not working")
            print("PASS HTTPS " + kind + " prediction; active model preserved.", flush=True)
        try:
            with client.open(origin + "/api/admin/model", timeout=20):
                pass
        except urllib.error.HTTPError as error:
            with error:
                require(error.code == 401, "Admin API protection check failed")
        else:
            raise DeployError("Admin API is unexpectedly public")

    def verify_release(self):
        for service, ref in self.images.items():
            container = self.container(service)
            require(container["State"].get("Health", {}).get("Status") == "healthy"
                    and container["Config"]["Image"] == ref, "Release container is not healthy or has the wrong image")
        self.ensure_preserved()
        self.smoke(loopback=True)

    def rollback(self):
        old = (self.archive / "previous.env").read_text(encoding="utf-8")
        atomic_write(self.env_file, replace_env(old, {IMAGE_KEYS[s]: tag for s, tag in self.rollback_images.items()}))
        atomic_write(self.compose_file, (self.archive / "previous-compose.yaml").read_text(encoding="utf-8"))
        self.start()
        self.ensure_preserved()
        self.smoke(loopback=True)

    def deploy(self):
        with self.lock():
            self.preflight()
            self.pull()
            self.snapshot()
            try:
                self.install()
                self.start()
                self.verify_release()
            except BaseException:
                print("Deployment failed; restoring the previous preview images/configuration.", flush=True)
                try:
                    self.rollback()
                except BaseException:
                    raise DeployError("Preview rollback failed; inspect the protected backup directory on the VPS") from None
                raise DeployError("Deployment failed; previous preview restored. Database and model volumes retained.") from None
            report = {"commit": self.sha, "images": self.images,
                      "modelVersion": self.model_version, "status": "healthy",
                      "completedAt": datetime.datetime.now(datetime.timezone.utc).isoformat()}
            atomic_write(self.archive / "report.json", json.dumps(report, indent=2) + "\n")
            print("Manual preview updated.", flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("release", help="actions-<run-id>-<attempt>-<40-char-commit>")
    parser.add_argument("--check", action="store_true", help="Validate the existing preview and candidate without pulling or updating")
    parser.add_argument("--smoke-only", metavar="MODEL_VERSION", help="Check public HTTPS endpoints without connecting to Docker")
    parser.add_argument("--settings", type=Path, help="Private deployment settings JSON delivered by Actions")
    args = parser.parse_args()
    os.umask(0o077)
    try:
        if args.settings:
            check_path(args.settings, private=True)
            settings = Settings(json.loads(args.settings.read_text(encoding="utf-8")))
        else:
            settings = Settings.from_env()
        deployment = Deployment(args.release, settings=settings)
        if args.smoke_only:
            require(re.fullmatch(r"[A-Za-z0-9._-]{1,128}", args.smoke_only), "Invalid expected model version")
            deployment.model_version = args.smoke_only
            deployment.smoke()
        elif args.check:
            deployment.preflight()
        else:
            deployment.deploy()
    except (DeployError, VALIDATOR.ConfigError) as error:
        print("Manual preview operation failed: " + str(error), file=sys.stderr)
        return 1
    except (ValueError, KeyError, TypeError, OSError, urllib.error.URLError):
        print("Manual preview operation failed. Inspect the preview/backup on the VPS; secrets were not printed.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
