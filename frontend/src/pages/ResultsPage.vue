<template>
  <div class="results-page">
    <div class="logo-container">
      <router-link to="/" class="logo-link">
        <img src="@/assets/logo.png" alt="logo" class="logo" />
      </router-link>
    </div>
    
    <div class="results-card">
      <h1 class="title">Прогнозирование риска развития сердечно-сосудистых заболеваний</h1>
      <div class="div-line"></div>
      
      <div class="results-text-block">
        <h2 class="results-title">РЕЗУЛЬТАТЫ</h2>
        <p class="regular-text">Спасибо за прохождение опроса!</p>
        <p class="regular-text">Пожалуйста, обратите внимание на ваши риски и проконсультируйтесь со специалистом. <b>Берегите себя!</b></p>
      </div>
      
      <div class="results-content">
        <div class="risk-element-general-risk">
          <div class="risk-icon-general-risk">
            <svg 
              width="72"
              height="72" 
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            > 
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#EA566F"/> 
            </svg>
          </div>
          <label class="risk-percent-general-risk">{{ general?.probability }}%</label>
          <label class="risk-name-general-risk">{{ general?.name }}</label>
        </div>

        <div 
          v-for="(item, index) in diseases"
          :key="index"
          class="risk-element"
          :class="getRiskClass(item.probability)"
        >
          <div class="risk-icon">
            <svg 
              :width="getRiskIconSize(item.probability).width"
              :height="getRiskIconSize(item.probability).height"
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <rect width="100%" height="100%" rx="2" fill="currentColor" />
            </svg>
          </div>
          
          <label class="risk-percent">{{ item.probability }}%</label>
          <label class="risk-name">{{ item.name }}</label>
        </div>
      </div>
      
      <button class="btn-home-page" @click="goHome">На главную</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSurveyStore } from '@/stores/survey.store'

const router = useRouter()
const store = useSurveyStore()

const general = computed(() => store.predictions[0])
const diseases = computed(() => store.predictions.slice(1))

const goHome = () => {
  store.reset()
  router.push('/')
}

const getRiskClass = (risk: number) => {
  if (risk >= 70) return 'risk-severe';
  if (risk >= 50) return 'risk-high';
  if (risk >= 30) return 'risk-medium';
  return 'risk-low';
}

const getRiskIconSize = (risk: number) => {
  if (risk >= 70) return { height: 70, width: 66 };
  if (risk >= 50) return { height: 60, width: 44 };
  if (risk >= 30) return { height: 48, width: 29 };
  return { height: 36, width: 19 };
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.results-page {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  align-items: center;
  min-height: 100vh;
  padding: 40px 20px;
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
  
  @media (max-width: 768px) {
    .logo { width: 70px; height: 70px; }
  }
}

.results-card {
  background: $color-white;
  border-radius: 24px;
  padding: 48px 60px;
  width: 100%;
  max-width: 1000px; //
  box-shadow: 0 10px 30px rgba(7, 14, 44, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  
  @media (max-width: 768px) {
    padding: 32px 24px;
  }
  @media (max-width: 480px) {
    padding: 24px 16px;
  }
}

.title {
  font-size: 1.4rem;
  font-weight: 700;
  color: $color-text;
  line-height: 1.3;

  @media (max-width: 768px) {
    font-size: 1.2rem;
  }
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
    
    @media (max-width: 768px) {
      font-size: 1.3rem;
    }
  }

  .regular-text {
    font-weight: 400;
    font-size: 1rem;
    color: $color-text;
    line-height: 1.4;
  }
}

.results-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  margin-bottom: 32px;

  .risk-element {
    display: flex;
    flex-direction: row;
    align-items: center;
    text-align: left;
    gap: 20px;
    
    // --- ДИНАМИЧЕСКИЕ СТИЛИ 
    &.risk-severe { color: $color-text; font-size: 1.8rem; font-weight: 800; }
    &.risk-high { color: $color-blue-dark; font-size: 1.65rem; font-weight: 800; }
    &.risk-medium { color: $color-accent; font-size: 1.45rem; font-weight: 600; }
    &.risk-low { color: $color-accent-lighter; font-size: 1.15rem; font-weight: 600; }

    @media (max-width: 768px) {
      gap: 12px;
      &.risk-severe { font-size: 1.5rem; }
      &.risk-high { font-size: 1.3rem; }
      &.risk-medium { font-size: 1.1rem; }
      &.risk-low { font-size: 0.9rem; }
    }

    .risk-icon {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      width: 87px; 
      flex-shrink: 0;
      
      @media (max-width: 768px) {
        width: 50px;
        transform: scale(0.7);
        transform-origin: left center;
      }
    }
    
    .risk-percent {
      min-width: 90px;
      font-weight: inherit; 
      text-align: center;
      flex-shrink: 0;
      
      @media (max-width: 768px) {
        min-width: 60px;
      }
    }
    
    .risk-name {
      font-weight: inherit;
      line-height: 1.2;
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
  margin-block: 16px;
  padding-block: 12px;
  border-radius: $radius-basic-card;
  background-color: $color-lighter-red;

  @media (max-width: 768px) {
    gap: 12px;
  }
}

.risk-icon-general-risk {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  color: $color-cold-red;
  width: 72px;
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    width: 60px;
    transform: scale(0.7);
    transform-origin: left center;
  }
}

.risk-percent-general-risk {
  font-size: 2rem;
  min-width: 90px;
  font-weight: 800; 
  text-align: center;
  color: $color-cold-red;
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    font-size: 1.8rem;
    min-width: 70px;
  }
}

.risk-name-general-risk {
  font-size: 2rem;
  font-weight: 800; 
  text-align: left;
  line-height: 1.2;
  
  @media (max-width: 768px) {
    font-size: 1.6rem;
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
  margin-top: 16px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: $color-accent-lighter;
  }
  
  @media (max-width: 480px) {
    width: 100%;
  }
}
</style>