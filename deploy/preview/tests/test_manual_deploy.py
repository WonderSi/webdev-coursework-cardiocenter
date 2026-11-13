import contextlib
import copy
import importlib.util
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
import urllib.error
from unittest.mock import Mock, patch

SCRIPT = Path(__file__).resolve().parents[1] / "deploy-manual.py"
SPEC = importlib.util.spec_from_file_location("manual_deploy", SCRIPT)
DEPLOY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DEPLOY)
TEST_ENV = {
    "PREVIEW_URL": "https://preview.example.invalid",
    "PREVIEW_BASE_DIR": "/home/deployer/preview",
    "PREVIEW_COMPOSE_PROJECT": "example-preview",
    "PREVIEW_FRONTEND_PORT": "18080",
    "GITHUB_REPOSITORY": "example/cardiocenter",
}
SETTINGS = DEPLOY.Settings(TEST_ENV)
SHA = "a" * 40
RELEASE = "actions-123-1-" + SHA
IMAGES = {s: SETTINGS.prefix + "-" + ("ml" if s == "ml-service" else s) + "@sha256:" + "b" * 64
          for s in DEPLOY.APP_SERVICES}


def model(project=SETTINGS.project, *, pr=False):
    backend = {
        "SPRING_PROFILES_ACTIVE": "prod,preview", "DB_HOST": "postgres", "DB_PORT": "5432",
        "DB_NAME": "preview", "DB_USER": "preview", "DB_PASSWORD": "synthetic-db-password",
        "REDIS_HOST": "redis", "REDIS_PORT": "6379", "REDIS_PASSWORD": "synthetic-redis-password",
        "ML_SERVICE_URL": "http://ml-service:8000", "APP_AUTHCOOKIE_SECURE": "true",
        "ALLOWED_ORIGINS": "https://pr-74.preview.example.invalid" if pr else SETTINGS.origin,
        "JAVA_TOOL_OPTIONS": "-Xms128m -Xmx384m", "JWT_SECRET": "eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHg=",
        "BOOTSTRAP_SUPER_ADMIN_EMAIL": "example@example.invalid",
        "BOOTSTRAP_SUPER_ADMIN_PASSWORD": "synthetic-admin-password", "TRUSTED_PROXY_REGEX": r"^172\.20\.0\.6$",
    }
    services = {}
    for name, (memory, cpu) in DEPLOY.VALIDATOR.BUDGETS.items():
        suffix = "ml" if name == "ml-service" else name
        image = {"postgres": "postgres:16-alpine", "redis": "redis:7-alpine"}.get(name)
        if image is None:
            image = SETTINGS.prefix + "-" + suffix + ":" + SHA if pr else IMAGES[name]
        services[name] = {"image": image, "mem_limit": memory * 1024 ** 2, "cpus": cpu,
                          "restart": "unless-stopped", "networks": {"default": None},
                          "healthcheck": {"test": ["CMD", "true"]}}
    services["backend"]["environment"] = backend
    services["postgres"]["environment"] = {"POSTGRES_DB": "preview", "POSTGRES_USER": "preview",
                                          "POSTGRES_PASSWORD": backend["DB_PASSWORD"]}
    services["redis"]["environment"] = {"REDISCLI_AUTH": backend["REDIS_PASSWORD"]}
    services["redis"]["command"] = ["redis-server", "--requirepass", backend["REDIS_PASSWORD"],
                                    "--maxmemory", "32mb", "--maxmemory-policy", "noeviction"]
    services["frontend"]["environment"] = {"PREVIEW_PROXY_IP": "172.20.0.1"}
    services["frontend"]["ports"] = [{"host_ip": "127.0.0.1", "target": 80, "published": SETTINGS.frontend_port, "protocol": "tcp"}]
    for name, volume, target in (("postgres", "postgres_data", "/var/lib/postgresql/data"),
                                 ("redis", "redis_data", "/data"), ("ml-service", "ml_models", "/app/app/models")):
        services[name]["volumes"] = [{"type": "volume", "source": volume, "target": target, "volume": {}}]
    services["backend"]["depends_on"] = {s: {"condition": "service_healthy"} for s in ("postgres", "redis", "ml-service")}
    services["frontend"]["depends_on"] = {"backend": {"condition": "service_healthy"}}
    return {"name": project, "services": services, "networks": {"default": {"name": project + "_default"}},
            "volumes": {s: {"name": project + "_" + s} for s in ("postgres_data", "redis_data", "ml_models")}}


