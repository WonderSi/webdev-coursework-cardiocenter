import type { Doctor, DoctorAccessLevel, DoctorCapabilities } from '@/types/doctor'
import type { AdminSection } from '@/types/admin-section'
import type { DoctorAccessState } from '@/types/doctor'

const sectionsByLevel: Record<DoctorAccessLevel, readonly AdminSection[]> = {
  VIEW: ['VIEW_DATA', 'DASHBOARDS'],
  EDIT: ['VIEW_DATA', 'DASHBOARDS', 'UPLOAD_DATA'],
  RETRAIN: ['VIEW_DATA', 'DASHBOARDS', 'UPLOAD_DATA', 'MODEL_TRAINING', 'RETRAIN'],
}

export function sectionsForLevel(level: DoctorAccessLevel): AdminSection[] {
  return [...sectionsByLevel[level]]
}

export function sameSections(a: readonly AdminSection[], b: readonly AdminSection[]): boolean {
  const left = new Set(a)
  const right = new Set(b)
  return left.size === right.size && [...left].every(section => right.has(section))
}

// Вкладки перечислены в том же порядке, что и в Sidebar
const accessPages = {
  questionnaire: { page: 'questionnaire', label: 'Получить прогноз' },
  data: { page: 'view-data', label: 'Просмотр данных' },
  dashboards: { page: 'dashboards', label: 'Аналитика' },
  upload: { page: 'upload-data', label: 'Внесение данных' },
  training: { page: 'model-training', label: 'Модели' },
} as const

export const accessGuide = [
  {
    level: 'VIEW',
    label: 'Просмотр',
    description: 'Сотрудник может просматривать данные пациентов и аналитику без изменения записей.',
    pages: [accessPages.questionnaire, accessPages.data, accessPages.dashboards],
  },
  {
    level: 'EDIT',
    label: 'Редактирование',
    description: 'Помимо просмотра и аналитики сотрудник может добавлять и изменять данные пациентов.',
    pages: [accessPages.questionnaire, accessPages.data, accessPages.dashboards, accessPages.upload],
  },
  {
    level: 'RETRAIN',
    label: 'Полный',
    description: 'Сотруднику доступны все возможности работы с данными, метрики моделей и переобучение.',
    pages: [accessPages.questionnaire, accessPages.data, accessPages.dashboards, accessPages.upload, accessPages.training],
  },
] as const

export const doctorAccessLevels = accessGuide.map(({ level, label, description }) => ({
  value: level,
  label,
  description,
}))

export function effectiveDoctorAccess(
  doctor: Doctor,
  _capabilities?: DoctorCapabilities,
): DoctorAccessState {
  if (!doctor.allowedSections.length) return 'NONE'
  for (const level of ['VIEW', 'EDIT', 'RETRAIN'] as const) {
    if (sameSections(doctor.allowedSections, sectionsByLevel[level])) return level
  }
  return 'CUSTOM'
}

export const doctorAccessStates = [
  ...doctorAccessLevels,
  { value: 'NONE' as const, label: 'Нет доступа' },
  { value: 'CUSTOM' as const, label: 'Индивидуальный доступ' },
]

export const doctorAccessByLevel = {
  VIEW: accessGuide[0],
  EDIT: accessGuide[1],
  RETRAIN: accessGuide[2],
} as const
