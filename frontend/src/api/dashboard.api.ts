export interface GenderStat { label: string; count: number; percent: number }

export interface GenderDiagnosisStat {
  maleDiagnosed: number
  malePercent: number
  femaleDiagnosed: number
  femalePercent: number
}

export interface AgeGenderStat {
  ageGroup: string
  healthyMale: number
  diagnosedMale: number
  healthyFemale: number
  diagnosedFemale: number
}

export interface BmiCategoryStat {
  category: string
  healthyCount: number
  diagnosedCount: number
  diagnosedPercent: number
}

export interface AlcoholStat {
  category: string
  healthyCount: number
  diagnosedCount: number
  diagnosedPercent: number
}

export interface ProfessionStat {
  profName: string
  diagnosedCount: number
  diagnosedPercent: number
}

export interface DiagnosisStat {
  diagName: string
  diagnosedCount: number
  percent: number
}

export interface DistrictStat {
  name: string
  numberOfDiagnoses: number
}

export interface DashboardStatsResponse {
  totalPatients: number
  genderDistribution: GenderStat[]
  averageAge: number | null
  genderDiagnoses: GenderDiagnosisStat
  ageGender: AgeGenderStat[]
  weightBmi: BmiCategoryStat[]
  alcohol: AlcoholStat[]
  topProfessions: ProfessionStat[]
  topDiagnoses: DiagnosisStat[]
  districts: DistrictStat[]
}

export async function fetchDashboardStats(): Promise<DashboardStatsResponse> {
  const res = await fetch('/api/admin/dashboard')
  if (!res.ok) throw new Error(`Dashboard fetch failed: ${res.status}`)
  return res.json()
}
