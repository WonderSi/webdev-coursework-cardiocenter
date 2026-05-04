import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.store'
import HomePage from '@/pages/HomePage.vue'
import SurveyPage from '@/pages/SurveyPage.vue'
import ResultsPage from '@/pages/ResultsPage.vue'
import LoginPage from '@/pages/LoginPage.vue'
import DashboardsPage from '@/pages/menu pages/DashboardsPage.vue'
import ViewDataPage from '@/pages/menu pages/ViewDataPage.vue'
// import UploadDataPage from '@/pages/menu pages/UploadDataPage.vue'
// import ModelTrainingPage from '@/pages/menu pages/ModelTrainingPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',               component: HomePage },
    { path: '/survey',         component: SurveyPage },
    { path: '/results',        component: ResultsPage },
    { path: '/login',          component: LoginPage },
    { path: '/dashboards',     component: DashboardsPage, meta: { requiresAuth: true } },
    { path: '/view-data',      component: ViewDataPage,   meta: { requiresAuth: true } },
    { path: '/upload-data',    component: ViewDataPage,   meta: { requiresAuth: true } },
    { path: '/model-training', component: ViewDataPage,   meta: { requiresAuth: true } },
  ]
})

router.beforeEach((to) => {
  const { isAuthenticated } = useUserStore()
  if (to.meta.requiresAuth && !isAuthenticated) return { path: '/login' }
  if (to.path === '/login' && isAuthenticated) return { path: '/dashboards' }
})

export default router