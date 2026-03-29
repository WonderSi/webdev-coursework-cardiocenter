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
            <div class="radio-group">
              <label class="radio-label">
                <span>Мужской</span>
                <input type="radio" value="male" v-model="form.gender" />
              </label>
              <label class="radio-label">
                <span>Женский</span>
                <input type="radio" value="female" v-model="form.gender" />
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

        </div>

        <div v-if="currentStepIndex === 1" class="form-container">
          <p style="text-align: center; color: gray;">Здесь будет следующий вопрос...</p>
        </div>
      </div>

      <button 
        class="next-btn" 
        :class="{ disabled: isNextBtnDisabled }"
        :disabled="isNextBtnDisabled"
        @click="nextStep"
      >
        {{ isLastStep ? 'Завершить' : 'Далее' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// Хранилище всех ответов пользователя
const form = ref<Record<string, any>>({
  gender: null,
  age: null,
  height: null,
  weight: null
})

// Конфигурация шагов
const stepsMeta = ref([
  {
    id: 1,
    title: 'Укажите, пожалуйста, ваш пол и возраст:',
    requiredFields: ['gender', 'age', 'height', 'weight'] // Эти поля обязательны для перехода
  },
  {
    id: 2,
    title: 'Дополнительная информация',
    requiredFields: [] // Шаг без обязательных полей (можно пропустить)
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

// Управление состоянием шагов
const currentStepIndex = ref(0)

const currentStepData = computed(() => stepsMeta.value[currentStepIndex.value])
const isLastStep = computed(() => currentStepIndex.value === stepsMeta.value.length - 1)

// Логика блокировки кнопки
const isNextBtnDisabled = computed(() => {
  const required = currentStepData.value.requiredFields
  
  // Проверяем, есть ли незаполненные обязательные поля
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
    console.log('Опрос завершен! Собранные данные:', form.value)
    // добавим вызов API для отправки данных
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
  margin-bottom: 24px;
  line-height: 1.4;
}

.progress-bar {
  display: flex;
  width: 90%;
  margin-bottom: 24px;
  gap: 0;

  .step {
    flex: 1;
    height: 8px;
    background-color: $color-accent-lighter; // цвет неактивного шага
    transition: background-color 0.3s ease;
    border-radius: 0;

    border-right: 10px solid $color-white;

    // Первый шаг: скруглен только слева
    &:first-child {
      border-radius: 4px 0 0 4px;
    }

    // Последний шаг: скруглен только справа
    &:last-child {
      border-radius: 0 4px 4px 0;
      border-right: none;
    }

    // Закрашиваем пройденные и текущий шаги
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
  width: 80px;
  font-weight: 800;
  color: $color-text;
  font-size: 0.95rem;
}

.radio-group {
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

.input-group {
  display: flex;
  align-items: center;
  gap: 12px;

  .input-field {
    width: 60px;
    height: 28px;
    padding: 8px 12px;
    border: 1.5px solid $color-secondary;
    border-radius: 6px;
    font-size: 1rem;
    font-family: inherit;
    text-align: center;
    outline: none;
    transition: border-color 0.2s;
    color: $color-text;

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
    font-size: 1 rem;
  }
}

.next-btn {
  width: 360px;
  padding: 16px;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover:not(.disabled) {
    background: $color-accent-lighter;
  }

  &.disabled {
    background: $color-accent-lighter; // Тусклый цвет
    cursor: not-allowed;
  }
}
</style>