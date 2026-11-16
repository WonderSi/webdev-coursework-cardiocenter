<script setup lang="ts">
import { computed, onMounted, onScopeDispose, reactive, shallowRef, useTemplateRef } from 'vue'
import FieldValidationAlert from '@/components/doctors/FieldValidationAlert.vue'
import CloseButton from '@/components/CloseButton.vue'
import DoctorAccessDetails from './DoctorAccessDetails.vue'
import { accessGuide, effectiveDoctorAccess } from '@/config/doctor-access'
import { adminSections } from '@/types/admin-section'
import { saveDoctor, doctorErrorMessage } from '@/services/doctors'
import type { Doctor, DoctorCapabilities, DoctorDraft } from '@/types/doctor'

const { doctor, capabilities, occupiedEmails = [] } = defineProps<{
  doctor: Doctor | null
  capabilities: DoctorCapabilities
  occupiedEmails?: readonly string[]
}>()
const emit = defineEmits<{
  close: []
  saved: [doctor: Doctor]
  updated: [doctor: Doctor]
}>()
const dialog = useTemplateRef<HTMLDialogElement>('dialog')
const form = useTemplateRef<HTMLFormElement>('form')
const fieldError = shallowRef<{ field: string; message: string } | null>(null)
const saving = shallowRef(false)
const controller = new AbortController()
const error = shallowRef('')
const showPassword = shallowRef(false)
const savedDoctor = shallowRef(doctor)
const initialAccess = doctor ? effectiveDoctorAccess(doctor) : 'VIEW'
const existingSections = computed(() => adminSections
  .filter(section => savedDoctor.value?.allowedSections.includes(section.code))
  .map(section => section.label).join(', ') || 'Нет доступных разделов')
const draft = reactive<DoctorDraft>({
  name: doctor?.name ?? '',
  email: doctor?.email ?? '',
  password: '',
  // Существующий врач сохраняет прежний уровень; новый начинает с просмотра.
  accessLevel: initialAccess === 'NONE' || initialAccess === 'CUSTOM' ? null : initialAccess,
})
const title = computed(() => doctor ? 'Редактирование врача' : 'Новый врач')
const canSave = computed(() => !saving.value)

onMounted(() => {
  dialog.value?.showModal()
  dialog.value?.querySelector<HTMLInputElement>('input:not([disabled]):not([readonly])')?.focus()
})

onScopeDispose(() => {
  controller.abort()
  draft.password = ''
  dialog.value?.close()
})

function close() {
  if (saving.value) return
  draft.password = ''
  dialog.value?.close()
  emit('close')
}

// Проверяем ограничения полей, но показываем собственное сообщение вместо
// системного окна браузера. Первый неверный input получает фокус.
function validationMessage(input: HTMLInputElement): string {
  if (!input.willValidate) return ''
  if (input.name === 'name' && !input.value.trim()) return 'Укажите ФИО.'
  if (input.validity.valueMissing) {
    return input.name === 'email' ? 'Укажите email для входа.' : 'Укажите пароль.'
  }
  if (input.validity.typeMismatch) return 'Введите корректный email с символом @.'
  if (input.name === 'email' && !savedDoctor.value
    && occupiedEmails.some(email => email.toLowerCase() === input.value.trim().toLowerCase())) {
    return 'Этот email уже используется. Укажите другой адрес для нового врача.'
  }
  if (input.name === 'password' && input.value) {
    if (input.value.length < 12) return 'Пароль должен содержать не менее 12 символов.'
    if (input.value.length > 100) return 'Пароль должен содержать не более 100 символов.'
  }
  return input.validity.valid ? '' : input.validationMessage
}

function validateForm(): boolean {
  fieldError.value = null
  const inputs = form.value?.querySelectorAll<HTMLInputElement>('input') ?? []
  for (const input of inputs) {
    const message = validationMessage(input)
    if (message) {
      fieldError.value = { field: input.name, message }
      input.focus()
      return false
    }
  }
  return true
}

function updateFieldError(event: Event) {
  const input = event.target
  if (!(input instanceof HTMLInputElement) || fieldError.value?.field !== input.name) return
  const message = validationMessage(input)
  fieldError.value = message ? { field: input.name, message } : null
}

