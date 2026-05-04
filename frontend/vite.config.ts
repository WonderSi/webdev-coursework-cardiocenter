import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
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
