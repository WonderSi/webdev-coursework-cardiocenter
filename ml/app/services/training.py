"""Train exactly city and region with patient-grouped train/validation/test sets."""

import hashlib
import json
from pathlib import Path

import pandas as pd
from catboost import CatBoostClassifier, Pool
from sklearn.metrics import (
    accuracy_score,
    average_precision_score,
    balanced_accuracy_score,
    brier_score_loss,
    confusion_matrix,
    f1_score,
    log_loss,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split

from app.services.preprocess import build_features, load_configs, PIPELINE_VERSION
from app.schemas.predict import SCHEMA_VERSION
from app.schemas.profession import PROFESSION_CLASSIFICATION, profession_catalog

TARGETS = {
    "city": "Составной исход обследования: инсульт, СН, ИМ/стенокардия/ИБС, АГ или смерть",
    "region": "Любой из составных сердечно-сосудистых исходов за весь период наблюдения",
}


def split_patients(df: pd.DataFrame, random_state: int = 1000) -> pd.Series:
    # One global patient split, shared by both model types and all visits.
    labels = df.groupby("id", sort=True)["target"].max()
    try:
        train, rest = train_test_split(
            labels.index.to_numpy(),
            test_size=0.3,
            random_state=random_state,
            stratify=labels.to_numpy(),
        )
        test, validation = train_test_split(
            rest,
            test_size=1 / 3,
            random_state=random_state,
            stratify=labels.loc[rest].to_numpy(),
        )
    except ValueError as exc:
        raise ValueError(
            "Недостаточно пациентов обоих классов для разделения 70/10/20"
        ) from exc
    assignments = {
        str(i): name
        for name, ids in (("train", train), ("validation", validation), ("test", test))
        for i in ids
    }
    return df["id"].map(assignments)


def calculate_metrics(y, probabilities) -> dict:
    predicted = (probabilities >= 0.5).astype(int)
    tn, fp, fn, tp = map(int, confusion_matrix(y, predicted, labels=[0, 1]).ravel())
    return {
        "accuracy": float(accuracy_score(y, predicted)),
        "balanced_accuracy": float(balanced_accuracy_score(y, predicted)),
        "roc_auc": float(roc_auc_score(y, probabilities)),
        "pr_auc": float(average_precision_score(y, probabilities)),
        "f1": float(f1_score(y, predicted, zero_division=0)),
        "precision": float(precision_score(y, predicted, zero_division=0)),
        "recall": float(recall_score(y, predicted, zero_division=0)),
        "logloss": float(log_loss(y, probabilities, labels=[0, 1])),
        "brier": float(brier_score_loss(y, probabilities)),
        "tn": tn,
        "fp": fp,
        "fn": fn,
        "tp": tp,
    }


def train_models(
    df: pd.DataFrame,
    *,
    out_dir: Path,
    reference_dir: Path,
    random_state: int = 1000,
    iteration_overrides: dict | None = None,
) -> dict:
    out_dir.mkdir(parents=True, exist_ok=True)
    configs = load_configs(reference_dir)
    splits = split_patients(df, random_state)
    split_table = df[["id", "model_kind", "year", "target"]].copy()
    split_table["split"] = splits
    split_table.to_csv(out_dir / "splits.csv", index=False)
    metrics = []
    # Validate both branches before spending time fitting either.
    for kind in ("city", "region"):
        for name in ("train", "validation", "test"):
            target = df.loc[(df["model_kind"] == kind) & (splits == name), "target"]
            if target.nunique() != 2:
                raise ValueError(f"{kind}/{name}: нужны пациенты обоих классов")
    for kind in ("city", "region"):
        rows = df[df["model_kind"] == kind]
        config = configs[kind]
        X = build_features(rows, kind, reference_dir, config)
        y = rows["target"]
        masks = {
            name: splits.loc[rows.index].eq(name)
            for name in ("train", "validation", "test")
        }
        pools = {
            name: Pool(X.loc[mask], y.loc[mask], cat_features=config["cat_features"])
            for name, mask in masks.items()
        }
        iterations = (iteration_overrides or {}).get(kind, config["iterations"])
        model = CatBoostClassifier(
            iterations=iterations,
            learning_rate=config["learning_rate"],
            depth=8,
            loss_function="Logloss",
            eval_metric="AUC",
            class_weights=[float(w) for w in config["class_weights"]],
            feature_weights=[
                float(config["feature_weights"].get(f, 1)) for f in config["features"]
            ],
            random_seed=100,
            thread_count=4,
            allow_writing_files=False,
            verbose=False,
        )
        model.fit(
            pools["train"],
            eval_set=pools["validation"],
            use_best_model=True,
            early_stopping_rounds=50,
        )
        probabilities = model.predict_proba(pools["test"])[:, 1]
        metric = {
            "model_stem": kind,
            "target": TARGETS[kind],
            "feature_count": X.shape[1],
            "iterations": int(model.tree_count_),
            "threshold": 0.5,
            "class_weight_0": config["class_weights"][0],
            "class_weight_1": config["class_weights"][1],
            **calculate_metrics(y.loc[masks["test"]], probabilities),
        }
        if "model_variant" in config:
            metric["model_variant"] = config["model_variant"]
        for name, mask in masks.items():
            metric[f"{name}_rows"] = int(mask.sum())
            metric[f"{name}_patients"] = int(rows.loc[mask, "id"].nunique())
            metric[f"{name}_pos"] = int(y.loc[mask].sum())
            metric[f"{name}_neg"] = int(mask.sum() - y.loc[mask].sum())
        metrics.append(metric)
        model.save_model(str(out_dir / f"{kind}_model.cbm"))
        (out_dir / f"{kind}_features.txt").write_text(
            "\n".join(config["features"]), encoding="utf-8"
        )
        pd.DataFrame(
            {
                "feature": config["features"],
                "importance": model.get_feature_importance(),
            }
        ).sort_values("importance", ascending=False).to_csv(
            out_dir / f"{kind}_feature_importance.csv", index=False
        )
        predictions = rows.loc[masks["test"], ["id", "year", "target"]].copy()
        predictions["probability"] = probabilities
        predictions["prediction"] = (probabilities >= 0.5).astype(int)
        predictions.to_csv(out_dir / f"{kind}_test_predictions.csv", index=False)
    (out_dir / "metrics.json").write_text(
        json.dumps(metrics, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    pd.DataFrame(metrics).to_csv(out_dir / "metrics.csv", index=False)
    return {
        "metrics": metrics,
        "model_configs": configs,
        "split_seed": random_state,
        "schema_version": SCHEMA_VERSION,
        "pipeline_version": PIPELINE_VERSION,
        "profession_classification": PROFESSION_CLASSIFICATION,
        "profession_categories": profession_catalog(),
        "split": "patient-grouped 70/10/20 shared by both models",
        "reference_sha256": {
            p.name: hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(reference_dir.iterdir())
            if p.is_file()
        },
    }
