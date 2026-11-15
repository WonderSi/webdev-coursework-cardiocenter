import { apiFetch } from './client'

export type DoctorAccessLevel = 'VIEW' | 'EDIT' | 'RETRAIN'

export const doctorAccessLevels = [
  { value: 'VIEW', label: 'Просмотр', description: 'Данные пациентов и аналитика' },
  { value: 'EDIT', label: 'Редактирование', description: 'Данные, аналитика и загрузка пациентов' },
  { value: 'RETRAIN', label: 'Полный', description: 'Данные, загрузка, метрики и переобучение моделей' },
] as const

export interface Doctor {
  id: number
  email: string
  isBlocked: boolean
  name?: string
  username?: string
  accessLevel?: DoctorAccessLevel
}

export interface DoctorCapabilities {
  profiles: boolean
  accessLevels: boolean
}

export interface DoctorDirectory {
  doctors: Doctor[]
  capabilities: DoctorCapabilities
}

export interface DoctorDraft {
  name: string
  username: string
  email: string
  password: string
  accessLevel: DoctorAccessLevel
}

const baseUrl = '/api/admin/super-admin'

async function checkResponse(response: Response, fallback: string): Promise<void> {
  if (response.ok) return
  if (response.status === 401) throw new Error('Сессия завершена. Войдите снова.')
  if (response.status === 403) throw new Error('Недостаточно прав для управления врачами.')
  if (response.status === 404 || response.status === 405) {
    throw new Error('Эта операция пока недоступна. Обновите список или обратитесь к администратору системы.')
  }
  if (response.status === 409) throw new Error('Этот логин или email уже используется.')
  if (response.status === 400) throw new Error('Проверьте данные. Логин и email должны быть уникальными, пароль — от 12 до 100 символов.')
  throw new Error(fallback)
}

function isDoctor(value: unknown): value is Doctor {
  if (!value || typeof value !== 'object') return false
  const doctor = value as Record<string, unknown>
  return typeof doctor.id === 'number' && typeof doctor.email === 'string'
    && typeof doctor.isBlocked === 'boolean'
    && (doctor.name === undefined || typeof doctor.name === 'string')
    && (doctor.username === undefined || typeof doctor.username === 'string')
    && (doctor.accessLevel === undefined || doctorAccessLevels.some(level => level.value === doctor.accessLevel))
}

export async function fetchDoctors(signal?: AbortSignal): Promise<DoctorDirectory> {
  const response = await apiFetch(`${baseUrl}/doctors`, { signal })
  await checkResponse(response, 'Не удалось загрузить врачей. Попробуйте ещё раз.')
  const data: unknown = await response.json()
  // The current API returns an array. An explicit capability envelope enables
  // the extended contract without silently discarding fields on the old API.
  const directory = data as Partial<DoctorDirectory> | null
  const doctors = Array.isArray(data) ? data : directory?.doctors
  if (!Array.isArray(doctors) || !doctors.every(isDoctor)) {
    throw new Error('Список врачей получен в неверном формате. Обратитесь к администратору системы.')
  }
  const capabilities = {
    profiles: !Array.isArray(data) && directory?.capabilities?.profiles === true,
    accessLevels: !Array.isArray(data) && directory?.capabilities?.accessLevels === true,
  }
  if (doctors.some(doctor => (capabilities.profiles && (!doctor.name?.trim() || !doctor.username?.trim()))
    || (capabilities.accessLevels && !doctor.accessLevel))) {
    throw new Error('В списке врачей отсутствуют данные профиля или права доступа. Обратитесь к администратору системы.')
  }
  return { doctors, capabilities }
}

export async function saveDoctor(
  doctor: Doctor | null,
  draft: DoctorDraft,
  capabilities: DoctorCapabilities,
): Promise<Doctor> {
  if (doctor && !capabilities.profiles && !capabilities.accessLevels) {
    // Password reset is supported by the existing API, independently of profiles.
    const response = await apiFetch(`${baseUrl}/users/${doctor.id}/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ newPassword: draft.password }),
    })
    await checkResponse(response, 'Не удалось изменить пароль. Попробуйте ещё раз.')
    return doctor
  }

  const payload = {
    ...(!doctor || capabilities.profiles ? { email: draft.email.trim() } : {}),
    ...(capabilities.profiles ? { name: draft.name.trim(), username: draft.username.trim() } : {}),
    ...(capabilities.accessLevels ? { accessLevel: draft.accessLevel } : {}),
    ...(draft.password ? { password: draft.password } : {}),
  }
  const response = await apiFetch(doctor ? `${baseUrl}/doctors/${doctor.id}` : `${baseUrl}/doctors`, {
    method: doctor ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  await checkResponse(response, 'Не удалось сохранить учётную запись. Попробуйте ещё раз.')
  const saved: unknown = await response.json()
  if (!isDoctor(saved)
    || (capabilities.profiles && (!saved.name?.trim() || !saved.username?.trim()))
    || (capabilities.accessLevels && saved.accessLevel !== draft.accessLevel)) {
    throw new Error('Сервер не подтвердил сохранённые настройки. Закройте окно и обновите список перед повторной попыткой.')
  }
  return saved
}

export async function setDoctorBlocked(id: number, isBlocked: boolean): Promise<void> {
  const response = await apiFetch(`${baseUrl}/doctors/${id}/blocked`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ isBlocked }),
  })
  await checkResponse(response, 'Не удалось изменить статус врача. Попробуйте ещё раз.')
}
