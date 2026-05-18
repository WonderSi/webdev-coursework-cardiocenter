<template>
  <div class="results-page">
    <div class="logo-container">
      <router-link to="/" class="logo-link">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
      </router-link>
    </div>

    <div class="results-card">
      <h1 class="title">Прогнозирование риска развития<br>сердечно-сосудистых заболеваний</h1>
      <div class="div-line"></div>
      <div class="results-text-block">
        <h2 class="results-title">РЕЗУЛЬТАТЫ</h2>
        <p class="regular-text">Спасибо за прохождение опроса!</p>
        <p class="regular-text">Пожалуйста, обратите внимание на ваши риски и проконсультируйтесь со специалистом. Берегите себя!</p>
      </div>
      <div class="results-content">
        <div class="risk-element">
          <div class="risk-icon"></div>
          <label class="risk-percent"></label>
          <label class="risk-name"></label>
        </div> <!-- Всегда 7 штук -->
        


          <!-- <div class="heart-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#EA566F"/> </svg>
          </div> -->
      </div>

      


        <button class="btn-home-page" @click="goHome">На главную</button>
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

// const DISEASES: { mlKey: string, label: string }[] = [
//   { mlKey: 'general', label: 'Общий Риск' },
//   { mlKey: 'stroke', label: 'Инсульт' },
//   { mlKey: 'heart_failure', label: 'Сердечная Недостаточность (СН)' },
//   { mlKey: 'arrhythmia_or_ihd', label: 'Нарушение Ритма или другие Ишемические Болезни Сердца (ИБС)' },
//   { mlKey: 'angina', label: 'Стенокардия' },
//   { mlKey: 'mi', label: 'Инфаркт Миокарда' },
//   { mlKey: 'ah', label: 'Артериальная Гипертензия (АГ)' },
// ]

// данные сразу поданы в формате ОБЩИЙ РИСК И ДАЛЬШЕ ПО УБЫВАНИЮ. РОВНО ТАК,
// В КАКОМ ПОРЯДКЕ НУЖНО ОТОБРАЖАТЬ
const DISEASESMOCK: { name: string, risk: number }[] = [
  { name: 'Общий Риск', risk: 95 },
  { name: 'Инсульт', risk: 53 },
  { name: 'Сердечная Недостаточность (СН)', risk: 45 },
  { name: 'Нарушение Ритма или другие Ишемические Болезни Сердца (ИБС)', risk: 34 },
  { name: 'Стенокардия', risk: 26 },
  { name: 'Инфаркт Миокарда', risk: 19 },
  { name: 'Артериальная Гипертензия (АГ)', risk: 5 },
]

</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// стили сверху вниз в порядке элементов страницы

.results-page {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  align-items: center;
  height: 100vh;
  padding-inline: 200px;
  padding-block: 40px;
  gap: 24px;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.8;
  }
}

.logo-container {
  flex-shrink: 0;
  .logo { width: 100px; height: 100px; object-fit: contain; }
}

.results-card {
  background: $color-white;
  border-radius: 24px;
  padding: 48px 60px;
  min-width: 800px;
  width: 90%;
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
}

.div-line {
  width: 100%;
  border-top: 4px solid $color-accent;
  border-radius: 1000px;
  height: 0;
  margin-block: 24px;
}

.results-text-block {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .results-title {
    font-weight: 800;
    font-size: 1.5rem;
    color: $color-text;
  }

  .regular-text {
    font-weight: 400;
    font-size: 1rem;
    color: $color-text;
  }
}

.results-content {
  display: flex;
  flex-direction: column;
  width: 100%;

  .risk-element {
    display: flex;
    flex-direction: row;
    
    .risk-icon {

    }
    .risk-percent {

    }
    .risk-name {

    }
  }
}



.btn-home-page {
  padding: 13px 48px;
  border: none;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: $color-white;
  font-size: 1rem;
  font-weight: 600;

  cursor: pointer;
  transition: background 0.2s;
    &:hover {
      background: $color-accent-lighter;
    }
}

</style>
