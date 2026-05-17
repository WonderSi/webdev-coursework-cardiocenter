import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchDashboardStats, type DashboardStatsResponse } from '@/api/dashboard.api'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref<DashboardStatsResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function load() {
    if (stats.value) return
    loading.value = true
    error.value = null
    try {
      stats.value = await fetchDashboardStats()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Ошибка загрузки'
    } finally {
      loading.value = false
    }
  }

  return { stats, loading, error, load }
})
