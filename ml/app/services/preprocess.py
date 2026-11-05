"""Shared, ID-free feature construction for training and prediction."""

from pathlib import Path
import json
import numpy as np
import pandas as pd

from app.schemas.predict import PredictRequest
from app.schemas.profession import PROFESSION_NAMES

PIPELINE_VERSION = 5

REGIONS = {
    1: "рудничный",
    2: "центральный",
    3: "заводский",
    4: "кировский",
    5: "ленинский",
    6: "сельская местность",
}
PERIODS = (2013, 2017, 2019, 2021)
REFERENCE_FILES = {
    "city": "ap_city_by_district_period.csv",
    "region": "ap_region_by_district.csv",
}
CLINICAL_COLUMNS = {
    "age": "возраст",
    "height": "рост",
    "weight": "вес",
    "chronic_lung_disease": "хрон заболевания легких",
    "glucose": "глюкоза",
    "cholesterol": "холестерин",
    "hdl_cholesterol": "холестерин лпвп",
    "ldl_cholesterol": "холестерин лпнп",
    "triglycerides": "тг",
}


def load_configs(reference_dir: Path) -> dict:
    return json.loads(
        (reference_dir / "model_configs.json").read_text(encoding="utf-8")
    )


def ecology_for_rows(
    rows: pd.DataFrame, kind: str, reference_dir: Path
) -> pd.DataFrame:
    keys = pd.DataFrame({"district": rows["region"].map(REGIONS).to_numpy()})
    if keys["district"].isna().any():
        raise ValueError("Неизвестный район")
    if kind == "city":
        if not rows["year"].isin(PERIODS).all():
            raise ValueError(f"Доступные периоды экологии: {PERIODS}")
        keys["year"] = rows["year"].to_numpy(dtype=int)
    lookup = pd.read_csv(reference_dir / REFERENCE_FILES[kind])
    columns = list(keys)
    joined = keys.merge(
        lookup,
        on=columns,
        how="left",
        sort=False,
        validate="many_to_one",
        indicator=True,
    )
    if not joined["_merge"].eq("both").all():
        raise ValueError("В справочнике нет экологии для района/периода")
    exposure = joined.drop(columns=columns + ["_merge"])
    exposure.index = rows.index
    return exposure


def build_features(
    rows: pd.DataFrame, kind: str, reference_dir: Path, config: dict
) -> pd.DataFrame:
    # Whitelist: id, target and diagnosis history can never become features.
    frame = pd.DataFrame(index=rows.index)
    for source, name in CLINICAL_COLUMNS.items():
        frame[name] = pd.to_numeric(rows[source], errors="raise").astype(float)
    frame["имт"] = frame["вес"] / (frame["рост"] / 100) ** 2
    frame["район"] = rows["region"].map(REGIONS)
    frame["пол"] = rows["gender"].map({1: "муж", 2: "жен"})
    frame["город 1 /село 2"] = np.where(rows["region"] == 6, "2", "1")
    frame["алкоголь"] = rows["alcohol"].map(
        {0: "нет", 1: "употреблял ранее", 2: "да в настоящее время"}
    )
    profession = rows["profession"].map(PROFESSION_NAMES)
    if profession.isna().any():
        raise ValueError(
            "Профессия должна соответствовать классификации из 13 категорий"
        )
    # Normalize the notebook's 13 labels consistently for training and prediction.
    frame["профессия"] = profession.str.lower()
    exposure = ecology_for_rows(rows, kind, reference_dir)
    if kind == "city":
        exposure = exposure.drop(
            columns="суммарный канцерогенный индивидуальный риск ", errors="ignore"
        )
    frame = pd.concat([frame, exposure], axis=1)
    for col in config["round_to_2_cols"]:
        frame[col] = frame[col].round(2)
    if kind == "city":
        for col in config["round_to_6_cols"]:
            frame[col] = frame[col].round(6)
    interactions = {}
    for col in config["interaction_columns"]:
        interactions[col + " * вес"] = frame[col] * frame["вес"] / 70
        interactions[col + " * возраст"] = frame[col] * frame["возраст"] / 50
        interactions[col + " * имт"] = frame[col] * frame["имт"] / 30
        interactions[col + " * возраст * имт"] = (
            frame[col] * frame["возраст"] * frame["имт"] / 50 / 30
        )
    frame = pd.concat([frame, pd.DataFrame(interactions, index=frame.index)], axis=1)
    if kind == "region":
        for col in config["round_to_6_cols"]:
            frame[col] = frame[col].round(6)
    result = frame[config["features"]].copy()
    for col in result:
        if col in config["cat_features"]:
            result[col] = result[col].astype(str)
        else:
            result[col] = result[col].astype(float)
            if np.isinf(result[col]).any():
                raise ValueError(f"Бесконечное значение признака: {col}")
    return result


def request_frame(request: PredictRequest) -> pd.DataFrame:
    return pd.DataFrame([request.model_dump(mode="json")])
