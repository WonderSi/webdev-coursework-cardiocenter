<script setup lang="ts">
import { onScopeDispose, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.store'

const router = useRouter()
const userStore = useUserStore()
const email = shallowRef('')
const password = shallowRef('')
const isPasswordVisible = shallowRef(false)
const loading = shallowRef(false)
const error = shallowRef('')
let active = true

onScopeDispose(() => {
  active = false
  password.value = ''
})

async function handleLogin() {
  if (loading.value) return
  error.value = ''
  loading.value = true
  try {
    await userStore.login(email.value.trim(), password.value)
    if (active) await router.push('/dashboards')
  } catch (cause) {
    if (active) error.value = cause instanceof Error
      ? cause.message
      : 'Не удалось войти. Попробуйте ещё раз.'
  } finally {
    if (active) loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <img src="@/assets/logo.png" alt="НИИ КПССЗ" class="login-logo" />
    <section class="login-card" aria-labelledby="login-title">
      <header class="login-header">
        <h1 id="login-title" class="login-card-header">Добро пожаловать</h1>
        <p class="login-card-header-subtext">Войдите, чтобы использовать сервис</p>
      </header>

      <form class="login-form" :aria-busy="loading" @submit.prevent="handleLogin">
        <div class="form-field">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="email"
            type="email"
            name="email"
            class="login-input"
            autocomplete="username"
            autocapitalize="none"
            spellcheck="false"
            placeholder="doctor@niikpssz.ru"
            required
          />
        </div>
        <div class="form-field">
          <label for="password">Пароль</label>
          <div class="password-wrapper">
            <input
              id="password"
              v-model="password"
              :type="isPasswordVisible ? 'text' : 'password'"
              name="password"
              class="login-input"
              autocomplete="current-password"
              placeholder="••••••••"
              required
            />
            <button
              class="eye-btn"
              type="button"
              :aria-label="isPasswordVisible ? 'Скрыть пароль' : 'Показать пароль'"
              :aria-pressed="isPasswordVisible"
              aria-controls="password"
              @click="isPasswordVisible = !isPasswordVisible"
            >
              <svg class="eye-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <template v-if="isPasswordVisible">
                  <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="2" />
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" />
                </template>
                <path
                  v-else
                  d="M15 18L14.278 14.75M2 8C2.74835 10.0508 4.10913 11.8219 5.8979 13.0733C7.68667 14.3247 9.81695 14.9959 12 14.9959C14.1831 14.9959 16.3133 14.3247 18.1021 13.0733C19.8909 11.8219 21.2516 10.0508 22 8M20 15L18.274 12.95M4 15L5.726 12.95M9 18L9.722 14.75"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>
        <div class="login-actions">
          <p id="login-error" class="login-error" role="alert">{{ error }}</p>
          <button class="login-btn" type="submit" :disabled="loading">
            {{ loading ? 'Вход...' : 'Войти' }}
          </button>
        </div>
      </form>

      <RouterLink class="backToHomePage-btn" to="/">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path d="m15 18-6-6 6-6" />
        </svg>
        Вернуться
      </RouterLink>
    </section>
  </main>
</template>

<style scoped lang="scss">
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  padding: max(16px, env(safe-area-inset-top)) max(16px, env(safe-area-inset-right))
    max(16px, env(safe-area-inset-bottom)) max(16px, env(safe-area-inset-left));
  box-sizing: border-box;
}

.login-logo {
  display: block;
  flex-shrink: 0;
  width: clamp(72px, 14vh, 100px);
  height: auto;
}

.login-card {
  box-sizing: border-box;
  width: 100%;
  max-width: 420px;
  min-width: 0;
  padding: clamp(24px, 5vw, 40px) clamp(20px, 7vw, 50px);
  border-radius: $radius-basic-card;
  background: $color-white;
  color: $color-text;
}

.login-header {
  margin-bottom: 40px;
  text-align: center;
}

.login-card-header {
  font-size: clamp(1.25rem, 5vw, 1.5rem);
  font-weight: 700;
  line-height: 1.3;
}

.login-card-header-subtext {
  margin-top: 10px;
  color: $color-secondary;
  font-size: 1.1rem;
  font-weight: 300;
  line-height: 1.4;
}

.login-form,
.form-field {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.login-form {
  gap: 8px;
}

.form-field + .form-field {
  margin-top: 24px;
}

.form-field {
  gap: 8px;

  label {
    font-size: 0.9rem;
    font-weight: 600;
  }
}

.login-input {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  min-height: 46px;
  padding: 12px 16px;
  border: 1.5px solid $color-secondary;
  border-radius: 16px;
  background: $color-white;
  color: $color-text;
  font: inherit;
  font-size: 1rem;

  &:focus {
    border-color: $color-accent;
    outline: none;
  }

  &::placeholder {
    color: rgba($color-secondary, 0.6);
  }
}

.login-error {
  min-height: 18px;
  margin-bottom: 10px;
  color: $color-red;
  font-size: 0.875rem;
  line-height: 1.4;
  text-align: center;
  overflow-wrap: anywhere;
}

.login-btn {
  width: 100%;
  min-height: 46px;
  padding: 12px;
  border: 0;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: $color-white;
  font: inherit;
  font-weight: 600;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: $color-accent-lighter;
  }

  &:disabled {
    opacity: 0.6;
    cursor: wait;
  }
}

.backToHomePage-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  margin-top: 24px;
  color: $color-accent;
  text-decoration: none;
}

.back-icon {
  width: 20px;
  height: 20px;
}

.login-btn:focus-visible,
.backToHomePage-btn:focus-visible {
  @include focus-ring;
}

.password-wrapper {
  --password-surface: #{$color-white};
  position: relative;
  min-width: 0;

  .login-input {
    padding-right: 44px;
  }

  &:has(input:autofill) {
    --password-surface: #{$color-ui-surface-hover};
  }

  &:has(input:-webkit-autofill) {
    --password-surface: #{$color-ui-surface-hover};
  }
}

.login-input:autofill {
  color: $color-text;
  box-shadow: 0 0 0 1000px $color-ui-surface-hover inset;
}

.login-input:-webkit-autofill {
  -webkit-text-fill-color: $color-text;
  box-shadow: 0 0 0 1000px $color-ui-surface-hover inset;
}

.eye-btn {
  position: absolute;
  inset: 2px 2px 2px auto;
  display: grid;
  place-items: center;
  width: 40px;
  padding: 0;
  border: 0;
  border-radius: 0 14px 14px 0;
  background: var(--password-surface);
  color: $color-secondary;
  cursor: pointer;

  &::before {
    content: '';
    position: absolute;
    inset: 0 100% 0 auto;
    width: 24px;
    background: linear-gradient(to right, transparent, var(--password-surface));
    pointer-events: none;
  }

  &:focus-visible {
    outline: 1px solid $color-accent;
    outline-offset: -4px;
  }
}

.eye-icon {
  width: 24px;
  height: 24px;
}

@media (max-height: 600px) {
  .login-page {
    gap: 16px;
  }

  .login-logo {
    width: 64px;
  }

  .login-card {
    padding-block: 24px;
  }

  .login-header {
    margin-bottom: 24px;
  }

  .form-field + .form-field {
    margin-top: 16px;
  }

  .backToHomePage-btn {
    margin-top: 16px;
  }
}
</style>
