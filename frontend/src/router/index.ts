import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.store'
import { useSurveyStore } from '@/stores/survey.store'
import HomePage from '@/pages/HomePage.vue'
import SurveyPage from '@/pages/SurveyPage.vue'
import ResultsPage from '@/pages/ResultsPage.vue'
import LoginPage from '@/pages/LoginPage.vue'
import DashboardsPage from '@/pages/menu pages/DashboardsPage.vue'
import ViewDataPage from '@/pages/menu pages/ViewDataPage.vue'
import UploadDataPage from '@/pages/menu pages/UploadDataPage.vue'
import ModelTrainingPage from '@/pages/menu pages/ModelTrainingPage.vue'
import DoctorsPage from '@/pages/menu pages/DoctorsPage.vue'

type AppRole = 'ROLE_DOCTOR' | 'ROLE_SUPER_ADMIN'

const clinicalRoles: AppRole[] = ['ROLE_DOCTOR', 'ROLE_SUPER_ADMIN']

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: AppRole[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',               component: HomePage },
    { path: '/survey',         component: SurveyPage },
    { path: '/results',        component: ResultsPage },
    { path: '/login',          component: LoginPage },
    { path: '/dashboards',     component: DashboardsPage,    meta: { requiresAuth: true, roles: clinicalRoles } },
    { path: '/view-data',      component: ViewDataPage,      meta: { requiresAuth: true, roles: clinicalRoles } },
    { path: '/upload-data',    component: UploadDataPage,    meta: { requiresAuth: true, roles: clinicalRoles } },
    { path: '/model-training', component: ModelTrainingPage, meta: { requiresAuth: true, roles: clinicalRoles } },
    { path: '/doctors',        component: DoctorsPage,       meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN'] } },
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const allowedRoles = to.meta.roles

  const needAuth = to.meta.requiresAuth === true || Boolean(allowedRoles?.length)

  if (needAuth && !userStore.isAuthenticated) return { path: '/login' }
  if (allowedRoles?.length && !allowedRoles.some((role) => role === userStore.role)) return { path: '/' }
  if (to.path === '/login' && userStore.isAuthenticated) return { path: userStore.canAccessClinicalData ? '/dashboards' : '/' }
  if (to.path === '/results' && !useSurveyStore().submitted) return { path: '/' }
})

export default router
