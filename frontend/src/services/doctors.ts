import { createDoctor, updateDoctorSections, resetDoctorPassword, setDoctorBlocked } from '@/api/doctors.api'
import { sameSections, sectionsForLevel } from '@/config/doctor-access'
import type { Doctor, DoctorDraft, DoctorCapabilities } from '@/types/doctor'

export interface DoctorSaveProgress {
  doctor: Doctor
  passwordSaved: boolean
}

export async function saveDoctor(
  doctor: Doctor | null,
  draft: DoctorDraft,
  _capabilities: DoctorCapabilities,
  signal?: AbortSignal,
  onProgress?: (progress: DoctorSaveProgress) => void,
): Promise<Doctor> {
  const allowedSections = draft.accessLevel
    ? sectionsForLevel(draft.accessLevel)
    : doctor?.allowedSections
  if (!allowedSections) throw new Error('Выберите уровень доступа.')

  let saved: Doctor
  if (!doctor) {
    saved = await createDoctor(draft.email.trim(), draft.password, allowedSections, signal)
    if (signal?.aborted) throw new DOMException('Aborted', 'AbortError')
    onProgress?.({ doctor: saved, passwordSaved: true })
    if (!sameSections(saved.allowedSections, allowedSections)) {
      throw new Error('Врач создан, но сервер не подтвердил выбранные права. Проверьте их перед сохранением.')
    }
  } else {
    saved = doctor
    if (!sameSections(doctor.allowedSections, allowedSections)) {
      const response = await updateDoctorSections(doctor.id, allowedSections, signal)
      if (signal?.aborted) throw new DOMException('Aborted', 'AbortError')
      if (!sameSections(response.allowedSections, allowedSections)) {
        throw new Error('Сервер не подтвердил выбранные права. Обновите список.')
      }
      saved = { ...doctor, ...response, email: doctor.email, persistedEmail: response.email }
      onProgress?.({ doctor: saved, passwordSaved: false })
    }
    if (draft.password) {
      try {
        await resetDoctorPassword(saved.id, draft.password, signal)
      } catch (cause) {
        if (signal?.aborted) throw cause
        throw new Error('Не удалось изменить пароль. Уже сохранённые права остаются в силе.')
      }
      if (signal?.aborted) throw new DOMException('Aborted', 'AbortError')
      onProgress?.({ doctor: saved, passwordSaved: true })
    }
  }

  const persistedEmail = saved.persistedEmail ?? saved.email
  return {
    ...saved,
    persistedEmail,
    name: draft.name.trim(),
    email: doctor ? draft.email.trim() : saved.email,
    pendingFields: [
      ...(draft.name.trim() ? ['name' as const] : []),
      ...(doctor && draft.email.trim() !== persistedEmail ? ['email' as const] : []),
    ],
  }
}

export async function updateDoctorBlocked(
  doctor: Doctor,
  isBlocked: boolean,
  signal?: AbortSignal,
): Promise<Doctor> {
  await setDoctorBlocked(doctor.id, isBlocked, signal)
  return { ...doctor, isBlocked }
}

export { doctorErrorMessage } from '@/utils/doctor-errors'
