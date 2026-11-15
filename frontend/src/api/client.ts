import router from '@/router'
import { useUserStore } from '@/stores/user.store'

let refreshing: Promise<boolean> | null = null

export async function apiFetch(input: RequestInfo, init?: RequestInit): Promise<Response> {
  let res = await fetch(input, init)
  const userStore = useUserStore()
  if (res.status === 401) {
    refreshing ??= userStore.refreshSession().catch(() => false).finally(() => { refreshing = null })
    if (await refreshing) res = await fetch(input, init)
  }
  if (res.status === 401) {
    userStore.clearUser()
    router.push('/login')
  }
  if (res.status === 403) {
    void userStore.fetchMe().catch(() => {})
  }
  return res
}
