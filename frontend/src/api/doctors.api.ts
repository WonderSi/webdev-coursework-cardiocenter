import { apiFetch } from './client'
import { adminSections } from '@/types/admin-section'
import type { DoctorDto, DoctorDirectory } from '@/types/doctor'
import type { AdminSection } from '@/types/admin-section'

const baseUrl = '/api/admin/super-admin'

async function checkResponse(response: Response, fallback: string): Promise<void> {
  if (response.ok) return
  if (response.status === 401) throw new Error('Сессия завершена. Войдите снова.')
  if (response.status === 403) throw new Error('Недостаточно прав для управления врачами.')
  if (response.status === 404 || response.status === 405) {
    throw new Error('Эта операция пока недоступна. Обновите список или обратитесь к администратору системы.')
  }
  if (response.status === 409) throw new Error('Этот email уже используется.')
  const body: unknown = await response.json().catch(() => null)
  if (body && typeof body === 'object' && 'detail' in body && typeof body.detail === 'string') {
    throw new Error(body.detail)
  }
  throw new Error(fallback)
}

function isDoctor(value: unknown): value is DoctorDto {
  if (!value || typeof value !== 'object') return false
  const doctor = value as Record<string, unknown>
  return typeof doctor.id === 'number' && Number.isSafeInteger(doctor.id) && doctor.id > 0
    && typeof doctor.email === 'string' && Boolean(doctor.email.trim())
    && typeof doctor.isBlocked === 'boolean'
    && Array.isArray(doctor.allowedSections)
    && doctor.allowedSections.every(section => adminSections.some(item => item.code === section))
}

async function readDoctor(response: Response, fallback: string): Promise<DoctorDto> {
  await checkResponse(response, fallback)
  const doctor: unknown = await response.json()
  if (!isDoctor(doctor)) {
    throw new Error('Сервер вернул неверные данные врача. Обновите список перед повторной попыткой.')
  }
  return doctor
}

export async function fetchDoctors(signal?: AbortSignal): Promise<DoctorDirectory> {
  const response = await apiFetch(`${baseUrl}/doctors`, { signal })
  await checkResponse(response, 'Не удалось загрузить врачей. Попробуйте ещё раз.')
  const doctors: unknown = await response.json()
  if (!Array.isArray(doctors) || !doctors.every(isDoctor)) {
    throw new Error('Список врачей получен в неверном формате. Обратитесь к администратору системы.')
  }
  return { doctors, capabilities: { profiles: false, accessLevels: true } }
}

export async function createDoctor(
  email: string,
  password: string,
  allowedSections: readonly AdminSection[],
  signal?: AbortSignal,
): Promise<DoctorDto> {
  const response = await apiFetch(`${baseUrl}/doctors`, {
    method: 'POST',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, allowedSections }),
  })
  return readDoctor(response, 'Не удалось создать врача. Обновите список перед повторной попыткой.')
}

export async function updateDoctorSections(
  id: number,
  allowedSections: readonly AdminSection[],
  signal?: AbortSignal,
): Promise<DoctorDto> {
  const response = await apiFetch(`${baseUrl}/doctors/${id}/sections`, {
    method: 'PUT',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ allowedSections }),
  })
  return readDoctor(response, 'Не удалось сохранить права доступа.')
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

export async function resetDoctorPassword(id: number, newPassword: string, signal?: AbortSignal): Promise<void> {
  const response = await apiFetch(`${baseUrl}/users/${id}/reset-password`, {
    method: 'POST',
    signal,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newPassword }),
  })
  await checkResponse(response, 'Не удалось изменить пароль. Попробуйте ещё раз.')
}
