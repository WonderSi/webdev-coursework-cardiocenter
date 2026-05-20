<template>
  <sidebar/>
  <div class="model-training-page">
    <div class="disease-card">
      <div class="card-header">
        <div class="header-icon">
          <svg width="25" height="29" viewBox="0 0 25 29" fill="none" xmlns="http://www.w3.org/2000/svg"> 
            <path d="M24 12.3953C25.3333 13.1651 25.3333 15.0896 24 15.8594L3 27.9837C1.66666 28.7535 -1.38408e-06 27.7913 -1.31678e-06 26.2517L-2.56832e-07 2.00296C-1.89534e-07 0.46336 1.66667 -0.498889 3 0.270912L24 12.3953Z" fill="#242942"/> 
          </svg>
        </div>
        <h1 class="disease-name">{{DISEASELIST[0]}}</h1>
      </div>

      <div class="card-content">
        <div class="col first-col">
          <div class="subheader">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
            <span>Настройки модели</span>
          </div>
          
          <div class="stats-list">
            <div class="one-stat">
              <div class="icon-and-name">
                <svg width="17" height="17" viewBox="0 0 17 17" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14.1674 8.49996C14.1674 8.09763 14.1242 7.70521 14.0441 7.32696L15.3432 6.18513L13.9265 3.73146L12.2818 4.28821C11.6958 3.7603 11.006 3.36054 10.2567 3.1145L9.91737 1.41663H7.08404L6.74474 3.11379C5.99538 3.35983 5.30562 3.75959 4.71962 4.2875L3.07487 3.73146L1.6582 6.18513L2.95729 7.32696C2.79364 8.1004 2.79364 8.89952 2.95729 9.67296L1.6582 10.8148L3.07487 13.2685L4.71962 12.7117C5.30562 13.2396 5.99538 13.6394 6.74474 13.8854L7.08404 15.5833H9.91737L10.2567 13.8861C11.006 13.6401 11.6958 13.2403 12.2818 12.7124L13.9265 13.2692L15.3432 10.8155L14.0441 9.67367C14.1259 9.28779 14.1673 8.89442 14.1674 8.49996ZM8.5007 11.3333C7.74926 11.3333 7.02859 11.0348 6.49723 10.5034C5.96588 9.97208 5.66737 9.25141 5.66737 8.49996C5.66737 7.74851 5.96588 7.02784 6.49723 6.49649C7.02859 5.96514 7.74926 5.66663 8.5007 5.66663C9.25215 5.66663 9.97282 5.96514 10.5042 6.49649C11.0355 7.02784 11.334 7.74851 11.334 8.49996C11.334 9.25141 11.0355 9.97208 10.5042 10.5034C9.97282 11.0348 9.25215 11.3333 8.5007 11.3333Z" fill="#0F356E"/></svg>

                <span class="one-stat-name">число параметров:</span>
              </div>
              <div class="stat-number">98</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">размер тестовой выборки:</span>
              </div>
              <div class="stat-number">3944</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">размер обучающей выборки:</span>
              </div>
              <div class="stat-number">8004</div>
            </div>
          </div>
          <div class="weights-note">
            <div class="note-line"></div>
            <div class="note-content">
              <span class="note-header">ПРИМЕЧАНИЕ: ВЕСА МОДЕЛИ</span>
              <span class="note-content">Веса присутствуют из-за дисбаланса размеров выборок</span>
              <div class="two-weights-span">
                <span class="note-content">вес больных: 1.00</span>
                <span class="note-content">вес здоровых: 0.63</span>
              </div>
            </div>
            
          </div>
        </div>

        <div class="div-line"></div>

        <div class="col second-col">
          <div class="subheader">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
            <span>Метрики качества модели</span>
          </div>

          <div class="stats-list">
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">Общая точность (ROC-AUC):</span>
              </div>
              <div class="stat-number">0.79</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">Доля правильных ответов:</span>
              </div>
              <div class="stat-number">0.76</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">Точность (Precision):</span>
              </div>
              <div class="stat-number">0.85</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">Полнота (Recall):</span>
              </div>
              <div class="stat-number">0.82</div>
            </div>
            <div class="one-stat">
              <div class="icon-and-name">
                <div class="circle-icon"></div>
                <span class="one-stat-name">F1-мера:</span>
              </div>
              <div class="stat-number">0.83</div>
            </div>
          </div>
        </div>

        <div class="div-line"></div>

        <div class="col third-col">
          <div class="subheader">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
            <span>Матрица ошибок</span>
          </div>

          <div class="confusion-matrix">
            <div class="matrix-cell true-positive" style="border-radius: 12px 0 0 0;">
              <span class="cell-label">True Positive</span>
              <span class="cell-value">2359</span>
              <span class="cell-label-explanatory">верное определение больного</span>
            </div>
            <div class="matrix-cell neutral-colored" style="border-radius: 0 12px 0 0;">
              <span class="cell-label">False Negative</span>
              <span class="cell-value">415</span>
              <span class="cell-label-explanatory">больной принят за здорового</span>
            </div>
            <div class="matrix-cell neutral-colored" style="border-radius: 0 0 0 12px;">
              <span class="cell-label">False Positive</span>
              <span class="cell-value">514</span>
              <span class="cell-label-explanatory">здоровый принят за больного</span>
            </div>
            <div class="matrix-cell true-negative" style="border-radius: 0 0 12px 0;">
              <span class="cell-label">True Negative</span>
              <span class="cell-value">656</span>
              <span class="cell-label-explanatory">верное определение здорового</span>
            </div>
          </div>
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

