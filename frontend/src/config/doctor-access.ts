import type { Doctor, DoctorAccessLevel, DoctorCapabilities } from '@/types/doctor'

// Вкладки перечислены в том же порядке, что и в Sidebar
const accessPages = {
  data: { page: 'view-data', label: 'Просмотр данных' },
  dashboards: { page: 'dashboards', label: 'Аналитика' },
  upload: { page: 'upload-data', label: 'Загрузка данных' },
  training: { page: 'model-training', label: 'Обучение модели' },
} as const

export const accessGuide = [
  {
    level: 'VIEW',
    label: 'Просмотр',
    description: 'Сотрудник может просматривать данные пациентов и аналитику без изменения записей.',
    pages: [accessPages.data, accessPages.dashboards],
  },
  {
    level: 'EDIT',
    label: 'Редактирование',
    description: 'Помимо просмотра и аналитики сотрудник может добавлять и изменять данные пациентов.',
    pages: [accessPages.data, accessPages.dashboards, accessPages.upload],
  },
  {
    level: 'RETRAIN',
    label: 'Полный',
    description: 'Сотруднику доступны все возможности работы с данными, метрики моделей и переобучение.',
    pages: [accessPages.data, accessPages.dashboards, accessPages.upload, accessPages.training],
  },
] as const

export const doctorAccessLevels = accessGuide.map(({ level, label, description }) => ({
  value: level,
  label,
  description,
}))

export function effectiveDoctorAccess(
  doctor: Doctor,
  capabilities: DoctorCapabilities,
): DoctorAccessLevel | undefined {
  return doctor.accessLevel ?? (capabilities.accessLevels ? undefined : 'RETRAIN')
}

export const doctorAccessByLevel = {
  VIEW: accessGuide[0],
  EDIT: accessGuide[1],
  RETRAIN: accessGuide[2],
} as const
