<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, shallowRef, useTemplateRef, watch } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import DoctorEditorDialog from '@/components/doctors/DoctorEditorDialog.vue'
import { useUserStore } from '@/stores/user.store'
import {
  doctorAccessLevels, fetchDoctors, setDoctorBlocked,
  type Doctor, type DoctorAccessLevel, type DoctorCapabilities,
} from '@/api/doctors.api'

const userStore = useUserStore()
const doctors = shallowRef<Doctor[]>([])
const capabilities = shallowRef<DoctorCapabilities>({ profiles: false, accessLevels: false })
const loading = shallowRef(false)
const loaded = shallowRef(false)
const error = shallowRef('')
const feedback = shallowRef('')
const query = shallowRef('')
const statusFilter = shallowRef<'all' | 'active' | 'blocked'>('all')
const accessFilter = shallowRef<DoctorAccessLevel | 'all'>('all')
const editorOpen = shallowRef(false)
const editingDoctor = shallowRef<Doctor | null>(null)
const blockingDoctor = shallowRef<Doctor | null>(null)
const blockPending = shallowRef(false)
const blockError = shallowRef('')
const blockDialog = useTemplateRef<HTMLDialogElement>('blockDialog')
let loadController: AbortController | undefined

const activeCount = computed(() => doctors.value.filter(doctor => !doctor.isBlocked).length)
const statusOptions = computed(() => [
  { value: 'all' as const, label: 'Все', count: doctors.value.length },
  { value: 'active' as const, label: 'Активные', count: activeCount.value },
  { value: 'blocked' as const, label: 'Заблокированные', count: doctors.value.length - activeCount.value },
])
const hasFilters = computed(() => Boolean(query.value.trim()) || statusFilter.value !== 'all' || accessFilter.value !== 'all')
const visibleDoctors = computed(() => {
  const term = query.value.trim().toLocaleLowerCase('ru')
  return doctors.value.filter(doctor => {
    const matchesQuery = [doctor.name, doctor.username, doctor.email].some(value => value?.toLocaleLowerCase('ru').includes(term))
    const matchesStatus = statusFilter.value === 'all' || (statusFilter.value === 'blocked' ? doctor.isBlocked : !doctor.isBlocked)
    const matchesAccess = accessFilter.value === 'all' || doctor.accessLevel === accessFilter.value
    return matchesQuery && matchesStatus && matchesAccess
  }).sort((a, b) => (a.name || a.email).localeCompare(b.name || b.email, 'ru'))
})

function accessLabel(doctor: Doctor): string {
  if (!capabilities.value.accessLevels) return 'Полный доступ'
  return doctorAccessLevels.find(level => level.value === doctor.accessLevel)?.label ?? 'Не указан'
}

async function loadDoctors() {
  if (!userStore.isSuperAdmin) return
  loadController?.abort()
  const controller = new AbortController()
  loadController = controller
  loading.value = true
  error.value = ''
  feedback.value = ''
  try {
    const directory = await fetchDoctors(controller.signal)
    if (controller.signal.aborted) return
    doctors.value = directory.doctors
    capabilities.value = directory.capabilities
    loaded.value = true
    if (!directory.capabilities.accessLevels) accessFilter.value = 'all'
  } catch (cause) {
    if (controller.signal.aborted) return
    error.value = cause instanceof Error ? cause.message : 'Нет соединения с сервером. Попробуйте ещё раз.'
  } finally {
    if (!controller.signal.aborted) loading.value = false
  }
}

watch(() => userStore.isSuperAdmin, allowed => {
  if (allowed) void loadDoctors()
  else {
    loadController?.abort()
    doctors.value = []
    editorOpen.value = false
    blockDialog.value?.close()
  }
}, { immediate: true })
onBeforeUnmount(() => loadController?.abort())

function resetFilters() {
  query.value = ''
  statusFilter.value = 'all'
  accessFilter.value = 'all'
}

function openEditor(doctor: Doctor | null = null) {
  editingDoctor.value = doctor
  editorOpen.value = true
  feedback.value = ''
}

