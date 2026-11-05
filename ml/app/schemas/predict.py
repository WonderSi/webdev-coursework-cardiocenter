from enum import IntEnum
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator
from app.schemas.profession import Profession

SCHEMA_VERSION = 5


class Gender(IntEnum):
    MALE = 1
    FEMALE = 2


class Alcohol(IntEnum):
    NO = 0
    HAD_CONSUMED_BEFORE = 1
    YES = 2


class Region(IntEnum):
    RUDNICHNY = 1
    CENTRAL = 2
    ZAVODSKOY = 3
    KIROVSKY = 4
    LENINSKY = 5
    RURAL = 6


class DiseaseStatus(IntEnum):
    PRESENT = 1
    ABSENT = 0


class PredictRequest(BaseModel):
    """One clinical observation; training uploads contain full patient histories."""

    model_config = ConfigDict(extra="forbid", allow_inf_nan=False)
    model_kind: Literal["city", "region"]
    year: Literal[2013, 2017, 2019, 2021] | None = Field(
        None, description="Период экологии city; для region — null"
    )
    gender: Gender
    age: float = Field(ge=0, le=120)
    height: float = Field(gt=0, le=250, description="Рост, см")
    weight: float = Field(gt=0, le=300, description="Вес, кг")
    alcohol: Alcohol = Field(
        description="0 — нет, 1 — употреблял ранее, 2 — употребляет"
    )
    profession: Profession = Field(
        description="Классификация из 13 категорий; коды и названия: /dataset/schema"
    )
    region: Region
    chronic_lung_disease: DiseaseStatus = Field(
        description="Хронические заболевания легких: 0/1"
    )
    glucose: float | None = Field(None, ge=0)
    cholesterol: float | None = Field(None, ge=0)
    hdl_cholesterol: float | None = Field(None, ge=0)
    ldl_cholesterol: float | None = Field(None, ge=0)
    triglycerides: float | None = Field(None, ge=0)
    # Retained history fields. These are NOT predictors of the composite label.
    stroke: DiseaseStatus | None = None
    stroke_year: int | None = Field(None, ge=1900, le=2100)
    heart_failure: DiseaseStatus | None = None
    heart_failure_year: int | None = Field(None, ge=1900, le=2100)
    cad_chd_ihd: DiseaseStatus | None = None
    cad_chd_ihd_year: int | None = Field(None, ge=1900, le=2100)
    angine: DiseaseStatus | None = None
    angine_year: int | None = Field(None, ge=1900, le=2100)
    myocardial_infarction: DiseaseStatus | None = None
    myocardial_infarction_year: int | None = Field(None, ge=1900, le=2100)
    arterial_hypertension: DiseaseStatus | None = None
    arterial_hypertension_year: int | None = Field(None, ge=1900, le=2100)

    @model_validator(mode="after")
    def validate_period(self):
        if self.model_kind == "city" and self.year is None:
            raise ValueError("Для city нужен year: 2013, 2017, 2019 или 2021")
        if self.model_kind == "region" and self.year is not None:
            raise ValueError(
                "Для region year должен быть null: экология усреднена за 2009–2020"
            )
        return self


class DiseasePrediction(BaseModel):
    disease: Literal["city", "region"]
    probability: float = Field(ge=0, le=1)
    prediction: int


class PredictResponse(BaseModel):
    predictions: list[DiseasePrediction]
    model_version: str
