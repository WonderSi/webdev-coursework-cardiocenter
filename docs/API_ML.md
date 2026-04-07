# API для ML

Бэкенд вызывает ML-сервис по адресу `http://localhost:8000` (env-переменная `ML_SERVICE_URL`).

---

## 1. `/predict` — получение прогноза

**Метод:** `POST`
**URL:** `http://localhost:8000/predict`
**Content-Type:** `application/json`

### Запрос

```json
{
  "gender": 1,
  "age": 35,
  "height": 175,
  "weight": 70,
  "hip_measurement": 90,
  "alcohol": 1,
  "profession": 7,
  "region": 2,
  "glucose": 4.5,
  "cholesterol": 5.3,
  "non_hdl_cholesterol": 3.2,
  "vldl_cholesterol": 0.8,
  "hdl_cholesterol": 1.2,
  "ldl_cholesterol": 3.0,
  "apolipoprotein_a": 1.5,
  "apolipoprotein_b": 0.9,
  "triglycerides": 1.7,
  "stroke": false,
  "stroke_year": null,
  "heart_failure": true,
  "heart_failure_year": 2018,
  "cad_chd_ihd": false,
  "cad_chd_ihd_year": null,
  "angina": false,
  "angina_year": null,
  "myocardial_infarction": false,
  "myocardial_infarction_year": null,
  "arterial_hypertension": true,
  "arterial_hypertension_year": 2015
}
```

### Ожидаемый ответ

```json
{
  "diseaseType": "Артериальная гипертензия",
  "probability": 0.67,
  "message": "По результатам опроса у вас повышенный риск развития сердечно-сосудистых заболеваний (АГ). Рекомендуем обратиться к кардиологу."
}
```
`probability` - обязательно<br/>
`diseaseType` и `message` - опционально<br/>
Что точно можно вывести от ML - не понятно, не решили. Поэтому сойдемся на этом.

### Формат ответа

| Поле | Тип | Описание |
|---|---|---|
| `diseaseType` | `string` | Тип предсказанного заболевания |
| `probability` | `number` | Вероятность от 0.0 до 1.0 |
| `message` | `string` | Текст для вывода пациенту |

### Обработка ошибок

Если ML-сервис недоступен (timeout, 5xx, 404) — бэкенд возвращает заглушку:
```json
{
  "diseaseType": "Артериальная гипертензия",
  "probability": "67%",
  "message": "...повышенный риск..."
}
```

**Это временное решение, возможно вовсе ничего не будем выводить**

---

## 2. `/retrain` — переобучение модели

**(Пока в разработке)** Бэкенд будет вызывать этот эндпоинт для переобучения модели на новых данных пациентов.

**Метод:** `POST`
**URL:** `http://localhost:8000/retrain`

