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
        <p class="regular-text">Пожалуйста, обратите внимание на ваши риски и проконсультируйтесь со специалистом. <b>Берегите себя!</b></p>
      </div>
      
      <div class="results-content">
        <div class="risk-element-general-risk">
          <svg 
              :width=90
              :height=90 
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            > 
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#EA566F"/> 
            </svg>
            <label class="risk-percent-general-risk">{{ GENERALRISK.risk }}%</label>
            <label class="risk-name-general-risk">{{  GENERALRISK.name }}</label>
        </div>
        <div 
          v-for="(item, index) in DISEASESMOCK" 
          :key="index"
          class="risk-element"
          :class="getRiskClass(item.risk)"
        >
          <div class="risk-icon">
            <svg 
              :width="getRiskIconSize(item.risk).width" 
              :height="getRiskIconSize(item.risk).height" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <rect width="100%" height="100%" fill="currentColor" />
            </svg>
          </div>
          
          <label class="risk-percent">{{ item.risk }}%</label>
          <label class="risk-name">{{ item.name }}</label>
        </div>
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

const GENERALRISK = { name: 'Общий Риск', risk: 100 };

const DISEASESMOCK: { name: string, risk: number }[] = [
  { name: 'Инсульт', risk: 93 },
  { name: 'Сердечная Недостаточность (СН)', risk: 70 },
  { name: 'Нарушение Ритма или другие Ишемические Болезни Сердца (ИБС)', risk: 59 },
  { name: 'Стенокардия', risk: 30 },
  { name: 'Инфаркт Миокарда', risk: 19 },
  { name: 'Артериальная Гипертензия (АГ)', risk: 5 },
]

// Функция для определения CSS-класса на основе риска
const getRiskClass = (risk: number) => {
  if (risk >= 70) return 'risk-severe';
  if (risk >= 50) return 'risk-high';
  if (risk >= 30) return 'risk-medium';
  return 'risk-low';
}


const getRiskIconSize = (risk: number) => {
  if (risk >= 70) return { height: 87, width: 66 };
  if (risk >= 50) return { height: 72, width: 44 };
  if (risk >= 30) return { height: 54, width: 29 };
  return { height: 46, width: 19 };
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

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
  gap: 0px;
  margin-block: 32px;

  .risk-element {
    display: flex;
    flex-direction: row;
    align-items: center;
    text-align: left;
    gap: 20px;
   
    // --- ДИНАМИЧЕСКИЕ СТИЛИ 
    &.risk-severe {
      color: $color-text;
      font-size: 2rem;
      font-weight: 800;
    }
    
    &.risk-high {
      color: $color-blue-dark;
      font-size: 1.65rem;
      font-weight: 800;
    }
    
    &.risk-medium {
      color: $color-accent;
      font-size: 1.35rem;
      font-weight: 600;
    }
    
    &.risk-low {
      color: $color-accent-lighter;
      font-size: 1rem;
      font-weight: 600;
    }

    .risk-icon {
      display: flex;
      justify-content: left;
      align-items: left;
      width: 87px; // по макс. ширине
      flex-shrink: 0;
    }
    
    .risk-percent {
      min-width: 90px;
      font-weight: inherit; 
      text-align: center;
      .general-risk {
        color: $color-cold-red;
      }
    }
    
    .risk-name {
      font-weight: inherit;
    }
  }
}

// general risk
  .risk-element-general-risk {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  color: $color-cold-red;
  text-align: left;
  gap: 20px;
}

.risk-icon-general-risk {
  display: flex;
  justify-content: left;
  align-items: left;
  color: $color-cold-red;
  width: 87px;
  flex-shrink: 0
}

.risk-percent-general-risk {
  font-size: 2.2rem;
  min-width: 90px;
  font-weight: 800; 
  text-align: center;
  color: $color-cold-red;
  font-weight: 800;
}

.risk-name-general-risk {
  font-size: 2.2rem;
  font-weight: 800; 
  text-align: center;
}

.btn-home-page {
  padding: 13px 48px;
  border: none;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: $color-white;
  font-size: 1rem;
  font-weight: 600;
  margin-top: 16px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: $color-accent-lighter;
  }
}

</style>
