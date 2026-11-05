import argparse
from datetime import datetime, timezone
import fcntl
import hashlib
import json
import os
from pathlib import Path
import shutil
import tempfile
from uuid import uuid4

from app.config import settings
from app.services.dataset import (
    read_dataset,
    build_patient_dataset,
    DATASET_SCHEMA_VERSION,
)
from app.services.model_registry import load_bundle
from app.services.training import train_models


class RetrainingBusy(RuntimeError):
    pass


def retrain_from_dataset_csv(
    dataset_csv: Path,
    *,
    models_dir: Path,
    reference_dir: Path | None = None,
    iteration_overrides: dict | None = None,
) -> dict:
    reference_dir = reference_dir or settings.ECOLOGY_DIR
    models_dir.mkdir(parents=True, exist_ok=True)
    # flock works across threads/processes and avoids two writers publishing over each other.
    with (models_dir / ".retrain.lock").open("a") as lock:
        try:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as exc:
            raise RetrainingBusy("Переобучение уже выполняется") from exc
        patients = read_dataset(dataset_csv)
        frame = build_patient_dataset(patients)
        version = (
            datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
            + "-"
            + uuid4().hex[:12]
        )
        versions = models_dir / "versions"
        versions.mkdir(exist_ok=True)
        staging = Path(tempfile.mkdtemp(prefix=".training-", dir=versions))
        try:
            shutil.copytree(reference_dir, staging / "ecology")
            result = train_models(
                frame,
                out_dir=staging,
                reference_dir=staging / "ecology",
                iteration_overrides=iteration_overrides,
            )
            result["model_version"] = version
            result["dataset_sha256"] = hashlib.sha256(
                dataset_csv.read_bytes()
            ).hexdigest()
            result["input_format"] = "patients_wide"
            result["input_schema_version"] = DATASET_SCHEMA_VERSION
            result["input_patients"] = len(patients)
            result["prepared_rows"] = frame.groupby("model_kind").size().to_dict()
            result["prepared_dataset_sha256"] = hashlib.sha256(
                frame.to_csv(index=False).encode("utf-8")
            ).hexdigest()
            (staging / "manifest.json").write_text(
                json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8"
            )
            load_bundle(
                staging
            )  # Refuse activation if either model/snapshot cannot be loaded.
            os.replace(staging, versions / version)
            pointer = models_dir / f".CURRENT-{version}"
            try:
                pointer.write_text(version + "\n", encoding="utf-8")
                os.replace(pointer, models_dir / "CURRENT")
            finally:
                pointer.unlink(missing_ok=True)
        finally:
            if staging.exists():
                shutil.rmtree(staging)
    return {
        "status": "ok",
        "saved_models": ["city_model.cbm", "region_model.cbm"],
        "model_version": version,
        "metrics": result["metrics"],
        "input_patients": result["input_patients"],
        "prepared_rows": result["prepared_rows"],
    }


def main():
    parser = argparse.ArgumentParser(description="Обучить city и region из нового CSV")
    parser.add_argument(
        "--dataset", type=Path, default=Path("app/data/train_dataset.csv")
    )
    parser.add_argument("--models-dir", type=Path, default=settings.MODELS_DIR)
    args = parser.parse_args()
    print(
        json.dumps(
            retrain_from_dataset_csv(args.dataset, models_dir=args.models_dir),
            ensure_ascii=False,
            indent=2,
        )
    )


if __name__ == "__main__":
    main()
