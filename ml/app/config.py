from pathlib import Path
from pydantic import FilePath
from pydantic_settings import BaseSettings

BASE_DIR = Path(__file__).resolve().parent


class Settings(BaseSettings):
    MODEL_VERSION: str = "0.0.1"
    MODEL_PATH: FilePath = BASE_DIR / "models" / "model.cbm"


settings = Settings()
