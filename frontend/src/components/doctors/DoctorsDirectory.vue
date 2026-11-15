<script setup lang="ts">
import { nextTick, useTemplateRef } from 'vue'
import DoctorsTable from './DoctorsTable.vue'
import DoctorAccessFilter from './DoctorAccessFilter.vue'
import { useDoctorFilters } from '@/composables/useDoctorFilters'
import type { Doctor, DoctorCapabilities } from '@/types/doctor'

const { doctors, capabilities, loading, loaded, error } = defineProps<{
  doctors: readonly Doctor[]
  capabilities: DoctorCapabilities
  loading: boolean
  loaded: boolean
  error: string
}>()
const emit = defineEmits<{
  refresh: []
  create: []
  edit: [doctor: Doctor]
  block: [doctor: Doctor]
}>()
const summary = useTemplateRef<HTMLHeadingElement>('summary')
const { query, statusFilter, accessFilter, visibleRows, statusOptions, hasFilters, resetFilters } = useDoctorFilters(
  () => doctors,
  () => capabilities,
)

async function focusFallback() {
  await nextTick()
  if (document.activeElement === document.body) summary.value?.focus()
}

defineExpose({ resetFilters, focusFallback })
</script>

<template>
  <section
    class="directory-card"
    aria-label="Список врачей"
    :aria-busy="loading"
  >
    <div class="directory-heading">
      <div>
        <h2
          ref="summary"
          class="section-title"
          tabindex="-1"
        >
          Учётные записи
        </h2>
        <p class="section-description">Профиль можно заблокировать, сохранив все данные.</p>
      </div>
      <div class="directory-actions">
        <button
          type="button"
          class="button button-create"
          :disabled="loading || !loaded || Boolean(error)"
          @click="emit('create')"
        >
          <span class="plus-icon" aria-hidden="true">+</span>
          Добавить врача
        </button>
        <button
          type="button"
          class="text-button"
          :disabled="loading"
          @click="emit('refresh')"
        >
          {{ loading ? 'Обновление…' : 'Обновить список' }}
        </button>
      </div>
    </div>
    <div class="directory-toolbar">
      <label class="search-field">
        <svg
          class="search-icon"
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          aria-hidden="true"
        >
          <circle
            cx="10.5"
            cy="10.5"
            r="6.5"
          />
          <path d="m16 16 4.5 4.5" />
        </svg>
        <span class="sr-only">Поиск по ФИО или email</span>
        <input
          v-model="query"
          class="search-input"
          type="search"
          placeholder="ФИО или email"
          :disabled="loading || !loaded"
        />
      </label>
      <DoctorAccessFilter
        v-model="accessFilter"
        class="access-filter"
        :disabled="loading || !loaded"
      />
    </div>
    <div class="status-bar">
      <div
        class="status-filters"
        role="group"
        aria-label="Статус учётной записи"
      >
        <button
          v-for="status in statusOptions"
          :key="status.value"
          type="button"
          class="status-filter"
          :class="{ selected: statusFilter === status.value }"
          :aria-pressed="statusFilter === status.value"
          :disabled="loading || !loaded"
          @click="statusFilter = status.value"
        >
          {{ status.label }}
          <span class="filter-count">{{ loaded ? status.count : '—' }}</span>
        </button>
      </div>
      <span
        v-if="loaded && !loading && !error"
        class="result-count"
        role="status"
      >
        Показано
        {{ visibleRows.length }}
        из
        {{ doctors.length }}
      </span>
    </div>
    <div
      v-if="loading"
      class="state-panel"
      role="status"
    >
      <h3 class="state-title">Загружаем список врачей…</h3>
      <p class="state-description">Получаем учётные записи и настройки доступа.</p>
    </div>
    <div
      v-else-if="error"
      class="state-panel"
      role="alert"
    >
      <h3 class="state-title">Список недоступен</h3>
      <p class="state-description">{{ error }}</p>
      <button
        type="button"
        class="button button-primary"
        @click="emit('refresh')"
      >
        Попробовать снова
      </button>
    </div>
    <div v-else-if="!visibleRows.length" class="state-panel">
      <h3 class="state-title">{{ hasFilters ? 'Врачи не найдены' : 'Добавьте первого врача' }}</h3>
      <p class="state-description">
        {{ hasFilters ? 'Измените запрос или сбросьте фильтры.' : 'Создайте учётную запись, чтобы врач мог войти в систему.' }}
      </p>
      <button
        v-if="hasFilters"
        type="button"
        class="button button-secondary"
        @click="resetFilters"
      >
        Сбросить фильтры
      </button>
    </div>
    <DoctorsTable
      v-else
      :rows="visibleRows"
      @edit="emit('edit', $event)"
      @block="emit('block', $event)"
    />
  </section>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: $radius-rect-buttons;
  padding: 8px 16px;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
}

