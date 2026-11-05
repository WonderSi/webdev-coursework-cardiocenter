from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from loguru import logger

from app.logging_config import setup_logging
from app.config import settings
from app.routers.metrics import router as metrics_router
from app.routers.predictions import router as predictions_router
from app.routers.retrain import router as retrain_router
from app.services.model_registry import active_bundle, ModelsUnavailable


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging()
    try:
        bundle = active_bundle(settings.MODELS_DIR)
        logger.info("Loaded city and region, version={}", bundle.version)
    except ModelsUnavailable:
        logger.info("No active pair yet; /retrain is available")
    yield


app = FastAPI(title="ML Service", lifespan=lifespan)

app.include_router(predictions_router)
app.include_router(retrain_router)
app.include_router(metrics_router)


@app.exception_handler(RequestValidationError)
async def validation_error(request, exc):
    # Report locations/messages without reflecting clinical values or non-finite JSON.
    return JSONResponse(
        status_code=422,
        content={
            "detail": [
                {"loc": list(error["loc"]), "msg": error["msg"], "type": error["type"]}
                for error in exc.errors()
            ]
        },
    )


@app.get("/health")
def health_check():
    try:
        bundle = active_bundle(settings.MODELS_DIR)
        return {
            "status": "ok",
            "models": ["city", "region"],
            "model_version": bundle.version,
        }
    except ModelsUnavailable:
        return {"status": "models_not_trained", "models": []}
