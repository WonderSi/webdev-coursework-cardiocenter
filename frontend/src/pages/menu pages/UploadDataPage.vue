<template>
  <Sidebar />

  <main class="upload-page">
    <div class="page-header">
      <h1 class="page-title">Загрузка данных</h1>
      <button class="template-btn" @click="downloadTemplate" :disabled="templateLoading">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/>
          <line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        {{ templateLoading ? 'Загрузка...' : 'Скачать шаблон' }}
      </button>
    </div>

    <div class="upload-card">
      <div
        class="drop-zone"
        :class="{ 'drag-over': isDragging, 'has-file': selectedFile }"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleDrop"
        @click="triggerFileInput"
      >
        <input
          ref="fileInputRef"
          type="file"
          accept=".xlsx,.xls,.csv"
          class="file-input-hidden"
          @change="handleFileChange"
        />

        <template v-if="!selectedFile">
          <svg class="drop-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          <p class="drop-text">Перетащите файл или нажмите для выбора</p>
          <span class="drop-hint">.xlsx, .xls, .csv — до 10 МБ</span>
        </template>

        <template v-else>
          <svg class="file-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <div class="file-info">
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">{{ formatSize(selectedFile.size) }}</span>
          </div>
          <button class="clear-btn" @click.stop="clearFile" title="Удалить файл">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </template>
      </div>

      <div v-if="fileError" class="inline-error">{{ fileError }}</div>

      <button
        class="upload-btn"
        :disabled="!selectedFile || uploading"
        @click="uploadFile"
      >
        <svg v-if="!uploading" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="17 8 12 3 7 8"/>
          <line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
        <span class="spinner" v-else></span>
        {{ uploading ? 'Обработка...' : 'Загрузить' }}
      </button>

      <div v-if="uploadError" class="error-banner">{{ uploadError }}</div>
    </div>

    <div v-if="report" class="report-card">
      <div class="report-stats">
        <div class="stat stat--saved">
          <span class="stat-value">{{ report.savedRows }}</span>
          <span class="stat-label">сохранено</span>
        </div>
        <div class="stat stat--skipped">
          <span class="stat-value">{{ report.skippedRows }}</span>
          <span class="stat-label">пропущено</span>
        </div>
        <div class="stat stat--total">
          <span class="stat-value">{{ report.totalRows }}</span>
          <span class="stat-label">всего строк</span>
        </div>
      </div>

      <div v-if="report.errors.length > 0" class="errors-section">
        <h3 class="errors-title">Ошибки валидации</h3>
        <div class="errors-table-wrap">
          <table class="errors-table">
            <thead>
              <tr>
                <th>Строка</th>
                <th>Причина</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="err in report.errors" :key="err.row">
                <td class="row-num">{{ err.row }}</td>
                <td>{{ err.reason }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-else class="success-notice">
        Все строки успешно сохранены
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

interface RowError {
  row: number
  reason: string
}

interface UploadReport {
  totalRows: number
  savedRows: number
  skippedRows: number
  errors: RowError[]
}

const ALLOWED_EXTENSIONS = ['.xlsx', '.xls', '.csv']
const MAX_SIZE_BYTES = 10 * 1024 * 1024

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile  = ref<File | null>(null)
const isDragging    = ref(false)
const fileError     = ref<string | null>(null)
const uploading     = ref(false)
const uploadError   = ref<string | null>(null)
const templateLoading = ref(false)
const report        = ref<UploadReport | null>(null)

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0]) setFile(input.files[0])
}

function handleDrop(e: DragEvent) {
  isDragging.value = false
  const file = e.dataTransfer?.files[0]
  if (file) setFile(file)
}

function setFile(file: File) {
  fileError.value = null
  report.value = null
  uploadError.value = null

  const ext = '.' + file.name.split('.').pop()?.toLowerCase()
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    fileError.value = `Недопустимый формат. Разрешены: ${ALLOWED_EXTENSIONS.join(', ')}`
    return
  }
  if (file.size > MAX_SIZE_BYTES) {
    fileError.value = 'Файл превышает 10 МБ'
    return
  }
  selectedFile.value = file
}

function clearFile() {
  selectedFile.value = null
  fileError.value = null
  report.value = null
  uploadError.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

async function uploadFile() {
  if (!selectedFile.value) return
  uploading.value = true
  uploadError.value = null
  report.value = null

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const res = await fetch('/api/admin/upload', { method: 'POST', body: formData, credentials: 'include' })
    if (!res.ok) {
      const body = await res.json().catch(() => null)
      uploadError.value = (res.status === 401 || res.status === 403)
        ? 'Сессия истекла — войдите заново'
        : body?.error ?? `Ошибка сервера: ${res.status}`
      return
    }
    report.value = await res.json()
    clearFile()
  } catch {
    uploadError.value = 'Ошибка соединения с сервером'
  } finally {
    uploading.value = false
  }
}

