"""Read one row per patient and derive both model datasets inside the service."""

import argparse
import hashlib
import json
from io import StringIO
from pathlib import Path

import numpy as np
import pandas as pd
from pydantic import ValidationError

from app.schemas.predict import PredictRequest, SCHEMA_VERSION
from app.schemas.profession import (
    encode_profession,
    PROFESSION_CLASSIFICATION,
    PROFESSION_NAMES,
    profession_catalog,
)
from app.services.preprocess import PERIODS, REGIONS

INPUT_COLUMNS = list(PredictRequest.model_fields)
DATASET_COLUMNS = ["id", *INPUT_COLUMNS, "target"]
# Original notebook label sources; label-only fields never enter build_features.
REGION_TARGET_COLUMNS = [
    "новые случаи им инсульт 2016 2021",
    "госпитализации 2016 2021",
    "новые случаи значимых ссз им онмк стенокардия фп плюс госпитализации 2016 2021",
    "новые случаи значимых ссз+смерть от ссз 2016 2021",
    "инсульт 2013",
    "инсульт 2017",
    "инсульт 2019",
    "инсульт 2021",
    "инсульт 2016 2021",
    "им стенокардия 2013",
    "ибс 2013",
    "ибс стенокардия 2017",
    "ибс стенокардия 2019",
    "ибс стенокардия 2021",
    "им 2016 2021",
    "ибс стенокардия и им 2016 2021",
    "им 2017",
    "им 2019",
    "им 2021",
    "ибс стенокардия 2016 2021",
    "смерть год 2015",
    "смерть 2017",
    "смерть 2019",
    "смерть 2021",
    "смерть 2016 2021",
    "смерть сердечная 2017",
    "смерть сердечная 2019",
    "смерть сердечная 2021",
    "смерть сердечная 2016 2021",
    "нарушения ритма ибс 2013",
    "нарушение ритма 2019",
    "нарушение ритма 2021",
    "нарушение ритма 2016 2021",
    "сн 2013",
    "сн 2017",
    "сн 2019",
    "сн 2021",
    "сн 2016 2021",
    "сердечные 2017",
    "сердечные 2019",
    "сердечные 2021",
    "сердечно сосудистые 2013",
    "сердечно сосудистые 2016 2021",
]

DATASET_SCHEMA_VERSION = 6
MEASUREMENTS = {
    "height": "рост",
    "weight": "вес",
    "cholesterol": "холестерин",
    "hdl_cholesterol": "холестерин лпвп",
    "ldl_cholesterol": "холестерин лпнп",
    "triglycerides": "тг",
}
PATIENT_NUMERIC_COLUMNS = ["возраст 2013", "глюкоза 2019 2021"] + [
    f"{name} {period}"
    for name in MEASUREMENTS.values()
    for period in ("2013", "2019 2021")
]
PATIENT_FLAG_COLUMNS = list(
    dict.fromkeys(
        REGION_TARGET_COLUMNS
        + [
            "код по аг после измерений 1 2013",
            "аг 2017",
            "аг 2019",
            "аг 2021",
            "аг 2016 2021",
            "хрон заболевания легких 2013",
        ]
    )
)
PATIENT_COLUMNS = [
    "id",
    "район",
    "город 1 /село 2",
    "пол 2013",
    "профессия 2013",
    "алкоголь 2013",
    *PATIENT_NUMERIC_COLUMNS,
    *PATIENT_FLAG_COLUMNS,
]
DERIVED_COLUMNS = {"model_kind", "year", "target"}


def normalized(series):
    return (
        series.astype(str).str.lower().str.strip().str.replace(r"\s+", " ", regex=True)
    )


