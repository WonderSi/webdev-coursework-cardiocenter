<template>
  <sidebar/>
  

  
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'

import { ref, onMounted } from 'vue';
import { fetchModelsStats, retrainModel, type FullModelsResponse } from '@/api/modeltraining.api'

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


</style>
