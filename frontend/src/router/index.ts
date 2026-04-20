import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import SurveyPage from '@/pages/SurveyPage.vue'
import ResultsPage from '@/pages/ResultsPage.vue'
import LoginPage from '@/pages/LoginPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',         component: HomePage },
    { path: '/survey',   component: SurveyPage },
    { path: '/results',  component: ResultsPage },

    { path: '/login',  component: LoginPage },
  ]
})

export default router