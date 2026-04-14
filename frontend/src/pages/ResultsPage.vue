<template>
  <div class="results-page">
    <div class="logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
    </div>

    <div class="results-card">
      <h1 class="title">Результаты опроса</h1>

      <div v-if="store.prediction" class="result">
        <div class="result-icon">
          <span class="icon-circle">!</span>
        </div>

        <p class="prediction-text">
          Вероятность: <strong>{{ store.prediction.probability }}</strong>
          <span v-if="store.prediction.diseaseType"> — {{ store.prediction.diseaseType }}</span>
        </p>

        <p class="result-message">{{ store.prediction.message }}</p>

        <button class="btn-back" @click="goHome">На главную</button>
      </div>

      <div v-else class="result">
        <p class="result-message">Что-то пошло не так. Попробуйте пройти опрос заново.</p>
        <button class="btn-back" @click="goHome">На главную</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSurveyStore } from '@/stores/survey.store'

const router = useRouter()
const store = useSurveyStore()

const goHome = () => {
  store.reset()
  router.push('/')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.results-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  padding: 40px 20px;
  gap: 24px;
}

.logo-container {
  flex-shrink: 0;
  .logo { width: 100px; height: 100px; object-fit: contain; }
}

.results-card {
  background: $color-white;
  border-radius: 24px;
  padding: 48px 60px;
  width: 650px;
  max-width: 100%;
  box-shadow: 0 10px 30px rgba(7, 14, 44, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.title {
  font-size: 1.4rem;
  font-weight: 700;
  color: $color-text;
  margin-bottom: 32px;
}

.result-icon {
  margin-bottom: 24px;
  .icon-circle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: $color-accent-lighter2;
    color: $color-accent;
    font-size: 2rem;
    font-weight: 700;
  }
}

.prediction-text {
  font-size: 1.1rem;
  color: $color-text;
  margin-bottom: 16px;
  strong { color: $color-accent; }
}

.result-message {
  font-size: 1rem;
  color: $color-text;
  line-height: 1.6;
  margin-bottom: 32px;
}

.btn-back {
  padding: 12px 48px;
  border: none;
  border-radius: 12px;
  background: $color-accent;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;

  &:hover { background: darken($color-accent, 10%); }
}
</style>
