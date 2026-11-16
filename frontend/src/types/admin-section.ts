export const adminSections = [
  { code: 'DASHBOARDS', label: 'Аналитика' },
  { code: 'UPLOAD_DATA', label: 'Внесение данных' },
  { code: 'MODEL_TRAINING', label: 'Модели' },
  { code: 'RETRAIN', label: 'Переобучение модели' },
  { code: 'VIEW_DATA', label: 'Просмотр данных' },
] as const

export type AdminSection = typeof adminSections[number]['code']

export interface AuthUser {
  email: string
  role: string
  allowedSections: AdminSection[]
}
