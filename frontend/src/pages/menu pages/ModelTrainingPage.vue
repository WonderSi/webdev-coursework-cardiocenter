<template>
  <sidebar/>
  <div class="model-training-page">
    <div class="disease-card">
      <div class="card-header">
        <div class="header-icon">
          <svg width="25" height="29" viewBox="0 0 25 29" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M24 12.3953C25.3333 13.1651 25.3333 15.0896 24 15.8594L3 27.9837C1.66666 28.7535 -1.38408e-06 27.7913 -1.31678e-06 26.2517L-2.56832e-07 2.00296C-1.89534e-07 0.46336 1.66667 -0.498889 3 0.270912L24 12.3953Z" fill="#242942"/> </svg>
        </div>
        <h1 class="disease-name">{{DISEASELIST[0]}}</h1>
      </div>
      <div class="card-content">
        <div class="first-col">
          <div class="one-stat">
            <div class="icon-and-name">
              <div class="one-stat-icon"></div>
              <label class="one-stat-name">число параметров:</label>
            </div>
            <div class="stat-number">100</div>
          </div>
        </div>

        <div class="div-line"></div>

        <div class="second-col">
          
        </div>

        <div class="div-line"></div>

        <div class="third-col">
          
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'

import { ref, onMounted } from 'vue';
import { fetchModelsStats, retrainModel, type FullModelsResponse } from '@/api/modeltraining.api'

const DISEASELIST = [ "Общий риск", "Инсульт", "Сердечная недостаточность", "Нарушение ритма или другие ИБС", "Стенокардия", "Инфаркт миокарда", "Артериальная гипертензия" ];

const STATSMOCK = {

}

// import {
//   CHART_COLORS,
//   COMMON_TOOLTIP,
//   TOOLTIP_ALT,
//   AXIS_STYLE,
//   COMMON_GRID
// } from '@/utils/chartConstants';

const modelData = ref<FullModelsResponse | null>(null)
const isLoading = ref(false)
const isRetraining = ref(false) // лоадер для кнопки переобучения
const errorMessage = ref('')

// Функция первичной загрузки (и обновления)
const loadData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    modelData.value = await fetchModelsStats()
  } catch (e: any) {
    errorMessage.value = 'Ошибка загрузки метрик: ' + e.message
  } finally {
    isLoading.value = false
  }
}

// Обработчик нажатия на кнопку "Переобучить"
const handleRetrain = async () => {
  isRetraining.value = true
  errorMessage.value = ''
  try {
    const response = await retrainModel()
    if (response.status === 'ok') {
      // Если переобучение прошло успешно, ОБНОВЛЯЕМ метрики
      await loadData()
    } else {
      errorMessage.value = 'Ошибка переобучения со стороны ML сервера'
    }
  } catch (e: any) {
    errorMessage.value = 'Ошибка запроса на переобучение: ' + e.message
  } finally {
    isRetraining.value = false
  }
}

// При открытии страницы загружаем данные
onMounted(() => {
  loadData()
})

</script>



<style scoped lang="scss">
@use '@/styles/variables' as *;

// --- утилити

.col-container {
  display: flex;
  flex-direction: column;
}

.div-line {
  height: 100%;
  border-right: 2px solid $color-accent-lighter2;
  border-radius: 1000px;
  width: 0;
  margin-inline: 32px;
}

.model-training-page {
  margin-left: 240px;
  padding-inline: 80px;
  box-sizing: border-box;

  display: flex;
  flex-direction: column;
  gap: 40px;
  margin-block: 40px;

  @media (max-width: 1200px) {
    padding-inline: 40px;
  }
  @media (max-width: 768px) {
    margin-left: 0;
    padding-inline: 16px;
  }
}

.disease-card {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    flex-direction: row;
    gap: 8px;
    padding: 16px 32px;

    background-color: rgba($color-glass-white, 0.8);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
    border-radius: $radius-glass-card $radius-glass-card 0 0;

    .header-icon {
      padding-top: 6px;
      border-radius: 4px;
    }

    .disease-name {
      font-size: 2rem;
      color: $color-text;
      font-weight: 800;
    }
  }

  .card-content {
    background-color: rgba($color-glass-white, 0.8);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
    border-radius: 0 $radius-glass-card  $radius-glass-card;

    padding: 32px;
    display: flex;
    flex-direction: row;

    .one-stat {
      display: flex;
      flex-direction: row;
      .icon-and-name {
        gap: 4px;
        .one-stat-icon {

        }
        .one-stat-name {
          font-weight: 600;
          font-size: 1rem;
          color: $color-blue-dark;
          white-space: nowrap;
        }
      }
      justify-content: space-between; // "приклеить" stat-number к правому краю, icon-and-name к левому
      align-items: center;
      width: 100%;
      .stat-number {
        font-weight: 600;
        font-size: 1rem;
        color: $color-blue-dark;
        text-align: right;
      }
    }
  }
}

</style>
