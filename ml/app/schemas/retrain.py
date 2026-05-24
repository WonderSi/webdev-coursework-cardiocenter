from pydantic import BaseModel, Field


class RetrainMetric(BaseModel):
    model_stem: str
    feature_count: int
    train_rows: int
    test_rows: int
    train_pos: int
    train_neg: int
    test_pos: int
    test_neg: int
    class_weight_0: float
    class_weight_1: float
    roc_auc: float
    pr_auc: float
    accuracy: float
    balanced_accuracy: float
    precision: float
    recall: float
    f1: float
    logloss: float
    brier: float
    tn: int
    fp: int
    fn: int
    tp: int


class RetrainResponse(BaseModel):
    status: str = Field(description="ok / error")
    saved_models: list[str] = Field(description="Список сохранённых файлов моделей")
    model_version: str = Field(description="Версия модели")
    metrics: list[RetrainMetric] = Field(description="Метрики переобученных моделей")
