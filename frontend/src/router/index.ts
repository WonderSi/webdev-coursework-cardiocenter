import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.store'
import { useSurveyStore } from '@/stores/survey.store'
import HomePage from '@/pages/HomePage.vue'
import type { AdminSection } from '@/types/admin-section'

const SurveyPage = () => import('@/pages/SurveyPage.vue')
const ResultsPage = () => import('@/pages/ResultsPage.vue')
const LoginPage = () => import('@/pages/LoginPage.vue')
const DashboardsPage = () => import('@/pages/menu pages/DashboardsPage.vue')
const ViewDataPage = () => import('@/pages/menu pages/ViewDataPage.vue')
const UploadDataPage = () => import('@/pages/menu pages/UploadDataPage.vue')
const ModelTrainingPage = () => import('@/pages/menu pages/ModelTrainingPage.vue')
const DoctorsPage = () => import('@/pages/menu pages/DoctorsPage.vue')
const EmptyPanelPage = () => import('@/pages/menu pages/EmptyPanelPage.vue')

type AppRole = 'ROLE_DOCTOR' | 'ROLE_SUPER_ADMIN'

const clinicalRoles: AppRole[] = ['ROLE_DOCTOR', 'ROLE_SUPER_ADMIN']

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: AppRole[]
    section?: AdminSection
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',               component: HomePage },
    { path: '/survey',         component: SurveyPage },
    { path: '/results',        component: ResultsPage },
    { path: '/login',          component: LoginPage },
    { path: '/panel',          component: EmptyPanelPage,    meta: { requiresAuth: true, roles: clinicalRoles } },
    { path: '/dashboards',     component: DashboardsPage,    meta: { requiresAuth: true, roles: clinicalRoles, section: 'DASHBOARDS' } },
    { path: '/view-data',      component: ViewDataPage,      meta: { requiresAuth: true, roles: clinicalRoles, section: 'VIEW_DATA' } },
    { path: '/upload-data',    component: UploadDataPage,    meta: { requiresAuth: true, roles: clinicalRoles, section: 'UPLOAD_DATA' } },
    { path: '/model-training', component: ModelTrainingPage, meta: { requiresAuth: true, roles: clinicalRoles, section: 'MODEL_TRAINING' } },
    { path: '/doctors',        component: DoctorsPage,       meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN'] } },
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const allowedRoles = to.meta.roles

  const needAuth = to.meta.requiresAuth === true || Boolean(allowedRoles?.length)
  if (needAuth && userStore.isAuthenticated) {
    try { await userStore.fetchMe() } catch { return false }
  }

  if (needAuth && !userStore.isAuthenticated) return { path: '/login' }
  if (allowedRoles?.length && !allowedRoles.some((role) => role === userStore.role)) return { path: '/' }
  if (to.meta.section && !userStore.canAccessSection(to.meta.section)) return { path: '/panel' }
  if (to.path === '/login' && userStore.isAuthenticated) return { path: userStore.canAccessClinicalData ? '/dashboards' : '/' }
  if (to.path === '/results' && !useSurveyStore().submitted) return { path: '/' }
})

export default router