class SettingsTests(unittest.TestCase):
    def test_environment_settings_survive_private_json_delivery(self):
        with patch.dict(os.environ, TEST_ENV, clear=True):
            settings = DEPLOY.Settings.from_env()
        delivered = DEPLOY.Settings(json.loads(json.dumps(settings.values)))
        self.assertEqual(delivered.validation_args, SETTINGS.validation_args)
        self.assertEqual(delivered.base_dir, TEST_ENV['PREVIEW_BASE_DIR'])
        self.assertEqual(delivered.frontend_port, TEST_ENV['PREVIEW_FRONTEND_PORT'])
        self.assertEqual(delivered.prefix, 'ghcr.io/example/cardiocenter-preview')
        self.assertEqual(delivered.source, 'https://github.com/example/cardiocenter')

    def test_missing_settings_and_unsafe_values_fail_without_echoing_values(self):
        for key in TEST_ENV:
            values = dict(TEST_ENV)
            values.pop(key)
            with self.subTest(missing=key), patch.dict(os.environ, values, clear=True), self.assertRaises(DEPLOY.DeployError):
                DEPLOY.Settings.from_env()
        for key, value in (
            ('PREVIEW_URL', 'http://preview.example.invalid'),
            ('PREVIEW_URL', 'https://user:synthetic-private-value@preview.example.invalid'),
            ('PREVIEW_URL', 'https://preview.example.invalid/path'),
            ('PREVIEW_URL', 'https://preview.example.invalid:8443'),
            ('PREVIEW_BASE_DIR', '/home/deployer/../production'),
            ('PREVIEW_BASE_DIR', '/home/deployer/preview;synthetic-private-value'),
            ('PREVIEW_BASE_DIR', 'relative/preview'),
            ('PREVIEW_COMPOSE_PROJECT', 'bad;synthetic-private-value'),
            ('PREVIEW_FRONTEND_PORT', '80'), ('PREVIEW_FRONTEND_PORT', '3000'),
            ('PREVIEW_FRONTEND_PORT', '65536'), ('GITHUB_REPOSITORY', '../example/repo'),
        ):
            with self.subTest(key=key, value=value), self.assertRaises((DEPLOY.DeployError, DEPLOY.VALIDATOR.ConfigError)) as error:
                DEPLOY.Settings({**TEST_ENV, key: value})
            self.assertNotIn('synthetic-private-value', str(error.exception))


