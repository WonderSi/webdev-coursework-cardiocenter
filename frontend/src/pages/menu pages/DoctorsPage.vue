<script setup lang="ts">
import { computed, shallowRef, useTemplateRef, watch } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import DoctorsHelpPanel from '@/components/doctors/DoctorsHelpPanel.vue'
import DoctorsDirectory from '@/components/doctors/DoctorsDirectory.vue'
import DoctorEditorDialog from '@/components/doctors/DoctorEditorDialog.vue'
import DoctorBlockDialog from '@/components/doctors/DoctorBlockDialog.vue'
import { fetchDoctors } from '@/api/doctors.api'
import { useDoctorsDirectory } from '@/composables/useDoctorsDirectory'
import { useUserStore } from '@/stores/user.store'
import type { Doctor } from '@/types/doctor'

const userStore = useUserStore()
const { doctors, capabilities, loading, loaded, error, loadDoctors, upsertDoctor } = useDoctorsDirectory(
  () => userStore.isSuperAdmin,
  fetchDoctors,
)
const directory = useTemplateRef<InstanceType<typeof DoctorsDirectory>>('directory')
const helpButton = useTemplateRef<HTMLButtonElement>('helpButton')
const isHelpOpen = shallowRef(false)
const editorOpen = shallowRef(false)
const editingDoctor = shallowRef<Doctor | null>(null)
const blockingDoctor = shallowRef<Doctor | null>(null)
const occupiedEmails = computed(() => [
  userStore.email ?? '',
  ...doctors.value.map(doctor => doctor.persistedEmail ?? doctor.email),
])

watch(() => userStore.isSuperAdmin, allowed => {
  if (!allowed) {
    closeEditor()
    blockingDoctor.value = null
    isHelpOpen.value = false
  }
})

function closeHelp() {
  isHelpOpen.value = false
  helpButton.value?.focus()
}

function openEditor(doctor: Doctor | null = null) {
  if (!userStore.isSuperAdmin) return
  editingDoctor.value = doctor
  editorOpen.value = true
}

function closeEditor() {
  editorOpen.value = false
  editingDoctor.value = null
}

function onDoctorSaved(doctor: Doctor) {
  upsertDoctor(doctor, editingDoctor.value?.id)
  closeEditor()
  directory.value?.resetFilters()
  void directory.value?.focusFallback()
}

function onDoctorBlocked(doctor: Doctor) {
  upsertDoctor(doctor)
  blockingDoctor.value = null
  void directory.value?.focusFallback()
}
</script>

<template>
  <Sidebar class="doctors-sidebar" />
  <main class="doctors-page">
    <section class="page-main-card" aria-labelledby="doctors-page-title">
      <header class="page-header">
        <h1 id="doctors-page-title" class="page-title">Управление аккаунтами врачей</h1>
        <button
          ref="helpButton"
          type="button"
          class="info-btn"
          :aria-expanded="isHelpOpen"
          aria-controls="doctors-page-help"
          @click="isHelpOpen = !isHelpOpen"
        >
          <svg
            class="info-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
            focusable="false"
          >
            <circle
              cx="12"
              cy="12"
              r="9"
            />
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
            <circle
              cx="12"
              cy="17"
              r="0.75"
              fill="currentColor"
              stroke="none"
            />
          </svg>
          <span>Справка (как пользоваться страницей)</span>
        </button>
      </header>
      <DoctorsHelpPanel v-if="isHelpOpen" @close="closeHelp" />
      <section
        v-if="!userStore.isSuperAdmin"
        class="directory-card state-panel"
        aria-labelledby="access-denied-title"
      >
        <h2 id="access-denied-title" class="state-title">Доступ ограничен</h2>
        <p class="state-description">Управлять учётными записями врачей может только суперадминистратор.</p>
        <RouterLink to="/dashboards" class="text-link">Перейти к аналитике</RouterLink>
      </section>
      <template v-else>
        <DoctorsDirectory
          ref="directory"
          :doctors="doctors"
          :capabilities="capabilities"
          :loading="loading"
          :loaded="loaded"
          :error="error"
          @refresh="loadDoctors"
          @create="openEditor()"
          @edit="openEditor"
          @block="blockingDoctor = $event"
        />
        <DoctorEditorDialog
          v-if="editorOpen"
          :doctor="editingDoctor"
          :capabilities="capabilities"
          @close="closeEditor"
          :occupied-emails="occupiedEmails"
          @saved="onDoctorSaved"
          @updated="upsertDoctor"
        />
        <DoctorBlockDialog
          v-if="blockingDoctor"
          :doctor="blockingDoctor"
          @close="blockingDoctor = null"
          @updated="onDoctorBlocked"
        />
      </template>
    </section>
  </main>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.doctors-page {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-self: flex-start;
  width: calc(100% - 240px);
  min-width: 0;
  margin-left: 240px;
  padding: 28px 32px;
}

.page-main-card {
  flex: 1;
  min-width: 0;
  min-height: calc(100dvh - 56px);
  padding: 24px;
  border: 1px solid rgba($color-white, 0.5);
  border-radius: $radius-glass-card;
  background: rgba($color-white, 0.65);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px 24px;
  margin-bottom: 24px;
}

.page-title {
  min-width: 0;
  color: $color-text;
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.3;
  overflow-wrap: anywhere;
}

.info-btn {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  gap: 8px;
  max-width: 100%;
  padding: 10px 14px;
  border: 0;
  border-radius: $radius-rect-buttons;
  background: $color-white;
  color: $color-blue-dark;
  font: inherit;
  font-size: 0.9rem;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
}

.info-btn:hover {
  background: $color-ui-surface-hover;
}

.info-icon {
  display: block;
  flex: 0 0 20px;
  width: 20px;
  height: 20px;
}

.info-btn:focus-visible {
  @include focus-ring(3px, 3px);
}

.doctors-sidebar :deep(.username) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doctors-sidebar :deep(.user-profile > .icon),
.doctors-sidebar :deep(.logout-btn) {
  flex-shrink: 0;
}

.directory-card {
  overflow: hidden;
  background: rgba($color-glass-white, 0.92);
  border: 1px solid rgba($color-white, 0.5);
  border-radius: $radius-glass-card;
}

.text-link {
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

.text-link:focus-visible {
  @include focus-ring(3px, 3px);
}

@media (max-width: 768px) {
  .doctors-sidebar.sidebar-glass {
    position: relative;
    width: 100%;
    height: auto;
    padding: 20px;
    border-right: 0;
    border-bottom: 1px solid rgba($color-white, 0.5);
  }

  .doctors-sidebar :deep(.divider) {
    margin: 16px 0;
  }

  .doctors-sidebar :deep(.main-menu) {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
  }

  .doctors-sidebar :deep(.sidebar-footer) {
    margin: 24px 0 0;
  }

  .doctors-sidebar :deep(.user-profile) {
    justify-content: flex-start;
    gap: 12px;
    min-width: 0;
  }

  .doctors-sidebar :deep(.username) {
    overflow-wrap: anywhere;
    min-width: 0;
  }

  .doctors-page {
    width: 100%;
    margin-left: 0;
    padding: 16px;
  }

  .page-main-card {
    min-height: 0;
    padding: 20px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

@media (max-width: 560px) {
  .page-main-card {
    padding: 16px;
  }

  .page-title {
    font-size: 1.25rem;
  }
}
</style>
