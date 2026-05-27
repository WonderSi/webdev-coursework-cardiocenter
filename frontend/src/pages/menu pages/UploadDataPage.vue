<template>
  <Sidebar />

  <div class="upload-page">
    <Transition name="fade">
      <div v-if="showErrors && hasErrors" class="error-banner">
        <span>{{ currentErrorMessage }}</span>
      </div>
    </Transition>

    <div class="upload-duo-cards">
      <div class="upload-card">
        <div 
          class="upload-card-upper-part"
          :class="{ 'drag-over': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
          @click="triggerFileInput"
        >
          <div class="upload-border">
            <div class="upload-icon">
              <svg width="77" height="76" viewBox="0 0 78 76" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="38.5" cy="37.8032" rx="38.5" ry="37.8032" fill="#242942"/>
                <path fill-rule="evenodd" clip-rule="evenodd" d="M28.6863 55.0743C26.0839 55.0743 23.5881 54.0405 21.748 52.2003C19.9078 50.3602 18.874 47.8644 18.874 45.262V39.3747C18.874 38.8542 19.0808 38.355 19.4488 37.987C19.8168 37.619 20.316 37.4122 20.8365 37.4122C21.3569 37.4122 21.8561 37.619 22.2241 37.987C22.5922 38.355 22.7989 38.8542 22.7989 39.3747V45.262C22.7989 46.8234 23.4192 48.3209 24.5233 49.425C25.6274 50.5291 27.1248 51.1494 28.6863 51.1494H48.3107C49.8722 51.1494 51.3696 50.5291 52.4737 49.425C53.5778 48.3209 54.1981 46.8234 54.1981 45.262V39.3747C54.1981 38.8542 54.4048 38.355 54.7729 37.987C55.1409 37.619 55.6401 37.4122 56.1605 37.4122C56.681 37.4122 57.1802 37.619 57.5482 37.987C57.9162 38.355 58.123 38.8542 58.123 39.3747V45.262C58.123 47.8644 57.0892 50.3602 55.249 52.2003C53.4089 54.0405 50.9131 55.0743 48.3107 55.0743H28.6863Z" fill="white"/>
                <path fill-rule="evenodd" clip-rule="evenodd" d="M28.2661 31.7203C28.0857 31.536 27.9434 31.3179 27.8473 31.0786C27.7512 30.8392 27.7033 30.5833 27.7062 30.3254C27.7091 30.0675 27.7628 29.8127 27.8643 29.5756C27.9657 29.3384 28.113 29.1237 28.2975 28.9435L37.1285 20.3087C37.4952 19.9503 37.9875 19.7496 38.5003 19.7496C39.013 19.7496 39.5054 19.9503 39.872 20.3087L48.703 28.9435C48.8912 29.1227 49.042 29.3375 49.1465 29.5754C49.251 29.8134 49.3073 30.0697 49.3119 30.3296C49.3166 30.5894 49.2696 30.8476 49.1736 31.0891C49.0777 31.3307 48.9347 31.5507 48.7531 31.7366C48.5714 31.9224 48.3546 32.0703 48.1153 32.1717C47.876 32.2731 47.6189 32.3259 47.3591 32.3271C47.0992 32.3284 46.8416 32.278 46.6014 32.1788C46.3611 32.0797 46.143 31.9339 45.9595 31.7498L40.4627 26.3746V43.2988C40.4627 43.8192 40.256 44.3184 39.8879 44.6864C39.5199 45.0545 39.0207 45.2612 38.5003 45.2612C37.9798 45.2612 37.4806 45.0545 37.1126 44.6864C36.7446 44.3184 36.5378 43.8192 36.5378 43.2988V26.3746L31.043 31.7498C30.8586 31.9302 30.6406 32.0725 30.4012 32.1686C30.1619 32.2646 29.9059 32.3126 29.648 32.3097C29.3901 32.3068 29.1353 32.253 28.8982 32.1516C28.6611 32.0501 28.4463 31.9029 28.2661 31.7184V31.7203Z" fill="white"/>
              </svg>
            </div>
            <p class="upload-main-p">
              <span class="underlined-hover">Выберите</span> или перетащите файлы
            </p>
            <p class="upload-formats-p">.xlsx, .xls, .csv — до 10 МБ</p>
            
            <input 
              type="file" 
              ref="fileInputRef" 
              style="display: none" 
              multiple 
              accept=".xlsx,.xls,.csv" 
              @change="handleFileChange" 
            />
          </div>
        </div>

        <div class="upload-card-bottom-part">
          <div class="bottom-card-text-block">
            <p class="alert-p">Пожалуйста, перед загрузкой убедитесь, что ваши данные соответствуют <b>шаблону</b></p>
            <p class="attention-p">Обратите внимание на лист <b>«<span class="underlined">справочник</span>»</b></p>
          </div>
          <button class="download-template-btn" @click="downloadTemplate" :disabled="templateLoading">
            {{ templateLoading ? 'Загрузка...' : 'Скачать шаблон' }}
          </button>
        </div>
      </div>

      <div class="files-list-card">
        <div class="wrapper_column">
          <div class="section-header">Список загружаемых файлов</div>

          <div class="wrapper_row">
            <div class="files-progress-bar">
              <div class="progress-fill" :style="{ height: uploadProgress + '%' }"></div>
            </div>
            
            <div class="main-content-files-list">
              <div class="file-list">
                <p v-if="files.length === 0" style="color: #8A8FA8; margin: auto; font-size: 1rem;">
                  Нет выбранных файлов
                </p>

                <div class="file-item" v-for="(file, index) in files" :key="index">
                  <div class="file-item-main">
                    <div class="file-icon"> 
                      <span class="file-extension">{{ getFileExtension(file.name) }}</span> 
                    </div>
                  
                    <div class="file-details">
                      <p class="file-name">{{ file.name }}</p>
                    </div>

                    <div class="file-right-side">
                      <p class="file-size">{{ formatSize(file.size) }}</p>
                      <button class="delete-file-btn" @click="removeFile(index)" :disabled="uploading">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                          <path d="M14 1.41L12.59 0L7 5.59L1.41 0L0 1.41L5.59 7L0 12.59L1.41 14L7 8.41L12.59 14L14 12.59L8.41 7L14 1.41Z"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="files-list-actions">
            <button class="cancel-all-btn" @click="cancelAll" :disabled="uploading || files.length === 0">
              Отменить все
            </button>
            <button class="upload-btn" @click="uploadFiles" :disabled="uploading || files.length === 0">
              {{ uploading ? 'Загрузка...' : 'Загрузить' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="one-patient-input-card">
      <div class="input-patient-part-one"> 
        <div class="input-patient-one-col1">
          <div class="section-header">Ввести одного пациента</div>

          <div class="section-info">
            <div class="input-row">
              <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.gender }">Пол:</div>
              <div class="radio-group-horizontal" :class="{ 'group-error': showPatientErrors && patientFieldErrors.gender }">
                <label class="radio-label">Мужской<input type="radio" v-model="singlePatient.gender" :value="1"></label>
                <label class="radio-label">Женский<input type="radio" v-model="singlePatient.gender" :value="2"></label>
              </div>
            </div>

            <div class="input-row">
              <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.age }">Возраст:</div>
              <div class="input-group">
                <input type="number" class="input-field" :class="{ 'field-error': showPatientErrors && patientFieldErrors.age }" placeholder="27" v-model="singlePatient.age">
                <span class="unit">лет</span>
              </div>
            </div>

            <div class="input-row">
              <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.height }">Рост:</div>
              <div class="input-group">
                <input type="number" class="input-field" :class="{ 'field-error': showPatientErrors && patientFieldErrors.height }" placeholder="165" v-model="singlePatient.height">
                <span class="unit">см</span>
              </div>
            </div>

            <div class="input-row">
              <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.weight }">Вес:</div>
              <div class="input-group">
                <input type="number" class="input-field" :class="{ 'field-error': showPatientErrors && patientFieldErrors.weight }" placeholder="60" v-model="singlePatient.weight">
                <span class="unit">кг</span>
              </div>
            </div>

            <div class="input-row">
              <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.hipMeasurement }">Объём бёдер:</div>
              <div class="input-group">
                <input type="number" class="input-field" :class="{ 'field-error': showPatientErrors && patientFieldErrors.hipMeasurement }" placeholder="90" v-model="singlePatient.hipMeasurement">
                <span class="unit">см</span>
              </div>
            </div>
          </div>

          <div class="div-line"></div>

          <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.alcohol }">Алкоголь:</div>
          <div class="radio-group-vertical" :class="{ 'group-error-vert': showPatientErrors && patientFieldErrors.alcohol }">
            <label class="radio-label"><input type="radio" v-model="singlePatient.alcohol" :value="3"> Употребляю</label>
            <label class="radio-label"><input type="radio" v-model="singlePatient.alcohol" :value="2"> Употреблял ранее</label>
            <label class="radio-label"><input type="radio" v-model="singlePatient.alcohol" :value="1"> Никогда не употреблял</label>
          </div>
        </div>
        
        <div class="input-patient-one-col2">
          <div class="patient-subheader" style="flex-shrink: 0;" :class="{ 'label-error': showPatientErrors && patientFieldErrors.profession }">Основной род деятельности:</div>

          <div class="list-wrapper" :class="{ 'group-error-vert': showPatientErrors && patientFieldErrors.profession }">
            <div class="scrollable-radio-list radio-group-vertical" style="margin-top: 0;">
              <label class="radio-label" v-for="prof in PROFESSIONS" :key="prof.valueId">
                <input type="radio" v-model="singlePatient.profession" :value="prof.valueId">
                {{ prof.label }}
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="div-line2"></div>

      <div class="input-patient-part-two">
        <div class="part-two-columns">
          <div class="part-two-col1">
            <div class="patient-subheader" :class="{ 'label-error': showPatientErrors && patientFieldErrors.region }">Район проживания:</div>
            <div class="div-line-small"></div>
            <div class="radio-group-vertical" :class="{ 'group-error-vert': showPatientErrors && patientFieldErrors.region }">
              <label class="radio-label" v-for="reg in REGIONS" :key="reg.valueId">
                <input type="radio" v-model="singlePatient.region" :value="reg.valueId">
                {{ reg.label }}
              </label>
            </div>
          </div>

          <div class="part-two-col2">
            <div class="patient-subheader">Лабораторные параметры:</div>
            <div class="div-line-small"></div>
            <div class="lab-inputs" style="margin-top: 16px;">
              <div class="input-row" v-for="lab in LABS" :key="lab.key">
                <div class="lab-label">{{ lab.label }}:</div>
                <div class="input-group">
                  <input type="number" class="input-field" :placeholder="lab.placeholder" v-model="singlePatient[lab.key]">
                  <span class="unit" style="width: auto; margin-left: 4px;">{{ lab.unit }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="part-two-col3">
            <div class="patient-subheader">Хронические заболевания и диагнозы:</div>
            <div class="div-line-small"></div>

            <div class="disease-list-wrapper">
              <div class="lab-inputs" style="margin-top: 16px;">
                <div v-for="disease in DISEASES" :key="disease.key" class="disease-item">
                  <div class="lab-label disease-label">{{ disease.label }}</div>
                
                  <div class="disease-controls">
                    <div class="radio-group-horizontal">
                      <label class="radio-label">Нет<input type="radio" v-model="singlePatient[disease.key]" :value="0"></label>
                      <label class="radio-label">Да<input type="radio" v-model="singlePatient[disease.key]" :value="1"></label>
                    </div>
                  
                    <Transition name="fade">
                      <div v-if="singlePatient[disease.key] === 1" class="inline-year-input">
                        <span class="lab-label inline-label" :class="{ 'label-error': showPatientErrors && patientFieldErrors[disease.yearKey] }">Год:</span>
                        <div class="input-group">
                          <input type="number" class="input-field year-field" :class="{ 'field-error': showPatientErrors && patientFieldErrors[disease.yearKey] }" placeholder="2018" v-model="singlePatient[disease.yearKey]">
                        </div>
                      </div>
                    </Transition>
                  </div>
                </div>
              </div> 
            </div>
          </div>
        </div>

        <div class="submit-patient-action">
          <p class="patient-form-msg"
             :class="{
               'msg-visible': (showPatientErrors && (patientHasErrors || patientBackendError)) || patientSuccess,
               'msg-error': showPatientErrors && (patientHasErrors || patientBackendError)
             }">
            {{ showPatientErrors && (patientHasErrors || patientBackendError) ? patientCurrentError : 'Пациент успешно добавлен' }}
          </p>
          <button class="upload-btn" @click="submitSinglePatient"
            :disabled="submittingPatient || (showPatientErrors && patientHasErrors)">
            {{ submittingPatient ? 'Сохранение...' : 'Внести пациента' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import { apiFetch } from '@/api/client'

// --- ИНТЕРФЕЙСЫ И КОНСТАНТЫ
interface RowError {
  row: number
  reason: string
}

interface UploadReport {
  fileName: string
  totalRows: number
  savedRows: number
  skippedRows: number
  errors: RowError[]
}

const ALLOWED_EXTENSIONS = ['.xlsx', '.xls', '.csv']
const MAX_SIZE_BYTES = 10 * 1024 * 1024

// --- СОСТОЯНИЕ ФАЙЛОВ
const fileInputRef = ref<HTMLInputElement | null>(null)
const files = ref<File[]>([])
const isDragging = ref(false)
const globalError = ref<string | null>(null)

// --- СОСТОЯНИЕ ЗАГРУЗКИ
const uploading = ref(false)
const uploadProgress = ref(0)
const templateLoading = ref(false)
const reports = ref<UploadReport[]>([])
const submittingPatient = ref(false)
const patientSuccess = ref(false)

// --- СПРАВОЧНИКИ ИЗ survey.config.ts
const PROFESSIONS = [
  { valueId: 14, label: 'Представители законодательных органов власти, высокопоставленные должностные лица, менеджеры и руководители' },
  { valueId: 10, label: 'Квалифицированные специалисты сельского хозяйства и рыболовного' },
  { valueId: 4, label: 'Низкоквалифицированные и неквалифицированные работники, рабочие, ручной труд' },
  { valueId: 1, label: 'Ведение домашнего хозяйства' },
  { valueId: 2, label: 'Вооруженные силы' },
  { valueId: 5, label: 'Операторы и монтажники установок и машинного оборудования' },
  { valueId: 6, label: 'Служащие, сфера обслуживания, работники среднего звена' },
  { valueId: 7, label: 'Никогда не работающие домохозяйки' },
  { valueId: 8, label: 'Дипломированные специалисты, умственный труд' },
  { valueId: 3, label: 'Лица свободных профессий' },
  { valueId: 11, label: 'Пенсионеры' },
  { valueId: 12, label: 'Ремесленники и представители других отраслей промышленности' },
  { valueId: 13, label: 'Техники и младшие специалисты' },
  { valueId: 9, label: 'Другое' },
]

const REGIONS = [
  { valueId: 6, label: 'Сельская местность' },
  { valueId: 2, label: 'Центральный' },
  { valueId: 3, label: 'Заводской' },
  { valueId: 1, label: 'Рудничный' },
  { valueId: 4, label: 'Кировский' },
  { valueId: 5, label: 'Ленинский' },
]

type PatientKeys = keyof typeof singlePatient;

const LABS: { key: PatientKeys, label: string, placeholder: string, unit: string }[] = [
  { key: 'glucose', label: 'Глюкоза', placeholder: '4,5', unit: 'ммоль/л' },
  { key: 'cholesterol', label: 'Холестерин', placeholder: '5,3', unit: 'ммоль/л' },
  { key: 'nonHdlCholesterol', label: 'Холестерин не-ЛПВП', placeholder: '3,2', unit: 'ммоль/л' },
  { key: 'vldlCholesterol', label: 'Холестерин ЛПОНП', placeholder: '0,8', unit: 'ммоль/л' },
  { key: 'hdlCholesterol', label: 'Холестерин ЛПВП', placeholder: '1,2', unit: 'ммоль/л' },
  { key: 'ldlCholesterol', label: 'Холестерин ЛПНП', placeholder: '3,0', unit: 'ммоль/л' },
  { key: 'apolipoproteinA', label: 'Апо А', placeholder: '1,5', unit: 'г/л' },
  { key: 'apolipoproteinB', label: 'Апо B', placeholder: '0,9', unit: 'г/л' },
  { key: 'triglycerides', label: 'Триглицериды', placeholder: '1,7', unit: 'ммоль/л' },
]

const DISEASES: { key: PatientKeys, yearKey: PatientKeys, label: string }[] = [
  { key: 'stroke', yearKey: 'strokeYear', label: 'Диагностировался ли инсульт?' },
  { key: 'heartFailure', yearKey: 'heartFailureYear', label: 'Диагностировалась ли сердечная недостаточность (СН)?' },
  { key: 'cad', yearKey: 'cadYear', label: 'Диагностировалось ли нарушение ритма или другие ИБС?' },
  { key: 'angina', yearKey: 'anginaYear', label: 'Диагностировалась ли стенокардия?' },
  { key: 'myocardialInfarction', yearKey: 'myocardialInfarctionYear', label: 'Диагностировался ли инфаркт миокарда?' },
  { key: 'arterialHypertension', yearKey: 'arterialHypertensionYear', label: 'Диагностировалась ли артериальная гипертензия (АГ)?' },
]

// --- СОСТОЯНИЕ ФОРМЫ ПАЦИЕНТА (СООТВЕТСТВИЕ survey.config.ts)
const singlePatient = reactive({
  gender: null as number | null,
  age: null as number | null,
  height: null as number | null,
  weight: null as number | null,
  hipMeasurement: null as number | null,
  
  alcohol: null as number | null,
  profession: null as number | null,
  region: null as number | null,

  glucose: null as number | null,
  cholesterol: null as number | null,
  nonHdlCholesterol: null as number | null,
  vldlCholesterol: null as number | null,
  hdlCholesterol: null as number | null,
  ldlCholesterol: null as number | null,
  apolipoproteinA: null as number | null,
  apolipoproteinB: null as number | null,
  triglycerides: null as number | null,

  stroke: null as number | null, 
  strokeYear: null as number | null,
  heartFailure: null as number | null, 
  heartFailureYear: null as number | null,
  cad: null as number | null, 
  cadYear: null as number | null,
  angina: null as number | null, 
  anginaYear: null as number | null,
  myocardialInfarction: null as number | null, 
  myocardialInfarctionYear: null as number | null,
  arterialHypertension: null as number | null, 
  arterialHypertensionYear: null as number | null,
})

// --- ВАЛИДАЦИЯ ФОРМЫ ПАЦИЕНТА
const showPatientErrors = ref(false)
const patientBackendError = ref<string | null>(null)

const patientFieldErrors = computed<Record<string, string>>(() => {
  const errors: Record<string, string> = {}
  const p = singlePatient
  const currentYear = new Date().getFullYear()

  if (p.gender === null) errors.gender = 'Укажите пол'

  const ageVal = p.age !== null && (p.age as any) !== '' ? Number(p.age) : null
  if (ageVal === null) errors.age = 'Укажите возраст'
  else if (isNaN(ageVal) || ageVal < 18 || ageVal > 120) errors.age = 'Возраст: 18–120 лет'

  if (p.alcohol === null) errors.alcohol = 'Укажите употребление алкоголя'
  if (p.profession === null) errors.profession = 'Укажите род деятельности'
  if (p.region === null) errors.region = 'Укажите район проживания'

  const rangeChecks: { key: keyof typeof p; required: boolean; min: number; max: number; empty: string; range: string }[] = [
    { key: 'height',         required: true,  min: 50,  max: 250, empty: 'Укажите рост',        range: 'Рост: 50–250 см' },
    { key: 'weight',         required: true,  min: 20,  max: 300, empty: 'Укажите вес',         range: 'Вес: 20–300 кг' },
    { key: 'hipMeasurement', required: false, min: 40,  max: 200, empty: '',                    range: 'Объём бёдер: 40–200 см' },
  ]
  for (const { key, required, min, max, empty, range } of rangeChecks) {
    const v = p[key]
    const isEmpty = v === null || (v as any) === ''
    if (isEmpty) {
      if (required) errors[key] = empty
    } else {
      const n = Number(v)
      if (isNaN(n) || n < min || n > max) errors[key] = range
    }
  }

  for (const { key, yearKey } of DISEASES) {
    const yearVal = p[yearKey]
    if (p[key] === 1 && yearVal !== null && (yearVal as any) !== '') {
      const y = Number(yearVal)
      if (isNaN(y) || y < 1900 || y > currentYear) errors[yearKey] = `Год: 1900–${currentYear}`
    }
  }

  return errors
})

const patientHasErrors = computed(() => Object.keys(patientFieldErrors.value).length > 0)

const patientCurrentError = computed(() =>
  patientBackendError.value ?? Object.values(patientFieldErrors.value)[0] ?? ''
)

watch(singlePatient, () => {
  if (!patientHasErrors.value) showPatientErrors.value = false
}, { deep: true })

async function submitSinglePatient() {
  patientBackendError.value = null

  if (patientHasErrors.value) {
    showPatientErrors.value = true
    return
  }

  submittingPatient.value = true
  showPatientErrors.value = false

  try {
    const payload = Object.fromEntries(
      Object.entries(singlePatient).map(([k, v]) => [k, (v as unknown) === '' ? null : v])
    )

    const res = await apiFetch('/api/admin/patients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const body = await res.json().catch(() => null)
      patientBackendError.value = (res.status === 401 || res.status === 403)
        ? 'Сессия истекла — войдите заново'
        : body?.error ?? `Ошибка при сохранении пациента (${res.status})`
      showPatientErrors.value = true
      return
    }

    Object.assign(singlePatient, {
      gender: null, age: null, height: null, weight: null,
      hipMeasurement: null, alcohol: null, profession: null, region: null,
      glucose: null, cholesterol: null, nonHdlCholesterol: null,
      vldlCholesterol: null, hdlCholesterol: null, ldlCholesterol: null,
      apolipoproteinA: null, apolipoproteinB: null, triglycerides: null,
      stroke: null, strokeYear: null, heartFailure: null, heartFailureYear: null,
      cad: null, cadYear: null, angina: null, anginaYear: null,
      myocardialInfarction: null, myocardialInfarctionYear: null,
      arterialHypertension: null, arterialHypertensionYear: null,
    })
    patientBackendError.value = null
    patientSuccess.value = true
    setTimeout(() => { patientSuccess.value = false }, 3000)
  } catch {
    patientBackendError.value = 'Ошибка соединения с сервером'
    showPatientErrors.value = true
  } finally {
    submittingPatient.value = false
  }
}

// --- ЛОГИКА БАННЕРА ОШИБОК
const showErrors = ref(false)

watch(globalError, (newVal) => {
  if (newVal) {
    showErrors.value = true
  }
})

const fieldErrors = computed<Record<string, string>>(() => {
  const errors: Record<string, string> = {}
  
  if (globalError.value) {
    errors['file'] = globalError.value
  }

  return errors
})

const hasErrors = computed(() => Object.keys(fieldErrors.value).length > 0)

const currentErrorMessage = computed(() => {
  const errorsList = Object.values(fieldErrors.value)
  return errorsList.length > 0 ? errorsList[0] : 'Пожалуйста, проверьте введённые данные'
})

// Сбрасываем показ баннера, если ошибок больше нет
watch([() => singlePatient, globalError], () => {
  if (!hasErrors.value) {
    showErrors.value = false
  }
}, { deep: true })


// --- ЛОГИКА ВЫБОРА И DRAG & DROP
function triggerFileInput() {
  if (!uploading.value) fileInputRef.value?.click()
}

function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.length) {
    addFiles(Array.from(input.files))
  }
  input.value = ''
}

