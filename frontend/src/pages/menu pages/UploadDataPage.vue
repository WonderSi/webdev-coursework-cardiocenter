<template>
  <Sidebar />

  <div class=upload-page>
    <div class="upload-duo-cards">
      <div class=upload-card>
        <div class="upload-card-upper-part">
          <div class="upload-border">
            <div class="upload-icon">
              <svg width="77" height="76" viewBox="0 0 78 76" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="38.5" cy="37.8032" rx="38.5" ry="37.8032" fill="#0f356e"/>
                <path fill-rule="evenodd" clip-rule="evenodd" d="M28.6863 55.0743C26.0839 55.0743 23.5881 54.0405 21.748 52.2003C19.9078 50.3602 18.874 47.8644 18.874 45.262V39.3747C18.874 38.8542 19.0808 38.355 19.4488 37.987C19.8168 37.619 20.316 37.4122 20.8365 37.4122C21.3569 37.4122 21.8561 37.619 22.2241 37.987C22.5922 38.355 22.7989 38.8542 22.7989 39.3747V45.262C22.7989 46.8234 23.4192 48.3209 24.5233 49.425C25.6274 50.5291 27.1248 51.1494 28.6863 51.1494H48.3107C49.8722 51.1494 51.3696 50.5291 52.4737 49.425C53.5778 48.3209 54.1981 46.8234 54.1981 45.262V39.3747C54.1981 38.8542 54.4048 38.355 54.7729 37.987C55.1409 37.619 55.6401 37.4122 56.1605 37.4122C56.681 37.4122 57.1802 37.619 57.5482 37.987C57.9162 38.355 58.123 38.8542 58.123 39.3747V45.262C58.123 47.8644 57.0892 50.3602 55.249 52.2003C53.4089 54.0405 50.9131 55.0743 48.3107 55.0743H28.6863Z" fill="white"/>
                <path fill-rule="evenodd" clip-rule="evenodd" d="M28.2661 31.7203C28.0857 31.536 27.9434 31.3179 27.8473 31.0786C27.7512 30.8392 27.7033 30.5833 27.7062 30.3254C27.7091 30.0675 27.7628 29.8127 27.8643 29.5756C27.9657 29.3384 28.113 29.1237 28.2975 28.9435L37.1285 20.3087C37.4952 19.9503 37.9875 19.7496 38.5003 19.7496C39.013 19.7496 39.5054 19.9503 39.872 20.3087L48.703 28.9435C48.8912 29.1227 49.042 29.3375 49.1465 29.5754C49.251 29.8134 49.3073 30.0697 49.3119 30.3296C49.3166 30.5894 49.2696 30.8476 49.1736 31.0891C49.0777 31.3307 48.9347 31.5507 48.7531 31.7366C48.5714 31.9224 48.3546 32.0703 48.1153 32.1717C47.876 32.2731 47.6189 32.3259 47.3591 32.3271C47.0992 32.3284 46.8416 32.278 46.6014 32.1788C46.3611 32.0797 46.143 31.9339 45.9595 31.7498L40.4627 26.3746V43.2988C40.4627 43.8192 40.256 44.3184 39.8879 44.6864C39.5199 45.0545 39.0207 45.2612 38.5003 45.2612C37.9798 45.2612 37.4806 45.0545 37.1126 44.6864C36.7446 44.3184 36.5378 43.8192 36.5378 43.2988V26.3746L31.043 31.7498C30.8586 31.9302 30.6406 32.0725 30.4012 32.1686C30.1619 32.2646 29.9059 32.3126 29.648 32.3097C29.3901 32.3068 29.1353 32.253 28.8982 32.1516C28.6611 32.0501 28.4463 31.9029 28.2661 31.7184V31.7203Z" fill="white"/>
              </svg>
            </div>
            <p class="upload-main-p"><span class=underlined-hover>Выберите</span> или перетащите файлы</p>
            <p class="upload-formats-p">.xlsx, .xls, .csv — до 10 МБ</p>
          </div>
        </div>

        <div class="upload-card-bottom-part">
          <div class="bottom-card-text-block">
            <p class="alert-p">Пожалуйста, перед загрузкой убедитесь,<br>что ваши данные соответствуют <b>шаблону</b></p>
            <p class="attention-p">Обратите внимание на лист <b>«<span class=underlined>справочник</span>»</b></p>
          </div>
          <div class="download-template-btn">Скачать шаблон</div>
        </div>
      </div>


      <div class=files-list-card>
        <div class="files-progress-bar">
          <div class="progress-fill" style="width: 100%;"></div>
        </div>
        <div class="main-content-files-list">
          <div class="file-list-header">Список загружаемых файлов</div>
          
          <div class="file-list">
            <div class="file-item">
              <div class="file-item-main">
                <div class="file-icon">
                  <span class="file-extension">xlsx</span>
                </div>

                <div class="file-details">
                  <p class="file-name">Пациенты.xlsx</p>
                  <p class="file-size">1.2 MB</p>
                </div>

                <button class="delete-file-btn">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M14 1.41L12.59 0L7 5.59L1.41 0L0 1.41L5.59 7L0 12.59L1.41 14L7 8.41L12.59 14L14 12.59L8.41 7L14 1.41Z"/>
                  </svg>
                </button>
              </div>

            </div>
          </div>

          <div class="files-list-actions">
            <div class="cancel-all-btn">Отменить все</div>
           <div class="upload-btn">Загрузить</div>
          </div>
        </div>
      </div>
    </div>

    <div class="one-patient-input"></div>
  </div>

  <!-- <main class="upload-page">
    <button class="download-template-btn" @click="downloadTemplate" :disabled="templateLoading">
      {{ templateLoading ? 'Загрузка...' : 'Скачать шаблон' }}
    </button> -->

    <!-- <div class="upload-card">
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
    </div> -->

    <!-- <div v-if="report" class="report-card">
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
    </div> -->
  <!-- </main> -->
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

