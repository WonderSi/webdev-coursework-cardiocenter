import json
from pathlib import Path
from typing import Any

from fastapi import APIRouter, HTTPException

from app.config import settings


router = APIRouter(tags=["Metrics"])


@router.get(
    "/metrics",
    summary="Возвращает содержимое metrics.json",
)
def get_metrics() -> Any:
    metrics_path = Path(settings.MODELS_DIR) / "metrics.json"
    if not metrics_path.exists():
        raise HTTPException(status_code=404, detail="metrics.json not found")

    try:
        return json.loads(metrics_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise HTTPException(
            status_code=500,
            detail="metrics.json contains invalid JSON",
        ) from exc