function handleDrop(e: DragEvent) {
  isDragging.value = false
  if (e.dataTransfer?.files?.length && !uploading.value) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

function addFiles(newFiles: File[]) {
  globalError.value = null

  const validFiles = newFiles.filter(file => {
    const ext = '.' + file.name.split('.').pop()?.toLowerCase()

    if (!ALLOWED_EXTENSIONS.includes(ext)) {
      globalError.value = `Недопустимый формат. Разрешены: ${ALLOWED_EXTENSIONS.join(', ')}`
      return false
    }
    if (file.size > MAX_SIZE_BYTES) {
      globalError.value = 'Один или несколько файлов превышают лимит в 10 МБ'
      return false
    }
    return true
  })

  // Добавляем только те файлы, которых еще нет в списке (проверка по имени и размеру)
  validFiles.forEach(validFile => {
    const exists = files.value.some(f => f.name === validFile.name && f.size === validFile.size)
    if (!exists) {
      files.value.push(validFile)
    }
  })

  // Сбрасываем прогресс, если добавляем новые
  if (!hasErrors) uploadProgress.value = 0
}

function removeFile(index: number) {
  files.value.splice(index, 1)
  if (files.value.length === 0) uploadProgress.value = 0
  globalError.value = null // Очищаем ошибку, если она была, при удалении файла
}

function cancelAll() {
  files.value = []
  uploadProgress.value = 0
  globalError.value = null
  reports.value = []
}

// --- УТИЛИТЫ
function getFileExtension(filename: string): string {
  return filename.split('.').pop()?.toLowerCase() || ''
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} Б`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} КБ`
  return `${(bytes / 1024 / 1024).toFixed(1)} МБ`
}