function onDoctorSaved(doctor: Doctor) {
  const isNew = !editingDoctor.value
  const existing = doctors.value.some(item => item.id === doctor.id)
  doctors.value = existing ? doctors.value.map(item => item.id === doctor.id ? doctor : item) : [...doctors.value, doctor]
  editorOpen.value = false
  // A new or renamed account should not disappear behind an old search.
  resetFilters()
  feedback.value = isNew ? `Учётная запись ${doctor.email} создана.` : `Изменения для ${doctor.email} сохранены.`
}

async function confirmBlock(doctor: Doctor) {
  blockingDoctor.value = doctor
  blockError.value = ''
  feedback.value = ''
  await nextTick()
  blockDialog.value?.showModal()
}

function closeBlockDialog() {
  if (blockPending.value) return
  blockDialog.value?.close()
  blockingDoctor.value = null
}

async function updateBlocked() {
  const doctor = blockingDoctor.value
  if (!doctor || blockPending.value || !userStore.isSuperAdmin) return
  blockPending.value = true
  blockError.value = ''
  try {
    const isBlocked = !doctor.isBlocked
    await setDoctorBlocked(doctor.id, isBlocked)
    doctors.value = doctors.value.map(item => item.id === doctor.id ? { ...item, isBlocked } : item)
    feedback.value = isBlocked ? `Учётная запись ${doctor.email} заблокирована.` : `Доступ для ${doctor.email} восстановлен.`
    blockDialog.value?.close()
    blockingDoctor.value = null
  } catch (cause) {
    blockError.value = cause instanceof Error ? cause.message : 'Нет соединения с сервером. Попробуйте ещё раз.'
  } finally {
    blockPending.value = false
  }
}
</script>

