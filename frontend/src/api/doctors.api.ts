import { apiFetch } from './client'
import { doctorAccessLevels } from '@/config/doctor-access'
import type {
  DoctorDto,
  DoctorDirectory,
  DoctorDraft,
  DoctorCapabilities,
} from '@/types/doctor'

const baseUrl = '/api/admin/super-admin'

async function checkResponse(response: Response, fallback: string): Promise<void> {
  if (response.ok) return
  if (response.status === 401) throw new Error('Сессия завершена. Войдите снова.')
  if (response.status === 403) throw new Error('Недостаточно прав для управления врачами.')
  if (response.status === 404 || response.status === 405) {
    throw new Error('Эта операция пока недоступна. Обновите список или обратитесь к администратору системы.')
  }
  if (response.status === 409) throw new Error('Этот email уже используется.')
  if (response.status === 400) {
    throw new Error('Проверьте данные. Email должен быть уникальным, пароль — от 12 до 100 символов.')
  }
  throw new Error(fallback)
}

function isDoctor(value: unknown): value is DoctorDto {
  if (!value || typeof value !== 'object') return false
  const doctor = value as Record<string, unknown>
  return typeof doctor.id === 'number' && Number.isSafeInteger(doctor.id) && doctor.id > 0
    && typeof doctor.email === 'string' && Boolean(doctor.email.trim())
    && typeof doctor.isBlocked === 'boolean'
    && (doctor.name === undefined || typeof doctor.name === 'string')
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
  if (doctors.some(doctor => (capabilities.profiles && !doctor.name?.trim())
    || (capabilities.accessLevels && !doctor.accessLevel))) {
    throw new Error('В списке врачей отсутствуют данные профиля или права доступа. Обратитесь к администратору системы.')
  }
  return { doctors, capabilities }
}

export async function persistDoctor(
  doctor: DoctorDto | null,
  draft: DoctorDraft,
  capabilities: DoctorCapabilities,
  signal?: AbortSignal,
): Promise<DoctorDto> {
  const payload = {
    name: draft.name.trim(),
    email: draft.email.trim(),
    accessLevel: draft.accessLevel,
    ...(draft.password ? { password: draft.password } : {}),
  }

  const isExisting = doctor !== null && doctor.id > 0
  const url = isExisting ? `${baseUrl}/doctors/${doctor.id}` : `${baseUrl}/doctors`
  const response = await apiFetch(url, {
    method: isExisting ? 'PUT' : 'POST',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  await checkResponse(response, 'Не удалось сохранить учётную запись. Попробуйте ещё раз.')
  const saved: unknown = await response.json()
  if (!isDoctor(saved)
    || (capabilities.profiles && !saved.name?.trim())
    || (capabilities.accessLevels && saved.accessLevel !== draft.accessLevel)) {
    throw new Error('Сервер не подтвердил сохранённые настройки. Закройте окно и обновите список перед повторной попыткой.')
  }
  return saved
}

export async function setDoctorBlocked(id: number, isBlocked: boolean, signal?: AbortSignal): Promise<void> {
  const response = await apiFetch(`${baseUrl}/doctors/${id}/blocked`, {
    method: 'PUT',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ isBlocked }),
  })
  await checkResponse(response, 'Не удалось изменить статус врача. Попробуйте ещё раз.')
}

export async function resetDoctorPassword(
  id: number,
  newPassword: string,
  signal?: AbortSignal,
): Promise<void> {
  const response = await apiFetch(`${baseUrl}/users/${id}/reset-password`, {
    method: 'POST',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newPassword }),
  })
  await checkResponse(response, 'Не удалось изменить пароль. Попробуйте ещё раз.')
}
