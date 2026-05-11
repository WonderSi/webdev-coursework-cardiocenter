import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,             // + блоки добавлены для реалтайм редактуры 
    port: 5173,             // +
    watch: {                // +
      usePolling: true,     // +
    },                      // +
    proxy: {
      '/api': {
        target: 'http://backend:8080', // изменено с //localhost:8080 для реалтайма
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // Автоматически подключает переменные и миксины в каждый компонент
        additionalData: `@use "@/styles/_variables.scss" as *; @use "@/styles/_mixins.scss" as *;`
      }
    }
  }
})
