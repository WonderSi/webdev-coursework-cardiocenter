<script setup lang="ts">
import { computed, onMounted, reactive, shallowRef, useTemplateRef } from 'vue'
import {
  doctorAccessLevels,
  saveDoctor,
  type Doctor,
  type DoctorCapabilities,
  type DoctorDraft,
} from '@/api/doctors.api'

const { doctor, capabilities } = defineProps<{
  doctor: Doctor | null
  capabilities: DoctorCapabilities
}>()
const emit = defineEmits<{
  close: []
  saved: [doctor: Doctor]
}>()
const dialog = useTemplateRef<HTMLDialogElement>('dialog')
const saving = shallowRef(false)
const error = shallowRef('')
const showPassword = shallowRef(false)
const draft = reactive<DoctorDraft>({
  name: doctor?.name ?? '',
  username: doctor?.username ?? '',
  email: doctor?.email ?? '',
  password: '',
  // Legacy DOCTOR accounts currently have all clinical permissions.
  accessLevel: doctor?.accessLevel ?? (capabilities.accessLevels ? 'VIEW' : 'RETRAIN'),
})
const passwordOnly = computed(
  () => Boolean(doctor) && !capabilities.profiles && !capabilities.accessLevels,
)
const title = computed(() => doctor ? 'Редактирование врача' : 'Новый врач')
const canSave = computed(
  () => !saving.value && (!passwordOnly.value || draft.password.length >= 12),
)

onMounted(() => {
  dialog.value?.showModal()
  dialog.value?.querySelector<HTMLInputElement>('input:not([disabled]):not([readonly])')?.focus()
})

function close() {
  if (saving.value) return
  draft.password = ''
  dialog.value?.close()
  emit('close')
}

