<template>
  <div class="login-page">
    <div class="smaller-logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
    </div>
    
    <div class="login-card">
      <div class="login-card-header">Добро пожаловать</div>
      <div class="login-card-header-subtext">Войдите, чтобы использовать сервис</div>

      <div class="login-form">
        <div class="login-fields">
          <div class="form-field">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              class="login-input"
              placeholder="doctor@niikpssz.ru"
            />
          </div>

          <div class="form-field">
            <label for="password">Пароль</label>
            <div class="password-wrapper">
              <input
                id="password"
                v-model="password"
                :type="isPasswordVisible ? 'text' : 'password'"
                class="login-input"
                placeholder="••••••••"
              />
              <button class="eye-btn" @click="togglePasswordVisibility" type="button">
                <span v-if="isPasswordVisible" v-html="eyeOpenSvg"></span>
                <span v-else v-html="eyeClosedSvg"></span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <button class="login-btn" @click="handleLogin">Войти</button>

      <button class="backToHomePage-btn" @click="goBackHomePage"> 
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        Вернуться
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Состояния формы
const email = ref('')
const password = ref('')
const isPasswordVisible = ref(false)

const recoveryEmail = ref('')

const eyeOpenSvg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 6.5C13.8387 6.49389 15.6419 7.00678 17.2021 7.97973C18.7624 8.95267 20.0164 10.3462 20.82 12C19.17 15.37 15.8 17.5 12 17.5C8.2 17.5 4.83 15.37 3.18 12C3.98362 10.3462 5.23763 8.95267 6.79788 7.97973C8.35813 7.00678 10.1613 6.49389 12 6.5ZM12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5C17 19.5 21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5ZM12 9.5C12.663 9.5 13.2989 9.76339 13.7678 10.2322C14.2366 10.7011 14.5 11.337 14.5 12C14.5 12.663 14.2366 13.2989 13.7678 13.7678C13.2989 14.2366 12.663 14.5 12 14.5C11.337 14.5 10.7011 14.2366 10.2322 13.7678C9.76339 13.2989 9.5 12.663 9.5 12C9.5 11.337 9.76339 10.7011 10.2322 10.2322C10.7011 9.76339 11.337 9.5 12 9.5ZM12 7.5C9.52 7.5 7.5 9.52 7.5 12C7.5 14.48 9.52 16.5 12 16.5C14.48 16.5 16.5 14.48 16.5 12C16.5 9.52 14.48 7.5 12 7.5Z" fill="#8A8FA8"/></svg>`
const eyeClosedSvg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M15 18L14.278 14.75M2 8C2.74835 10.0508 4.10913 11.8219 5.8979 13.0733C7.68667 14.3247 9.81695 14.9959 12 14.9959C14.1831 14.9959 16.3133 14.3247 18.1021 13.0733C19.8909 11.8219 21.2516 10.0508 22 8M20 15L18.274 12.95M4 15L5.726 12.95M9 18L9.722 14.75" stroke="#8A8FA8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`

// Логика кнопок
const goBackHomePage = () => router.push('/')

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value
}

const handleLogin = () => {
  console.log('Авторизация:', email.value, password.value)
  // Здесь будет вызов API
}

const sendRecoveryEmail = () => {
  if (!recoveryEmail.value) return alert('Введите Email')
  
  const adminMail = 'kseniagorbush@gmail.com' // НАДО ПОМЕНЯТЬ НА КОРПОРАТИВНЫЙ EMAIL или чёт такое
  const subject = encodeURIComponent('ЗАЯВКА НА ВОССТАНОВЛЕНИЕ ПАРОЛЯ')
  const body = encodeURIComponent(`# ЗАЯВКА НА ВОССТАНОВЛЕНИЕ ПАРОЛЯ\nEMAIL: ${recoveryEmail.value}`)
  
  window.location.href = `mailto:${adminMail}?subject=${subject}&body=${body}`
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  overflow: hidden;
  padding: 20px;
  gap: 24px;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.login-fields {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  label {
    font-size: 0.9rem;
    font-weight: 600;
    color: $color-text;
  }

  .login-input {
    width: 100%;
    padding: 12px 16px;
    border: 1.5px solid $color-secondary;
    border-radius: 16px;
    font-size: 1rem;
    font-family: inherit;
    color: $color-text;
    background: $color-white;
    transition: border-color 0.2s ease;
    box-sizing: border-box;

    &:focus {
      outline: none;
      border-color: $color-accent;
    }

    &::placeholder {
      color: rgba($color-secondary, 0.6);
    }
  }
}

// CARD REDO

.login-card {
  flex: 0 0 420px;
  display: flex;
  flex-direction: column;

  background: $color-white;
  border-radius: $radius-basic-card;
  padding: 40px 50px;
  filter: drop-shadow(0 5 20 #070E2C, 0.2); // поправить
}

.login-card-header {
  font-size: 1.5rem;
  font-weight: 700;
  color: $color-text;
  text-align: center;
}

.login-card-header-subtext {
  font-weight: 300;
  padding-top: 10px;
  padding-bottom: 40px;
  font-size: 1.1rem;
  color: $color-secondary;
  text-align: center;
}


.login-card-text {
  display: flex;
  flex-direction: column;
  gap: 12px;

  p {
    font-size: 0.9rem;
    line-height: 1.6;
    color: $color-text;
    text-align: justify;
  }
}

// buttons

.login-btn {
  width: 100%;
  border: none;
  margin-block: 32px;
  padding: 13px;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: $color-white;
  font-size: 1rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: $color-accent-lighter;
  }
}

.backToHomePage-btn {
  display: flex;
  justify-content: center;
  gap: 8px;
  width: 100%;
  border: none;
  background: transparent;
  color: $color-accent;
  font-size: 1rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.7;
  }
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;

  .eye-btn {
    position: absolute;
    right: 13px;
    top: 13px;
    background: none;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    color: $color-secondary;
    &:hover { opacity: 1; }
  }
}

.remember-me {
    font-weight: 400;
    display: flex;
    align-items: center;
    gap: 4px;
    color: $color-secondary;
    cursor: pointer;
    
    input[type="checkbox"] {
      appearance: none;
      -webkit-appearance: none;
      width: 16px;
      height: 16px;
      border: 1.5px solid $color-secondary;
      border-radius: 4px;
      background-color: transparent;
      cursor: pointer;
      position: relative;
      transition: all 0.2s ease;
      flex-shrink: 0;

      &:checked {
        background-color: $color-accent;
        border-color: $color-accent; 

        &::after {
          content: '';
          position: absolute;
          left: 4.2px;
          top: 1.25px;
          width: 5px;
          height: 9px;
          border: solid $color-white;
          border-width: 0 2px 2px 0;
          transform: rotate(45deg);
        }
      }
    }
  }

</style>