// ПОДЧЁРКИВАНИЯ: 
.underlined {
  text-decoration: underline;
  text-decoration-color: #0f356e;
  text-decoration-thickness: 1px;
  text-underline-offset: 2px;
}

.underlined-hover {
  &:hover {
    text-decoration: underline;
    text-decoration-color: #0f356e;
    text-decoration-thickness: 2px;
    text-underline-offset: 2px;
    cursor: pointer;
  }
}

// КОНТЕЙНЕР СТРАНИЦЫ
.upload-page {
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  gap: 64px;
  margin-block: 40px;
  margin-inline: 200px;
}

.upload-duo-cards {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  gap: 32px;
  width: 100%;
}

// КАРТОЧКА СПИСКА ФАЙЛОВ (ПРАВО ВЕРХ)
.files-list-card {
  display: flex;
  flex-direction: row;
  flex: 1;
  min-width: 400px;
  padding: 32px;

  background-color: rgba($color-glass-white, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.4);
  border-radius: $radius-glass-card;

  .files-progress-bar {
    display: flex;
    flex-direction: column;
    color: $color-accent;
    background: rgba($color-secondary, 0.3);
    border-radius: 1000px;
    margin-right: 8px;
    width: 20px;
    height: 100%;
    overflow: hidden;

    .progress-fill {

    }
  }

  .main-content-files-list {
    display: flex;
    flex-direction: column;

    .file-list-header {
      font-size: 1.5rem;
      font-weight: 600;
      white-space: nowrap;
      color: $color-blue-dark;
      margin-bottom: 24px;
    }
    .file-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex-grow: 1;
      margin-bottom: 16px;

      overflow-y: auto;
      padding-right: 8px; // место под скролл
    
      // СТИЛЬ ОДНОГО ФАЙЛА
      .file-item {
        display: flex;
        flex-direction: row;
        background: none;
        border-radius: 10;
        margin: 0;

        &:hover {
          border: 2.5px solid rgba($color-secondary, 0.3); 
          border-radius: 100;
          cursor: pointer
        }

        .file-item-main {
          display: flex;
          flex-grow: 1;
          align-items: center;
          padding: 8px;
          gap: 16px;

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
        
          .file-details {
            flex-grow: 1;
            display: flex;
            flex-direction: row;
            gap: 24px;

            .file-name {
              font-weight: 600;
              color: $color-blue-dark;
              font-size: 1rem;
            }
            .file-size {
              font-weight: 400;
              color: $color-secondary-text;
              font-size: 1rem;
            }
          }

          .delete-file-btn {
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

    .files-list-actions {

    }
  }

}

// КАРТОЧКА ЗАГРУЗКИ (ЛЕВО ВЕРХ)
.upload-card {
  display: flex;
  flex-direction: column;
  // align-items: center;

  // ВЕРХ
  .upload-card-upper-part {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    max-width: 1000px;
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
        color: $color-blue-dark;
        font-weight: 600;
        font-size: 1.25rem;
        margin-bottom: 8px;
      }
      .upload-formats-p {
        margin: 0;
        font-size: 1rem;
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
    max-width: 900px;
    width: 100%;
    margin-top: -30px;
    z-index: 1;

    padding: 32px;
    border-radius: 0 0 $radius-glass-card $radius-glass-card;

    background: rgba($color-glass-white, 0.35);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
  
    gap: 20px;
  }

  .bottom-card-text-block {
    min-width: 400px;
    
    .alert-p {
      margin-top: 12px;
      font-size: 1.25rem;
      color: $color-text;
    }

    .attention-p {
      margin-top: 16px;
      font-size: 1rem;
      color: $color-text;
    }
  }
  .download-template-btn {
    margin-top: 16px;
    padding-block: 13px;
    font-size: 1.15rem;
    font-weight: 600;
    padding-inline: 22px;
    border-radius: $radius-rect-buttons;
    background: $color-accent;
    color: $color-white;
    font-family: inherit;
    cursor: pointer;
    transition: background 0.2s ease;

    white-space: nowrap;
    width: max-content;

    &:hover {
      background: $color-accent-lighter;
    }
  }
}


// .upload-page {
//   margin-left: 240px;
//   padding: 40px 40px 40px 48px;
//   min-height: 100vh;
//   display: flex;
//   flex-direction: column;
//   gap: 24px;
// }


// .file-input-hidden {
//   display: none;
// }

// .file-icon {
//   flex-shrink: 0;
//   opacity: 0.9;
// }

// .file-info {
//   flex: 1;
//   display: flex;
//   flex-direction: column;
//   gap: 4px;
//   overflow: hidden;
// }

// .file-name {
//   font-weight: 600;
//   font-size: 0.95rem;
//   white-space: nowrap;
//   overflow: hidden;
//   text-overflow: ellipsis;
// }

// .file-size {
//   font-size: 0.8rem;
//   opacity: 0.65;
// }

// .clear-btn {
//   background: none;
//   border: none;
//   color: rgba($color-glass-white, 0.7);
//   cursor: pointer;
//   padding: 4px;
//   display: flex;
//   align-items: center;
//   flex-shrink: 0;
//   transition: color 0.2s;
//   &:hover { color: $color-cold-red; }
// }

// .inline-error {
//   color: $color-cold-red;
//   font-size: 0.85rem;
//   padding: 8px 12px;
//   background: rgba($color-cold-red, 0.1);
//   border-radius: 8px;
// }

// .upload-btn {
//   display: flex;
//   align-items: center;
//   justify-content: center;
//   gap: 8px;
//   padding: 14px;
//   border: none;
//   border-radius: $radius-rect-buttons;
//   background: $color-accent;
//   color: $color-white;
//   font-size: 1rem;
//   font-weight: 600;
//   font-family: inherit;
//   cursor: pointer;
//   transition: background 0.2s, opacity 0.2s;

//   &:hover:not(:disabled) { background: $color-blue-dark; }
//   &:disabled { opacity: 0.45; cursor: not-allowed; }
// }

// .spinner {
//   width: 16px;
//   height: 16px;
//   border: 2px solid rgba($color-glass-white, 0.35);
//   border-top-color: $color-white;
//   border-radius: 50%;
//   animation: spin 0.7s linear infinite;
//   flex-shrink: 0;
// }

// @keyframes spin {
//   to { transform: rotate(360deg); }
// }

// .error-banner {
//   padding: 12px 16px;
//   background: rgba($color-cold-red, 0.12);
//   border: 1px solid rgba($color-cold-red, 0.4);
//   border-radius: 10px;
//   color: $color-cold-red;
//   font-size: 0.9rem;
// }

// .report-card {
//   background: rgba($color-glass-white, 0.35);
//   backdrop-filter: blur(12px);
//   border: 1px solid rgba($color-glass-white, 0.5);
//   border-radius: $radius-glass-card;
//   padding: 32px;
//   display: flex;
//   flex-direction: column;
//   gap: 24px;
//   max-width: 640px;
// }

// .report-stats {
//   display: flex;
//   gap: 16px;
// }

// .stat {
//   flex: 1;
//   display: flex;
//   flex-direction: column;
//   align-items: center;
//   padding: 20px 12px;
//   border-radius: 14px;
//   background: rgba($color-glass-white, 0.2);

//   &--saved   { border-top: 3px solid $color-green; }
//   &--skipped { border-top: 3px solid $color-cold-red; }
//   &--total   { border-top: 3px solid $color-accent-lighter; }
// }

// .stat-value {
//   font-size: 2rem;
//   font-weight: 700;
//   color: $color-white;
// }

// .stat-label {
//   font-size: 0.8rem;
//   color: rgba($color-glass-white, 0.75);
//   margin-top: 4px;
// }

// .errors-title {
//   font-size: 1rem;
//   font-weight: 600;
//   color: $color-white;
//   margin: 0 0 12px;
// }

// .errors-table-wrap {
//   overflow-x: auto;
//   border-radius: 10px;
//   border: 1px solid rgba($color-glass-white, 0.3);
// }

// .errors-table {
//   width: 100%;
//   border-collapse: collapse;
//   font-size: 0.875rem;

//   th, td {
//     padding: 10px 14px;
//     text-align: left;
//     color: $color-white;
//     border-bottom: 1px solid rgba($color-glass-white, 0.15);
//   }

//   th {
//     background: rgba($color-glass-white, 0.15);
//     font-weight: 600;
//     font-size: 0.8rem;
//     text-transform: uppercase;
//     letter-spacing: 0.04em;
//   }

//   tr:last-child td { border-bottom: none; }

//   .row-num {
//     font-weight: 600;
//     color: $color-cold-red;
//     white-space: nowrap;
//   }
// }

// .success-notice {
//   text-align: center;
//   color: $color-green-light;
//   font-weight: 500;
//   font-size: 0.95rem;
//   padding: 12px;
//   background: rgba($color-green, 0.1);
//   border-radius: 10px;
// }
</style>
