<template>
  <sidebar/>
  <div class="model-training-page">
    <div class="disease-card">
      <div class="card-header" @click="toggleCollapse">
        <div class="header-icon" :class="{ 'collapsed': isCollapsed }">
          <svg width="25" height="29" viewBox="0 0 25 29" fill="none" xmlns="http://www.w3.org/2000/svg"> 
            <path d="M24 12.3953C25.3333 13.1651 25.3333 15.0896 24 15.8594L3 27.9837C1.66666 28.7535 -1.38408e-06 27.7913 -1.31678e-06 26.2517L-2.56832e-07 2.00296C-1.89534e-07 0.46336 1.66667 -0.498889 3 0.270912L24 12.3953Z" fill="#242942"/> 
          </svg>
        </div>
        <h1 class="disease-name">{{DISEASELIST[0]}}</h1>
      </div>

      <transition name="collapse">
        <div class="card-content" v-show="!isCollapsed">
          <div class="col first-col">
            <div class="subheader">
              <svg width="16" height="16" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
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

              <div class="one-stat-ratio">
                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">размер тестовой выборки:</span>
                  </div>
                  <div class="stat-number">3944</div>
                </div>
                <div class="diseased-healthy">
                  <div class="span-container"><span class="diseased">больные:</span><span class="diseased">1000 | 90%</span></div>
                  <div class="span-container"><span class="healthy">здоровые:</span><span class="healthy">234 | 10%</span></div>
                </div>
                <div class="ratio-rectangle" style="--ratio: 72%;"></div>
              </div>

              <div class="one-stat-ratio">
                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">размер обучающей выборки:</span>
                  </div>
                  <div class="stat-number">8004</div>
                </div>
                <div class="diseased-healthy">
                  <div class="span-container"><span class="diseased">больные:</span><span class="diseased">1000 | 90%</span></div>
                  <div class="span-container"><span class="healthy">здоровые:</span><span class="healthy">234 | 10%</span></div>
                </div>
                <div class="ratio-rectangle" style="--ratio: 72%;"></div>
              </div>

            </div>
            <div class="weights-note">
              <div class="note-line"></div>
              <div class="note-content">
                <span class="note-header">ПРИМЕЧАНИЕ: ВЕСА МОДЕЛИ</span>
                <span>Веса присутствуют из-за дисбаланса размеров выборок</span>
                <div class="two-weights-span">
                  <span class="note-text-smaller">вес больных: 1.00</span>
                  <span class="note-text-smaller">вес здоровых: 0.63</span>
                </div>
              </div>
              
            </div>
          </div>

          <div class="div-line"></div>

          <div class="col second-col">
            <div class="subheader">
              <svg width="16" height="16" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
              <span>Метрики качества модели</span>
            </div>

            <div class="stats-list">
              <div class="one-stat">
                <div class="icon-and-name">
                  <div class="circle-icon"></div>
                  <span class="one-stat-name">общая точность (roc-auc):</span>
                </div>
                <div class="stat-number">0.79</div>
              </div>
              <div class="one-stat">
                <div class="icon-and-name">
                  <div class="circle-icon"></div>
                  <span class="one-stat-name">доля правильных ответов:</span>
                </div>
                <div class="stat-number">0.76</div>
              </div>
              <div class="one-stat">
                <div class="icon-and-name">
                  <div class="circle-icon"></div>
                  <span class="one-stat-name">точность (precision):</span>
                </div>
                <div class="stat-number">0.85</div>
              </div>
              <div class="one-stat">
                <div class="icon-and-name">
                  <div class="circle-icon"></div>
                  <span class="one-stat-name">полнота (recall):</span>
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
            <div class="subheader smaller">
              <span>дополнительные метрики качества</span> </div>
              <div class="stats-list">
                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">logloss:</span>
                  </div>
                  <div class="stat-number">0.83</div>
                </div>

                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">brier:</span>
                  </div>
                  <div class="stat-number">0.83</div>
                </div>

                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">pr_auc:</span>
                  </div>
                  <div class="stat-number">0.83</div>
                </div>

                <div class="one-stat">
                  <div class="icon-and-name">
                    <div class="circle-icon"></div>
                    <span class="one-stat-name">balanced_accuracy:</span>
                  </div>
                  <div class="stat-number">0.83</div>
                </div>
              </div>
            

          </div>

          <div class="div-line"></div>

          <div class="col third-col">
            <div class="subheader">
              <svg width="16" height="16" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M0 2C0 0.89543 0.895431 0 2 0H19.1716C20.9534 0 21.8457 2.15428 20.5858 3.41421L12 12L3.41422 20.5858C2.15429 21.8457 0 20.9534 0 19.1716V2Z" fill="#8A8FA8"/></svg>
              <span>Матрица ошибок</span>
            </div>

            <div class="matrix-container">
              <div class="matrix-label-top">Прогноз</div>
              <div class="matrix-content">
                <div class="matrix-label-left">Реальность</div>
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
      </transition>
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

