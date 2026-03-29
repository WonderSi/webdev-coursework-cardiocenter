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

        <div v-if="currentStepIndex === 0" class="form-container">
          <div class="form-group">
            <label class="label">Пол:</label>
            <div class="radio-group-horizontal">
              <label class="radio-label">
                <span>Мужской</span>
                <input type="radio" :value="1" v-model="form.gender" />
              </label>
              <label class="radio-label">
                <span>Женский</span>
                <input type="radio" :value="2" v-model="form.gender" />
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="label">Возраст:</label>
            <div class="input-group">
              <input type="number" class="input-field" placeholder="27" v-model="form.age" />
              <span class="unit">лет</span>
            </div>
          </div>
          
          <div class="form-group">
            <label class="label">Рост:</label>
            <div class="input-group">
              <input type="number" class="input-field" placeholder="165" v-model="form.height" />
              <span class="unit">см</span>
            </div>
          </div>

          <div class="form-group">
            <label class="label">Вес:</label>
            <div class="input-group">
              <input type="number" class="input-field" placeholder="60" v-model="form.weight" />
              <span class="unit">кг</span>
            </div>
          </div>

          <div class="form-group">
            <label class="label">Объём бёдер:</label>
            <div class="input-group">
              <input type="number" class="input-field" placeholder="90" v-model="form.hipMeasurement" />
              <span class="unit">см</span>
            </div>
          </div>
        </div>

        <div v-if="currentStepIndex === 1" class="form-container">
           <div class="radio-group-vertical">
            <label class="radio-label">
              <input type="radio" :value="3" v-model="form.alcohol" />
              <span>Употребляю</span>
            </label>
            <label class="radio-label">
              <input type="radio" :value="2" v-model="form.alcohol" />
              <span>Употреблял ранее</span>
            </label>
            <label class="radio-label">
              <input type="radio" :value="1" v-model="form.alcohol" />
              <span>Никогда не употреблял</span>
            </label>
          </div>
        </div>

        <div v-if="currentStepIndex > 1" class="form-container">
           <p style="text-align: center; color: gray;">Здесь будет следующий вопрос...</p>
        </div>

      </div>

      <div class="actions">
        <button 
          class="prev-btn" 
          :class="{ hidden: currentStepIndex === 0 }"
          @click="prevStep"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
          Назад
        </button>

        <button 
          class="next-btn" 
          :class="{ disabled: isNextBtnDisabled, expanded: currentStepIndex === 0 }"
          :disabled="isNextBtnDisabled"
          @click="nextStep"
        >
          {{ isLastStep ? 'Завершить' : 'Далее' }}
          <svg v-if="!isLastStep" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const form = ref<Record<string, any>>({
  gender: null,
  age: null,
  height: null,
  weight: null,
  hipMeasurement: null,
  alcohol: null
})

const stepsMeta = ref([
  {
    id: 1,
    title: 'Укажите, пожалуйста, ваши основные параметры:',
    requiredFields: ['gender', 'age', 'height', 'weight']
  },
  {
    id: 2,
    title: 'Употребляете ли вы алкоголь?',
    requiredFields: ['alcohol']
  },
  {
    id: 3,
    title: 'Медицинские показатели',
    requiredFields: []
  },
  {
    id: 4,
    title: 'Образ жизни',
    requiredFields: []
  }
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
  if (!isLastStep.value) {
    currentStepIndex.value++
  } else {
    console.log('Опрос завершен! Данные:', form.value)
  }
}

const prevStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value--
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.survey-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 24px;
}

.logo-container {
  .logo {
    width: 100px;
    height: 100px;
    object-fit: contain;
  }
}

.survey-card {
  background: $color-white;
  border-radius: 24px;
  padding: 40px 60px;
  width: 100%;
  max-width: 700px;
  box-shadow: 0 10px 30px rgba(7, 14, 44, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 1.4rem;
  font-weight: 700;
  text-align: center;
  color: $color-text;
  margin-bottom: 32px;
  line-height: 1.4;
}

.progress-bar {
  display: flex;
  width: 95%;
  margin-bottom: 24px;
  gap: 0;

  .step {
    flex: 1;
    height: 8px;
    background-color: $color-accent-lighter; // цвет неактивного шага
    transition: background-color 0.3s ease;
    border-radius: 0;
    border-right: 10px solid $color-white;

    &:first-child {
      border-radius: 4px 0 0 4px;
    }

    &:last-child {
      border-radius: 0 4px 4px 0;
      border-right: none;
    }

    &.active {
      background-color: $color-accent;
    }
  }
}

.step-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  margin-bottom: 40px;
  min-height: 150px;
}

.question-text {
  text-align: center;
  font-size: 1.1rem;
  color: $color-text;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 24px;
}

.label {
  width: 116px;
  font-weight: 800;
  color: $color-text;
  font-size: 1rem;
}

/* Радио-кнопки в ряд (горизонтальные) */
.radio-group-horizontal {
  display: flex;
  gap: 32px;
  align-items: center;

  .radio-label {
    display: flex;
    flex-direction: row-reverse;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    font-size: 0.95rem;
    color: $color-text;

    input[type="radio"] {
      cursor: pointer;
      width: 16px;
      height: 16px;
      accent-color: $color-accent;
    }
  }
}

/* Радио-кнопки в столбик (вертикальные) */
.radio-group-vertical {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;

  .radio-label {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    font-size: 1rem;
    color: $color-text;

    input[type="radio"] {
      cursor: pointer;
      width: 18px;
      height: 18px;
      accent-color: $color-accent;
    }
  }
}

.input-group {
  display: flex;
  align-items: center;
  gap: 12px;

  .input-field {
    width: 60px;
    height: 24px;
    padding: 8px 12px;
    border: 1.5px solid $color-secondary;
    border-radius: 8px;
    font-size: 1rem;
    font-family: inherit;
    text-align: center;
    outline: none;
    transition: border-color 0.2s;
    color: $color-text;

    &::placeholder {
      color: rgba($color-secondary, 0.8); 
      font-weight: 400;
    }

    &:focus {
      border-color: $color-accent;
    }

    &::-webkit-outer-spin-button,
    &::-webkit-inner-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }
  }

  .unit {
    color: $color-secondary;
    font-size: 1rem;
  }
}

/* КОНТЕЙНЕР АНИМИРОВАННЫХ КНОПОК */
.actions {
  display: flex;
  justify-content: space-between;
  width: 100%;
  max-width: 360px; // Общая ширина, которую займет кнопка "Далее" на 1 шаге
  height: 48px;
}

.prev-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-right: 12px;
  width: 130px;
  height: 100%;
  background: transparent;
  color: $color-accent;
  border: 1.5px solid $color-accent;
  border-radius: $radius-rect-buttons;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  
  /* плавное появление */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  white-space: nowrap; // Не дает тексту съезжать при сужении

  &:hover {
    background: rgba($color-accent, 0.05);
  }

  /* на первом шаге кпноки нет */
  &.hidden {
    width: 0;
    opacity: 0;
    padding: 0;
    border-width: 0;
    pointer-events: none;
  }
}

.next-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-left: 12px;
  width: 130px;
  height: 100%;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  
  /* плавное сужение */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;

  &:hover:not(.disabled) {
    background: $color-accent-lighter;
  }

  &.disabled {
    background: $color-accent-lighter;
    cursor: not-allowed;
  }

  &.expanded {
    width: 100%; // всю ширину max-width: 360px
  }
}
</style>