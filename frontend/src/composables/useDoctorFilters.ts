import { computed, shallowRef } from 'vue'
import { effectiveDoctorAccess } from '@/config/doctor-access'
import type { Doctor, DoctorAccessState, DoctorCapabilities, DoctorRow } from '@/types/doctor'

export function useDoctorFilters(
  doctors: () => readonly Doctor[],
  capabilities: () => DoctorCapabilities,
) {
  const query = shallowRef('')
  const statusFilter = shallowRef<'all' | 'active' | 'blocked'>('all')
  const accessFilter = shallowRef<DoctorAccessState | 'all'>('all')
  const collator = new Intl.Collator('ru')

  // Сортировка и представление данных меняется только когда меняются записи или возможности доктора
  const sortedRows = computed(() => {
    const rows = doctors().map(doctor => ({
      doctor,
      displayName: doctor.name || 'Имя не указано',
      initials: (doctor.name?.trim() || doctor.email).slice(0, 1).toLocaleUpperCase('ru'),
      accessLevel: effectiveDoctorAccess(doctor, capabilities()),
      searchText: [doctor.name, doctor.email].filter(Boolean).join(' ').toLocaleLowerCase('ru'),
    }))
    return rows.sort((a, b) => collator.compare(
      a.doctor.name || a.doctor.email,
      b.doctor.name || b.doctor.email,
    ))
  })

  const visibleRows = computed<DoctorRow[]>(() => {
    const term = query.value.trim().toLocaleLowerCase('ru')
    return sortedRows.value.filter(row => {
      const matchesQuery = !term || row.searchText.includes(term)
      const matchesStatus = statusFilter.value === 'all'
        || (statusFilter.value === 'blocked' ? row.doctor.isBlocked : !row.doctor.isBlocked)
      return matchesQuery && matchesStatus
        && (accessFilter.value === 'all' || row.accessLevel === accessFilter.value)
    })
  })
  const activeCount = computed(() => doctors().filter(doctor => !doctor.isBlocked).length)
  const statusOptions = computed(() => [
    { value: 'all' as const, label: 'Все', count: doctors().length },
    { value: 'active' as const, label: 'Активные', count: activeCount.value },
    { value: 'blocked' as const, label: 'Заблокированные', count: doctors().length - activeCount.value },
  ])
  const hasFilters = computed(() => Boolean(query.value.trim())
    || statusFilter.value !== 'all'
    || accessFilter.value !== 'all')

  function resetFilters() {
    query.value = ''
    statusFilter.value = 'all'
    accessFilter.value = 'all'
  }

  return {
    query,
    statusFilter,
    accessFilter,
    visibleRows,
    statusOptions,
    hasFilters,
    resetFilters,
  }
}
