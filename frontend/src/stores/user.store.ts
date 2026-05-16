import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const email = ref<string | null>(null)
  const role = ref<string | null>(null)

  const isAuthenticated = computed(() => email.value !== null)
  const isExtended = computed(() => role.value === 'ROLE_DOCTOR_EXTENDED')

  async function login(emailInput: string, password: string, rememberMe = false): Promise<void> {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: emailInput, password, rememberMe }),
    })

    if (!res.ok) throw new Error('Неверный логин или пароль')

    const data = await res.json()
    email.value = data.email
    role.value = data.role
  }

  async function fetchMe(): Promise<void> {
    const res = await fetch('/api/auth/me')
    if (!res.ok) return

    const data = await res.json()
    email.value = data.email
    role.value = data.role
  }

  async function logout(): Promise<void> {
    await fetch('/api/auth/logout', { method: 'POST' })
    email.value = null
    role.value = null
    router.push('/login')
  }

  return { email, role, isAuthenticated, isExtended, login, fetchMe, logout }
})