async function submit() {
  if (!canSave.value) return
  error.value = ''
  if (!validateForm()) return
  saving.value = true
  try {
    const saved = await saveDoctor(savedDoctor.value, draft, capabilities, controller.signal, progress => {
      if (controller.signal.aborted) return
      savedDoctor.value = progress.doctor
      if (progress.passwordSaved) draft.password = ''
      emit('updated', progress.doctor)
    })
    if (controller.signal.aborted) return
    draft.password = ''
    dialog.value?.close()
    emit('saved', saved)
  } catch (cause) {
    if (!controller.signal.aborted) error.value = doctorErrorMessage(cause, 'Не удалось сохранить учётную запись.')
  } finally {
    if (!controller.signal.aborted) saving.value = false
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
    <div class="editor-scroll">
      <form
        ref="form"
        class="editor-form"
        novalidate
        @input="updateFieldError"
        :aria-busy="saving"
        @submit.prevent="submit"
      >
        <header class="editor-header">
          <div>
            <h2 id="doctor-editor-title" class="editor-title">{{ title }}</h2>
            <p id="doctor-editor-description" class="editor-description">
              {{ doctor ? 'Данные учётной записи и доступ к системе.' : 'Создайте учётную запись для врача. Помните - логин уникален.' }}
            </p>
          </div>
          <CloseButton
            class="close-button"
            :disabled="saving"
            @click="close"
          />
        </header>
        <fieldset class="account-fields" :disabled="saving">
          <label class="field field-wide">
            <span>ФИО</span>
            <input
              v-model="draft.name"
              class="text-input"
              name="name"
              :aria-invalid="fieldError?.field === 'name' ? true : undefined"
              :aria-describedby="fieldError?.field === 'name' ? 'doctor-name-error' : undefined"
              autocomplete="off"
              maxlength="255"
              placeholder="Фамилия Имя Отчество"
              required
              pattern=".*\S.*"
            />
            <FieldValidationAlert
              v-if="fieldError?.field === 'name'"
              id="doctor-name-error"
              :message="fieldError.message"
            />
            <span class="field-help">Изменение ФИО действует до обновления списка.</span>
          </label>
          <label class="field">
            <span>Email (логин)</span>
            <input
              v-model="draft.email"
              class="text-input"
              name="email"
              :aria-invalid="fieldError?.field === 'email' ? true : undefined"
              :aria-describedby="fieldError?.field === 'email' ? 'doctor-email-error' : undefined"
              type="email"
              autocomplete="off"
              required
              maxlength="255"
              placeholder="doctor@example.ru"
            />
            <FieldValidationAlert
              v-if="fieldError?.field === 'email'"
              id="doctor-email-error"
              :message="fieldError.message"
            />
            <span v-if="doctor" class="field-help">Изменение email действует до обновления списка; логин для входа останется прежним.</span>
          </label>
          <div class="field">
            <label for="doctor-password">
              {{ doctor ? 'Новый пароль' : 'Пароль' }}
              <span v-if="doctor" class="optional-label">(необязательно)</span>
            </label>
            <div class="password-field">
              <input
                id="doctor-password"
                v-model="draft.password"
                class="text-input"
                name="password"
                :aria-invalid="fieldError?.field === 'password' ? true : undefined"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                :required="!savedDoctor"
                minlength="12"
                maxlength="100"
                :aria-describedby="fieldError?.field === 'password' ? 'password-help doctor-password-error' : 'password-help'"
                placeholder="Не менее 12 символов"
              />
              <button
                type="button"
                class="password-toggle"
                :aria-label="showPassword ? 'Скрыть пароль' : 'Показать пароль'"
                :aria-pressed="showPassword"
                aria-controls="doctor-password"
                @click="showPassword = !showPassword"
              >
                <svg
                  v-if="showPassword"
                  class="password-eye"
                  aria-hidden="true"
                  focusable="false"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M12 6.5C13.8387 6.49389 15.6419 7.00678 17.2021 7.97973C18.7624 8.95267 20.0164 10.3462 20.82 12C19.17 15.37 15.8 17.5 12 17.5C8.2 17.5 4.83 15.37 3.18 12C3.98362 10.3462 5.23763 8.95267 6.79788 7.97973C8.35813 7.00678 10.1613 6.49389 12 6.5ZM12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5C17 19.5 21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5ZM12 9.5C12.663 9.5 13.2989 9.76339 13.7678 10.2322C14.2366 10.7011 14.5 11.337 14.5 12C14.5 12.663 14.2366 13.2989 13.7678 13.7678C13.2989 14.2366 12.663 14.5 12 14.5C11.337 14.5 10.7011 14.2366 10.2322 13.7678C9.76339 13.2989 9.5 12.663 9.5 12C9.5 11.337 9.76339 10.7011 10.2322 10.2322C10.7011 9.76339 11.337 9.5 12 9.5ZM12 7.5C9.52 7.5 7.5 9.52 7.5 12C7.5 14.48 9.52 16.5 12 16.5C14.48 16.5 16.5 14.48 16.5 12C16.5 9.52 14.48 7.5 12 7.5Z" fill="#8A8FA8" />
                </svg>
                <svg
                  v-else
                  class="password-eye"
                  aria-hidden="true"
                  focusable="false"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M15 18L14.278 14.75M2 8C2.74835 10.0508 4.10913 11.8219 5.8979 13.0733C7.68667 14.3247 9.81695 14.9959 12 14.9959C14.1831 14.9959 16.3133 14.3247 18.1021 13.0733C19.8909 11.8219 21.2516 10.0508 22 8M20 15L18.274 12.95M4 15L5.726 12.95M9 18L9.722 14.75"
                    stroke="#8A8FA8"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>
            <FieldValidationAlert
              v-if="fieldError?.field === 'password'"
              id="doctor-password-error"
              :message="fieldError.message"
            />
            <p id="password-help" class="field-help">
              {{ doctor ? 'Оставьте пустым, чтобы сохранить текущий пароль.' : 'От 12 до 100 символов.' }}
            </p>
          </div>
        </fieldset>
        <fieldset class="access-fields" :disabled="saving">
          <legend class="section-title">Права доступа</legend>
          <p class="access-help">Каждый следующий уровень включает все возможности предыдущего.</p>
          <p v-if="draft.accessLevel === null" class="access-help" role="status">
            Текущие права: {{ existingSections }}. Они сохранятся, пока вы не выберете один из уровней ниже.
          </p>
          <div
            class="access-options-grid"
            role="group"
            aria-label="Уровень доступа"
          >
            <button
              v-for="level in accessGuide"
              :key="level.level"
              type="button"
              class="access-option"
              :aria-pressed="draft.accessLevel === level.level"
              @click="draft.accessLevel = level.level"
              :class="{ selected: draft.accessLevel === level.level }"
            >
              <DoctorAccessDetails :level="level.level" />
            </button>
          </div>
        </fieldset>
        <p v-if="doctor?.isBlocked" class="availability-note">Учётная запись заблокирована. Сохранение настроек не восстановит вход.</p>
        <p
          v-if="error"
          class="form-error"
          role="alert"
        >
          {{ error }}
        </p>
        <footer class="editor-actions">
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
    </div>
  </dialog>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.doctor-editor {
  position: fixed;
  inset: 0;
  width: min(960px, calc(100% - 32px));
  max-height: calc(100vh - 32px);
  max-height: calc(100dvh - 32px);
  overflow: hidden;
  margin: auto;
  padding: 16px 8px;
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

.editor-scroll {
  max-height: calc(100vh - 64px);
  max-height: calc(100dvh - 64px);
  overflow-y: auto;
  @include custom-scrollbar;
}

.editor-form {
  padding: 16px 24px;
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
  margin-top: 16px;
  font-size: 0.9rem;
  line-height: 1.5;
  color: $color-secondary-text;
  margin-bottom: 24px;
}

.close-button {
  flex-shrink: 0;
}

.availability-note {
  padding: 12px 16px;
  background: $color-ui-surface-hover;
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
  border: 1px solid $color-field-border;
  border-radius: $radius-rect-buttons;
  padding: 12px;
  background: $color-white;
  font: inherit;
  color: $color-text;
  appearance: none;
  -webkit-appearance: none;
  outline: none;
  box-shadow: none;
}

.text-input:focus {
  border-color: $color-accent;
}

.text-input:autofill {
  color: $color-text;
  caret-color: $color-text;
  box-shadow: 0 0 0 1000px $color-ui-surface-hover inset;
}

.text-input:-webkit-autofill {
  -webkit-text-fill-color: $color-text;
  caret-color: $color-text;
  box-shadow: 0 0 0 1000px $color-ui-surface-hover inset;
}

.password-field:has(.text-input:autofill) {
  background: $color-ui-surface-hover;
}

.password-field:has(.text-input:-webkit-autofill) {
  background: $color-ui-surface-hover;
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
  min-width: 0;
  border: 1px solid $color-field-border;
  border-radius: $radius-rect-buttons;
  overflow: hidden;
}

.password-field .text-input {
  border: 0;
  border-radius: 0;
}

.password-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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
  margin-top: 24px;
  min-width: 0;
}

.access-help {
  margin: -6px 0 12px;
}

.access-options-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.access-option {
  --access-description-color: #{$color-secondary-text};
  --access-page-padding: 8px;
  --access-page-font-size: 0.8125rem;
  --access-pages-title-size: 0.875rem;
  --access-description-line-height: 1.6;
  --access-description-font-size: 0.875rem;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 16px;
  border: 1px solid #d7ddeb;
  border-radius: 12px;
  background: transparent;
  color: $color-text;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.access-option.selected {
  border-color: $color-accent;
  background: $color-ui-surface-hover;
}

.access-fields:disabled .access-option {
  cursor: default;
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
  border: 1px solid $color-accent;
  background: transparent;
  color: $color-accent;
}

.button-secondary:hover:not(:disabled) {
  background: $color-accent;
  color: $color-white;
}

.button:disabled {
  opacity: 0.55;
  cursor: wait;
}

.button:disabled:not([aria-busy]) {
  cursor: default;
}

.button:focus-visible {
  @include focus-ring(3px, 3px);
}

.access-option:focus-visible {
  border-color: $color-accent;
  outline: none;
}

.password-field:focus-within {
  border-color: $color-accent;
}

.password-toggle:focus-visible {
  outline: 1px solid $color-accent;
  outline-offset: -4px;
  border-radius: 6px;
}

.password-eye {
  display: block;
  width: 24px;
  height: 24px;
}

@media (max-width: 850px) {
  .access-options-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .editor-form {
    padding: 8px 12px;
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