def validate_patients_dataset(frame: pd.DataFrame) -> pd.DataFrame:
    """Validate the public wide format. Unknown clinical outcomes stay unknown."""
    if frame.empty:
        raise ValueError("CSV не содержит пациентов")
    if DERIVED_COLUMNS.intersection(frame):
        raise ValueError(
            "Нужна одна строка на пациента без model_kind, year и target; сервис вычислит их сам"
        )
    missing = sorted(set(PATIENT_COLUMNS) - set(frame))
    if missing:
        raise ValueError("В исходном CSV отсутствуют столбцы: " + ", ".join(missing))
    p = frame[PATIENT_COLUMNS].reset_index(drop=True).copy()
    if p["id"].isna().any() or p["id"].astype(str).str.strip().eq("").any():
        raise ValueError("Нужен непустой id каждого пациента")
    p["id"] = p["id"].astype(str).str.strip()
    if p["id"].duplicated().any():
        raise ValueError("Нужна одна строка на пациента: id не должны повторяться")
    for column in PATIENT_FLAG_COLUMNS:
        numeric = pd.to_numeric(p[column], errors="coerce")
        if not numeric.isin([0, 1]).all():
            raise ValueError(
                f"{column}: нужны известные значения 0/1; пропуски не считаются отсутствием события"
            )
        p[column] = numeric.astype(int)
    for column in PATIENT_NUMERIC_COLUMNS:
        original = p[column]
        numeric = pd.to_numeric(original, errors="coerce")
        if (
            (original.notna() & numeric.isna()) | np.isinf(numeric) | (numeric < 0)
        ).any():
            raise ValueError(
                f"{column}: ожидаются конечные неотрицательные числа или пропуск"
            )
        p[column] = numeric.astype(float)
    required_measurements = {
        "возраст 2013": 120,
        "рост 2013": 250,
        "рост 2019 2021": 250,
        "вес 2013": 300,
        "вес 2019 2021": 300,
    }
    for column, maximum in required_measurements.items():
        values = p[column]
        if (
            values.isna().any()
            or (values > maximum).any()
            or (column != "возраст 2013" and (values <= 0).any())
        ):
            raise ValueError(
                f"{column}: требуется известное значение в допустимом диапазоне"
            )
    gender = normalized(p["пол 2013"]).map({"муж": "муж", "жен": "жен"})
    gender = gender.fillna(
        pd.to_numeric(p["пол 2013"], errors="coerce").map({1: "муж", 2: "жен"})
    )
    if gender.isna().any():
        raise ValueError("пол 2013: ожидается муж/жен или 1/2")
    p["пол 2013"] = gender
    alcohol = normalized(p["алкоголь 2013"]).map(
        {
            "нет": "нет",
            "употреблял ранее": "употреблял ранее",
            "да в настоящее время": "да в настоящее время",
        }
    )
    alcohol = alcohol.fillna(
        pd.to_numeric(p["алкоголь 2013"], errors="coerce").map(
            {0: "нет", 1: "употреблял ранее", 2: "да в настоящее время"}
        )
    )
    if alcohol.isna().any():
        raise ValueError(
            "алкоголь 2013: ожидается нет/употреблял ранее/да в настоящее время или 0/1/2"
        )
    p["алкоголь 2013"] = alcohol
    profession = p["профессия 2013"].map(encode_profession)
    codes = pd.to_numeric(p["профессия 2013"], errors="coerce")
    profession = profession.fillna(codes.where(codes.isin(PROFESSION_NAMES)))
    if profession.isna().any():
        raise ValueError(
            "Профессии должны соответствовать 13 категориям из new_patients.csv; объединённые старые категории неоднозначны"
        )
    p["профессия 2013"] = profession.map(PROFESSION_NAMES)
    rural = pd.to_numeric(p["город 1 /село 2"], errors="coerce")
    if not rural.isin([1, 2]).all():
        raise ValueError("город 1 /село 2: ожидается 1 или 2")
    p["город 1 /село 2"] = rural.astype(int)
    p["район"] = normalized(p["район"])
    p.loc[rural == 2, "район"] = "сельская местность"
    if not p["район"].isin(REGIONS.values()).all():
        raise ValueError("район: неизвестная территория")
    return p


def any_flag(patients, columns):
    values = patients[columns].apply(pd.to_numeric, errors="raise")
    if not values.isin([0, 1]).all().all():
        raise ValueError(
            "Исходные диагнозы/исходы должны содержать известные значения 0/1"
        )
    return values.eq(1).any(axis=1).astype(int)


