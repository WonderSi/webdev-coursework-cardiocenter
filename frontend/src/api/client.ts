import router from '@/router'
import { useUserStore } from '@/stores/user.store'

export async function apiFetch(input: RequestInfo, init?: RequestInit): Promise<Response> {
  const res = await fetch(input, init)
  if (res.status === 401) {
    const userStore = useUserStore()
    userStore.email = null
    userStore.role = null
    router.push('/login')
  }
  return res
}
