<template>
  <sidebar/>
  <div class="model-training-page">
    <div class="disaese-card">
      <div class="header">
        <div class="header-icon">
          <!-- svg -->
        </div>
        <h1 class="disease-name">{{[DISEASELIST[1]]}}</h1>
      </div>
      <div class="disease-card-content">
        <div class="col-container">

        </div>

        <div class="col-container">
          
        </div>

        <div class="col-container">
          
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
  background-color: rgba($color-glass-white, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.4);
  border-radius: $radius-glass-card;
  
  min-width: 100px;
}

</style>
