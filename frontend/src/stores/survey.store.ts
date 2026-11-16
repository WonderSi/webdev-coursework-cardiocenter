import { defineStore } from 'pinia'
import { ref, reactive, watch } from 'vue'
import { buildPatientPayload, createPatient, type PatientPayload } from '@/api/patients.api'
import { useUserStore } from '@/stores/user.store'
import { SURVEY_CONFIG } from '@/config/survey.config'

export interface GlossaryOption {
  valueId: number | string
  label: string
}

export interface ValidationRule {
  min?: number
  max?: number
  integer?: boolean
  required?: boolean
}

// Описание одного поля анкеты
export interface SurveyField {
  key: string
  type: 'radio' | 'number' | 'yesno' | 'select'
  label: string
  unit?: string
  placeholder?: string
  horizontal?: boolean
  vertical?: boolean
  yearKey?: string
  options?: GlossaryOption[]
  visibleWhen?: { key: string; value: number | string }
  validation?: ValidationRule
}

// Группа полей с заголовками и списком обязательных полей
export interface SurveyGroup {
  id: number
  title: string
  description?: string
  requiredFields: string[]
  fields: SurveyField[]
}

// Вся конфигурация анкеты с группами полей
export interface SurveyConfig {
  groups: SurveyGroup[]
}

// Один составной исход ML и его вероятность (целое 0–100).
export interface DiseasePrediction {
  name: string
  probability: number
  modelKind: 'city' | 'region'
  prediction: 0 | 1
}

// Ответ от бэкенда при отправке анкеты
export interface SurveyResponse {
  predictions: DiseasePrediction[]
  modelVersion: string | null
}

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')

async function apiFetch<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) throw new Error(`API ${url}: ${res.status} ${res.statusText}`)
  return res.json() as Promise<T>
}

// Создание store с названием survey
function createSurveyState() {
  const patientSnapshot = ref<PatientPayload | null>(null)
  const patientSaved = ref(false)
  const savingPatient = ref(false)
  const patientError = ref('')
  let generation = 0
  const config = ref<SurveyConfig | null>(null)
  const answers = reactive<Record<string, string | number | null>>({})
  const loading = ref(false)
  const predictions = ref<DiseasePrediction[]>([])
  const submitted = ref(false)
  const modelVersion = ref<string | null>(null)

  // Загрузка из локального конфига
  function loadConfig() {
    reset()
    config.value = SURVEY_CONFIG
    
    config.value.groups.forEach(group => {
      group.fields.forEach(field => {
        // На каждый вопрос, включая заболевания лёгких, нужен явный ответ.
        answers[field.key] = null
      })
    })
  }

  async function submit(): Promise<boolean> {
    if (loading.value) return false
    const request = generation
    loading.value = true
    try {
      const data = buildPayload()
      const snapshot = buildPatientPayload(answers)
      const response = await apiFetch<SurveyResponse>('/survey/submit', {
        method: 'POST',
        body: JSON.stringify(data),
      })
      if (request !== generation) return false
      if (!response.predictions?.length) throw new Error('Prediction missing')
      patientSnapshot.value = snapshot
      predictions.value = response.predictions
      modelVersion.value = response.modelVersion ?? null
      submitted.value = true
      return true
    } catch (e) {
      console.error('Survey submit failed:', e)
      return false
    } finally {
      if (request === generation) loading.value = false
    }
  }

  function reset() {
    generation++
    loading.value = false
    patientSnapshot.value = null
    patientSaved.value = false
    patientError.value = ''
    for (const key of Object.keys(answers)) {
      answers[key] = null
    }
    predictions.value = []
    modelVersion.value = null
    submitted.value = false
  }

  async function savePatient() {
    if (savingPatient.value || patientSaved.value || !patientSnapshot.value) return
    if (!useUserStore().canAccessSection('UPLOAD_DATA')) {
      patientError.value = 'Недостаточно прав для сохранения пациента.'
      return
    }
    const request = generation
    savingPatient.value = true
    patientError.value = ''
    try {
      await createPatient(patientSnapshot.value)
      if (request === generation) patientSaved.value = true
    } catch (cause) {
      if (request === generation) patientError.value = cause instanceof Error ? cause.message : 'Не удалось сохранить пациента.'
    } finally {
      savingPatient.value = false
    }
  }

  function buildPayload() {
    const parseNum = (v: unknown) => v !== null && v !== undefined && v !== '' ? Number(v) : undefined

    return {
      modelKind: answers.modelKind,
      year: answers.modelKind === 'city' ? 2021 : null,
      chronicLungDisease: answers.chronicLungDisease,
      genderId: answers.gender ?? undefined,
      age: parseNum(answers.age) ?? undefined,
      height: parseNum(answers.height),
      weight: parseNum(answers.weight),
      alcoholId: answers.alcohol ?? undefined,
      professionId: answers.profession ?? undefined,
      regionId: answers.region ?? undefined,
      glucose: parseNum(answers.glucose),
      cholesterol: parseNum(answers.cholesterol),
      hdlCholesterol: parseNum(answers.hdlCholesterol),
      ldlCholesterol: parseNum(answers.ldlCholesterol),
      triglycerides: parseNum(answers.triglycerides),
    }
  }

  return { config, answers, loading, predictions, modelVersion, submitted, loadConfig, submit, reset,
    patientSaved, savingPatient, patientError, savePatient }
}

export const useSurveyStore = defineStore('survey', createSurveyState)

export const useDoctorSurveyStore = defineStore('doctor-survey', () => {
  const state = createSurveyState()
  const user = useUserStore()
  watch(() => user.email, () => state.reset(), { flush: 'sync' })
  return state
})
