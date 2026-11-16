import { beforeEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import router from '@/router'
import { useUserStore } from '@/stores/user.store'
import { apiFetch } from '@/api/client'
import { adminSections, type AuthUser } from '@/types/admin-section'
import { useDoctorSurveyStore, useSurveyStore } from '@/stores/survey.store'

test('doctor questionnaire saves the submitted snapshot once and keeps public answers independent', async () => {
  useUserStore().setUser({ ...current, allowedSections: ['UPLOAD_DATA'] })
  const doctor = useDoctorSurveyStore()
  const publicSurvey = useSurveyStore()
  doctor.loadConfig()
  publicSurvey.loadConfig()
  Object.assign(doctor.answers, {
    modelKind: 'city', gender: 1, age: 45, height: 180, weight: 80,
    alcohol: 1, profession: 2, region: 1, chronicLungDisease: 0,
  })
  const writes: Record<string, unknown>[] = []
  globalThis.fetch = async (input, init) => {
    if (String(input).endsWith('/survey/submit')) return response({
      predictions: [{ probability: 12, name: 'Risk', modelKind: 'city', prediction: 0 }],
      modelVersion: 'test',
    })
    if (String(input).endsWith('/patients')) {
      writes.push(JSON.parse(String(init?.body)))
      return new Response(null, { status: 200 })
    }
    return response(current)
  }
  assert.equal(await doctor.submit(), true)
  assert.equal(publicSurvey.submitted, false)
  assert.equal(publicSurvey.answers.age, null)
  doctor.answers.age = 99
  await Promise.all([doctor.savePatient(), doctor.savePatient()])
  assert.equal(writes.length, 1)
  assert.equal(writes[0]?.age, 45)
  assert.equal(writes[0]?.year, 2021)
  assert.equal(writes[0]?.gender, 1)
  assert.equal('genderId' in writes[0]!, false)
  assert.equal(doctor.patientSaved, true)
  await doctor.savePatient()
  assert.equal(writes.length, 1)
  useUserStore().clearUser()
  assert.equal(doctor.submitted, false)
  assert.equal(doctor.patientSaved, false)
})

test('doctor questionnaire guards direct results and saving without upload permission', async () => {
  useUserStore().setUser(current)
  await router.push('/questionnaire/results')
  assert.equal(router.currentRoute.value.path, '/questionnaire')
  const doctor = useDoctorSurveyStore()
  doctor.loadConfig()
  Object.assign(doctor.answers, { modelKind: 'region', age: 50 })
  globalThis.fetch = async () => response({ predictions: [{ probability: 10 }] })
  assert.equal(await doctor.submit(), true)
  let writes = 0
  globalThis.fetch = async () => { writes++; return response({}) }
  await doctor.savePatient()
  assert.equal(writes, 0)
  assert.equal(doctor.patientSaved, false)
  assert.match(doctor.patientError, /прав/)
})

test('questionnaire preserves results after a failed save and ignores a prediction after reset', async () => {
  current = { ...current, allowedSections: ['UPLOAD_DATA'] }
  useUserStore().setUser(current)
  const doctor = useDoctorSurveyStore()
  doctor.loadConfig()
  Object.assign(doctor.answers, { modelKind: 'region', age: 50 })
  globalThis.fetch = async () => response({ predictions: [{ probability: 10 }] })
  await doctor.submit()
  globalThis.fetch = async () => response({ detail: 'Test failure' }, 400)
  await doctor.savePatient()
  assert.equal(doctor.patientSaved, false)
  assert.equal(doctor.submitted, true)
  assert.equal(doctor.predictions[0]?.probability, 10)
  assert.equal(doctor.patientError, 'Test failure')
  globalThis.fetch = async () => new Response(null, { status: 200 })
  await doctor.savePatient()
  assert.equal(doctor.patientSaved, true)
  assert.equal(doctor.patientError, '')
  doctor.loadConfig()
  doctor.answers.modelKind = 'city'
  let finish!: (response: Response) => void
  globalThis.fetch = () => new Promise(resolve => { finish = resolve })
  const pending = doctor.submit()
  doctor.reset()
  finish(response({ predictions: [{ probability: 99 }] }))
  assert.equal(await pending, false)
  assert.equal(doctor.submitted, false)
  assert.equal(doctor.predictions.length, 0)
})

let current: AuthUser
let requests: { path: string; init?: RequestInit }[]
const response = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status, headers: { 'Content-Type': 'application/json' },
})

