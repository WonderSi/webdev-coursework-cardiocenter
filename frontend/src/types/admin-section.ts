export const adminSections = [
  { code: 'DASHBOARDS', label: 'Аналитика' },
  { code: 'UPLOAD_DATA', label: 'Загрузка данных' },
  { code: 'MODEL_TRAINING', label: 'Обучение модели' },
  { code: 'VIEW_DATA', label: 'Просмотр данных' },
] as const

export type AdminSection = typeof adminSections[number]['code']

export interface AuthUser {
  email: string
  role: string
  allowedSections: AdminSection[]
}
