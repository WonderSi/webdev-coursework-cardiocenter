import asyncio
import fcntl
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import numpy as np
import pandas as pd

from app.config import settings
from app.main import app
from app.schemas.predict import PredictRequest, SCHEMA_VERSION
from app.schemas.profession import Profession, encode_profession, PROFESSION_NAMES
from app.services.dataset import (
    read_dataset,
    validate_dataset,
    build_patient_dataset,
    validate_patients_dataset,
    PATIENT_FLAG_COLUMNS,
    PATIENT_COLUMNS,
    DATASET_SCHEMA_VERSION,
)
from app.services.model_registry import active_bundle, load_bundle, ModelsUnavailable
from app.services.preprocess import (
    build_features,
    ecology_for_rows,
    load_configs,
    request_frame,
)
from app.services.retrain import retrain_from_dataset_csv
from app.services.training import split_patients

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/data/train_dataset.csv"
REFERENCES = ROOT / "app/data/ecology"
EXAMPLE = dict(
    model_kind="city",
    year=2021,
    gender=1,
    age=55,
    height=175,
    weight=80,
    alcohol=0,
    profession=2,
    region=5,
    chronic_lung_disease=0,
    glucose=5.7,
    cholesterol=5.2,
    hdl_cholesterol=1.5,
    ldl_cholesterol=3.4,
    triglycerides=1.2,
)


async def asgi_request(
    path, *, body=b"", content_type="application/json", method="POST"
):
    sent = False
    messages = []

    async def receive():
        nonlocal sent
        if not sent:
            sent = True
            return {"type": "http.request", "body": body, "more_body": False}
        await asyncio.Future()

    async def send(message):
        messages.append(message)

    scope = {
        "type": "http",
        "asgi": {"version": "3.0"},
        "http_version": "1.1",
        "method": method,
        "scheme": "http",
        "path": path,
        "raw_path": path.encode(),
        "query_string": b"",
        "root_path": "",
        "headers": [(b"content-type", content_type.encode())],
        "client": ("127.0.0.1", 1),
        "server": ("test", 80),
    }

    # Keep the in-process loop responsive in restricted runtimes without a socket server.
    async def pulse():
        while True:
            await asyncio.sleep(0.01)

    ticker = asyncio.create_task(pulse())
    try:
        await asyncio.wait_for(app(scope, receive, send), timeout=120)
    finally:
        ticker.cancel()
        try:
            await ticker
        except asyncio.CancelledError:
            pass
    status = next(m["status"] for m in messages if m["type"] == "http.response.start")
    content = b"".join(
        m.get("body", b"") for m in messages if m["type"] == "http.response.body"
    )
    return status, json.loads(content)


def api(path, data=None, method="POST"):
    return asyncio.run(
        asgi_request(
            path,
            body=json.dumps(data).encode() if data is not None else b"",
            method=method,
        )
    )


def upload(csv):
    boundary = "cardiocenter-test-boundary"
    body = (
        (
            f'--{boundary}\r\nContent-Disposition: form-data; name="dataset"; filename="patients.csv"\r\n'
            "Content-Type: text/csv\r\n\r\n"
        ).encode()
        + csv
        + f"\r\n--{boundary}--\r\n".encode()
    )
    return asyncio.run(
        asgi_request(
            "/retrain",
            body=body,
            content_type=f"multipart/form-data; boundary={boundary}",
        )
    )


class PipelineTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.patients = read_dataset(SOURCE)
        cls.frame = build_patient_dataset(cls.patients)
        cls.configs = load_configs(REFERENCES)

    def test_contract_and_removed_columns(self):
        deleted = {
            "hip_measurement",
            "non_hdl_cholesterol",
            "vldl_cholesterol",
            "apolipoprotein_a",
            "apolipoprotein_b",
        }
        self.assertFalse(deleted & set(self.frame))
        self.assertIn("chronic_lung_disease", self.frame)
        for name in deleted:
            self.assertEqual(api("/predict", dict(EXAMPLE, **{name: 1}))[0], 422)
        for overrides in (
            {"year": 2026},
            {"year": None},
            {"region": 99},
            {"height": 0},
            {"chronic_lung_disease": 2},
            {"glucose": float("inf")},
            {"model_kind": "region", "year": 2021},
        ):
            self.assertEqual(api("/predict", dict(EXAMPLE, **overrides))[0], 422)
        schema = api("/dataset/schema", method="GET")[1]
        self.assertEqual(schema["format"], "patients_wide")
        self.assertEqual(schema["derived_in_service"], ["model_kind", "year", "target"])
        self.assertFalse({"model_kind", "year", "target"} & set(schema["csv_columns"]))

    def test_profession_classification_13_categories(self):
        expected_counts = {
            1: 183,
            2: 939,
            3: 313,
            4: 137,
            5: 200,
            6: 21,
            7: 384,
            8: 262,
            9: 241,
            10: 9,
            11: 85,
            12: 126,
            13: 87,
        }
        observed = (
            self.frame[self.frame.model_kind.eq("region")]
            .profession.value_counts()
            .to_dict()
        )
        self.assertEqual(observed, expected_counts)
        self.assertEqual([int(code) for code in Profession], list(range(1, 14)))
        schema = api("/dataset/schema", method="GET")[1]
        self.assertEqual(schema["schema_version"], DATASET_SCHEMA_VERSION)
        self.assertEqual(schema["predict_schema_version"], SCHEMA_VERSION)
        self.assertEqual(len(schema["profession_categories"]), 13)
        for invalid in (0, 14, 99):
            self.assertEqual(api("/predict", dict(EXAMPLE, profession=invalid))[0], 422)
        for code, name in PROFESSION_NAMES.items():
            self.assertEqual(encode_profession(name.upper()), code)
            request = PredictRequest.model_validate(dict(EXAMPLE, profession=int(code)))
            X = build_features(
                request_frame(request), "city", REFERENCES, self.configs["city"]
            )
            self.assertEqual(X.iloc[0]["профессия"], name.lower())
        # These groups were merged in the old patients.csv and must now stay distinct.
        self.assertEqual(encode_profession("рабочие,ручного труда"), 7)
        self.assertEqual(encode_profession("низкоквал работник"), 9)
        self.assertEqual(encode_profession("служащие"), 4)
        self.assertEqual(encode_profession("работник в сфере обслуживания"), 5)
        self.assertEqual(encode_profession("работники среднего звена"), 3)
        self.assertEqual(encode_profession("лица свободных профессий"), 13)
        old = self.patients.iloc[:1].copy()
        old["профессия 2013"] = (
            "служащие, сфера обслуживания и работники среднего звена"
        )
        with self.assertRaisesRegex(ValueError, "new_patients.csv"):
            build_patient_dataset(old)

    def test_wide_input_validation(self):
        self.assertEqual(len(self.patients), 2987)
        self.assertTrue(self.patients.id.is_unique)
        self.assertFalse({"model_kind", "year", "target"} & set(self.patients))
        self.assertEqual(list(self.patients), PATIENT_COLUMNS)
        self.assertFalse(
            {
                "об1 2019 2021",
                "не лпвп 2013",
                "лпонп 2013",
                "липиды apoa 2013",
                "липиды apo b 2013",
            }
            & set(self.patients)
        )
        base = self.patients.iloc[:3].copy()
        for invalid in (
            base.assign(target=0),
            base.assign(model_kind="city"),
            base.assign(year=2021),
            base.drop(columns=["инсульт 2017"]),
            base.assign(id="duplicate"),
            base.assign(id=""),
            base.assign(**{"смерть 2021": np.nan}),
            base.assign(**{"инсульт 2017": 2}),
            base.assign(**{"возраст 2013": "invalid"}),
            base.assign(**{"рост 2013": 0}),
            base.assign(**{"город 1 /село 2": 9}),
            base.assign(**{"алкоголь 2013": "invalid"}),
        ):
            with self.assertRaises(ValueError):
                validate_patients_dataset(invalid)
        blank = base.copy()
        blank["глюкоза 2019 2021"] = np.nan
        self.assertTrue(
            validate_patients_dataset(blank)["глюкоза 2019 2021"].isna().all()
        )
        coded = base.copy()
        coded["профессия 2013"] = [2, 7, 13]
        coded["пол 2013"] = [1, 2, 1]
        coded["алкоголь 2013"] = [0, 1, 2]
        prepared = build_patient_dataset(coded)
        self.assertEqual(
            prepared.loc[prepared.model_kind.eq("region"), "profession"].tolist(),
            [2, 7, 13],
        )

    def test_automatic_labels_visits_and_death_exclusion(self):
        patients = self.patients.iloc[:2].copy()
        patients["id"] = ["deceased-example", "hospitalized-example"]
        patients[PATIENT_FLAG_COLUMNS] = 0
        patients.loc[patients.index[0], "смерть 2017"] = 1
        patients.loc[patients.index[0], "инсульт 2021"] = 1
        patients.loc[patients.index[1], "госпитализации 2016 2021"] = 1
        patients["возраст 2013"] = 40
        prepared = build_patient_dataset(patients)
        city = prepared[prepared.model_kind.eq("city")]
        deceased = city[city.id.eq("deceased-example")]
        self.assertEqual(deceased.year.tolist(), [2013, 2017])
        self.assertEqual(deceased.target.tolist(), [0, 1])
        hospitalized = city[city.id.eq("hospitalized-example")]
        self.assertEqual(hospitalized.target.tolist(), [0, 0, 0, 0])
        self.assertEqual(hospitalized.age.tolist(), [40, 44, 46, 48])
        region = prepared[prepared.model_kind.eq("region")]
        self.assertEqual(region.target.tolist(), [1, 1])

    def test_region_mean_rounding_matches_previous_csv_training(self):
        patients = self.patients.iloc[:1].copy()
        patients["тг 2013"] = 1.07
        patients["тг 2019 2021"] = 0.9
        patients["холестерин лпнп 2013"] = 3.61
        patients["холестерин лпнп 2019 2021"] = 4.1
        records = build_patient_dataset(patients)
        region = records[records.model_kind.eq("region")]
        features = build_features(region, "region", REFERENCES, self.configs["region"])
        self.assertEqual(features.iloc[0]["тг"], 0.98)
        self.assertEqual(features.iloc[0]["холестерин лпнп"], 3.86)

    def test_old_profession_models_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "manifest.json").write_text(
                json.dumps({"schema_version": 2, "pipeline_version": 2})
            )
            with self.assertRaises(ModelsUnavailable):
                load_bundle(root)

    def test_id_free_ecology_and_interactions(self):
        rows = pd.DataFrame([EXAMPLE, dict(EXAMPLE, weight=100)])
        rows["id"] = ["old-patient", "never-seen-patient"]
        for kind in ("city", "region"):
            exposure = ecology_for_rows(rows, kind, REFERENCES)
            pd.testing.assert_series_equal(
                exposure.iloc[0], exposure.iloc[1], check_names=False
            )
            altered = rows.assign(id=["another", "someone"])
            pd.testing.assert_frame_equal(
                exposure, ecology_for_rows(altered, kind, REFERENCES)
            )
            X = build_features(rows, kind, REFERENCES, self.configs[kind])
            direct = [f for f in X if f in exposure]
            np.testing.assert_allclose(
                X.loc[0, direct].astype(float),
                X.loc[1, direct].astype(float),
                equal_nan=True,
            )
            feature = (
                "неканцерогенный риск Аммиак * возраст * имт"
                if kind == "city"
                else "неканцерогенный риск Фенол * вес"
            )
            self.assertAlmostEqual(X.loc[1, feature] / X.loc[0, feature], 100 / 80)
            self.assertNotIn("id", X)
            self.assertNotIn("target", X)

    def test_ecology_matches_notebook_tables(self):
        for kind in ("city", "region"):
            rows = self.frame[self.frame.model_kind.eq(kind)].iloc[:100]
            actual = ecology_for_rows(rows, kind, REFERENCES)
            name = (
                "ap_city_by_district_period.csv"
                if kind == "city"
                else "ap_region_by_district.csv"
            )
            lookup = pd.read_csv(REFERENCES / name)
            known = lookup[lookup.district.eq("ленинский")]
            chosen = rows[rows.region.eq(5)]
            for idx, row in chosen.iterrows():
                expected = (
                    known[known.year.eq(row.year)].iloc[0]
                    if kind == "city"
                    else known.iloc[0]
                )
                np.testing.assert_allclose(
                    actual.loc[idx].to_numpy(float),
                    expected[actual.columns].to_numpy(float),
                    equal_nan=True,
                )

    def test_dataset_validation_and_group_split(self):
        splits = split_patients(self.frame)
        self.assertEqual(splits.groupby(self.frame["id"]).nunique().max(), 1)
        self.assertEqual(
            self.frame.assign(split=splits).groupby("split").id.nunique().to_dict(),
            {"train": 2090, "validation": 299, "test": 598},
        )
        for changed in (
            self.frame.assign(target=0.5),
            self.frame.assign(target=np.nan),
            self.frame.assign(id=""),
            pd.concat([self.frame, self.frame.iloc[:1]]),
            self.frame.assign(injected_ecology=1),
            self.frame[self.frame.model_kind.eq("city")],
        ):
            with self.assertRaises(ValueError):
                validate_dataset(changed)

    def test_csv_and_json_produce_identical_features(self):
        for kind in ("city", "region"):
            row = self.frame[self.frame.model_kind.eq(kind)].iloc[10:11]
            data = (
                row.drop(columns=["id", "target"])
                .astype(object)
                .where(pd.notna(row.drop(columns=["id", "target"])), None)
                .iloc[0]
                .to_dict()
            )
            request = PredictRequest.model_validate(data)
            a = build_features(row, kind, REFERENCES, self.configs[kind]).reset_index(
                drop=True
            )
            b = build_features(
                request_frame(request), kind, REFERENCES, self.configs[kind]
            )
            pd.testing.assert_frame_equal(a, b)
            # Zero remains zero; unknown labs remain NaN, without hard-coded imputations.
            altered = dict(data, glucose=0, ldl_cholesterol=None)
            c = build_features(
                request_frame(PredictRequest.model_validate(altered)),
                kind,
                REFERENCES,
                self.configs[kind],
            )
            self.assertEqual(c.iloc[0]["глюкоза"], 0)
            self.assertTrue(pd.isna(c.iloc[0]["холестерин лпнп"]))

    def test_city_without_cholesterol_flag_contract(self):
        config = self.configs["city"]
        self.assertEqual(
            config["model_variant"], "city_filtered_5_without_cholesterol_status"
        )
        self.assertEqual(len(config["features"]), 25)
        self.assertNotIn("итог холестерин", config["features"])
        self.assertNotIn("cholesterol_status", PredictRequest.model_fields)
        self.assertNotIn("итог холестерин 2019 2021", self.patients)
        self.assertEqual(
            config["cat_features"], ["район", "пол", "профессия", "алкоголь"]
        )
        self.assertEqual(config["class_weights"], [1, 0.578])
        self.assertEqual(
            config["feature_weights"], {"профессия": 5, "район": 5, "алкоголь": 4}
        )
        self.assertEqual(config["iterations"], 50)
        self.assertEqual(api("/predict", dict(EXAMPLE, cholesterol_status=1))[0], 422)
        self.assertEqual(len(self.configs["region"]["features"]), 26)

    def test_full_http_retrain_predict_reload_and_failure(self):
        with tempfile.TemporaryDirectory() as directory:
            models = Path(directory) / "models"
            with (
                patch.object(settings, "MODELS_DIR", models),
                patch.object(settings, "ECOLOGY_DIR", REFERENCES),
            ):
                self.assertEqual(api("/predict", EXAMPLE)[0], 503)
                status, trained = upload(SOURCE.read_bytes())
                self.assertEqual(status, 200, trained)
                self.assertEqual(
                    trained["saved_models"], ["city_model.cbm", "region_model.cbm"]
                )
                self.assertEqual(trained["input_patients"], 2987)
                self.assertEqual(
                    trained["prepared_rows"], {"city": 11789, "region": 2987}
                )
                bundle = active_bundle(models)
                self.assertEqual(bundle.metadata["input_format"], "patients_wide")
                self.assertEqual(bundle.metadata["input_patients"], 2987)
                self.assertEqual(
                    bundle.metadata["prepared_rows"], {"city": 11789, "region": 2987}
                )
                reloaded = load_bundle(bundle.directory)
                for kind in ("city", "region"):
                    request = dict(
                        EXAMPLE, model_kind=kind, year=2021 if kind == "city" else None
                    )
                    status, response = api("/predict", request)
                    self.assertEqual(status, 200, response)
                    self.assertEqual(
                        response["model_version"], trained["model_version"]
                    )
                    self.assertEqual(response["predictions"][0]["disease"], kind)
                    self.assertEqual(api("/predictions/predict", request)[1], response)
                    rows = self.frame[self.frame.model_kind.eq(kind)]
                    X = build_features(
                        rows,
                        kind,
                        bundle.reference_dir,
                        bundle.metadata["model_configs"][kind],
                    )
                    probabilities = reloaded.models[kind].predict_proba(X)[:, 1]
                    heldout = pd.read_csv(
                        bundle.directory / f"{kind}_test_predictions.csv",
                        dtype={"id": str},
                    )
                    keys = ["id", "year"] if kind == "city" else ["id"]
                    recovered = rows[keys].copy()
                    recovered["computed"] = probabilities
                    paired = heldout.merge(recovered, on=keys, validate="one_to_one")
                    np.testing.assert_allclose(
                        paired.probability, paired.computed, atol=1e-12
                    )
                self.assertEqual(len(api("/metrics", method="GET")[1]), 2)
                current = (models / "CURRENT").read_bytes()
                self.assertEqual(upload(b"id,age\n1,45")[0], 422)
                self.assertEqual(upload(b"")[0], 422)
                self.assertEqual(
                    upload(self.frame.to_csv(index=False).encode())[0], 422
                )
                self.assertEqual(
                    upload(
                        self.patients.drop(columns=["смерть 2021"])
                        .to_csv(index=False)
                        .encode()
                    )[0],
                    422,
                )
                with patch(
                    "app.services.retrain.train_models",
                    side_effect=RuntimeError("intentional failure"),
                ):
                    with self.assertRaises(RuntimeError):
                        retrain_from_dataset_csv(
                            SOURCE, models_dir=models, reference_dir=REFERENCES
                        )
                self.assertEqual((models / "CURRENT").read_bytes(), current)
                self.assertFalse(list((models / "versions").glob(".training-*")))
                with (models / ".retrain.lock").open("a") as lock:
                    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    self.assertEqual(upload(SOURCE.read_bytes())[0], 409)
                with patch.object(settings, "MAX_UPLOAD_BYTES", 8):
                    self.assertEqual(upload(SOURCE.read_bytes())[0], 413)
                self.assertEqual((models / "CURRENT").read_bytes(), current)
                # Publishing a second version is observed by a worker that cached the first.
                second = retrain_from_dataset_csv(
                    SOURCE,
                    models_dir=models,
                    reference_dir=REFERENCES,
                    iteration_overrides={"city": 2, "region": 2},
                )
                self.assertNotEqual(second["model_version"], trained["model_version"])
                self.assertEqual(
                    api("/predict", EXAMPLE)[1]["model_version"],
                    second["model_version"],
                )
                # Unrelated legacy files are never auto-discovered as active models.
                (models / "old_model.cbm").write_text("not a model")
                self.assertEqual(set(active_bundle(models).models), {"city", "region"})


if __name__ == "__main__":
    unittest.main()
