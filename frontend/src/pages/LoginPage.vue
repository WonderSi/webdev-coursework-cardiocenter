<template>
    <div class="login-page">
        <div class="smaller-logo-container">
            <img src="@/assets/logo.png" alt="logo" class="logo" />
        </div>
        
        <div class="basic-card">
            <div class="basic-card-header">Добро пожаловать</div>
            <div class="basic-card-header-subtext">Войдите, чтобы использовать сервис</div>

            <div class="input-group">
                <input
                    type="text"
                    class="input-field"
                />
                <input
                    type="text"
                    class="input-field"
                />
            </div>



            <button class="basic-card-main-btn" @click="">Войти</button>
            <button class="basic-card-sub-btn"  @click="goBackHomePage">Вернуться</button>
         </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBackHomePage = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  overflow: hidden;
  padding: 40px 20px;
  box-sizing: border-box;
  gap: 24px;
}
</style>