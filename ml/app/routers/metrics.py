from fastapi import APIRouter, HTTPException
from app.config import settings
from app.services.model_registry import active_bundle, ModelsUnavailable

router = APIRouter(tags=["Metrics"])


@router.get("/metrics", summary="Тестовые метрики активных city и region")
def get_metrics():
    try:
        return active_bundle(settings.MODELS_DIR).metadata["metrics"]
    except ModelsUnavailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
