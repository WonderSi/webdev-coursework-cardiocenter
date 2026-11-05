import tempfile
from pathlib import Path

from fastapi import APIRouter, File, HTTPException, UploadFile
import pandas as pd

from app.config import settings
from app.schemas.predict import PredictRequest, SCHEMA_VERSION
from app.schemas.profession import PROFESSION_CLASSIFICATION, profession_catalog
from app.schemas.retrain import RetrainResponse
from app.services.dataset import (
    PATIENT_COLUMNS,
    PATIENT_NUMERIC_COLUMNS,
    PATIENT_FLAG_COLUMNS,
    DATASET_SCHEMA_VERSION,
)
from app.services.retrain import RetrainingBusy, retrain_from_dataset_csv

router = APIRouter(tags=["Retrain"])


@router.get("/dataset/schema", summary="Формат исходного CSV: одна строка на пациента")
def dataset_schema():
    return {
        "schema_version": DATASET_SCHEMA_VERSION,
        "format": "patients_wide",
        "row": "Одна строка на пациента, уникальный id",
        "csv_columns": PATIENT_COLUMNS,
        "required_columns": PATIENT_COLUMNS,
        "binary_columns": PATIENT_FLAG_COLUMNS,
        "numeric_columns": PATIENT_NUMERIC_COLUMNS,
        "derived_in_service": ["model_kind", "year", "target"],
        "profession_classification": PROFESSION_CLASSIFICATION,
        "profession_categories": profession_catalog(),
        "predict_schema_version": SCHEMA_VERSION,
        "predict_schema": PredictRequest.model_json_schema(),
    }


@router.post(
    "/retrain",
    response_model=RetrainResponse,
    summary="Собрать записи, рассчитать исходы и переобучить две модели из исходного CSV",
)
def retrain_model(
    dataset: UploadFile = File(..., description="CSV формата /dataset/schema"),
):
    # A sync route runs fitting in FastAPI's threadpool, keeping predict/health responsive.
    try:
        with tempfile.TemporaryDirectory(prefix="cardiocenter-upload-") as directory:
            path = Path(directory) / "dataset.csv"
            total = 0
            with path.open("wb") as target:
                while block := dataset.file.read(1024 * 1024):
                    total += len(block)
                    if total > settings.MAX_UPLOAD_BYTES:
                        raise HTTPException(
                            status_code=413, detail="CSV превышает допустимый размер"
                        )
                    target.write(block)
            result = retrain_from_dataset_csv(path, models_dir=settings.MODELS_DIR)
            return RetrainResponse(**result)
    except RetrainingBusy as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from exc
    except (ValueError, UnicodeError, pd.errors.ParserError) as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
    finally:
        dataset.file.close()
