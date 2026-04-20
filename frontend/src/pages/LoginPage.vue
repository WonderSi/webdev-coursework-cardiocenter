<template>
    <div class="login-page">
        <div class="smaller-logo-container">
            <img src="@/assets/logo.png" alt="logo" class="logo" />
        </div>
        
         <div class="basic-card">
            <div class="basic-card-header">
                Добро пожаловать!
            </div>
            <div class="basic-card-text">
                    <p>
                    <strong>Болезни сердечно-сосудистой системы</strong>
                    остаются ведущей причиной инвалидизации и смертности во всём мире.
                    </p>
                    <p>
                    Предлагаем вам выяснить, <strong>каков Ваш риск развития
                    сердечно-сосудистых заболеваний</strong> на основе ваших медицинских
                    показателей, профессии, района проживания и других факторов.
                    </p>
                    <p>
                    Расчёт будет произведён с использованием методов
                    <strong>искусственного интеллекта.</strong>
                    </p>
                    <p>
                    Перед прохождением опроса желательно иметь при себе медицинскую
                    карту или знать результаты своих обследований.
                    </p>
                </div>
                <button class="basic-card-main-btn" @click="">Начать</button>
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