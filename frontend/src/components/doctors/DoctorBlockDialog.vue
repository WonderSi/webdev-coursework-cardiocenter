<script setup lang="ts">
import { computed, onMounted, onScopeDispose, shallowRef, useTemplateRef } from 'vue'
import { doctorErrorMessage, updateDoctorBlocked } from '@/services/doctors'
import type { Doctor } from '@/types/doctor'

const { doctor } = defineProps<{ doctor: Doctor }>()
const emit = defineEmits<{
  close: []
  updated: [doctor: Doctor]
}>()
const dialog = useTemplateRef<HTMLDialogElement>('dialog')
const pending = shallowRef(false)
const error = shallowRef('')
const controller = new AbortController()
const title = computed(() => doctor.isBlocked
  ? 'Восстановить доступ?'
  : 'Заблокировать учётную запись?')
const description = computed(() => doctor.isBlocked
  ? 'Врач снова сможет войти в систему с прежними правами доступа.'
  : 'Врач не сможет войти в систему или продолжить работу в текущей сессии. Учётная запись и данные пациентов сохранятся.')
const submitLabel = computed(() => {
  if (pending.value) return 'Сохранение…'
  return doctor.isBlocked ? 'Восстановить доступ' : 'Заблокировать'
})

onMounted(() => dialog.value?.showModal())
onScopeDispose(() => {
  controller.abort()
  dialog.value?.close()
})

function close() {
  if (pending.value) return
  dialog.value?.close()
  emit('close')
}

async function submit() {
  if (pending.value) return
  pending.value = true
  error.value = ''
  try {
    const updated = await updateDoctorBlocked(doctor, !doctor.isBlocked, controller.signal)
    if (controller.signal.aborted) return
    dialog.value?.close()
    emit('updated', updated)
  } catch (cause) {
    if (!controller.signal.aborted) error.value = doctorErrorMessage(cause, 'Не удалось изменить статус врача.')
  } finally {
    if (!controller.signal.aborted) pending.value = false
  }
}
</script>

<template>
  <dialog
    ref="dialog"
    class="block-dialog"
    aria-labelledby="block-title"
    aria-describedby="block-description"
    @cancel.prevent="close"
  >
    <form
      class="block-form"
      :aria-busy="pending"
      @submit.prevent="submit"
    >
      <h2 id="block-title" class="block-title">{{ title }}</h2>
      <p class="block-identity">{{ doctor.name || doctor.email }}</p>
      <p id="block-description" class="block-description">
        {{ description }}
      </p>
      <p
        v-if="error"
        class="block-error"
        role="alert"
      >
        {{ error }}
      </p>
      <div class="block-actions">
        <button
          type="button"
          class="button button-secondary"
          autofocus
          :disabled="pending"
          @click="close"
        >
          Отмена
        </button>
        <button
          type="submit"
          class="button"
          :class="doctor.isBlocked ? 'button-primary' : 'button-danger'"
          :disabled="pending"
        >
          {{ submitLabel }}
        </button>
      </div>
    </form>
  </dialog>
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

.button-primary {
  background: $color-accent;
  color: $color-white;
}

.button-primary:hover:not(:disabled) {
  background: #1055be;
}

.button-secondary {
  padding: 12px 18px;
  border: 1px solid $color-accent;
  background: transparent;
  color: $color-accent;
  font-size: 0.875rem;
}

.button-secondary:hover:not(:disabled) {
  background: $color-accent;
  color: $color-white;
}

.button-danger {
  background: #c51c3a;
  color: $color-white;
}

.button-danger:hover:not(:disabled) {
  background: #aa1731;
}

.button:disabled {
  opacity: 0.55;
  cursor: default;
}

.block-dialog {
  position: fixed;
  inset: 0;
  margin: auto;
  border: 0;
  padding: 0;
  border-radius: 24px;
  width: min(480px, calc(100% - 32px));
  max-height: calc(100dvh - 32px);
  background: $color-white;
  color: $color-blue-dark;
  box-shadow: 0 24px 80px rgba($color-blue-dark, 0.25);
}

.block-dialog::backdrop {
  background: rgba($color-blue-dark, 0.45);
  backdrop-filter: blur(4px);
}

.block-form {
  padding: 32px;
}

.block-title {
  font-size: 1.2rem;
  font-weight: 600;
  line-height: 1.5;
}

.block-identity {
  color: $color-blue-dark;
  font-weight: 500;
  margin-top: 16px;
  overflow-wrap: anywhere;
}

.block-description {
  margin-top: 12px;
  font-size: 0.875rem;
  line-height: 1.7;
  color: $color-secondary-text;
}

.block-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
}

.block-error {
  padding: 12px;
  border-radius: 10px;
  color: $color-red;
  background: $color-red-bg;
  margin-top: 16px;
  font-size: 0.875rem;
  line-height: 1.5;
}

.button:focus-visible {
  @include focus-ring(3px, 3px);
}

@media (max-width: 560px) {
  .block-form {
    padding: 24px;
  }

  .block-actions {
    flex-direction: column-reverse;
  }
}
</style>
