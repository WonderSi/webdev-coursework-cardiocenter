from fastapi import APIRouter
from loguru import logger

from app.schemas.predict import DiseasePrediction, PredictResponse, PredictRequest
from app.config import settings
from app.services.predictions import predict_all


router = APIRouter(prefix="/predictions", tags=["Prediction"])


@router.post(
    "/predict",
    response_model=PredictResponse,
    summary="Возвращает вероятности заболеваний",
)
async def predict_risk(request: PredictRequest):
    logger.info("Predict request received")
    pairs = predict_all(request)
    predictions = [
        DiseasePrediction(disease=disease, probability=probability)
        for disease, probability in pairs
    ]
    return PredictResponse(
        predictions=predictions,
        model_version=settings.MODEL_VERSION,
    )