def validate_dataset(frame: pd.DataFrame) -> pd.DataFrame:
    """Validate internal derived records; callers upload wide patient rows instead."""
    required = {
        name
        for name, field in PredictRequest.model_fields.items()
        if field.is_required()
    }
    absent = (required | {"id", "target", "year"}) - set(frame)
    extra = set(frame) - set(DATASET_COLUMNS)
    if absent or extra:
        raise ValueError(
            f"Неверные столбцы CSV. Отсутствуют: {sorted(absent)}; лишние: {sorted(extra)}"
        )
    if frame.empty:
        raise ValueError("CSV не содержит строк")
    frame = frame.copy()
    if frame["id"].isna().any() or frame["id"].astype(str).str.strip().eq("").any():
        raise ValueError("Для группировки выборки нужен непустой id каждой строки")
    frame["id"] = frame["id"].astype(str).str.strip()
    targets = pd.to_numeric(frame["target"], errors="coerce")
    if not targets.isin([0, 1]).all():
        raise ValueError("target обязателен и должен быть 0 или 1")
    inputs = frame.drop(columns=["id", "target"]).astype(object)
    inputs = inputs.where(pd.notna(inputs), None)
    validated = []
    for line, data in enumerate(inputs.to_dict("records"), start=2):
        try:
            validated.append(
                PredictRequest.model_validate(data).model_dump(mode="json")
            )
        except ValidationError as exc:
            fields = [
                "/".join(map(str, e["loc"])) or "model_kind/year" for e in exc.errors()
            ]
            raise ValueError(
                f"Строка CSV {line}: неверные поля {', '.join(fields)}"
            ) from exc
    result = pd.DataFrame(validated)
    result.insert(0, "id", frame["id"].to_numpy())
    result["target"] = targets.to_numpy(dtype=int)
    if set(result["model_kind"]) != {"city", "region"}:
        raise ValueError("CSV должен содержать строки обеих моделей: city и region")
    if result.duplicated(["id", "model_kind", "year"]).any():
        raise ValueError("Повторяются строки id/model_kind/year")
    return result[DATASET_COLUMNS]


def read_dataset(path: Path) -> pd.DataFrame:
    # Check raw header before pandas can silently rename duplicate names.
    import csv

    with path.open(encoding="utf-8-sig", newline="") as source:
        header = next(csv.reader(source), [])
    if len(header) != len(set(header)):
        raise ValueError("В CSV повторяются названия столбцов")
    return validate_patients_dataset(
        pd.read_csv(path, dtype={"id": str}, encoding="utf-8-sig")
    )


