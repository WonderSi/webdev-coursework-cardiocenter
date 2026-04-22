import { createRouter, createWebHistory } from 'vue-router'
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
    { path: '/',         component: HomePage },
    { path: '/survey',   component: SurveyPage },
    { path: '/results',  component: ResultsPage },

    { path: '/login',  component: LoginPage },
    { path: '/dashboards',  component: DashboardsPage },
    { path: '/view-data',  component: ViewDataPage },
    { path: '/upload-data',  component: ViewDataPage },
    { path: '/model-training',  component: ViewDataPage }
  ]
})

export default router