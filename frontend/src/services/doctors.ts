import { persistDoctor, resetDoctorPassword, setDoctorBlocked } from '@/api/doctors.api'
import type { Doctor, DoctorDraft, DoctorCapabilities } from '@/types/doctor'

let nextPreviewId = -1

export async function saveDoctor(
  doctor: Doctor | null,
  draft: DoctorDraft,
  capabilities: DoctorCapabilities,
  signal?: AbortSignal,
): Promise<Doctor> {
  if (capabilities.profiles && capabilities.accessLevels) {
    return persistDoctor(doctor, draft, capabilities, signal)
  }

  // Локально нельзя создать аккаунт с большим количеством привилегий (уровень доступа) чем было выбрано (просмотр, ред., полный)
  if (doctor && doctor.id > 0 && draft.password) {
    await resetDoctorPassword(doctor.id, draft.password, signal)
  }

  const pendingFields = new Set<NonNullable<Doctor['pendingFields']>[number]>(doctor?.pendingFields)
  if (!doctor || doctor.id < 0) pendingFields.add('account')
  if (draft.name.trim() !== (doctor?.name ?? '')) pendingFields.add('name')
  if (draft.email.trim() !== doctor?.email) pendingFields.add('email')
  if (draft.accessLevel !== (doctor?.accessLevel ?? 'RETRAIN')) pendingFields.add('accessLevel')

  return {
    id: doctor?.id ?? nextPreviewId--,
    name: draft.name.trim(),
    email: draft.email.trim(),
    accessLevel: draft.accessLevel,
    isBlocked: doctor?.isBlocked ?? false,
    pendingFields: [...pendingFields],
  }
}

export async function updateDoctorBlocked(
  doctor: Doctor,
  isBlocked: boolean,
  signal?: AbortSignal,
): Promise<Doctor> {
  if (doctor.id > 0) await setDoctorBlocked(doctor.id, isBlocked, signal)
  return { ...doctor, isBlocked }
}

export { doctorErrorMessage } from '@/utils/doctor-errors'
