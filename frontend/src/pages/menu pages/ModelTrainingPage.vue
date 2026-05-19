<template>
  <Sidebar />

  <div class="training-page">
    <div class="training-card">
      <div class="section-header">Переобучение модели</div>

      <p class="training-desc">
        Запустите переобучение на основе всех пациентов, сохранённых в базе данных.
        Процесс может занять несколько минут.
      </p>

      <div class="training-action">
        <button
          class="retrain-btn"
          :disabled="loading"
          @click="runRetrain"
        >
          <span v-if="loading" class="spinner"></span>
          {{ loading ? 'Переобучение...' : 'Запустить переобучение' }}
        </button>
      </div>

      <div class="result-msg" :class="{ 'msg-visible': !!resultState }">
        <template v-if="resultState === 'success'">
          <p class="msg-success">Переобучение завершено успешно</p>
          <ul class="models-list">
            <li v-for="model in savedModels" :key="model">{{ model }}</li>
          </ul>
        </template>
        <template v-else-if="resultState === 'error'">
          <p class="msg-error">{{ errorText }}</p>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

const loading = ref(false)
const resultState = ref<'success' | 'error' | null>(null)
const savedModels = ref<string[]>([])
const errorText = ref('')

async function runRetrain() {
  loading.value = true
  resultState.value = null
  savedModels.value = []
  errorText.value = ''

  try {
    const res = await fetch('/api/admin/retrain', {
      method: 'POST',
      credentials: 'include',
    })

    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Ошибка ${res.status}`)
    }

    const data = await res.json()
    savedModels.value = data.savedModels ?? []
    resultState.value = 'success'
  } catch (e: any) {
    errorText.value = e?.message ?? 'Не удалось выполнить переобучение'
    resultState.value = 'error'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.training-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px;
}

.training-card {
  background: $color-white;
  border-radius: $radius-basic-card;
  padding: 40px 48px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 10px 30px rgba(7, 14, 44, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  font-size: 1.1rem;
  font-weight: 700;
  color: $color-text;
}

.training-desc {
  font-size: 0.95rem;
  color: $color-secondary;
  line-height: 1.5;
}

.training-action {
  display: flex;
  justify-content: flex-start;
}

.retrain-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 32px;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: 0.95rem;
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;

  &:hover:not(:disabled) { background: $color-accent-lighter; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: $color-white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.result-msg {
  opacity: 0;
  transition: opacity 0.3s;
  min-height: 24px;

  &.msg-visible { opacity: 1; }
}

.msg-success {
  font-size: 0.95rem;
  font-weight: 600;
  color: $color-green;
  margin-bottom: 10px;
}

.msg-error {
  font-size: 0.95rem;
  font-weight: 600;
  color: $color-red;
}

.models-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;

  li {
    font-size: 0.88rem;
    color: $color-secondary;
    padding: 6px 12px;
    background: rgba($color-accent, 0.05);
    border-radius: 8px;
    font-family: monospace;
  }
}
</style>
