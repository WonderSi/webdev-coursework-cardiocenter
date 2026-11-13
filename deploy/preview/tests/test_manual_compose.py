import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

from test_manual_deploy import DEPLOY, IMAGES, SHA, SETTINGS

COMPOSE = next((parent / "docker/docker-compose.preview.yaml" for parent in Path(__file__).resolve().parents
                if (parent / "docker/docker-compose.preview.yaml").is_file()), None)


@unittest.skipUnless(shutil.which("docker") and COMPOSE is not None, "Docker CLI and repository Compose required")
class ActualComposeParserTests(unittest.TestCase):
    def test_real_compose_matches_manual_contract(self):
        settings = {
            "COMPOSE_PROJECT_NAME": SETTINGS.project, "DB_NAME": "preview", "DB_USER": "preview",
            "DB_PASSWORD": "synthetic-db-password", "REDIS_PASSWORD": "synthetic-redis-password",
            "JWT_SECRET": "eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHg=",
            "BOOTSTRAP_SUPER_ADMIN_EMAIL": "example@example.invalid",
            "BOOTSTRAP_SUPER_ADMIN_PASSWORD": "synthetic-admin-password", "APP_AUTHCOOKIE_SECURE": "true",
            "ALLOWED_ORIGINS": SETTINGS.origin, "PREVIEW_PROXY_IP": "172.20.0.1", "FRONTEND_PORT": SETTINGS.frontend_port,
            "TRUSTED_PROXY_REGEX": r"^172\.20\.0\.6$",
            **{DEPLOY.IMAGE_KEYS[s]: ref for s, ref in IMAGES.items()},
        }
        with tempfile.TemporaryDirectory() as directory:
            env = Path(directory) / ".env.preview"
            env.write_text("\n".join(k + "=" + v for k, v in settings.items()) + "\n")
            result = subprocess.run([
                "docker", "compose", "--env-file", str(env), "-p", SETTINGS.project,
                "-f", str(COMPOSE), "config", "--format", "json",
            ], env={**os.environ, **settings}, capture_output=True, text=True, timeout=30)
        self.assertEqual(result.returncode, 0, "Real Docker Compose config failed")
        parsed = json.loads(result.stdout)
        DEPLOY.VALIDATOR.validate_manual(parsed, SHA, IMAGES, **SETTINGS.validation_args)
        self.assertEqual(str(parsed["services"]["frontend"]["ports"][0]["published"]), SETTINGS.frontend_port)


if __name__ == "__main__":
    unittest.main()
