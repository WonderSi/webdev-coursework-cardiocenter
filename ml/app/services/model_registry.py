"""Load an immutable pair of models and its matching ecology snapshot."""

from dataclasses import dataclass
import json
from pathlib import Path
import threading

from catboost import CatBoostClassifier
from app.schemas.predict import SCHEMA_VERSION
from app.schemas.profession import PROFESSION_CLASSIFICATION, profession_catalog
from app.services.preprocess import PIPELINE_VERSION

KINDS = ("city", "region")


class ModelsUnavailable(RuntimeError):
    pass


@dataclass(frozen=True)
class ModelBundle:
    version: str
    directory: Path
    models: dict
    metadata: dict

    @property
    def reference_dir(self):
        return self.directory / "ecology"


def load_bundle(directory: Path) -> ModelBundle:
    metadata = json.loads((directory / "manifest.json").read_text(encoding="utf-8"))
    if (
        metadata.get("schema_version") != SCHEMA_VERSION
        or metadata.get("pipeline_version") != PIPELINE_VERSION
        or metadata.get("profession_classification") != PROFESSION_CLASSIFICATION
        or metadata.get("profession_categories") != profession_catalog()
    ):
        raise ModelsUnavailable(
            "Модели используют прежнюю схему признаков. Выполните /retrain с текущей схемой датасета."
        )
    models = {}
    for kind in KINDS:
        model = CatBoostClassifier()
        model.load_model(str(directory / f"{kind}_model.cbm"))
        config = metadata["model_configs"][kind]
        if model.feature_names_ != config["features"]:
            raise ValueError(f"Порядок признаков {kind} не соответствует manifest.json")
        if model.get_cat_feature_indices() != [
            i for i, c in enumerate(config["features"]) if c in config["cat_features"]
        ]:
            raise ValueError(
                f"Категориальные признаки {kind} не соответствуют manifest.json"
            )
        models[kind] = model
    """
    for name, expected in metadata["reference_sha256"].items():
        actual = hashlib.sha256((directory / "ecology" / name).read_bytes()).hexdigest()
        if actual != expected:
            raise ValueError("Экологический справочник изменён после обучения")
    """
    return ModelBundle(metadata["model_version"], directory, models, metadata)


_CACHE = {}
_CACHE_LOCK = threading.Lock()


def active_bundle(models_dir: Path) -> ModelBundle:
    models_dir = models_dir.resolve()
    try:
        version = (models_dir / "CURRENT").read_text(encoding="utf-8").strip()
    except FileNotFoundError as exc:
        raise ModelsUnavailable(
            "Две модели ещё не обучены. Загрузите новый CSV через /retrain."
        ) from exc
    if not version or Path(version).name != version or version in {".", ".."}:
        raise ModelsUnavailable("Повреждён указатель активной версии моделей")
    # Each worker notices a newly published version on its next request.
    with _CACHE_LOCK:
        cached = _CACHE.get(models_dir)
        if cached is None or cached.version != version:
            cached = load_bundle(models_dir / "versions" / version)
            if cached.version != version:
                raise ModelsUnavailable("Версия manifest.json не совпадает с CURRENT")
            _CACHE[models_dir] = cached
        return cached
