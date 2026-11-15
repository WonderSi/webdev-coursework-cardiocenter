import { apiFetch } from './client'

export type DashboardModelKind = 'city' | 'region'
export interface DashboardCategory { name: string; count: number; percent: number }
export interface DashboardAgeGroup { ageGroup: string; maleCount: number; femaleCount: number }
export interface DashboardLabCoverage {
  field: string; label: string; filledCount: number; missingCount: number; percent: number
}
export interface DashboardStatsResponse {
  modelKind: DashboardModelKind
  totalRecords: number
  averageAge: number | null
  genderDistribution: DashboardCategory[]
  ageGender: DashboardAgeGroup[]
  bmi: DashboardCategory[]
  alcohol: DashboardCategory[]
  professions: DashboardCategory[]
  districts: DashboardCategory[]
  lungDisease: DashboardCategory[]
  labCoverage: DashboardLabCoverage[]
  diagnosedRecords: number
  recordedDiagnoses: DashboardCategory[]
}

export async function fetchDashboardStats(
  modelKind: DashboardModelKind = 'city', signal?: AbortSignal,
): Promise<DashboardStatsResponse> {
  const res = await apiFetch(`/api/admin/dashboard?modelKind=${modelKind}`, { signal })
  if (res.status === 403) throw new Error('Доступ запрещён')
  if (!res.ok) throw new Error('Не удалось загрузить аналитику.')
  return res.json()
}
