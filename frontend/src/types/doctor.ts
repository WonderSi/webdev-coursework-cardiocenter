export type DoctorAccessLevel = 'VIEW' | 'EDIT' | 'RETRAIN'

export interface DoctorDto {
  id: number
  email: string
  isBlocked: boolean
  name?: string
  accessLevel?: DoctorAccessLevel
}

export interface Doctor extends DoctorDto {
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
  accessLevel: DoctorAccessLevel
}

export interface DoctorRow {
  doctor: Doctor
  displayName: string
  initials: string
  accessLevel?: DoctorAccessLevel
}
