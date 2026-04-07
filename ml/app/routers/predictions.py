from fastapi import APIRouter
from app.schemas.predict import PredictResponse, PredictRequest
from app.config import settings


router = APIRouter(prefix="/predictions", tags=["Prediction"])


@router.post(
    "/predict",
    response_model=PredictResponse,
    summary="ВРЕМЕННО ВОЗВРАЩАЕТ КОНСТАНТУ 0.33",
)
async def predict_risk(request: PredictRequest):
    if request:
        pass
    probability = 0.33  # временное решение
    return PredictResponse(
        disease_type="",
        disease_probability=float(probability),
        message="",
        model_version=settings.MODEL_VERSION,
    )