// --- ЛОГИКА ЗАПРОСОВ
async function uploadFiles() {
  if (!files.value.length) return
  
  uploading.value = true
  globalError.value = null
  reports.value = []
  uploadProgress.value = 0

  const totalFiles = files.value.length

  try {
    for (let i = 0; i < totalFiles; i++) {
      const file = files.value[i]
      const formData = new FormData()
      formData.append('file', file)

      const res = await apiFetch('/api/admin/upload', { method: 'POST', body: formData })

      if (!res.ok) {
        const body = await res.json().catch(() => null)
        globalError.value = (res.status === 401 || res.status === 403)
          ? 'Сессия истекла — войдите заново'
          : body?.error ?? `Ошибка при загрузке ${file.name}: статус ${res.status}`
        throw new Error('Upload Interrupted')
      }

      const report = await res.json()
      reports.value.push({ fileName: file.name, ...report })
      
      // Обновляем прогресс
      uploadProgress.value = Math.round(((i + 1) / totalFiles) * 100)
    }

    // Успешная загрузка всех файлов
    setTimeout(() => {
      files.value = []
      uploadProgress.value = 0
    }, 1500)

  } catch (err: any) {
    if (err.message !== 'Upload Interrupted') {
      globalError.value = 'Ошибка соединения с сервером'
    }
  } finally {
    uploading.value = false
  }
}

