import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useUserStore } from '@/stores/user.store'
import router from '@/router/index'
import '@/styles/main.scss'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

useUserStore().fetchMe().finally(() => {
  app.use(router)
  app.mount('#app')
})