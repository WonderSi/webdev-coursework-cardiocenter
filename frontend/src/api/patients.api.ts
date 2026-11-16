import { apiFetch } from './client'

const numericFields = [
  'chronicLungDisease', 'gender', 'age', 'height', 'weight',
  'alcohol', 'profession', 'region', 'glucose', 'cholesterol',
  'hdlCholesterol', 'ldlCholesterol', 'triglycerides',
] as const

export function buildPatientPayload(answers: Record<string, unknown>) {
  if (answers.modelKind !== 'city' && answers.modelKind !== 'region') {
    throw new Error('Выберите модель обследования.')
  }
  const numbers = Object.fromEntries(numericFields.map(key => {
    const value = answers[key]
    const number = value === null || value === undefined || value === '' ? null : Number(value)
    if (number !== null && !Number.isFinite(number)) throw new Error('Проверьте числовые поля анкеты.')
    return [key, number]
  }))
  return {
    ...numbers,
    modelKind: answers.modelKind,
    year: answers.modelKind === 'city' ? 2021 : null,
  }
}

export type PatientPayload = ReturnType<typeof buildPatientPayload>

export async function createPatient(payload: PatientPayload): Promise<void> {
  let response: Response
  try {
    response = await apiFetch('/api/admin/patients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch {
    throw new Error('Ошибка соединения с сервером. Сохранение не подтверждено; перед повторной попыткой проверьте список пациентов.')
  }
  if (response.ok) return
  if (response.status === 401) throw new Error('Сессия истекла — войдите заново.')
  if (response.status === 403) throw new Error('Недостаточно прав для добавления пациента.')
  const body: unknown = await response.json().catch(() => null)
  const detail = body && typeof body === 'object' && 'detail' in body ? body.detail : null
  throw new Error(typeof detail === 'string' ? detail : 'Не удалось сохранить пациента. Попробуйте ещё раз.')
}