class ValidatorTests(unittest.TestCase):
    def test_accepts_manual_digests_and_preserves_pr_contract(self):
        DEPLOY.VALIDATOR.validate_manual(model(), SHA, IMAGES, **SETTINGS.validation_args)
        DEPLOY.VALIDATOR.validate(model("cardio-preview-pr-74", pr=True), "cardio-preview-pr-74", SHA,
                                  prefix=SETTINGS.prefix, origin="https://pr-74.preview.example.invalid")
        with self.assertRaises(DEPLOY.VALIDATOR.ConfigError):
            DEPLOY.VALIDATOR.validate(model(), SETTINGS.project, SHA, prefix=SETTINGS.prefix, origin=SETTINGS.origin)

    def test_rejects_unsafe_compose_changes(self):
        cases = []
        for name, value in (("ports", [{"host_ip": "0.0.0.0", "target": 80, "published": SETTINGS.frontend_port}]),
                            ("volumes", [{"type": "bind", "source": "/", "target": "/host"}]),
                            ("privileged", True), ("network_mode", "host")):
            data = model()
            data["services"]["frontend"][name] = value
            cases.append(data)
        data = model()
        data["volumes"]["ml_models"]["name"] = "production_models"
        cases.append(data)
        data = model()
        data["services"]["backend"]["environment"]["TRUSTED_PROXY_REGEX"] = ".*"
        cases.append(data)
        data = model()
        data["services"]["backend"]["environment"]["APP_AUTHCOOKIE_SECURE"] = "false"
        cases.append(data)
        data = model()
        data["services"]["backend"]["environment"]["ALLOWED_ORIGINS"] = "https://production.example.invalid"
        cases.append(data)
        data = model()
        data["services"]["backend"]["image"] = "cardio-backend:latest"
        cases.append(data)
        for index, data in enumerate(cases):
            with self.subTest(case=index), self.assertRaises(DEPLOY.VALIDATOR.ConfigError):
                DEPLOY.VALIDATOR.validate_manual(data, SHA, IMAGES, **SETTINGS.validation_args)

    def test_release_rejects_wrong_commit_registry_tag_and_missing_image(self):
        good = {"commit": SHA, "images": IMAGES}
        self.assertEqual(DEPLOY.validate_release(good, RELEASE, SETTINGS), (SHA, IMAGES))
        for value in (None, {}, {"commit": None, "images": IMAGES},
                      {"commit": "c" * 40, "images": IMAGES},
                      {"commit": SHA, "images": {"frontend": IMAGES["frontend"]}}):
            with self.subTest(value=value), self.assertRaises(DEPLOY.DeployError):
                DEPLOY.validate_release(value, RELEASE, SETTINGS)
        for ref in ("nginx:latest", IMAGES["frontend"].replace("example/", "another-owner/"),
                    IMAGES["frontend"].split("@")[0] + ":" + SHA):
            bad = copy.deepcopy(good)
            bad["images"]["frontend"] = ref
            with self.assertRaises(DEPLOY.DeployError):
                DEPLOY.validate_release(bad, RELEASE, SETTINGS)

    def test_env_update_preserves_passwords_and_rejects_duplicates(self):
        text = "DB_PASSWORD='synthetic-$-secret'\nFRONTEND_IMAGE=old\n"
        self.assertEqual(DEPLOY.replace_env(text, {"FRONTEND_IMAGE": "new"}), text.replace("=old", "=new"))
        with self.assertRaises(DEPLOY.DeployError):
            DEPLOY.replace_env("FRONTEND_IMAGE=a\nFRONTEND_IMAGE=b\n", {"FRONTEND_IMAGE": "new"})

    def test_rejects_symlink_and_public_env(self):
        path = Mock(name="path")
        path.exists.return_value = True
        path.name = ".env.preview"
        path.is_symlink.return_value = True
        with self.assertRaises(DEPLOY.DeployError):
            DEPLOY.check_path(path)
        path.is_symlink.return_value = False
        path.stat.return_value.st_uid = getattr(os, "getuid", lambda: 0)()
        path.stat.return_value.st_mode = 0o100644
        with self.assertRaises(DEPLOY.DeployError):
            DEPLOY.check_path(path, private=True)


class SimulatedDeployment(DEPLOY.Deployment):
    def __init__(self, home, failure=None):
        super().__init__(RELEASE, settings=SETTINGS, home=home, base_dir=Path(home) / "preview")
        self.events = []
        self.failure = failure
        self.base.mkdir(parents=True)
        self.incoming.mkdir(parents=True)
        self.compose_file.write_text("previous-compose\n")
        (self.incoming / "docker-compose.preview.yaml").write_text("new-compose\n")
        self.env_file.write_text("DB_PASSWORD=synthetic-private-value\nTRUSTED_PROXY_REGEX=^172\\.20\\.0\\.6$\n"
                                 "ML_IMAGE=old-ml\nBACKEND_IMAGE=old-backend\nFRONTEND_IMAGE=old-frontend\n")
        self.original_env = self.env_file.read_text()

    @contextlib.contextmanager
    def lock(self):
        self.events.append(("lock",))
        yield

    def preflight(self):
        self.sha, self.images = SHA, IMAGES
        self.model_version = "model-original"
        self.old = {s: self.container(s) for s in DEPLOY.VALIDATOR.BUDGETS}
        self.others = self.other_containers()
        self.events.append(("preflight",))

    def pull(self):
        self.events.append(("pull",))
        if self.failure == "pull":
            raise DEPLOY.DeployError("Pull failed")

    def snapshot(self):
        self.events.append(("snapshot",))
        if self.failure == "snapshot":
            raise DEPLOY.DeployError("Backup failed")
        self.archive.mkdir(parents=True)
        (self.archive / "previous.env").write_text(self.original_env)
        (self.archive / "previous-compose.yaml").write_text("previous-compose\n")
        self.rollback_images = {s: "rollback-" + s for s in DEPLOY.APP_SERVICES}

    def compose(self, *args, **kwargs):
        self.events.append(args)
        if args[0] == "up" and self.failure == "start":
            self.failure = None
            raise DEPLOY.DeployError("New service unhealthy")
        return ""

    def container(self, service):
        values = dict(line.split("=", 1) for line in self.env_file.read_text().splitlines())
        return {"Id": "original-" + service, "State": {"Health": {"Status": "healthy"}},
                "Config": {"Image": values.get(DEPLOY.IMAGE_KEYS.get(service, ""), "data-image"),
                           "Env": ["TRUSTED_PROXY_REGEX=" + values["TRUSTED_PROXY_REGEX"]]},
                "NetworkSettings": {"Networks": {SETTINGS.project + "_default": {"IPAddress": "172.20.0.6"}}}}

    def other_containers(self):
        return {"production": "untouched-production", "another-app": "untouched-app"}

    def smoke(self, **kwargs):
        self.events.append(("smoke",))
        if self.failure == "smoke":
            self.failure = None
            raise DEPLOY.DeployError("New prediction failed")