beforeEach(async () => {
  setActivePinia(createPinia())
  await router.push('/')
  current = { email: 'doctor@example.test', role: 'ROLE_DOCTOR', allowedSections: [] }
  requests = []
  globalThis.fetch = async (input, init) => {
    const path = String(input)
    requests.push({ path, ...(init ? { init } : {}) })
    if (path === '/api/auth/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'test-csrf' })
    return response(current)
  }
})

test('empty permissions deny all sections; super admin can access all', () => {
  const user = useUserStore()
  user.setUser(current)
  for (const { code } of adminSections) assert.equal(user.canAccessSection(code), false)
  user.setUser({ ...current, role: 'ROLE_SUPER_ADMIN' })
  for (const { code } of adminSections) assert.equal(user.canAccessSection(code), true)
})

test('retrain panel permission is independent from model metrics', () => {
  const user = useUserStore()
  user.setUser({ ...current, allowedSections: ['MODEL_TRAINING'] })
  assert.equal(user.canAccessSection('RETRAIN'), false)
  user.setUser({ ...current, allowedSections: ['RETRAIN'] })
  assert.equal(user.canAccessSection('RETRAIN'), true)
  assert.equal(user.canAccessSection('MODEL_TRAINING'), false)
})

test('closed dashboard redirects to empty panel before any dashboard request', async () => {
  useUserStore().setUser(current)
  await router.push('/dashboards')
  assert.equal(router.currentRoute.value.path, '/panel')
  assert.equal(requests.some(({ path }) => path === '/api/admin/dashboard'), false)
})

test('direct navigation respects current server permissions and doctors cannot manage accounts', async () => {
  current.allowedSections = ['UPLOAD_DATA']
  useUserStore().setUser(current)
  await router.push('/upload-data')
  assert.equal(router.currentRoute.value.path, '/upload-data')
  await router.push('/model-training')
  assert.equal(router.currentRoute.value.path, '/panel')
  await router.push('/doctors')
  assert.equal(router.currentRoute.value.path, '/')
})

test('permission changes are loaded again on navigation', async () => {
  current.allowedSections = ['VIEW_DATA']
  useUserStore().setUser(current)
  await router.push('/view-data')
  assert.equal(router.currentRoute.value.path, '/view-data')
  current = { ...current, allowedSections: [] }
  await router.push('/panel')
  await router.push('/view-data')
  assert.equal(router.currentRoute.value.path, '/panel')
})

test('login and refresh replace permissions from server responses', async () => {
  const user = useUserStore()
  current.allowedSections = ['UPLOAD_DATA']
  await user.login(current.email, 'UnusedTestPassword123!')
  assert.deepEqual([...user.allowedSections], ['UPLOAD_DATA'])
  current = { ...current, allowedSections: ['DASHBOARDS'] }
  assert.equal(await user.refreshSession(), true)
  assert.deepEqual([...user.allowedSections], ['DASHBOARDS'])
  assert.deepEqual(requests.find(({ path }) => path === '/api/auth/refresh')?.init?.headers,
    { 'X-XSRF-TOKEN': 'test-csrf' })
})

test('login distinguishes invalid credentials, denied access, rate limits and unavailable service', async () => {
  const user = useUserStore()
  for (const [status, message] of [
    [401, 'Неверный логин или пароль'],
    [403, 'Вход для этой учётной записи запрещён.'],
    [429, 'Слишком много попыток входа. Попробуйте позже.'],
    [502, 'Сервис входа временно недоступен. Попробуйте позже.'],
  ] as const) {
    globalThis.fetch = async () => response({}, status)
    await assert.rejects(user.login(current.email, 'UnusedTestPassword123!'), { message })
    assert.equal(user.isAuthenticated, false)
  }
  globalThis.fetch = async () => { throw new TypeError('Failed to fetch') }
  await assert.rejects(user.login(current.email, 'UnusedTestPassword123!'), {
    message: 'Не удалось связаться с сервером. Попробуйте позже.',
  })
})

test('API 403 returns the error to the caller and never retries or navigates away', async () => {
  useUserStore().setUser(current)
  const routeBefore = router.currentRoute.value.fullPath
  globalThis.fetch = async (input) => {
    const path = String(input)
    requests.push({ path })
    return path === '/api/auth/me' ? response(current) : response({ code: 'FORBIDDEN' }, 403)
  }
  assert.equal((await apiFetch('/api/admin/upload', { method: 'POST' })).status, 403)
  assert.equal(router.currentRoute.value.fullPath, routeBefore)
  assert.equal(useUserStore().isAuthenticated, true)
  assert.equal(requests.filter(({ path }) => path === '/api/admin/upload').length, 1)
})

test('expired access refreshes once and applies new sections before retry', async () => {
  useUserStore().setUser(current)
  let attempts = 0
  globalThis.fetch = async (input) => {
    const path = String(input)
    requests.push({ path })
    if (path === '/api/auth/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'test-csrf' })
    if (path === '/api/auth/refresh') return response({ ...current, allowedSections: ['UPLOAD_DATA'] })
    return response({}, ++attempts === 1 ? 401 : 200)
  }
  assert.equal((await apiFetch('/api/admin/upload/template')).status, 200)
  assert.equal(attempts, 2)
  assert.equal(useUserStore().canAccessSection('UPLOAD_DATA'), true)
})

test('logout uses CSRF and clears permissions', async () => {
  current.allowedSections = ['DASHBOARDS']
  const user = useUserStore()
  user.setUser(current)
  await user.logout()
  assert.equal(user.isAuthenticated, false)
  assert.deepEqual([...user.allowedSections], [])
  assert.deepEqual(requests.find(({ path }) => path === '/api/auth/logout')?.init?.headers,
    { 'X-XSRF-TOKEN': 'test-csrf' })
})