def build_patient_dataset(patients: pd.DataFrame) -> pd.DataFrame:
    p = validate_patients_dataset(patients)
    common = pd.DataFrame(index=p.index)
    common["id"] = p["id"].astype(str)
    common["gender"] = normalized(p["пол 2013"]).map({"муж": 1, "жен": 2})
    common["alcohol"] = normalized(p["алкоголь 2013"]).map(
        {"нет": 0, "употреблял ранее": 1, "да в настоящее время": 2}
    )
    common["profession"] = p["профессия 2013"].map(encode_profession)
    if common["profession"].isna().any():
        raise ValueError(
            "Источник содержит профессии, отсутствующие в классификации из 13 категорий. "
            "Используйте new_patients.csv или исходные раздельные профессии: "
            "объединённые категории старого patients.csv нельзя однозначно разделить."
        )
    common["region"] = normalized(p["район"]).map({v: k for k, v in REGIONS.items()})
    rural = pd.to_numeric(p["город 1 /село 2"], errors="raise")
    if not rural.isin([1, 2]).all():
        raise ValueError("Неверный код город/село в patients.csv")
    common.loc[rural == 2, "region"] = 6
    common["chronic_lung_disease"] = p["хрон заболевания легких 2013"]
    common["glucose"] = p["глюкоза 2019 2021"]
    frames = []
    deceased = pd.Series(False, index=p.index)
    for year in PERIODS:
        row = common.copy()
        row["model_kind"] = "city"
        row["year"] = year
        row["age"] = p["возраст 2013"] + year - 2013
        suffix = "2013" if year <= 2017 else "2019 2021"
        for name, source in MEASUREMENTS.items():
            row[name] = p[f"{source} {suffix}"]
        row["stroke"] = p[f"инсульт {year}"]
        row["heart_failure"] = p[f"сн {year}"]
        row["arterial_hypertension"] = p[
            "код по аг после измерений 1 2013" if year == 2013 else f"аг {year}"
        ]
        row["angine"] = p["ибс 2013" if year == 2013 else f"ибс стенокардия {year}"]
        # Baseline MI and angina were combined; do not invent a separate MI diagnosis.
        row["myocardial_infarction"] = np.nan if year == 2013 else p[f"им {year}"]
        row["cad_chd_ihd"] = (
            p["нарушения ритма ибс 2013"]
            if year == 2013
            else (p[f"нарушение ритма {year}"] if year in (2019, 2021) else np.nan)
        )
        outcome = [
            f"инсульт {year}",
            f"сн {year}",
            "код по аг после измерений 1 2013" if year == 2013 else f"аг {year}",
        ]
        outcome += (
            ["им стенокардия 2013", "ибс 2013"]
            if year == 2013
            else [
                f"ибс стенокардия {year}",
                f"им {year}",
                f"смерть {year}",
                f"смерть сердечная {year}",
            ]
        )
        if year == 2017:
            outcome += ["смерть год 2015"]
        row["target"] = any_flag(p, outcome)
        frames.append(row.loc[~deceased].copy())
        if year > 2013:
            death = [f"смерть {year}", f"смерть сердечная {year}"]
            if year == 2017:
                death.append("смерть год 2015")
            deceased |= any_flag(p, death).astype(bool)
    region = common.copy()
    region["model_kind"] = "region"
    region["year"] = np.nan
    region["age"] = p["возраст 2013"]
    for name, source in MEASUREMENTS.items():
        region[name] = (p[f"{source} 2013"] + p[f"{source} 2019 2021"]) / 2
    region["target"] = any_flag(p, REGION_TARGET_COLUMNS)
    history = {
        "stroke": [
            f"инсульт {s}" for s in ("2013", "2017", "2019", "2021", "2016 2021")
        ],
        "heart_failure": [
            f"сн {s}" for s in ("2013", "2017", "2019", "2021", "2016 2021")
        ],
        "cad_chd_ihd": [
            "нарушения ритма ибс 2013",
            "нарушение ритма 2019",
            "нарушение ритма 2021",
            "нарушение ритма 2016 2021",
        ],
        "angine": [
            "ибс 2013",
            "ибс стенокардия 2017",
            "ибс стенокардия 2019",
            "ибс стенокардия 2021",
            "ибс стенокардия 2016 2021",
        ],
        "myocardial_infarction": ["им 2017", "им 2019", "им 2021", "им 2016 2021"],
        "arterial_hypertension": [
            "код по аг после измерений 1 2013",
            "аг 2017",
            "аг 2019",
            "аг 2021",
            "аг 2016 2021",
        ],
    }
    for name, columns in history.items():
        region[name] = any_flag(p, columns)
    frames.append(region)
    result = pd.concat(frames, ignore_index=True).reindex(columns=DATASET_COLUMNS)
    # The previous path wrote these records to CSV and read them with pandas before
    # model rounding. Preserve that float representation in memory: otherwise an
    # average at a decimal tie can round differently by 0.01 after removing the file.
    result = pd.read_csv(StringIO(result.to_csv(index=False)), dtype={"id": str})
    return validate_dataset(result)


def main():
    parser = argparse.ArgumentParser(
        description="Подготовить широкий CSV: одна строка на пациента, без target и model_kind"
    )
    parser.add_argument("--patients-csv", type=Path, required=True)
    parser.add_argument(
        "--output", type=Path, default=Path("app/data/train_dataset.csv")
    )
    args = parser.parse_args()
    result = read_dataset(args.patients_csv)
    prepared = build_patient_dataset(result)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    result.to_csv(args.output, index=False)
    manifest = {
        "source": args.patients_csv.name,
        "source_sha256": hashlib.sha256(args.patients_csv.read_bytes()).hexdigest(),
        "dataset_sha256": hashlib.sha256(args.output.read_bytes()).hexdigest(),
        "format": "patients_wide",
        "rows": len(result),
        "columns": len(result.columns),
        "derived_rows": prepared.groupby("model_kind").size().to_dict(),
        "patients": int(result["id"].nunique()),
        "schema_version": DATASET_SCHEMA_VERSION,
        "predict_schema_version": SCHEMA_VERSION,
        "region_bmi": "mean(weight) / (mean(height) / 100)^2",
        "missing_diagnosis_years": "unknown; left empty",
        "source_limitations": "prepared new_patients.csv; glucose 2019-2021 reused for historical city visits as in notebook",
        "profession_classification": PROFESSION_CLASSIFICATION,
        "profession_categories": profession_catalog(),
        "profession_source_counts": result["профессия 2013"].value_counts().to_dict(),
    }
    args.output.with_suffix(".manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(json.dumps(manifest, ensure_ascii=False))


if __name__ == "__main__":
    main()
