import json
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.routers.predictions import router
from app.services.predictions import load_model, REGION_DATA
from app.config import settings


@asynccontextmanager
async def lifespan():
    load_model(settings.MODEL_PATH)
    with open("app/data/region_data.json", "r", encoding="utf-8") as f:
        data = json.load(f)
        REGION_DATA.update(data)
    yield


app = FastAPI(title="ML Service")

app.include_router(router)


@app.get("/health")
def health_check():
    return {"status": "ok"}
