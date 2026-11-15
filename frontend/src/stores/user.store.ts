import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import type { AdminSection, AuthUser } from '@/types/admin-section'

export const useUserStore = defineStore('user', () => {
  const email = ref<string | null>(null)
  const role = ref<string | null>(null)
  const allowedSections = ref<AdminSection[]>([])

  function setUser(data: AuthUser): void {
    email.value = data.email
    role.value = data.role
    allowedSections.value = data.allowedSections ?? []
  }

  function clearUser(): void {
    email.value = null
    role.value = null
    allowedSections.value = []
  }

  function canAccessSection(section: AdminSection): boolean {
    return isSuperAdmin.value || (role.value === 'ROLE_DOCTOR' && allowedSections.value.includes(section))
  }

  const isAuthenticated = computed(() => email.value !== null)
  const isSuperAdmin = computed(() => role.value === 'ROLE_SUPER_ADMIN')

  const canAccessClinicalData = computed(() =>
    role.value === 'ROLE_DOCTOR' || role.value === 'ROLE_SUPER_ADMIN'
  )

  async function login(emailInput: string, password: string, rememberMe = false): Promise<void> {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: emailInput, password, rememberMe }),
    })

    if (!res.ok) throw new Error('Неверный логин или пароль')

    const data = await res.json()
    setUser(data)
  }

  async function fetchMe(): Promise<void> {
    const res = await fetch('/api/auth/me')
    if (res.status === 401) {
      if (!await refreshSession()) clearUser()
      return
    }
    if (!res.ok) throw new Error('Не удалось проверить доступ.')

    const data = await res.json()
    setUser(data)
  }

  async function refreshSession(): Promise<boolean> {
    const csrfResponse = await fetch('/api/auth/csrf')
    if (!csrfResponse.ok) return false
    const csrf = await csrfResponse.json()
    const response = await fetch('/api/auth/refresh', {
      method: 'POST', headers: { [csrf.headerName]: csrf.token },
    })
    if (!response.ok) return false
    setUser(await response.json())
    return true
  }

  async function logout(): Promise<void> {
    const csrfResponse = await fetch('/api/auth/csrf')
    if (!csrfResponse.ok) throw new Error('Не удалось выйти. Попробуйте снова.')
    const csrf = await csrfResponse.json()
    const response = await fetch('/api/auth/logout', {
      method: 'POST', headers: { [csrf.headerName]: csrf.token },
    })
    if (!response.ok) throw new Error('Не удалось выйти. Попробуйте снова.')
    clearUser()
    router.push('/login')
  }

  return { email, role, allowedSections, isAuthenticated, isSuperAdmin,
    canAccessClinicalData, canAccessSection, setUser, clearUser, login, fetchMe, refreshSession, logout }
})