<template>
  <div class="doctors-shell">
    <Sidebar class="doctors-sidebar" />
    <main class="doctors-page">
      <header class="page-header">
        <div>
          <h1 class="page-title">Врачи</h1>
          <p class="page-description">Учётные записи и доступ к работе с данными пациентов.</p>
        </div>
        <button v-if="userStore.isSuperAdmin" type="button" class="button button-create" :disabled="loading || !loaded || Boolean(error)" @click="openEditor()">
          <span class="plus-icon" aria-hidden="true">+</span> Добавить врача
        </button>
      </header>

      <section v-if="!userStore.isSuperAdmin" class="directory-card state-panel" aria-labelledby="access-denied-title">
        <h2 id="access-denied-title" class="state-title">Доступ ограничен</h2>
        <p class="state-description">Управлять учётными записями врачей может только суперадминистратор.</p>
        <RouterLink to="/dashboards" class="text-link">Перейти к аналитике</RouterLink>
      </section>

      <template v-else>
        <p v-if="feedback" class="feedback-banner" role="status">{{ feedback }}</p>
        <section class="directory-card" aria-label="Список врачей" :aria-busy="loading">
          <div class="directory-heading">
            <div>
              <h2 class="section-title">Учётные записи</h2>
              <p class="section-description">Профиль можно заблокировать, сохранив все данные.</p>
            </div>
            <button type="button" class="text-button" :disabled="loading" @click="loadDoctors">{{ loading ? 'Обновление…' : 'Обновить список' }}</button>
          </div>

          <div class="directory-toolbar">
            <label class="search-field">
              <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5" /><path d="m16 16 4.5 4.5" /></svg>
              <span class="sr-only">Поиск по ФИО, логину или email</span>
              <input v-model="query" class="search-input" type="search" placeholder="ФИО, логин или email" :disabled="loading || !loaded" />
            </label>
            <label class="access-filter">
              <span class="sr-only">Фильтр по правам доступа</span>
              <select v-model="accessFilter" class="filter-select" :disabled="loading || !capabilities.accessLevels">
                <option value="all">Все уровни доступа</option>
                <option v-for="level in doctorAccessLevels" :key="level.value" :value="level.value">{{ level.label }}</option>
              </select>
            </label>
          </div>

          <div class="status-bar">
            <div class="status-filters" role="group" aria-label="Статус учётной записи">
              <button v-for="status in statusOptions" :key="status.value" type="button" class="status-filter" :class="{ selected: statusFilter === status.value }" :aria-pressed="statusFilter === status.value" :disabled="loading || !loaded" @click="statusFilter = status.value">
                {{ status.label }} <span class="filter-count">{{ loaded ? status.count : '—' }}</span>
              </button>
            </div>
            <span v-if="loaded && !loading && !error" class="result-count" role="status">Показано {{ visibleDoctors.length }} из {{ doctors.length }}</span>
          </div>

          <div v-if="loading" class="state-panel" role="status">
            <h3 class="state-title">Загружаем список врачей…</h3>
            <p class="state-description">Получаем учётные записи и настройки доступа.</p>
          </div>
          <div v-else-if="error" class="state-panel" role="alert">
            <h3 class="state-title">Список недоступен</h3>
            <p class="state-description">{{ error }}</p>
            <button type="button" class="button button-primary" @click="loadDoctors">Попробовать снова</button>
          </div>
          <div v-else-if="!visibleDoctors.length" class="state-panel">
            <h3 class="state-title">{{ hasFilters ? 'Врачи не найдены' : 'Добавьте первого врача' }}</h3>
            <p class="state-description">{{ hasFilters ? 'Измените запрос или сбросьте фильтры.' : 'Создайте учётную запись, чтобы врач мог войти в систему.' }}</p>
            <button v-if="hasFilters" type="button" class="button button-secondary" @click="resetFilters">Сбросить фильтры</button>
            <button v-else type="button" class="button button-primary" @click="openEditor()">Добавить врача</button>
          </div>
          <div v-else class="table-container" tabindex="0" role="region" aria-label="Учётные записи врачей">
            <table class="doctors-table">
              <caption class="sr-only">Имя, логин, email, права и статус врачей</caption>
              <thead class="table-head">
                <tr><th scope="col">Врач</th><th scope="col">Логин</th><th scope="col">Email</th><th scope="col">Права доступа</th><th scope="col">Статус</th><th scope="col"><span class="sr-only">Действия</span></th></tr>
              </thead>
              <tbody>
                <tr v-for="doctor in visibleDoctors" :key="doctor.id" class="doctor-row" :class="{ 'is-blocked': doctor.isBlocked }">
                  <td class="doctor-name-cell" data-label="Врач">
                    <div class="doctor-identity">
                      <span class="doctor-avatar" aria-hidden="true">{{ (doctor.name?.trim() || doctor.email).slice(0, 1).toLocaleUpperCase('ru') }}</span>
                      <div class="doctor-name-block"><span class="doctor-name">{{ doctor.name || 'Имя не указано' }}</span><span class="doctor-role">Администратор</span></div>
                    </div>
                  </td>
                  <td data-label="Логин"><span class="doctor-username">{{ doctor.username || '—' }}</span></td>
                  <td class="email-cell" data-label="Email"><span>{{ doctor.email }}</span></td>
                  <td data-label="Права доступа"><span class="access-badge">{{ accessLabel(doctor) }}</span></td>
                  <td data-label="Статус"><span class="status-badge" :class="doctor.isBlocked ? 'status-blocked' : 'status-active'"><span class="status-dot" aria-hidden="true"></span>{{ doctor.isBlocked ? 'Заблокирован' : 'Активен' }}</span></td>
                  <td class="actions-cell">
                    <div class="row-actions">
                      <button type="button" class="row-button" :aria-label="`Редактировать ${doctor.name || doctor.email}`" @click="openEditor(doctor)">Редактировать</button>
                      <button type="button" class="row-button" :class="{ 'row-button-danger': !doctor.isBlocked }" :aria-label="`${doctor.isBlocked ? 'Разблокировать' : 'Заблокировать'} ${doctor.name || doctor.email}`" @click="confirmBlock(doctor)">{{ doctor.isBlocked ? 'Разблокировать' : 'Заблокировать' }}</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="directory-footer">
            <span class="footer-lock" aria-hidden="true"><svg width="16" height="18" viewBox="0 0 20 22" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="9" width="14" height="10" rx="2" /><path d="M6 9V6a4 4 0 0 1 8 0v3" /><path d="M10 13v3" /></svg></span>
            Управление врачами доступно только суперадминистратору.
          </footer>
        </section>

        <details class="access-guide">
          <summary class="guide-summary">Что входит в уровни доступа</summary>
          <div class="guide-levels">
            <div v-for="level in doctorAccessLevels" :key="level.value" class="guide-level">
              <h3 class="guide-title">{{ level.label }}</h3>
              <p class="guide-description">{{ level.description }}.</p>
            </div>
          </div>
          <p class="guide-note">Доступ к управлению врачами не входит ни в один из этих уровней.</p>
        </details>
        <p v-if="loaded && !error && (!capabilities.profiles || !capabilities.accessLevels)" class="legacy-note">
          <template v-if="!capabilities.profiles">Система пока хранит только email врача. Имя и отдельный логин будут доступны после обновления учётных записей. </template>
          <template v-if="!capabilities.accessLevels">Сейчас все врачи имеют полный доступ к данным и обучению моделей; ограничение прав пока недоступно.</template>
        </p>

        <DoctorEditorDialog v-if="editorOpen" :doctor="editingDoctor" :capabilities="capabilities" @close="editorOpen = false" @saved="onDoctorSaved" />
        <dialog ref="blockDialog" class="block-dialog" aria-labelledby="block-title" aria-describedby="block-description" @cancel.prevent="closeBlockDialog">
          <form class="block-form" :aria-busy="blockPending" @submit.prevent="updateBlocked">
            <h2 id="block-title" class="block-title">{{ blockingDoctor?.isBlocked ? 'Восстановить доступ?' : 'Заблокировать учётную запись?' }}</h2>
            <p class="block-identity">{{ blockingDoctor?.name || blockingDoctor?.email }}</p>
            <p id="block-description" class="block-description">{{ blockingDoctor?.isBlocked ? 'Врач снова сможет войти в систему с прежними правами доступа.' : 'Врач не сможет войти в систему или продолжить работу в текущей сессии. Учётная запись и данные пациентов сохранятся.' }}</p>
            <p v-if="blockError" class="block-error" role="alert">{{ blockError }}</p>
            <div class="block-actions">
              <button type="button" class="button button-secondary" autofocus :disabled="blockPending" @click="closeBlockDialog">Отмена</button>
              <button type="submit" class="button" :class="blockingDoctor?.isBlocked ? 'button-primary' : 'button-danger'" :disabled="blockPending">{{ blockPending ? 'Сохранение…' : blockingDoctor?.isBlocked ? 'Восстановить доступ' : 'Заблокировать' }}</button>
            </div>
          </form>
        </dialog>
      </template>
    </main>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.doctors-shell { width: 100%; min-height: 100vh; }