class DeploymentTests(unittest.TestCase):
    def test_updates_only_app_services_and_writes_report(self):
        with tempfile.TemporaryDirectory() as home, contextlib.redirect_stdout(io.StringIO()) as log:
            app = SimulatedDeployment(home)
            app.deploy()
            for command in app.events:
                if command[0] == "up":
                    self.assertIn("--no-deps", command)
                    self.assertNotIn("postgres", command)
                    self.assertNotIn("redis", command)
                    self.assertNotIn("--remove-orphans", command)
            self.assertLess(app.events.index(("snapshot",)), app.events.index(next(c for c in app.events if c[0] == "up")))
            self.assertEqual(json.loads((app.archive / "report.json").read_text())["status"], "healthy")
            report = (app.archive / "report.json").read_text()
            for value in (SETTINGS.origin, SETTINGS.base_dir, SETTINGS.project):
                self.assertNotIn(value, report)
                self.assertNotIn(value, log.getvalue())
            self.assertNotIn("synthetic-private-value", log.getvalue())

    def test_failed_pull_or_backup_never_changes_working_files(self):
        for failure in ("pull", "snapshot"):
            with self.subTest(failure=failure), tempfile.TemporaryDirectory() as home:
                app = SimulatedDeployment(home, failure)
                with self.assertRaises(DEPLOY.DeployError):
                    app.deploy()
                self.assertEqual(app.env_file.read_text(), app.original_env)
                self.assertEqual(app.compose_file.read_text(), "previous-compose\n")
                self.assertFalse(any(command[0] == "up" for command in app.events))

    def test_start_or_smoke_failure_restores_previous_images_and_compose(self):
        for failure in ("start", "smoke"):
            with self.subTest(failure=failure), tempfile.TemporaryDirectory() as home, contextlib.redirect_stdout(io.StringIO()):
                app = SimulatedDeployment(home, failure)
                with self.assertRaisesRegex(DEPLOY.DeployError, "previous preview restored"):
                    app.deploy()
                self.assertEqual(app.compose_file.read_text(), "previous-compose\n")
                for service in DEPLOY.APP_SERVICES:
                    self.assertIn(DEPLOY.IMAGE_KEYS[service] + "=rollback-" + service, app.env_file.read_text())
                self.assertIn("DB_PASSWORD=synthetic-private-value", app.env_file.read_text())
                self.assertFalse((app.archive / "report.json").exists())
                self.assertFalse(any("down" in command or "prune" in command for command in app.events))

    def test_rollback_failure_is_reported_without_secrets(self):
        with tempfile.TemporaryDirectory() as home, contextlib.redirect_stdout(io.StringIO()):
            app = SimulatedDeployment(home, "smoke")
            app.rollback = Mock(side_effect=RuntimeError("synthetic-private-value"))
            with self.assertRaisesRegex(DEPLOY.DeployError, "rollback failed") as failure:
                app.deploy()
            self.assertNotIn("synthetic-private-value", str(failure.exception))

    def test_changed_frontend_ip_updates_exact_proxy_trust_and_reloads_nginx(self):
        with tempfile.TemporaryDirectory() as home:
            app = SimulatedDeployment(home)
            original = app.container
            def changed(service):
                data = original(service)
                if service == "frontend":
                    data["NetworkSettings"]["Networks"][SETTINGS.project + "_default"]["IPAddress"] = "172.20.0.8"
                return data
            app.container = changed
            app.reconcile_proxy()
            self.assertIn(r"TRUSTED_PROXY_REGEX=^172\.20\.0\.8$", app.env_file.read_text())
            self.assertEqual(app.events[-1], ("exec", "-T", "frontend", "nginx", "-s", "reload"))
            self.assertEqual(app.events[0][-1], "backend")


