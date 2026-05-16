<template>
  <div class="survey-page">
    <Transition name="fade">
      <div v-if="hasErrors" class="error-banner">
        <span>{{ currentErrorMessage }}</span>
      </div>
    </Transition>

    <div class="smaller-logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
    </div>

    <div class="survey-card">
      <h1 class="title">
        Прогнозирование риска развития<br/>сердечно-сосудистых заболеваний
      </h1>

      <div v-if="loading" class="loading">
        <p>Загрузка опроса...</p>
      </div>

      <template v-else-if="currentGroup">
        <div class="progress-bar">
          <div
            v-for="(_, index) in store.config!.groups"
            :key="index"
            class="step"
            :class="{ active: index <= currentStepIndex }"
          ></div>
        </div>

        <div class="step-content">
          <p class="question-text">{{ currentGroup.title }}</p>
          <div class="divider"></div>

          <component
            :is="stepComponent"
            :group="currentGroup"
            :answers="store.answers"
            :errors="fieldErrors"
          />
        </div>

        <SurveyNavigation
          :current-step-index="currentStepIndex"
          :is-last-step="isLastStep"
          :is-next-btn-disabled="isNextDisabled || store.loading"
          @next="handleNext"
          @prev="prevStep"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useSurveyStore } from '@/stores/survey.store'

import SurveyNavigation from '@/components/survey/SurveyNavigation.vue'
import Step1 from '@/components/survey/Step1.vue'
import Step2 from '@/components/survey/Step2.vue'
import Step3 from '@/components/survey/Step3.vue'
import Step4 from '@/components/survey/Step4.vue'
import Step5 from '@/components/survey/Step5.vue'
import Step6 from '@/components/survey/Step6.vue'
import Step7 from '@/components/survey/Step7.vue'

const router = useRouter()
const store = useSurveyStore()
const loading = ref(true)

const currentStepIndex = ref(0)
const showErrors = ref(false)

const currentGroup = computed(() =>
  store.config?.groups?.[currentStepIndex.value] ?? null
)
const isLastStep = computed(() =>
  store.config ? currentStepIndex.value === store.config.groups.length - 1 : false
)

const stepComponent = computed(() => {
  const map: Record<number, any> = {
    0: Step1,
    1: Step2,
    2: Step3,
    3: Step4,
    4: Step5,
    5: Step6,
    6: Step7,
  }
  return map[currentStepIndex.value] ?? Step1
})

const fieldErrors = computed<Record<string, string>>(() => {
  const errors: Record<string, string> = {}
  const group = currentGroup.value
  if (!group) return errors

  for (const field of group.fields) {
    const value = store.answers[field.key]
    const isEmpty = value === null || value === undefined || value === ''

    // Required
    if (group.requiredFields.includes(field.key) && isEmpty) {
      errors[field.key] = 'Обязательное поле'
      continue
    }

    // Number
    if (field.type === 'number' && !isEmpty && field.validation) {
      const num = Number(value)
      if (isNaN(num)) {
        errors[field.key] = 'Введите число'
      } else if (field.validation.min !== undefined && num < field.validation.min) {
        errors[field.key] = `Минимум ${field.validation.min}`
      } else if (field.validation.max !== undefined && num > field.validation.max) {
        errors[field.key] = `Максимум ${field.validation.max}`
      }
    }

    // Валидация года
    if (field.yearKey && value === 1) {
      const yearValue = store.answers[field.yearKey]
      if (yearValue !== null && yearValue !== undefined && yearValue !== '') {
        const MINYEAR = 1900
        const year = Number(yearValue)
        const currentYear = new Date().getFullYear()
        const age = Number(store.answers.age)
        const birthYear = age ? currentYear - (age + 1) : MINYEAR
        if (isNaN(year)) {
          errors[field.yearKey] = 'Введите корректный год'
        } else if (year < MINYEAR) {
          errors[field.yearKey] = 'Год должен быть не ранее 1900'
        } else if (year > currentYear) {
          errors[field.yearKey] = `Год не может быть больше ${currentYear}`
        } else if (age && year < birthYear) {
          errors[field.yearKey] = `Год диагноза не может быть раньше года рождения (${birthYear})`
        }
      }
    }
  }

  return errors
})

const hasErrors = computed(() => Object.keys(fieldErrors.value).length > 0)

const currentErrorMessage = computed(() => {
  const errorsList = Object.values(fieldErrors.value)
  return errorsList.length > 0 ? errorsList[0] : 'Пожалуйста, проверьте введённые данные'
})

watch(() => store.answers, () => {
  showErrors.value = false
}, { deep: true })

const isNextDisabled = computed(() => hasErrors.value)

const handleNext = async () => {
  if (hasErrors.value) {
    showErrors.value = true
    return
  }

  if (isLastStep.value) {
    const success = await store.submit()
    if (success) {
      router.push('/results')
    } else {
      alert('Ошибка при отправке. Попробуйте ещё раз.')
    }
  } else {
    currentStepIndex.value++
    showErrors.value = false
  }
}

const prevStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value-- 
    showErrors.value = false
  }
  
}

onMounted(() => {
  store.loadConfig()
  loading.value = false
})
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
  overflow-y: scroll;

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
  font-size: 1.2rem;
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
    background-color: $color-accent-lighter2;
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
  margin-bottom: 40px;
  gap: 20px;
}

.question-text {
  text-align: center;
  font-size: 1.1rem;
  color: $color-text;
}

.loading {
  font-size: 1.1rem;
  color: $color-text;
  padding: 48px 0;
}

.error-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: $color-red;
  color: $color-white;
  padding: 12px 20px;
  font-size: 0.9rem;
  font-weight: 600;
  text-align: center;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(231, 76, 60, 0.3);
}

.fade-enter-active { animation: fadeIn 0.3s ease; }
.fade-leave-active { animation: fadeIn 0.2s ease reverse; }
@keyframes fadeIn {
  0% { opacity: 0; transform: translateY(-8px); }
  100% { opacity: 1; transform: translateY(0); }
}
</style>
