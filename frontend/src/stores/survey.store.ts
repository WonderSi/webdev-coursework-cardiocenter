import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { SURVEY_CONFIG } from '@/config/survey.config'

export interface GlossaryOption {
  valueId: number
  label: string
}

export interface ValidationRule {
  min?: number
  max?: number
  required?: boolean
}

// Описание одного поля анкеты
export interface SurveyField {
  key: string
  type: 'radio' | 'number' | 'yesno'
  label: string
  unit?: string
  placeholder?: string
  horizontal?: boolean
  vertical?: boolean
  yearKey?: string
  options?: GlossaryOption[]
  validation?: ValidationRule
}

// Группа полей с заголовками и списком обязательных полей
export interface SurveyGroup {
  id: number
  title: string
  requiredFields: string[]
  fields: SurveyField[]
}

// Вся конфигурация анкеты с группами полей
export interface SurveyConfig {
  groups: SurveyGroup[]
}

// Результат ML: тип заболевания, вероятность и сообщение для пользователя
export interface PredictionResult {
  diseaseType: string | null
  probability: string | null
  message: string
}

// Ответ от бэкенда при отправке анкеты, содержащий результат предсказания
export interface SurveyResponse {
  prediction: PredictionResult | null
}

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'

async function apiFetch<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) throw new Error(`API ${url}: ${res.status} ${res.statusText}`)
  return res.json() as Promise<T>
}

// Создание store с названием survey
export const useSurveyStore = defineStore('survey', () => {
  const config = ref<SurveyConfig | null>(null)
  const answers = reactive<Record<string, any>>({})
  const loading = ref(false)
  const prediction = ref<PredictionResult | null>(null)

  // ЗАГРУЗКА С БЭКЭНДА
  /*
  async function loadConfig() {
    config.value = await apiFetch<SurveyConfig>('/survey/config')
    const allKeys = config.value.groups.flatMap(g =>
      g.fields.flatMap(f => f.yearKey ? [f.key, f.yearKey] : [f.key])
    )
    for (const key of allKeys) {
      answers[key] = null
    }
  }
  */

  // Загрузка из локального конфига
  function loadConfig() {
    config.value = SURVEY_CONFIG
    const allKeys = config.value.groups.flatMap(g =>
      g.fields.flatMap(f => f.yearKey ? [f.key, f.yearKey] : [f.key])
    )
    for (const key of allKeys) {
      answers[key] = null
    }
  }

  async function submit(): Promise<boolean> {
    loading.value = true
    try {
      const data = buildPayload()
      const response = await apiFetch<SurveyResponse>('/survey/submit', {
        method: 'POST',
        body: JSON.stringify(data),
      })
      prediction.value = response.prediction ?? null
      return true
    } catch (e) {
      console.error('Survey submit failed:', e)
      return false
    } finally {
      loading.value = false
    }
  }

  function reset() {
    for (const key of Object.keys(answers)) {
      answers[key] = null
    }
    prediction.value = null
  }

  function buildPayload() {
    const parseNum = (v: unknown) => v !== null && v !== undefined && v !== '' ? Number(v) : undefined

    return {
      genderId: answers.gender ?? undefined,
      age: parseNum(answers.age) ?? undefined,
      height: parseNum(answers.height),
      weight: parseNum(answers.weight),
      hipMeasurement: parseNum(answers.hipMeasurement),
      alcoholId: answers.alcohol ?? undefined,
      professionId: answers.profession ?? undefined,
      regionId: answers.region ?? undefined,
      glucose: parseNum(answers.glucose),
      cholesterol: parseNum(answers.cholesterol),
      nonHdlCholesterol: parseNum(answers.nonHdlCholesterol),
      vldlCholesterol: parseNum(answers.vldlCholesterol),
      hdlCholesterol: parseNum(answers.hdlCholesterol),
      ldlCholesterol: parseNum(answers.ldlCholesterol),
      apolipoproteinA: parseNum(answers.apolipoproteinA),
      apolipoproteinB: parseNum(answers.apolipoproteinB),
      triglycerides: parseNum(answers.triglycerides),
      stroke: answers.stroke === 1 ? true : answers.stroke === 0 ? false : undefined,
      strokeYear: parseNum(answers.strokeYear),
      heartFailure: answers.heartFailure === 1 ? true : answers.heartFailure === 0 ? false : undefined,
      heartFailureYear: parseNum(answers.heartFailureYear),
      cad: answers.cad === 1 ? true : answers.cad === 0 ? false : undefined,
      cadYear: parseNum(answers.cadYear),
      angina: answers.angina === 1 ? true : answers.angina === 0 ? false : undefined,
      anginaYear: parseNum(answers.anginaYear),
      myocardialInfarction: answers.myocardialInfarction === 1 ? true : answers.myocardialInfarction === 0 ? false : undefined,
      myocardialInfarctionYear: parseNum(answers.myocardialInfarctionYear),
      arterialHypertension: answers.arterialHypertension === 1 ? true : answers.arterialHypertension === 0 ? false : undefined,
      arterialHypertensionYear: parseNum(answers.arterialHypertensionYear),
      diagnoses: []
    }
  }

  return { config, answers, loading, prediction, loadConfig, submit, reset }
})
