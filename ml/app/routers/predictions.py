from fastapi import APIRouter, HTTPException
from app.config import settings
from app.schemas.predict import PredictRequest, PredictResponse
from app.services.model_registry import ModelsUnavailable
from app.services.predictions import predict

router = APIRouter(tags=["Prediction"])


@router.post(
    "/predict",
    response_model=PredictResponse,
    summary="Прогноз выбранной модели city или region",
)
@router.post(
    "/predictions/predict",
    response_model=PredictResponse,
    summary="Альтернативный путь к /predict",
)
def predict_risk(request: PredictRequest):
    try:
        return predict(request, settings.MODELS_DIR)
    except ModelsUnavailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