.col-container {
  display: flex;
  flex-direction: column;
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
    gap: 24px;
    padding: 16px 32px;
    width: fit-content;

    background-color: rgba($color-glass-white, 0.8);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba($color-glass-white, 0.4);
    border-radius: $radius-glass-card $radius-glass-card 0 0;
    border-bottom: none;

    .header-icon {
      display: flex;
      align-items: center;
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
    align-items: stretch;

    .col {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .div-line {
      width: 2px;
      background-color: $color-accent-lighter2;
      border-radius: 2px;
      margin-inline: 32px;
    }

    // --- Переиспользуемые элементы внутри колонок

    .subheader {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 24px;
      
      span {
        font-size: 1.2rem;
        font-weight: 700;
        text-transform: uppercase;
        color: $color-secondary;
        letter-spacing: 0.05em;
      }
    }

    .stats-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .one-stat {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .icon-and-name {
        display: flex;
        align-items: center;
        gap: 10px;

        .circle-icon {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          background-color: $color-blue-dark;
        }

        .one-stat-name {
          font-weight: 600;
          font-size: 1rem;
          color: $color-blue-dark;
        }
      }

      .stat-number {
        font-weight: 600;
        font-size: 1rem;
        color: $color-blue-dark;
        text-align: right;
      }
    }

    // --- Матрица (Grid)

    .confusion-matrix {
      display: grid;
      grid-template-columns: 1fr 1fr;
      height: 100%;
      
      .matrix-cell {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 16px 8px;
        border-radius: 12px 0px 0px 0px;
        text-align: center;
        gap: 8px;

        .cell-label {
          font-size: 1rem;
          font-weight: 600;
          text-transform: lowercase;
          line-height: 1.2;
        }

        .cell-label-explanatory {
          font-size: 0.8rem;
          font-weight: 400;
          text-transform: lowercase;
        }

        .cell-value {
          font-size: 1.5rem;
          font-weight: 800;
        }

        &.true-negative {
          background-color: $color-true-nehative-bg;
          color: $color-green-text;
        }

        &.true-positive {
          background-color: $color-true-positive-bg;
          color: $color-red;
        }

        &.neutral-colored {
          background-color: $color-accent-lighter2;
          color: $color-blue-dark;
        }
      }
    }
  }
}
</style>