async function downloadTemplate() {
  templateLoading.value = true
  globalError.value = null
  
  try {
    const res = await apiFetch('/api/admin/upload/template')
    if (!res.ok) {
      globalError.value = res.status === 401 || res.status === 403
        ? 'Сессия истекла — войдите заново'
        : `Не удалось скачать шаблон (${res.status})`
      return
    }
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'patients_template.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    globalError.value = 'Ошибка загрузки шаблона'
  } finally {
    templateLoading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// --------- УТИЛИТИ

.div-line {
  width: 100%;
  border-top: 2px solid $color-accent;
  border-radius: 1000px;
  height: 0;
  margin-block: 24px;
}

.div-line2 {
  width: 100%;
  border-top: 1px solid $color-accent-lighter2;
  border-radius: 1000px;
  height: 0;
  margin-block: 32px;
}

.div-line-small {
  width: 100%;
  border-top: 2px solid $color-accent;
  border-radius: 1000px;
  height: 0;
  margin-block: 8px;
}

.wrapper_column {
  position: absolute;
  inset: 0;
  padding: 32px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.wrapper_row {
  display: flex;
  flex-direction: row;
  width: 100%;
  flex: 1;
  min-height: 0;
}

// ---- ПОДЧЁРКИВАНИЯ: 
.underlined {
  text-decoration: underline;
  text-decoration-color: $color-blue-dark;
  text-decoration-thickness: 1px;
  text-underline-offset: 2px;
}

.underlined-hover {
  &:hover {
    text-decoration: underline;
    text-decoration-color: $color-blue-dark;
    text-decoration-thickness: 2px;
    text-underline-offset: 2px;
    cursor: pointer;
  }
}

// -------------------------- КОНТЕЙНЕР СТРАНИЦЫ
.upload-page {
  margin-left: 240px;
  padding-inline: 200px;
  box-sizing: border-box;

  display: flex;
  flex-direction: column;
  gap: 32px;
  margin-block: 40px;

  @media (max-width: 1200px) {
    padding-inline: 40px;
  }
  @media (max-width: 768px) {
    margin-left: 0;
    padding-inline: 16px;
  }
}

.upload-duo-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: stretch;
  gap: 32px;
  width: 100%;

  > .upload-card,
  > .files-list-card {
    min-width: 0;
    min-height: 0;
    width: 100%;
  }
}

// КАРТОЧКА СПИСКА ФАЙЛОВ (ПРАВО ВЕРХ)

.section-header {
  font-size: 1.25rem;
  font-weight: 600;
  white-space: nowrap;
  color: $color-text;
  margin-bottom: 24px;
}

.files-list-card {
  position: relative;
  display: flex;
  flex-direction: column;
  

  background-color: rgba($color-glass-white, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.4);
  border-radius: $radius-glass-card;

  .files-progress-bar {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    color: $color-accent;
    background: rgba($color-secondary, 0.3);
    border-radius: 1000px;
    margin-right: 16px;
    width: 10px;
    height: 100%;
    overflow: hidden;
    flex-shrink: 0;

    .progress-fill {
      width: 100%;
      background: $color-accent;
      transition: height 0.3s ease;
    }
  }

  .main-content-files-list {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    min-height: 0;
    min-width: 0;

    .file-list {
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex-grow: 1;
      margin-bottom: 4px;

      overflow-y: auto;
      padding-right: 16px;

      &::-webkit-scrollbar { width: 10px; }
      &::-webkit-scrollbar-track { background: rgba($color-secondary, 0.3); border-radius: 10px; margin-bottom: 24px; }
      &::-webkit-scrollbar-thumb {
        background: rgba($color-secondary, 0.8);
        border-radius: 10px;
        &:hover { background: rgba($color-secondary, 1); }
      }
    
      // СТИЛЬ ОДНОГО ФАЙЛА
      .file-item {
        display: flex;
        flex-direction: row;
        background: none;
        border-radius: 10px;
        margin: 0;
        width: 100%;

        &:hover {
          border: 2px solid rgba($color-secondary, 0.3); 
          cursor: pointer
        }

        .file-item-main {
          display: flex;
          flex-grow: 1;
          align-items: center;
          padding: 8px;
          gap: 16px;
          width: 100%;

          .file-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 48px;
            height: 27px;
            background: rgba($color-secondary, 0.3);
            border-radius: 5px;

            .file-extension {
              color: $color-secondary-text;
              font-weight: 600;
            }
          }
        
          .file-right-side {
            align-items: center;
            display: flex;
            flex-direction: row;
            gap: 16px;
            flex-shrink: 0;

            .file-size {
              font-weight: 400;
              color: $color-secondary-text;
              font-size: 1rem;
              margin: 0;
            }
          }

          .file-details {
            flex-grow: 1;
            display: flex;
            flex-direction: row;
            gap: 4px;
            min-width: 0px;

            .file-name {
              font-weight: 600;
              color: $color-blue-dark;
              font-size: 1rem;
              margin: 0;

              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }

          .delete-file-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            background: transparent;
            border: none;
            color: $color-secondary-text;
            padding: 4px;
            margin-bottom: -3px; // выравнивание по центру контента
            cursor: pointer;
            transition: color 0.2s ease;
            &:hover {
              color: $color-red;
            }
          }
        }
      }
    }
  }
  .files-list-actions {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
  width: 100%;
  gap: 24px;
  flex-shrink: 0;

  .cancel-all-btn {
    justify-content: center;
    align-items: center;
    color: $color-accent;
    width: 100%;
    display: flex;
    padding: 13px;
    background: transparent;
    border: 2px solid $color-accent;
    border-radius: $radius-rect-buttons;
    white-space: nowrap;
    font-weight: 600;
    font-size: 1rem;
    &:hover {
      background: $color-accent;
      color: $color-white;
      cursor: pointer;
    }
  }

  .upload-btn {
    justify-content: center;
    display: flex;
    padding: 13px;
    width: 100%;
    background: $color-accent;
    white-space: nowrap;
    border: none;
    border-radius: $radius-rect-buttons;
    color: $color-white;
    font-weight: 600;
    font-size: 1rem;
    &:hover {
      background: $color-accent-lighter;
      cursor: pointer;
    }
  }
  
}

}

// КАРТОЧКА ЗАГРУЗКИ (ЛЕВО ВЕРХ)
.upload-card {
  display: flex;
  flex-direction: column;
  align-items: center;

  // ВЕРХ
  .upload-card-upper-part {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    max-width: 100%;
    padding: 32px;

    background-color: rgba($color-glass-white, 0.8);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
    border-radius: $radius-glass-card;

    position: relative;
    z-index: 2;

    .upload-border {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px 48px;

      border: 3px dashed rgba($color-secondary, 0.3);
      border-radius: 16px;

      .upload-icon {
        margin-bottom: 12px;
      }
      .upload-main-p {
        margin: 0;
        color: $color-text;
        font-weight: 600;
        font-size: 1rem;
        margin-bottom: 8px;
      }
      .upload-formats-p {
        margin: 0;
        font-size: 0.875rem;
        color: $color-secondary;
      }
    }

    &:hover, &.drag-over {
      border-color: $color-white;
      background: rgba($color-glass-white, 1);
      cursor: pointer;
    }
  }

  // НИЗ
  .upload-card-bottom-part {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    max-width: 100%;
    margin-top: -30px;
    z-index: 1;

    padding: 32px;
    border-radius: 0 0 $radius-glass-card $radius-glass-card;

    background: rgba($color-glass-white, 0.5);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
  
    gap: 20px;
  }

  .bottom-card-text-block {
    flex-grow: 1;
    
    .alert-p {
      margin-top: 12px;
      font-size: 1rem;
      color: $color-text;
    }

    .attention-p {
      margin-top: 16px;
      font-size: 0.85rem;
      color: $color-text;
    }
  }
  .download-template-btn {
    margin-top: 16px;
    padding-block: 13px;
    font-size: 1rem;
    font-weight: 600;
    padding-inline: 22px;
    border: none;
    border-radius: $radius-rect-buttons;
    background: $color-accent;
    color: $color-white;
    font-family: inherit;
    cursor: pointer;
    transition: background 0.2s ease;

    white-space: nowrap;
    width: max-content;
    flex-shrink: 0;

    &:hover {
      background: $color-accent-lighter;
    }
  }
}

// -------------- КАРТОЧКА ОДНОГО ПАЦИЕНТА

.radio-group-vertical {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;

  .radio-label {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    gap: 12px;
    cursor: pointer;
    font-size: 1rem;
    color: $color-secondary-text;
    line-height: 1.4;
  }
}


.one-patient-input-card {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    padding: 32px;

    background-color: rgba($color-glass-white, 0.8);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
    border-radius: $radius-glass-card;

  .patient-subheader {
    font-weight: 600;
    font-size: 1rem;
    color: $color-text;
    white-space: nowrap;
  }

  .input-patient-part-one {
    display: flex;
    flex-direction: row;
    align-items: stretch;
    gap: 64px;

    .input-patient-one-col1 {
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      flex: 0 0 360px;

        .unit {
        color: $color-secondary-text;
        align-content: flex-end;
        font-size: 1rem;
        min-width: 24px;
        text-align: right;
      }

      .section-info {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .input-row {
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
          width: 100%;
        }
      }
    }

    .input-patient-one-col2 {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
    }

    .patient-subheader {
      flex-shrink: 0;
      margin-bottom: 0px;
    }

    .list-wrapper {
      margin-top: 16px;
      flex-grow: 1;
      overflow-y: auto;
      padding-right: 16px;
      
      max-height: 360px;

      &::-webkit-scrollbar { width: 10px; }
      &::-webkit-scrollbar-track { background: rgba($color-secondary, 0.3); border-radius: 10px; margin-bottom: 24px; }
      &::-webkit-scrollbar-thumb {
        background: rgba($color-secondary, 0.8);
        border-radius: 10px;
        &:hover { background: rgba($color-secondary, 1); }
      }
    }
  }
}


// --- кнопочки 
.radio-group-horizontal {
  display: flex;
  gap: 24px;
  align-items: center;

  .radio-label {
    display: flex;
    flex-direction: row-reverse;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    color: $color-secondary-text;
  }
}

// Текстовые инпуты
.input-group {
  display: flex;
  align-items: center;
  gap: 10px;

  .input-field {
    width: 60px;
    height: 24px;
    padding: 8px 12px;
    background: rgba($color-white, 0.5);
    border: 1.5px solid rgba($color-secondary, 0.5);
    border-radius: 100px;
    font-size: 1rem;
    font-family: inherit;
    text-align: center;
    outline: none;
    transition: border-color 0.2s;
    color: $color-text;

    &::placeholder {
      color: rgba($color-secondary, 0.75);
      font-weight: 400;
    }

    &:focus {
      border-color: $color-accent;
    }

    &::-webkit-outer-spin-button,
    &::-webkit-inner-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }
  }

  .unit {
    color: $color-secondary-text;
    align-content: flex-end;
    font-size: 1rem;
    min-width: 70px;
    text-align: right;
  }
}

