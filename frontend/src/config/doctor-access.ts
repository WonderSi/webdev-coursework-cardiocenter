// Вкладки перечислены в том же порядке, что и в Sidebar.
const accessPages = {
  data: { page: 'view-data', label: 'Просмотр данных' },
  dashboards: { page: 'dashboards', label: 'Аналитика' },
  upload: { page: 'upload-data', label: 'Загрузка данных' },
  training: { page: 'model-training', label: 'Обучение модели' },
} as const

export const accessGuide = [
  {
    level: 'VIEW',
    description: 'Сотрудник может просматривать данные пациентов и аналитику без изменения записей.',
    pages: [accessPages.data, accessPages.dashboards],
  },
  {
    level: 'EDIT',
    description: 'Помимо просмотра и аналитики сотрудник может добавлять и изменять данные пациентов.',
    pages: [accessPages.data, accessPages.dashboards, accessPages.upload],
  },
  {
    level: 'RETRAIN',
    description: 'Сотруднику доступны все возможности работы с данными, метрики моделей и переобучение.',
    pages: [accessPages.data, accessPages.dashboards, accessPages.upload, accessPages.training],
  },
] as const