async function downloadTemplate() {
  templateLoading.value = true
  try {
    const res = await fetch('/api/admin/upload/template', { credentials: 'include' })
    if (!res.ok) {
      uploadError.value = res.status === 401 || res.status === 403
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
    uploadError.value = 'Ошибка загрузки шаблона'
  } finally {
    templateLoading.value = false
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} Б`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} КБ`
  return `${(bytes / 1024 / 1024).toFixed(1)} МБ`
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.upload-page {
  margin-left: 240px;
  padding: 40px 40px 40px 48px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: $color-white;
}

.template-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid rgba($color-glass-white, 0.6);
  border-radius: $radius-rect-buttons;
  background: rgba($color-glass-white, 0.15);
  color: $color-white;
  font-size: 0.9rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s;
  backdrop-filter: blur(8px);

  &:hover:not(:disabled) { background: rgba($color-glass-white, 0.28); }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.upload-card {
  background: rgba($color-glass-white, 0.35);
  backdrop-filter: blur(12px);
  border: 1px solid rgba($color-glass-white, 0.5);
  border-radius: $radius-glass-card;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 640px;
}

.drop-zone {
  border: 2px dashed rgba($color-glass-white, 0.5);
  border-radius: 16px;
  padding: 48px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  position: relative;
  min-height: 180px;
  color: $color-white;

  &:hover, &.drag-over {
    border-color: $color-white;
    background: rgba($color-glass-white, 0.12);
  }

  &.has-file {
    border-style: solid;
    border-color: rgba($color-glass-white, 0.7);
    flex-direction: row;
    padding: 24px 28px;
    min-height: auto;
    gap: 16px;
  }
}

.file-input-hidden {
  display: none;
}

.drop-icon {
  opacity: 0.8;
}

.drop-text {
  font-size: 1rem;
  font-weight: 500;
  margin: 0;
}

.drop-hint {
  font-size: 0.8rem;
  opacity: 0.65;
}

.file-icon {
  flex-shrink: 0;
  opacity: 0.9;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.file-name {
  font-weight: 600;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 0.8rem;
  opacity: 0.65;
}

.clear-btn {
  background: none;
  border: none;
  color: rgba($color-glass-white, 0.7);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  transition: color 0.2s;
  &:hover { color: $color-cold-red; }
}

.inline-error {
  color: $color-cold-red;
  font-size: 0.85rem;
  padding: 8px 12px;
  background: rgba($color-cold-red, 0.1);
  border-radius: 8px;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border: none;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: $color-white;
  font-size: 1rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;

  &:hover:not(:disabled) { background: $color-blue-dark; }
  &:disabled { opacity: 0.45; cursor: not-allowed; }
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba($color-glass-white, 0.35);
  border-top-color: $color-white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-banner {
  padding: 12px 16px;
  background: rgba($color-cold-red, 0.12);
  border: 1px solid rgba($color-cold-red, 0.4);
  border-radius: 10px;
  color: $color-cold-red;
  font-size: 0.9rem;
}

.report-card {
  background: rgba($color-glass-white, 0.35);
  backdrop-filter: blur(12px);
  border: 1px solid rgba($color-glass-white, 0.5);
  border-radius: $radius-glass-card;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 640px;
}

.report-stats {
  display: flex;
  gap: 16px;
}

.stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border-radius: 14px;
  background: rgba($color-glass-white, 0.2);

  &--saved   { border-top: 3px solid $color-green; }
  &--skipped { border-top: 3px solid $color-cold-red; }
  &--total   { border-top: 3px solid $color-accent-lighter; }
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: $color-white;
}

.stat-label {
  font-size: 0.8rem;
  color: rgba($color-glass-white, 0.75);
  margin-top: 4px;
}

.errors-title {
  font-size: 1rem;
  font-weight: 600;
  color: $color-white;
  margin: 0 0 12px;
}

.errors-table-wrap {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid rgba($color-glass-white, 0.3);
}

.errors-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;

  th, td {
    padding: 10px 14px;
    text-align: left;
    color: $color-white;
    border-bottom: 1px solid rgba($color-glass-white, 0.15);
  }

  th {
    background: rgba($color-glass-white, 0.15);
    font-weight: 600;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }

  tr:last-child td { border-bottom: none; }

  .row-num {
    font-weight: 600;
    color: $color-cold-red;
    white-space: nowrap;
  }
}

.success-notice {
  text-align: center;
  color: $color-green-light;
  font-weight: 500;
  font-size: 0.95rem;
  padding: 12px;
  background: rgba($color-green, 0.1);
  border-radius: 10px;
}
</style>
