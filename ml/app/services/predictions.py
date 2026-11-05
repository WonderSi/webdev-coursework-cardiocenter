from catboost import Pool

from app.schemas.predict import PredictRequest, PredictResponse, DiseasePrediction
from app.services.model_registry import active_bundle
from app.services.preprocess import build_features, request_frame


def predict(request: PredictRequest, models_dir) -> PredictResponse:
    bundle = active_bundle(models_dir)
    kind = request.model_kind
    config = bundle.metadata["model_configs"][kind]
    X = build_features(request_frame(request), kind, bundle.reference_dir, config)
    probability = float(
        bundle.models[kind].predict_proba(Pool(X, cat_features=config["cat_features"]))[
            0, 1
        ]
    )
    return PredictResponse(
        predictions=[
            DiseasePrediction(
                disease=kind,
                probability=probability,
                prediction=int(probability >= 0.5),
            )
        ],
        model_version=bundle.version,
    )
