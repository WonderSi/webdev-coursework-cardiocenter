from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="ML_")
    MODELS_DIR: Path = BASE_DIR / "models"
    ECOLOGY_DIR: Path = BASE_DIR / "data" / "ecology"
    MAX_UPLOAD_BYTES: int = 50 * 1024 * 1024


settings = Settings()
