<template>
  <div class="login-page">
    <div class="smaller-logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
    </div>
    
    <div class="login-card">
      <div class="login-card-header">Добро пожаловать</div>
      <div class="login-card-header-subtext">Войдите, чтобы использовать сервис</div>

      <div class="login-form">
        <div class="form-field">
          <label for="email">Email</label>
          <input
            id="email"
            type="email"
            class="login-input"
            placeholder="doctor@niikpssz.ru"
          />
        </div>
        <div class="form-field">
          <label for="password">Пароль</label>
          <input
            id="password"
            type="password"
            class="login-input"
            placeholder="••••••••"
          />
        </div>
      </div>

      <button
        class="login-btn"
        @click="handleLogin"
      >
        Войти
      </button>

      <button
        class="backToHomePage-btn"
        @click="goBackHomePage"
      > 
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      
        Вернуться на главную
      </button>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBackHomePage = () => {
  router.push('/')
}

const handleLogin = () => {
  console.log('Попытка входа...')
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
  gap: 30px;
  padding-bottom: 40px;
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
  padding: 13px;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: #ffffff;
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
  padding-top: 40px;
  border: none;
  background: transparent;
  color: $color-accent;
  font-size: 0.9rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.7;
  }
}

</style>