import type { AdminSection } from './admin-section'

export type DoctorAccessLevel = 'VIEW' | 'EDIT' | 'RETRAIN'
export type DoctorAccessState = DoctorAccessLevel | 'NONE' | 'CUSTOM'

export interface DoctorDto {
  id: number
  email: string
  isBlocked: boolean
  allowedSections: readonly AdminSection[]
  name?: string
  accessLevel?: DoctorAccessLevel
}

export interface Doctor extends DoctorDto {
  persistedEmail?: string
  pendingFields?: ReadonlyArray<'account' | 'name' | 'email' | 'accessLevel'>
}

export interface DoctorCapabilities {
  profiles: boolean
  accessLevels: boolean
}

export interface DoctorDirectory {
  doctors: DoctorDto[]
  capabilities: DoctorCapabilities
}

export interface DoctorDraft {
  name: string
  email: string
  password: string
  accessLevel: DoctorAccessLevel | null
}

export interface DoctorRow {
  doctor: Doctor
  displayName: string
  initials: string
  accessLevel?: DoctorAccessState
}