.doctors-sidebar :deep(.username) { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.doctors-sidebar :deep(.user-profile > .icon), .doctors-sidebar :deep(.logout-btn) { flex-shrink: 0; }
.doctors-page { margin-left: 240px; padding: 40px clamp(24px, 3.2vw, 64px); min-width: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 24px; margin-bottom: 28px; }
.page-title { font-size: 2rem; font-weight: 600; line-height: 1.2; color: $color-white; }
.page-description { margin-top: 10px; font-size: 0.9375rem; color: $color-white; line-height: 1.6; max-width: 65ch; }
.button { display: inline-flex; align-items: center; justify-content: center; gap: 8px; border: 1px solid transparent; border-radius: $radius-rect-buttons; padding: 12px 18px; font: inherit; font-size: 0.875rem; font-weight: 600; cursor: pointer; }
.button-create { background: $color-white; color: $color-accent; flex-shrink: 0; }
.button-create:hover:not(:disabled) { background: #edf4ff; }
.plus-icon { font-size: 1.5rem; line-height: 0.8; font-weight: 400; }
.button-primary { background: $color-accent; color: $color-white; }
.button-primary:hover:not(:disabled) { background: #1055be; }
.button-secondary { background: transparent; border-color: #c6cedd; color: $color-blue-dark; }
.button-secondary:hover:not(:disabled) { background: #edf4ff; }
.button-danger { background: #c51c3a; color: $color-white; }
.button-danger:hover:not(:disabled) { background: #aa1731; }
.button:disabled, .text-button:disabled, .status-filter:disabled { opacity: 0.55; cursor: default; }
.directory-card { overflow: hidden; background: rgba($color-glass-white, 0.92); border: 1px solid rgba($color-white, 0.5); border-radius: $radius-glass-card; backdrop-filter: blur(20px); }
.directory-heading { display: flex; justify-content: space-between; align-items: center; gap: 24px; padding: 28px 28px 24px; }
.section-title { color: $color-blue-dark; font-size: 1.25rem; font-weight: 600; }
.section-description { color: $color-secondary-text; font-size: 0.875rem; line-height: 1.5; margin-top: 8px; }
.text-button, .text-link { background: transparent; border: 0; padding: 6px 0; color: $color-accent; font: inherit; font-size: 0.875rem; font-weight: 500; cursor: pointer; text-underline-offset: 3px; }
.text-button:hover:not(:disabled) { text-decoration: underline; }
.directory-toolbar { display: flex; gap: 16px; padding: 0 28px 24px; }
.search-field { flex: 1; display: flex; align-items: center; gap: 10px; min-width: 0; border: 1px solid #c6cedd; border-radius: 10px; padding: 0 14px; background: rgba($color-white, 0.75); }
.search-icon { color: $color-secondary-text; flex-shrink: 0; }
.search-input { border: 0; background: transparent; color: $color-text; padding: 12px 0; font: inherit; font-size: 0.875rem; min-width: 0; width: 100%; }
.search-input::placeholder { color: #697185; }
.search-input:focus { outline: none; }
.search-field:focus-within { outline: 2px solid $color-accent; outline-offset: 2px; }
.filter-select { width: 100%; min-height: 43px; padding: 10px 32px 10px 14px; border: 1px solid #c6cedd; border-radius: 10px; color: $color-blue-dark; background: rgba($color-white, 0.75); font: inherit; font-size: 0.875rem; }
.filter-select:disabled { color: $color-secondary-text; background: #f1f4f9; }
.access-filter { flex: 0 1 255px; min-width: 180px; }
.status-bar { padding: 0 28px 16px; display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.status-filters { display: flex; flex-wrap: wrap; gap: 4px; }
.status-filter { display: inline-flex; align-items: center; gap: 8px; border: 0; padding: 9px 12px; border-radius: 9px; background: transparent; color: $color-secondary-text; font: inherit; font-size: 0.8125rem; cursor: pointer; }
.status-filter.selected { background: #e1ecfd; color: $color-blue-dark; font-weight: 600; }
.status-filter:hover:not(:disabled) { background: #e9eff8; }
.filter-count { font-variant-numeric: tabular-nums; font-size: 0.75rem; }
.result-count { color: $color-secondary-text; font-size: 0.75rem; }
.table-container { overflow-x: auto; }
.doctors-table { border-collapse: collapse; width: 100%; text-align: left; font-size: 0.8125rem; }
.table-head { background: rgba($color-accent-lighter2, 0.28); }
.table-head th { padding: 14px 16px; font-weight: 500; color: $color-secondary-text; white-space: nowrap; }
.doctor-row td { padding: 20px 16px; border-bottom: 1px solid #e0e6f0; vertical-align: middle; }
.doctors-table th:first-child, .doctor-row td:first-child { padding-left: 28px; }
.doctors-table th:last-child, .doctor-row td:last-child { padding-right: 28px; }
.doctor-row:last-child td { border-bottom: 0; }
.doctor-row:hover { background: rgba($color-white, 0.5); }
.doctor-identity { display: flex; gap: 12px; align-items: center; min-width: 170px; }
.doctor-avatar { display: grid; place-items: center; flex-shrink: 0; width: 38px; height: 38px; border-radius: 12px; color: $color-accent; background: #dfebfe; font-size: 1rem; font-weight: 600; }
.is-blocked .doctor-avatar { background: #e6e9ef; color: $color-secondary-text; }
.doctor-name-block { display: flex; flex-direction: column; gap: 5px; }
.doctor-name { color: $color-blue-dark; font-weight: 600; line-height: 1.5; overflow-wrap: anywhere; min-width: 100px; max-width: 260px; }
.doctor-role { color: $color-secondary-text; font-size: 0.75rem; }
.doctor-username { overflow-wrap: anywhere; }
.email-cell { overflow-wrap: anywhere; min-width: 140px; max-width: 240px; }
.access-badge { color: $color-blue-dark; line-height: 1.5; }
.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 6px 9px; border-radius: 7px; font-size: 0.75rem; white-space: nowrap; }
.status-active { background: #e4f2e9; color: #176936; }
.status-blocked { background: #fce8ed; color: #ad1d37; }
.status-dot { width: 5px; height: 5px; border-radius: 50%; background: currentColor; }
.row-actions { display: flex; flex-direction: column; align-items: flex-start; gap: 6px; }
.row-button { padding: 4px 0; color: $color-accent; background: transparent; border: 0; font: inherit; font-size: 0.8125rem; cursor: pointer; }
.row-button:hover { text-decoration: underline; text-underline-offset: 3px; }
.row-button-danger { color: #b01836; }
.directory-footer { display: flex; align-items: center; gap: 10px; padding: 18px 28px; border-top: 1px solid #dce4ef; color: $color-secondary-text; font-size: 0.75rem; line-height: 1.5; background: rgba($color-white, 0.25); }
.footer-lock { display: flex; flex-shrink: 0; color: $color-blue-dark; }
.state-panel { display: flex; align-items: center; flex-direction: column; gap: 12px; text-align: center; padding: 56px 28px; }
.state-title { color: $color-blue-dark; font-size: 1.125rem; font-weight: 600; }
.state-description { color: $color-secondary-text; font-size: 0.875rem; line-height: 1.6; max-width: 55ch; }
.state-panel .button { margin-top: 8px; }
.feedback-banner { padding: 14px 20px; border-radius: 12px; background: #e4f2e9; color: #176936; margin-bottom: 20px; font-size: 0.875rem; line-height: 1.6; overflow-wrap: anywhere; }
.access-guide { margin-top: 24px; border: 1px solid rgba($color-white, 0.55); background: rgba($color-white, 0.8); border-radius: 20px; padding: 20px 28px; }
.guide-summary { color: $color-blue-dark; font-size: 0.875rem; font-weight: 600; cursor: pointer; }
.guide-levels { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 24px; }
.guide-level + .guide-level { border-left: 1px solid #c6d4e7; padding-left: 24px; }
.guide-title { font-size: 0.875rem; font-weight: 600; color: $color-blue-dark; }
.guide-description { font-size: 0.8125rem; line-height: 1.6; margin-top: 8px; color: $color-secondary-text; }
.guide-note { margin-top: 20px; color: $color-secondary-text; font-size: 0.75rem; line-height: 1.5; }
.legacy-note { padding: 16px 20px; margin-top: 16px; font-size: 0.8125rem; line-height: 1.7; color: $color-blue-dark; background: rgba($color-white, 0.85); border-radius: 12px; }
.block-dialog { position: fixed; inset: 0; margin: auto; border: 0; padding: 0; border-radius: 24px; width: min(480px, calc(100% - 32px)); max-height: calc(100dvh - 32px); background: $color-white; color: $color-text; box-shadow: 0 24px 80px rgba($color-blue-dark, 0.25); }
.block-dialog::backdrop { background: rgba($color-blue-dark, 0.45); backdrop-filter: blur(4px); }
.block-form { padding: 32px; }
.block-title { font-size: 1.375rem; font-weight: 600; line-height: 1.4; }
.block-identity { color: $color-blue-dark; font-weight: 500; margin-top: 16px; overflow-wrap: anywhere; }
.block-description { margin-top: 12px; font-size: 0.875rem; line-height: 1.7; color: $color-secondary-text; }
.block-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 28px; }
.block-error { padding: 12px; border-radius: 10px; color: #b01835; background: $color-red-bg; margin-top: 16px; font-size: 0.875rem; line-height: 1.5; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; border: 0; }
.button:focus-visible, .text-button:focus-visible, .status-filter:focus-visible, .row-button:focus-visible, .filter-select:focus-visible, .guide-summary:focus-visible, .text-link:focus-visible, .table-container:focus-visible { outline: 3px solid $color-accent; outline-offset: 3px; }
@media (max-width: 1100px) {
  .doctors-page { padding: 32px 24px; }
  .doctors-table { min-width: 900px; }
  .page-header { align-items: flex-start; }
}
@media (max-width: 768px) {
  .doctors-shell .doctors-sidebar { position: relative; width: 100%; height: auto; padding: 20px; border-right: 0; border-bottom: 1px solid rgba($color-white, 0.5); }
  .doctors-sidebar :deep(.divider) { margin: 16px 0; }
  .doctors-sidebar :deep(.main-menu) { flex-direction: row; flex-wrap: wrap; gap: 16px; }
  .doctors-sidebar :deep(.sidebar-footer) { margin: 24px 0 0; }
  .doctors-sidebar :deep(.user-profile) { justify-content: flex-start; gap: 12px; min-width: 0; }
  .doctors-sidebar :deep(.username) { overflow-wrap: anywhere; min-width: 0; }
  .doctors-page { margin-left: 0; padding: 28px 16px; }
  .directory-toolbar { flex-direction: column; }
  .access-filter { flex-basis: auto; }
  .page-header { flex-wrap: wrap; gap: 20px; }
  .guide-levels { grid-template-columns: 1fr; gap: 16px; }
  .guide-level + .guide-level { padding: 16px 0 0; border-left: 0; border-top: 1px solid #c6d4e7; }
}
@media (max-width: 560px) {
  .directory-heading { align-items: flex-start; flex-direction: column; gap: 12px; padding: 24px 20px 20px; }
  .directory-toolbar { padding: 0 20px 20px; }
  .status-bar { padding: 0 14px 16px; }
  .status-filters { gap: 0; }
  .status-filter { gap: 6px; padding: 9px; font-size: 0.75rem; }
  .result-count { padding-left: 6px; }
  .doctors-table { min-width: 0; display: block; }
  .table-head { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); }
  .doctors-table tbody { display: block; }
  .doctor-row { display: grid; grid-template-columns: 1fr; padding: 20px; border-top: 1px solid #dce4ef; }
  .doctor-row td, .doctor-row td:first-child, .doctor-row td:last-child { display: flex; justify-content: space-between; gap: 16px; align-items: center; padding: 8px 0; border: 0; max-width: none; min-width: 0; }
  .doctor-row td[data-label]::before { content: attr(data-label); color: $color-secondary-text; font-size: 0.75rem; flex-shrink: 0; }
  .doctor-row .doctor-name-cell { padding: 0 0 12px; }
  .doctor-row .doctor-name-cell::before { display: none; }
  .doctor-name { max-width: none; }
  .doctor-identity { min-width: 0; }
  .email-cell span, .doctor-username, .access-badge { min-width: 0; text-align: right; }
  .doctor-row .actions-cell { margin-top: 12px; padding-top: 16px; border-top: 1px solid #dce4ef; }
  .row-actions { width: 100%; flex-direction: row; justify-content: space-between; gap: 16px; }
  .directory-footer { padding: 18px 20px; align-items: flex-start; }
  .access-guide { padding: 20px; }
  .block-form { padding: 24px; }
  .block-actions { flex-direction: column-reverse; }
}
</style>