// ГЛОБАЛЬНЫЙ СТИЛЬ РАДИО-КНОПОК
input[type="radio"] {
  appearance: none; 
  -webkit-appearance: none; 
  background-color: rgba($color-white, 0.5);
  
  margin: 0;
  flex-shrink: 0;
  width: 18px; 
  height: 18px;
  
  border: 2px solid rgba($color-secondary, 0.5); 
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;

  &:checked {
    border-color: $color-accent;
    
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 10px;
      height: 10px;
      background-color: $color-accent; 
      border-radius: 50%;
    }
  }
}

.error-banner {
  margin-left: 240px;
}

// --- СТИЛИ ДЛЯ ПАЦИЕНТА, ДОПОЛНИТЕЛЬНЫЕ
.input-patient-part-two {
  display: flex;
  flex-direction: column;
  gap: 32px;
  width: 100%;
}

.part-two-columns {
  display: grid;
  grid-template-columns: 0.85fr 1.5fr 2fr;
  gap: 32px;
  align-items: start;
}

.disease-list-wrapper {
  overflow-y: auto;
  padding-right: 16px;
  max-height: 360px;

  &::-webkit-scrollbar { width: 10px; }
  &::-webkit-scrollbar-track { background: rgba($color-secondary, 0.3); border-radius: 10px; margin-bottom: 24px; }
  &::-webkit-scrollbar-thumb {
    background: rgba($color-secondary, 0.8);
    border-radius: 10px;
    &:hover { background: rgba($color-secondary, 1); }
  }
}

