from pydantic import BaseModel, Field


class RetrainResponse(BaseModel):
    status: str = Field(description="ok / error")
    saved_models: list[str] = Field(description="Список сохранённых файлов моделей")
    model_version: str = Field(description="Версия модели")
