// - статус об успешности/неуспешности переобучения (после нажатия на кнопку проверяем) (status)

// // БЛОК ОБЩЕЙ ИНФОРМАЦИИ 
// - версия модели (model_version)
// - список активных моделей (saved_models список, нужен маппинг к ру-названиям)

// // БЛОК МОДЕЛЕЙ (каждая под своим именем в списке) ДЛЯ КАЖДОЙ:
//   // БЛОК ИНФОРМАЦИЯ О ДАННЫХ (Dataset info) / НАСТРОЙКИ МОДЕЛИ
//   - имя модели (model_stem маппед к русскому имени)
//   - количество признаков которые учитывает модель (feature_count)
//   - размер тестовой выборки (test_rows)
//     - (train_pos)
//     - (train_neg)
//   - размер обучающей выборки (train_rows)
//     - (test_pos)
//     - (test_neg)
//   - вес pos (class_weight_0)
//   - вес meg (lass_weight_1)

//   // БЛОК МЕТРИКИ КАЧЕСТВА МОДЕЛИ
//   - общая способность модели отличать пациентов от здоровых (roc_aus) 
//   - доля правильных ответов (accuracy)
//   - точность (precision)
//   - полнота (recall)
//   - F1-мера / баланс между точностью и полнотой (f1)
//   // БЛОК МЕТРИКИ КАЧЕСТВА МОДЕЛИ == ДОПОЛНИТЕЛЬНО
//       - (logloss)
//       - (brier)
//       - (pr_auc)
//       - (balanced_accuracy)

//   // МАТРИЦА ОШИБОК   
//   - (tp) Истинно положительные (Модель сказала "Болен", и человек реально болен).
//   - (tn) Истинно отрицательные (Модель сказала "Здоров", и человек реально здоров).
//   - (fp) Ложная тревога (Модель сказала "Болен", а человек здоров).
//   - (fn) Пропуск заболевания (Модель сказала "Здоров", а человек болен — самая опасная ошибка в медицине).


export interface GeneralStats {
  modelVersion: string
  savedModels: [] // как здесь обозначить что на вход придёт массив с 7 строками?  
}

export interface OneModelStats {
    modelStem: string
    featureCount: number
    trainRows: number
    testRows: number
    trainPos: number
    trainNeg: number
    testPos: number
    testNeg: number
    classWeight0: number 
    classWeight1: number
    rocAuc: number
    prAuc: number
    accuracy: number
    balancedAccuracy: number
    precision: number
    recall: number
    f1: number
    logloss: number
    brier: number
    tn: number
    fp: number
    fn: number
    tp: number
}

export interface RetrainStatusResponse { status: string }

export interface FullModelsResponse {
  GeneralStats: GeneralStats[]
}

export async function fetchDashboardStats(): Promise<FullModelsResponse> {
  const res = await fetch('/api/admin/model')
  if (!res.ok) throw new Error(`Model fetch failed: ${res.status}`)
  return res.json()
}
