<template>
  <div class="survey-page">
    <div class="logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
    </div>

    <div class="survey-card">
      <h1 class="title">
        Прогнозирование риска развития<br/>сердечно-сосудистых заболеваний
      </h1>

      <div class="progress-bar">
        <div
          v-for="(step, index) in stepsMeta"
          :key="step.id"
          class="step"
          :class="{ active: index <= currentStepIndex }"
        ></div>
      </div>

      <div class="step-content">
        <p class="question-text">{{ currentStepData.title }}</p>

        <component 
          :is="stepComponents[currentStepIndex]" 
          v-if="stepComponents[currentStepIndex]"
          :form="form" 
        />
        <div v-else class="form-container">
          <p style="text-align: center; color: gray;">Шаг в разработке...</p>
        </div>
      </div>

      <SurveyNavigation 
        :current-step-index="currentStepIndex"
        :is-last-step="isLastStep"
        :is-next-btn-disabled="isNextBtnDisabled"
        @next="nextStep"
        @prev="prevStep"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

import SurveyNavigation from '@/components/survey/SurveyNavigation.vue'
import Step1 from '@/components/survey/Step1.vue'
import Step2 from '@/components/survey/Step2.vue'
import Step3 from '@/components/survey/Step3.vue'

const stepComponents = [Step1, Step2, Step3]

const form = ref<Record<string, any>>({
  gender: null, age: null, height: null, weight: null,
  hipMeasurement: null, alcohol: null, profession: null
})

const stepsMeta = ref([
  { id: 1, title: 'Укажите, пожалуйста, ваши основные параметры:', requiredFields: ['gender', 'age', 'height', 'weight'] },
  { id: 2, title: 'Употребляете ли вы алкоголь?', requiredFields: ['alcohol'] },
  { id: 3, title: 'Выберите ваш род деятельности', requiredFields: ['profession'] },
  { id: 4, title: 'Образ жизни', requiredFields: [] }
])

const currentStepIndex = ref(0)
const currentStepData = computed(() => stepsMeta.value[currentStepIndex.value])
const isLastStep = computed(() => currentStepIndex.value === stepsMeta.value.length - 1)

const isNextBtnDisabled = computed(() => {
  const required = currentStepData.value.requiredFields
  return required.some(field => {
    const value = form.value[field]
    return value === null || value === undefined || value === ''
  })
})

const nextStep = () => {
  if (isNextBtnDisabled.value) return
  if (!isLastStep.value) { currentStepIndex.value++ } 
  else { console.log('Опрос завершен! Данные:', form.value) }
}

const prevStep = () => {
  if (currentStepIndex.value > 0) { currentStepIndex.value-- }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.survey-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  overflow: hidden;
  padding: 40px 20px;
  box-sizing: border-box;
  gap: 24px;
}

.logo-container {
  flex-shrink: 0; 
  .logo { 
    width: 100px;
    height: 100px;
    object-fit: contain; 
    }
}

.survey-card {
  background: $color-white;
  border-radius: 24px;
  padding: 32px 60px;
  width: 750px;
  max-width: 100%;
  box-shadow: 0 10px 30px rgba(7, 14, 44, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 0;
  overflow-y: auto;
  
  &::-webkit-scrollbar { width: 6px; color: $color-accent; }
  &::-webkit-scrollbar-track {
    background: transparent;
    margin: 20px 0;
    }
  &::-webkit-scrollbar-thumb {
    background-color: $color-accent;
    border-radius: 10px; 
    }
  &::-webkit-scrollbar-thumb:hover { background-color: $color-accent; }
}

.title {
  font-size: 1.4rem;
  font-weight: 700;
  text-align: center;
  color: $color-text;
  margin-bottom: 32px;
  line-height: 1.4;
  flex-shrink: 0;
}

.progress-bar {
  display: flex;
  width: 95%;
  margin-bottom: 24px;
  gap: 0;
  flex-shrink: 0;
  .step {
    flex: 1;
    height: 8px;
    background-color: $color-step-bg;
    transition: background-color 0.3s ease;
    border-radius: 0;
    border-right: 10px solid $color-white;
    &:first-child { border-radius: 4px 0 0 4px; }
    &:last-child { border-radius: 0 4px 4px 0; border-right: none; }
    &.active { background-color: $color-accent; }
  }
}

.step-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  margin-bottom: 40px;
}

.question-text {
  text-align: center;
  font-size: 1.1rem;
  color: $color-text;
}
</style>