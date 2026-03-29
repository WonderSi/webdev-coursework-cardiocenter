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
        <div class="step active"></div>
        <div class="step"></div>
        <div class="step"></div>
        <div class="step"></div>
      </div>

      <div class="step-content">
        <p class="question-text">Укажите, пожалуйста, ваш пол и возраст:</p>

        <div class="form-container">
          <div class="form-group">
            <label class="label">Пол:</label>
            <div class="radio-group">
              <label class="radio-label">
                <span>Мужской</span>
                <input type="radio" name="gender" value="male" v-model="form.gender" />
              </label>
              <label class="radio-label">
                <span>Женский</span>
                <input type="radio" name="gender" value="female" v-model="form.gender" />
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="label">Возраст:</label>
            <div class="input-group">
              <input type="number" class="age-input" placeholder="27" v-model="form.age" />
              <span class="unit">лет</span>
            </div>
          </div>
        </div>
      </div>

      <button class="next-btn" @click="nextStep">Далее</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// храним ответы пользователя
const form = ref({
  gender: null,
  age: null
})

const nextStep = () => {
  console.log('Данные шага 1:', form.value)
  // Позже здесь будет логика переключения шагов или вызов стора
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
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
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
  gap: 8px;
  width: 90%;
  margin-bottom: 24px;

  .step {
    flex: 1;
    height: 8px;
    background-color: $color-accent-lighter; // Светлый для неактивных шагов
    border-radius: 4px;
    transition: background-color 0.3s ease;

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
      accent-color: $color-accent; // Красим системный радиобаттон в синий
    }
  }
}

.input-group {
  display: flex;
  align-items: center;
  gap: 12px;

  .age-input {
    width: 50px;
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
  transition: background 0.2s ease;

  &:hover {
    background: $color-accent-lighter;
  }
}
</style>