.lab-inputs {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .input-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.lab-label {
  font-size: 1rem;
  color: $color-text;
  font-weight: 500;
}


// --- Стили для заболеваний с инлайн-инпутом
.disease-item {
  border-bottom: 1px solid rgba($color-secondary, 0.1);
  padding-bottom: 16px;
}

.disease-label {
  margin-bottom: 12px;
  line-height: 1.4;
}

.disease-controls {
  display: flex;
  align-items: center;
  min-height: 34px;
}

.inline-year-input {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 32px;
  
  .inline-label {
    margin: 0;
    color: $color-secondary-text;
    font-size: 1rem;
  }

  .year-field {
    width: 70px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}


.patient-form-msg {
  font-size: 1rem;
  font-weight: 500;
  margin: 0 0 12px;
  text-align: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: $color-accent;

  &.msg-visible { opacity: 1; }
  &.msg-error { color: $color-red; }
}

.input-field.field-error {
  border-color: $color-red;
}

.radio-group-horizontal.group-error {
  box-shadow: 0 0 0 1.5px $color-red;
  border-radius: 8px;
}

.radio-group-vertical.group-error-vert,
.list-wrapper.group-error-vert {
  box-shadow: 0 0 0 1.5px $color-red;
  border-radius: 8px;
}

.label-error {
  color: $color-red;
}

.submit-patient-action {
  display: flex;
  flex-direction: column;
  align-items: center;

  .upload-btn {
    justify-content: center;
    display: flex;
    padding: 13px;
    width: 100%;
    background: $color-accent;
    white-space: nowrap;
    border: none;
    border-radius: $radius-rect-buttons;
    color: $color-white;
    font-weight: 600;
    font-size: 1rem;
    &:hover {
      background: $color-accent-lighter;
      cursor: pointer;
    }
  }
}

</style>