class SmokeTests(unittest.TestCase):
    def test_local_https_keeps_hostname_and_certificate_validation(self):
        calls = []
        def command(args, **kwargs):
            calls.append(args)
            Path(args[args.index('--dump-header') + 1]).write_bytes(
                b'HTTP/1.1 200 OK\r\nStrict-Transport-Security: max-age=31536000\r\n\r\n')
            Path(args[args.index('--output') + 1]).write_bytes(b'{}')
            return '200'
        with DEPLOY.LoopbackHTTPSClient(command, SETTINGS.origin).open(SETTINGS.origin + '/', timeout=20) as response:
            self.assertEqual(response.status, 200)
            self.assertTrue(response.headers.get('Strict-Transport-Security'))
        self.assertIn('preview.example.invalid:443:127.0.0.1', calls[0])
        self.assertNotIn('--insecure', calls[0])
        self.assertNotIn('-k', calls[0])
        self.assertEqual(calls[0][-1], SETTINGS.origin + '/')

    def client(self, *, wrong_version=False, missing_https=False, public_admin=False):
        def open_url(request, **kwargs):
            url = request if isinstance(request, str) else request.full_url
            if url.endswith('/api/admin/model') and not public_admin:
                raise urllib.error.HTTPError(url, 401, 'unauthorized', {}, None)
            data = b'<html>preview</html>'
            if url.endswith('/api/survey/submit'):
                payload = json.loads(request.data)
                self.assertNotIn('stroke', payload)
                self.assertIn('chronicLungDisease', payload)
                data = json.dumps({'predictions': [{'modelKind': payload['modelKind'], 'probability': 52}],
                                   'modelVersion': 'wrong' if wrong_version else 'model-original'}).encode()
            response = io.BytesIO(data)
            response.status = 200
            response.headers = {} if missing_https else {'Strict-Transport-Security': 'max-age=31536000'}
            return response
        return Mock(open=Mock(side_effect=open_url))

    def test_public_checks_without_credentials_or_training(self):
        app = DEPLOY.Deployment(RELEASE, settings=SETTINGS)
        app.model_version = 'model-original'
        client = self.client()
        with patch.object(DEPLOY.urllib.request, 'build_opener', return_value=client), contextlib.redirect_stdout(io.StringIO()):
            app.smoke()
        self.assertEqual(client.open.call_count, 4)

    def test_wrong_model_version_https_trust_or_public_admin_fails(self):
        app = DEPLOY.Deployment(RELEASE, settings=SETTINGS)
        app.model_version = 'model-original'
        for arguments in ({'wrong_version': True}, {'missing_https': True}, {'public_admin': True}):
            with self.subTest(arguments=arguments), patch.object(
                DEPLOY.urllib.request, 'build_opener', return_value=self.client(**arguments)
            ), contextlib.redirect_stdout(io.StringIO()), self.assertRaises(DEPLOY.DeployError):
                app.smoke()