async function submit() {
  if (!canSave.value) return
  saving.value = true
  error.value = ''
  try {
    const saved = await saveDoctor(doctor, draft, capabilities)
    draft.password = ''
    dialog.value?.close()
    emit('saved', saved)
  } catch (cause) {
    error.value = cause instanceof Error
      ? cause.message
      : 'Нет соединения с сервером. Попробуйте ещё раз.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <dialog
    ref="dialog"
    class="doctor-editor"
    aria-labelledby="doctor-editor-title"
    aria-describedby="doctor-editor-description"
    @cancel.prevent="close"
  >
    <form
      class="editor-form"
      :aria-busy="saving"
      @submit.prevent="submit"
    >
      <header
        class="editor-header"
      >
        <div>
          <h2
            id="doctor-editor-title"
            class="editor-title"
          >
            {{ title }}
          </h2>
          <p
            id="doctor-editor-description"
            class="editor-description"
          >
            {{ doctor ? 'Данные учётной записи и доступ к системе.' : 'Создайте учётную запись администратора для врача.' }}
          </p>
        </div>
        <button
          type="button"
          class="close-button"
          aria-label="Закрыть"
          :disabled="saving"
          @click="close"
        >
          ×
        </button>
      </header>
      <p
        v-if="!capabilities.profiles || !capabilities.accessLevels"
        class="availability-note"
      >
        <template
          v-if="!capabilities.profiles"
        >
          Имя, отдельный логин и изменение email пока недоступны. Для входа используется email.
        </template>
        <template
          v-if="!capabilities.accessLevels"
        >
          Выбор прав пока недоступен: врачи имеют полный доступ к данным и обучению моделей.
        </template>
      </p>
      <fieldset
        class="account-fields"
        :disabled="saving"
      >
        <legend
          class="section-title"
        >
          Учётная запись
        </legend>
        <label
          class="field field-wide"
        >
          <span>
            ФИО
          </span>
          <input
            v-model="draft.name"
            class="text-input"
            name="name"
            autocomplete="off"
            maxlength="255"
            placeholder="Фамилия Имя Отчество"
            :required="capabilities.profiles"
            :disabled="!capabilities.profiles"
            pattern=".*\S.*"
           />
        </label>
        <label
          class="field"
        >
          <span>
            Логин
          </span>
          <input
            v-model="draft.username"
            class="text-input"
            name="username"
            autocomplete="off"
            maxlength="50"
            placeholder="ivan.petrov"
            pattern="[A-Za-z0-9._\-]{3,50}"
            title="От 3 до 50 латинских букв, цифр, точек, дефисов или подчёркиваний"
            :required="capabilities.profiles"
            :disabled="!capabilities.profiles"
           />
        </label>
        <label
          class="field"
        >
          <span>
            Email
          </span>
          <input
            v-model="draft.email"
            class="text-input"
            name="email"
            type="email"
            autocomplete="off"
            required
            maxlength="255"
            placeholder="doctor@example.ru"
            :readonly="Boolean(doctor) && !capabilities.profiles"
           />
        </label>
        <div
          class="field field-wide"
        >
          <label
            for="doctor-password"
          >
            {{ doctor ? 'Новый пароль' : 'Пароль' }}
            <span
              v-if="doctor && !passwordOnly"
              class="optional-label"
            >
              (необязательно)
            </span>
          </label>
          <div
            class="password-field"
          >
            <input
              id="doctor-password"
              v-model="draft.password"
              class="text-input"
              name="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="new-password"
              :required="!doctor || passwordOnly"
              minlength="12"
              maxlength="100"
              aria-describedby="password-help"
              placeholder="Не менее 12 символов"
             />
            <button
              type="button"
              class="password-toggle"
              :aria-pressed="showPassword"
              aria-controls="doctor-password"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? 'Скрыть' : 'Показать' }}
            </button>
          </div>
          <p
            id="password-help"
            class="field-help"
          >
            {{ doctor && !passwordOnly ? 'Оставьте пустым, чтобы сохранить текущий пароль.' : 'От 12 до 100 символов. Передайте пароль врачу лично.' }}
          </p>
        </div>
      </fieldset>
      <fieldset
        class="access-fields"
        :disabled="saving || !capabilities.accessLevels"
      >
        <legend
          class="section-title"
        >
          Права доступа
        </legend>
        <p
          class="access-help"
        >
          Каждый следующий уровень включает возможности предыдущего.
        </p>
        <label
          v-for="level in doctorAccessLevels"
          :key="level.value"
          class="access-option"
          :class="{ selected: draft.accessLevel === level.value }"
        >
          <input
            v-model="draft.accessLevel"
            type="radio"
            name="accessLevel"
            :value="level.value"
            required
           />
          <span
            class="access-copy"
          >
            <strong
              class="access-label"
            >
              {{ level.label }}
            </strong>
            <span
              class="access-description"
            >
              {{ level.description }}
            </span>
          </span>
        </label>
      </fieldset>
      <p
        class="management-note"
      >
        Управление врачами доступно только суперадминистратору.
      </p>
      <p
        v-if="doctor?.isBlocked"
        class="availability-note"
      >
        Учётная запись заблокирована. Сохранение настроек не восстановит вход.
      </p>
      <p
        v-if="error"
        class="form-error"
        role="alert"
      >
        {{ error }}
      </p>
      <footer
        class="editor-actions"
      >
        <button
          type="button"
          class="button button-secondary"
          :disabled="saving"
          @click="close"
        >
          Отмена
        </button>
        <button
          type="submit"
          class="button button-primary"
          :disabled="!canSave"
        >
          {{ saving ? 'Сохранение…' : doctor ? 'Сохранить изменения' : 'Создать учётную запись' }}
        </button>
      </footer>
    </form>
  </dialog>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;
.doctor-editor {
  position: fixed;
  inset: 0;
  width: min(640px, calc(100% - 32px));
  max-height: calc(100dvh - 32px);
  margin: auto;
  padding: 0;
  border: 0;
  border-radius: $radius-glass-card;
  color: $color-text;
  background: $color-white;
  box-shadow: 0 24px 80px rgba($color-blue-dark, 0.25);
  &::backdrop {
    background: rgba($color-blue-dark, 0.45);
    backdrop-filter: blur(4px);
  }
}

.editor-form {
  padding: 32px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}

.editor-title {
  font-size: 1.5rem;
  font-weight: 600;
}

