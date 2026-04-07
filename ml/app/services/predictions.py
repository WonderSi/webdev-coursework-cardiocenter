from catboost import CatBoost, CatBoostClassifier


model = CatBoostClassifier()

REGION_DATA: dict[str, dict[str, float]] = {}


def load_model(model_path) -> CatBoost:
    model.load_model(model_path)
    return model