@unittest.skipUnless(os.name == 'posix', 'VPS path permissions and flock require POSIX')
class PosixPreflightTests(unittest.TestCase):
    def fixture(self, home):
        app = DEPLOY.Deployment(RELEASE, settings=SETTINGS, home=home, base_dir=Path(home) / "preview")
        app.incoming.mkdir(parents=True)
        app.env_file.write_text('synthetic-env\n')
        app.env_file.chmod(0o600)
        app.compose_file.write_text('old-compose\n')
        for name in ('deploy-manual.py', 'validate-config.py', 'docker-compose.preview.yaml'):
            (app.incoming / name).write_text('fixture\n')
        (app.incoming / 'release.json').write_text(json.dumps({'commit': SHA, 'images': IMAGES}))
        parsed = model()
        containers = {}
        for service in DEPLOY.VALIDATOR.BUDGETS:
            mounts = []
            for volume in parsed['services'][service].get('volumes', []):
                mounts.append({'Type': 'volume', 'Name': SETTINGS.project + '_' + volume['source'],
                               'Destination': volume['target']})
            containers[service] = {'Id': 'old-' + service, 'Mounts': mounts,
                'Config': {'Env': [k + '=' + v for k, v in parsed['services'][service].get('environment', {}).items()]},
                'State': {'Health': {'Status': 'healthy'}}}
        app.compose = Mock(return_value=json.dumps(parsed))
        app.container = Mock(side_effect=lambda s: containers[s])
        app.command = Mock(return_value='model-original\n')
        app.other_containers = Mock(return_value={'production': 'untouched'})
        app.smoke = Mock()
        return app, parsed, containers

    def test_cli_loads_delivered_settings_without_server_environment(self):
        with tempfile.TemporaryDirectory() as directory, patch.dict(os.environ, {}, clear=True):
            path = Path(directory) / 'deployment-settings.json'
            path.write_text(json.dumps(TEST_ENV))
            path.chmod(0o600)
            with patch.object(DEPLOY, 'Deployment') as deployment, patch.object(
                DEPLOY.sys, 'argv', ['deploy-manual.py', RELEASE, '--settings', str(path), '--smoke-only', 'model-original']
            ):
                self.assertEqual(DEPLOY.main(), 0)
                received = deployment.call_args.kwargs['settings']
                self.assertEqual(received.values, TEST_ENV)
                deployment.return_value.smoke.assert_called_once_with()
            path.chmod(0o644)
            with patch.object(DEPLOY.sys, 'argv', ['deploy-manual.py', RELEASE, '--settings', str(path)]), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(DEPLOY.main(), 1)

    def test_configured_directory_must_stay_below_home_without_symlinks(self):
        with tempfile.TemporaryDirectory() as home, tempfile.TemporaryDirectory() as outside:
            app, _, _ = self.fixture(home)
            app.base = Path(outside)
            with self.assertRaisesRegex(DEPLOY.DeployError, 'below'):
                app.check_directories()
            link = Path(home) / 'linked-preview'
            link.symlink_to(Path(outside), target_is_directory=True)
            app.base = link
            with self.assertRaisesRegex(DEPLOY.DeployError, 'symlinked'):
                app.check_directories()

    def test_preflight_accepts_existing_manual_but_rejects_foreign_volume(self):
        with tempfile.TemporaryDirectory() as home, contextlib.redirect_stdout(io.StringIO()):
            app, _, containers = self.fixture(home)
            app.preflight()
            containers['ml-service']['Mounts'][0]['Name'] = 'production_models'
            with self.assertRaisesRegex(DEPLOY.DeployError, 'data mount'):
                app.preflight()

    def test_preflight_rejects_a_changed_manual_port(self):
        with tempfile.TemporaryDirectory() as home:
            app, parsed, _ = self.fixture(home)
            parsed['services']['frontend']['ports'][0]['published'] = '18081'
            app.compose.return_value = json.dumps(parsed)
            with self.assertRaisesRegex(DEPLOY.DeployError, 'configured frontend port'):
                app.preflight()

    def test_preflight_rejects_broken_public_prediction_before_mutation(self):
        with tempfile.TemporaryDirectory() as home:
            app, _, _ = self.fixture(home)
            app.smoke.side_effect = DEPLOY.DeployError('Existing preview prediction failed')
            with self.assertRaisesRegex(DEPLOY.DeployError, 'Existing preview prediction'):
                app.preflight()
            self.assertFalse(app.archive.exists())
            self.assertEqual(app.env_file.read_text(), 'synthetic-env\n')

    def test_lock_blocks_second_deployment_and_releases(self):
        with tempfile.TemporaryDirectory() as home:
            app, _, _ = self.fixture(home)
            second = DEPLOY.Deployment(RELEASE, settings=SETTINGS, home=home, base_dir=Path(home) / "preview")
            with app.lock():
                with self.assertRaisesRegex(DEPLOY.DeployError, 'Another'):
                    with second.lock():
                        self.fail('Second deployment acquired the lock')
            with second.lock():
                pass


if __name__ == "__main__":
    unittest.main()