.editor-description {
  margin-top: 8px;
  font-size: 0.875rem;
  line-height: 1.5;
  color: $color-secondary-text;
}

.close-button {
  border: 0;
  background: transparent;
  font-size: 1.8rem;
  line-height: 1;
  padding: 2px 6px;
  color: $color-secondary-text;
  cursor: pointer;
}

.availability-note {
  padding: 12px 16px;
  background: #edf4ff;
  border-left: 3px solid $color-accent;
  border-radius: 8px;
  font-size: 0.8125rem;
  line-height: 1.6;
  margin-bottom: 24px;
  color: $color-blue-dark;
}

.account-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  border: 0;
  min-width: 0;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  font-size: 0.875rem;
  font-weight: 500;
}

.field-wide {
  grid-column: 1 / -1;
}

.text-input {
  width: 100%;
  min-width: 0;
  border: 1px solid rgba($color-secondary, 0.55);
  border-radius: $radius-rect-buttons;
  padding: 12px;
  background: $color-white;
  font: inherit;
  color: $color-text;
}

.text-input::placeholder {
  color: #747a8a;
  font-weight: 400;
}

.text-input:disabled,
.text-input:read-only {
  color: $color-secondary-text;
  background: #f3f5f8;
}

.optional-label,
.field-help,
.access-help {
  font-weight: 400;
  color: $color-secondary-text;
  font-size: 0.8125rem;
  line-height: 1.5;
}

.password-field {
  display: flex;
  border: 1px solid rgba($color-secondary, 0.55);
  border-radius: $radius-rect-buttons;
  overflow: hidden;
}

.password-field .text-input {
  border: 0;
}

.password-toggle {
  padding: 0 12px;
  border: 0;
  background: transparent;
  font: inherit;
  font-size: 0.8125rem;
  color: $color-accent;
  cursor: pointer;
}

.access-fields {
  border: 0;
  padding: 0;
  margin-top: 28px;
  min-width: 0;
}

.access-help {
  margin: -6px 0 12px;
}

.access-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #d7ddeb;
  border-radius: 12px;
  margin-top: 8px;
  cursor: pointer;
}

.access-option.selected {
  border-color: $color-accent;
  background: #edf4ff;
}

.access-option input {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  accent-color: $color-accent;
}

.access-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.access-label {
  font-weight: 600;
  font-size: 0.875rem;
}

.access-description {
  font-size: 0.8125rem;
  color: $color-secondary-text;
  line-height: 1.5;
}

.access-fields:disabled .access-option {
  cursor: default;
}

.management-note {
  margin: 12px 0 24px;
  font-size: 0.75rem;
  color: $color-secondary-text;
  line-height: 1.5;
}

.form-error {
  margin-top: 16px;
  color: #b01835;
  background: $color-red-bg;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 0.875rem;
  line-height: 1.5;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e2e7f0;
  margin-top: 24px;
}

.button {
  padding: 12px 18px;
  border-radius: $radius-rect-buttons;
  font: inherit;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
}

.button-primary {
  border: 1px solid $color-accent;
  background: $color-accent;
  color: $color-white;
}

.button-primary:hover:not(:disabled) {
  background: #1055be;
}

.button-secondary {
  border: 1px solid #c6cedd;
  background: transparent;
  color: $color-blue-dark;
}

.button-secondary:hover:not(:disabled) {
  background: #edf4ff;
}

.button:disabled,
.close-button:disabled {
  opacity: 0.55;
  cursor: wait;
}

.button:disabled:not([aria-busy]) {
  cursor: default;
}

.text-input:focus-visible,
.button:focus-visible,
.close-button:focus-visible,
.password-toggle:focus-visible,
.access-option input:focus-visible {
  outline: 3px solid $color-accent;
  outline-offset: 3px;
}

.password-field:focus-within {
  outline: 2px solid $color-accent;
  outline-offset: 2px;
}

@media (max-width: 560px) {
  .editor-form {
    padding: 24px 20px;
  }

  .account-fields {
    grid-template-columns: 1fr;
  }

  .editor-actions {
    flex-direction: column-reverse;
  }

  .button {
    width: 100%;
  }
}
</style>