const isCollapsed = ref(false);
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

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
  width: calc(100% - 240px);
  flex-grow: 1;

  display: flex;
  flex-direction: column;
  gap: 40px;
  margin-block: 40px;

  @media (max-width: 1200px) {
    padding-inline: 40px;
  }
  @media (max-width: 768px) {
    margin-left: 0;
    width: 100%;
    padding-inline: 16px;
  }
}

.disease-card {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

  align-items: flex-start;

  .card-header {
    cursor: pointer;
    transition: background-color 0.2s ease;
     align-self: flex-start;

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

      transition: transform 0.3s ease;
      &.collapsed {
        transform: rotate(-90deg);
      }
    }

    .disease-name {
      font-size: 2rem;
      color: $color-text;
      font-weight: 800;
    }

    &:hover {
      background-color: rgba($color-glass-white, 0.95);
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
      display: flex;
      flex-direction: column;
      min-width: 0;

      &.first-col {
        flex: 1;
        .stats-list {
          gap: 36px;
        }
      }
      &.second-col { flex: 0.925; }
      &.third-col { flex: 1.4; }
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
        font-weight: 800;
        text-transform: uppercase;
        color: $color-secondary;
      }

      &.smaller {
        margin-top: 32px;
        margin-bottom: 16px;
        span {
          font-size: 1rem;
        }
      }
    }

    .stats-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    // --- Матрица (Grid)

    .matrix-container {
      display: flex;
      flex-direction: column;
      gap: 8px;
      height: 100%;

      .matrix-label-top {
        text-align: center;
        font-size: 1.2rem;
        font-weight: 800;
        color: $color-blue-dark;
        text-transform: uppercase;
        margin-left: 24px;
      }

      .matrix-content {
        display: flex;
        flex-direction: row;
        gap: 8px;
        flex: 1;
        .matrix-label-left {
          writing-mode: vertical-lr;
          transform: rotate(180deg);
          text-align: center;
          font-size: 1.2rem;
          font-weight: 800;
          color: $color-blue-dark;
          text-transform: uppercase;
        }
      }
    }

    .confusion-matrix {
      flex: 1;
      display: grid;
      grid-template-columns: 1fr 1fr;
      height: 100%;
      
      .matrix-cell {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 16px 16px;
        border-radius: 12px 0px 0px 0px;
        text-align: center;
        gap: 8px;

        .cell-label {
          font-size: 1.5rem;
          font-weight: 600;
          text-transform: lowercase;
          line-height: 1.2;
        }

        .cell-label-explanatory {
          font-size: 0.875rem;
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

// примечание внизу первого столбца
.weights-note {
  margin-top: 30px;
  color: $color-secondary-text;
  font-size: 1rem;
  display: flex;
  flex-direction: row;
  .note-line {
    height: 100%;
    border-right: 8px solid $color-secondary-text;
    margin-right: 8px;
  }
  .note-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
    .note-header {
      text-transform: uppercase;
      font-weight: 800;
    }
    .two-weights-span {
      margin-top: 10px;
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-self: center;
      width: 70%;
      .note-text-smaller {
        font-size: 0.875rem;
        font-weight: 400;
        text-align: center;
        align-self: center;
      }
    }
  }
}

// для "размер выборки"
.one-stat-ratio {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;

  .diseased-healthy {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    width: 100%;

    .span-container {
      display: flex;
      flex-direction: row;
      gap: 16px;
    }
    .diseased {
      color: $color-red;
      font-size: 1rem;
      font-weight: 500;
    }

    .healthy {
      color: $color-green;
      font-size: 1rem;
      font-weight: 500;
    }
  }

  .ratio-rectangle {
    color: $color-red;
    width: 100%;
    height: 10px;
    border-radius: 1000px;

    background: linear-gradient(
      to right,
      $color-red 0%,
      $color-red var(--ratio, 50%),
      $color-green var(--ratio, 50%),
      $color-green 100%
    );
  }
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

// --- АДАПТИВНОСТЬ

@media (max-width: 1024px) {

  .disease-card .card-content {
    flex-direction: column;
    gap: 32px;
    padding: 24px;
    
    .col {
      width: 100%;
      flex: none;
    }

    .div-line {
      width: 100%;
      height: 2px;
      margin-inline: 0;
    }
  }
}

@media (max-width: 1500px) {

  .one-stat-ratio {
    .diseased-healthy {
      display: flex;
      flex-direction: column;
      gap: 0;
      justify-content: space-between;
      width: 100%;

      .span-container {
        display: flex;
        flex-direction: row;
        gap: 16px;
      }
    }
  }
}

@media (max-width: 1000px) {

  .one-stat-ratio {
    .diseased-healthy {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      width: 100%;

      .span-container {
        display: flex;
        flex-direction: row;
        gap: 16px;
      }
    }
  }
}


// анимация сворачивания
.collapse-enter-active,
.collapse-leave-active {
  transition: max-height 0.4s ease-in-out, opacity 0.3s ease-in-out, padding 0.4s ease-in-out;
  max-height: 800px;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
}
</style>
