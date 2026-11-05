from pydantic import BaseModel, Field


class RetrainResponse(BaseModel):
    status: str
    saved_models: list[str] = Field(description="city_model.cbm и region_model.cbm")
    model_version: str
    metrics: list[dict]
    input_patients: int = Field(description="Число строк пациентов в загруженном CSV")
    prepared_rows: dict[str, int] = Field(
        description="Число автоматически подготовленных записей city/region"
    )
