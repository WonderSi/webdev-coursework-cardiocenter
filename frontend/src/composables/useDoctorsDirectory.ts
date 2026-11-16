import { onScopeDispose, readonly, shallowRef, watch } from 'vue'
import { doctorErrorMessage } from '@/utils/doctor-errors'
import type { Doctor, DoctorCapabilities, DoctorDirectory } from '@/types/doctor'

export function useDoctorsDirectory(
  canManage: () => boolean,
  fetchDirectory: (signal: AbortSignal) => Promise<DoctorDirectory>,
) {
  const doctors = shallowRef<Doctor[]>([])
  const capabilities = shallowRef<DoctorCapabilities>({ profiles: false, accessLevels: false })
  const loading = shallowRef(false)
  const loaded = shallowRef(false)
  const error = shallowRef('')
  let controller: AbortController | undefined
  let active = true

  function clear() {
    controller?.abort()
    doctors.value = []
    capabilities.value = { profiles: false, accessLevels: false }
    loading.value = false
    loaded.value = false
    error.value = ''
  }

  async function loadDoctors() {
    if (!active || !canManage()) return
    controller?.abort()
    const request = new AbortController()
    controller = request
    loading.value = true
    error.value = ''
    try {
      const directory = await fetchDirectory(request.signal)
      if (!active || request.signal.aborted || !canManage()) return
      doctors.value = directory.doctors
      capabilities.value = directory.capabilities
      loaded.value = true
    } catch (cause) {
      if (!active || request.signal.aborted) return
      error.value = doctorErrorMessage(cause, 'Не удалось загрузить врачей.')
    } finally {
      if (active && !request.signal.aborted) loading.value = false
    }
  }

  function upsertDoctor(doctor: Doctor, previousId = doctor.id) {
    if (!active || !canManage()) return
    const existing = doctors.value.some(item => item.id === previousId)
    doctors.value = existing
      ? doctors.value.map(item => item.id === previousId ? doctor : item)
      : [...doctors.value, doctor]
  }

  watch(canManage, allowed => {
    if (allowed) void loadDoctors()
    else clear()
  }, { immediate: true })

  onScopeDispose(() => {
    active = false
    controller?.abort()
  })

  return {
    doctors: readonly(doctors),
    capabilities: readonly(capabilities),
    loading: readonly(loading),
    loaded: readonly(loaded),
    error: readonly(error),
    loadDoctors,
    upsertDoctor,
  }
}
