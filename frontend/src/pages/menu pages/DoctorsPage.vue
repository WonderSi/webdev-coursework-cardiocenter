<template>
  <Sidebar />

  <main class="doctors-page">
    <h1>Врачи</h1>

    <form class="doctor-form" @submit.prevent="createDoctor">
      <h2>Создать врача</h2>
      <label>Email <input v-model.trim="email" type="email" required maxlength="255" /></label>
      <label>Пароль <input v-model="password" type="password" required minlength="12" maxlength="100" autocomplete="new-password" /></label>
      <fieldset :disabled="creating">
        <legend>Доступные разделы</legend>
        <label v-for="section in adminSections" :key="section.code">
          <input v-model="newSections" type="checkbox" :value="section.code" /> {{ section.label }}
        </label>
      </fieldset>
      <button :disabled="creating || loading" type="submit">{{ creating ? 'Создание…' : 'Создать' }}</button>
      <p v-if="createError" role="alert">{{ createError }}</p>
    </form>

    <p v-if="loading">Загрузка...</p>
    <p v-else-if="error" role="alert">{{ error }}</p>

    <template v-else>
      <p v-if="doctors.length === 0">Врачей пока нет.</p>

      <ul v-else class="doctor-list">
        <li v-for="doctor in doctors" :key="doctor.id">
          <h2>{{ doctor.email }}</h2>
          <p v-if="doctor.isBlocked">Аккаунт заблокирован</p>
          <fieldset :disabled="saving === doctor.id">
            <legend>Доступные разделы</legend>
            <label v-for="section in adminSections" :key="section.code">
              <input v-model="doctor.allowedSections" type="checkbox" :value="section.code" /> {{ section.label }}
            </label>
          </fieldset>
          <button type="button" :disabled="saving !== null" @click="saveSections(doctor)">
            {{ saving === doctor.id ? 'Сохранение…' : 'Сохранить доступ' }}
          </button>
          <p v-if="messages[doctor.id]" role="status">{{ messages[doctor.id] }}</p>
        </li>
      </ul>
    </template>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiFetch } from '@/api/client'
import Sidebar from '@/components/Sidebar.vue'
import { adminSections, type AdminSection } from '@/types/admin-section'

interface Doctor {
  id: number
  email: string
  isBlocked: boolean
  allowedSections: AdminSection[]
}

const doctors = ref<Doctor[]>([])
const loading = ref(true)
const error = ref('')
const email = ref('')
const password = ref('')
const newSections = ref<AdminSection[]>([])
const creating = ref(false)
const createError = ref('')
const saving = ref<number | null>(null)
const messages = ref<Record<number, string>>({})

async function responseError(response: Response, fallback: string): Promise<Error> {
  if (response.status === 401) return new Error('Сессия завершена. Войдите снова.')
  if (response.status === 403) return new Error('Доступ запрещён')
  const body = await response.json().catch(() => null)
  return new Error(body?.detail ?? fallback)
}

async function createDoctor(): Promise<void> {
  creating.value = true
  createError.value = ''
  try {
    const response = await apiFetch('/api/admin/super-admin/doctors', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value, allowedSections: newSections.value }),
    })
    if (!response.ok) throw await responseError(response, 'Не удалось создать врача.')
    doctors.value.push(await response.json())
    email.value = ''
    password.value = ''
    newSections.value = []
  } catch (cause) {
    createError.value = cause instanceof Error ? cause.message : 'Не удалось создать врача.'
  } finally { creating.value = false }
}

async function saveSections(doctor: Doctor): Promise<void> {
  saving.value = doctor.id
  messages.value[doctor.id] = ''
  try {
    const response = await apiFetch(`/api/admin/super-admin/doctors/${doctor.id}/sections`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ allowedSections: doctor.allowedSections }),
    })
    if (!response.ok) throw await responseError(response, 'Не удалось сохранить доступ.')
    const saved: Doctor = await response.json()
    doctor.allowedSections = saved.allowedSections
    messages.value[doctor.id] = 'Доступ сохранён.'
  } catch (cause) {
    messages.value[doctor.id] = cause instanceof Error ? cause.message : 'Не удалось сохранить доступ.'
  } finally { saving.value = null }
}

onMounted(async () => {
  try {
    const response = await apiFetch('/api/admin/super-admin/doctors')

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Сессия завершена. Войдите снова.')
      }

      if (response.status === 403) {
        throw new Error('Недостаточно прав для просмотра врачей.')
      }

      throw new Error('Не удалось загрузить список врачей.')
    }

    doctors.value = await response.json()
  } catch (cause) {
    error.value =
      cause instanceof Error ? cause.message : 'Ошибка загрузки.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.doctors-page {
  margin-left: 240px;
  padding: 32px;
}
.doctor-form, .doctor-list li { max-width: 640px; margin-bottom: 24px; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
.doctor-form > label, fieldset label { display: block; margin: 8px 0; }
.doctor-form > label input { display: block; width: 100%; box-sizing: border-box; padding: 8px; }
fieldset { margin: 12px 0; }
button { padding: 8px 16px; cursor: pointer; }
.doctor-list { list-style: none; padding: 0; }
</style>