.button-create {
  background: $color-accent;
  color: $color-white;
  flex-shrink: 0;
}

.button-create:hover:not(:disabled) {
  background: #1055be;
}

.plus-icon {
  font-size: 1.5rem;
  line-height: 0.8;
  font-weight: 400;
}

.button-primary {
  background: $color-accent;
  color: $color-white;
}

.button-primary:hover:not(:disabled) {
  background: #1055be;
}

.button-secondary {
  background: transparent;
  border-color: $color-accent;
  color: $color-accent;
}

.button-secondary:hover:not(:disabled) {
  background: $color-accent;
  color: $color-white;
}

.button:disabled,
.text-button:disabled,
.status-filter:disabled {
  opacity: 0.55;
  cursor: default;
}

.directory-card {
  overflow: hidden;
  background: rgba($color-glass-white, 0.92);
  border: 1px solid rgba($color-white, 0.5);
  border-radius: $radius-glass-card;
}

.directory-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px 20px;
}

.directory-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 28px 28px 24px;
}

.section-title {
  color: $color-blue-dark;
  font-size: 1.25rem;
  font-weight: 600;
}

.section-description {
  color: $color-secondary-text;
  font-size: 0.875rem;
  line-height: 1.5;
  margin-top: 8px;
}

.text-button {
  background: transparent;
  border: 0;
  padding: 6px 0;
  color: $color-accent;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  text-underline-offset: 3px;
}

.text-button:hover:not(:disabled) {
  text-decoration: underline;
}

.directory-toolbar {
  display: flex;
  gap: 16px;
  padding: 0 28px 24px;
}

.search-field {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  border: 1px solid $color-ui-border;
  border-radius: 10px;
  padding: 0 14px;
  background: rgba($color-white, 0.75);
}

.search-icon {
  color: $color-secondary-text;
  flex-shrink: 0;
}

.search-input {
  border: 0;
  background: transparent;
  color: $color-text;
  padding: 12px 0;
  font: inherit;
  font-size: 0.875rem;
  min-width: 0;
  width: 100%;
}

.search-input::placeholder {
  color: #697185;
}

.search-input:focus {
  outline: none;
}

.search-field:focus-within {
  border-color: $color-accent;
}

.access-filter {
  flex: 0 1 255px;
  min-width: 180px;
}

.status-bar {
  padding: 0 28px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.status-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.status-filter {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  padding: 9px 12px;
  border-radius: 9px;
  background: transparent;
  color: $color-secondary-text;
  font: inherit;
  font-size: 0.8125rem;
  cursor: pointer;
}

.status-filter.selected {
  background: #e1ecfd;
  color: $color-blue-dark;
  font-weight: 600;
}

.status-filter:hover:not(:disabled) {
  background: #e9eff8;
}

.filter-count {
  font-variant-numeric: tabular-nums;
  font-size: 0.75rem;
}

.result-count {
  color: $color-secondary-text;
  font-size: 0.75rem;
}

.state-panel {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 12px;
  text-align: center;
  padding: 56px 28px;
}

.state-title {
  color: $color-blue-dark;
  font-size: 1.125rem;
  font-weight: 600;
}

.state-description {
  color: $color-secondary-text;
  font-size: 0.85rem;
  line-height: 1.6;
  max-width: 55ch;
}

.state-panel .button {
  margin-top: 8px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
  border: 0;
}

.button:focus-visible,
.text-button:focus-visible,
.status-filter:focus-visible {
  @include focus-ring(3px, 3px);
}

@media (max-width: 768px) {
  .directory-toolbar {
    flex-direction: column;
  }

  .access-filter {
    flex-basis: auto;
  }
}

@media (max-width: 560px) {
  .directory-actions {
    width: 100%;
    align-items: flex-start;
    justify-content: flex-start;
  }

  .directory-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 24px 20px 20px;
  }

  .directory-toolbar {
    padding: 0 20px 20px;
  }

  .status-bar {
    padding: 0 14px 16px;
  }

  .status-filters {
    gap: 0;
  }

  .status-filter {
    gap: 6px;
    padding: 9px;
    font-size: 0.75rem;
  }

  .result-count {
    padding-left: 6px;
  }
}
</style>
