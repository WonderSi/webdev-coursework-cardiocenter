<template>
  <Sidebar />

  <main class="doctors-page">
    <h1>Врачи</h1>

    <p v-if="loading">Загрузка...</p>
    <p v-else-if="error" role="alert">{{ error }}</p>

    <template v-else>
      <p v-if="doctors.length === 0">Врачей пока нет.</p>

      <ul v-else>
        <li v-for="doctor in doctors" :key="doctor.id">
          {{ doctor.email }}
        </li>
      </ul>
    </template>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiFetch } from '@/api/client'
import Sidebar from '@/components/Sidebar.vue'

interface Doctor {
  id: number
  email: string
}

const doctors = ref<Doctor[]>([])
const loading = ref(true)
const error = ref('')

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
</style>
