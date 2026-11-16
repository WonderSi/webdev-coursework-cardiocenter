import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import { useSurveyStore, useDoctorSurveyStore } from '../src/stores/survey.store'

const originalFetch = globalThis.fetch
const originalConsoleError = console.error
console.error = () => {}

function fixture(kind: 'city' | 'region') {
  setActivePinia(createPinia())
  const store = useSurveyStore()
  store.loadConfig()
  Object.assign(store.answers, {
    modelKind: kind, gender: 1, age: 45, height: 175, weight: 80,
    alcohol: 1, profession: 2, region: 1, chronicLungDisease: 0,
  })
  return store
}

function prediction(kind: 'city' | 'region') {
  return { predictions: [{ name: kind, probability: 25, modelKind: kind, prediction: 0 }], modelVersion: 'test-version' }
}

try {
  for (const kind of ['city', 'region'] as const) {
    const store = fixture(kind)
    globalThis.fetch = async (url, init) => {
      assert.equal(url, '/api/survey/submit')
      assert.equal(init?.method, 'POST')
      assert.deepEqual(JSON.parse(String(init?.body)), {
        modelKind: kind, year: kind === 'city' ? 2021 : null,
        genderId: 1, age: 45, height: 175, weight: 80,
        alcoholId: 1, professionId: 2, regionId: 1, chronicLungDisease: 0,
      })
      return Response.json(prediction(kind))
    }
    assert.equal(await store.submit(), true)
    assert.equal(store.submitted, true)
    assert.deepEqual(store.predictions, prediction(kind).predictions)
    assert.equal(store.modelVersion, 'test-version')
    assert.equal(store.submitError, '')
  }
  console.log('PASS: city/region payloads match the backend contract; optional labs stay absent; explicit No stays 0')

  const store = fixture('city')
  const failures = [
    { status: 502, body: { code: 'ML_SERVICE_ERROR' }, message: /Сервис прогнозирования временно недоступен/ },
    { status: 503, body: {}, message: /временно недоступен/ },
    { status: 504, body: {}, message: /временно недоступен/ },
    { status: 400, body: { detail: 'Проверьте поля анкеты.' }, message: /Проверьте поля анкеты/ },
    { status: 422, body: {}, message: /Сервер отклонил данные анкеты/ },
    { status: 403, body: {}, message: /Сервер запретил получение прогноза/ },
    { status: 500, body: {}, message: /Не удалось получить прогноз/ },
    { status: 200, body: { predictions: [] }, message: /пустой прогноз/ },
    { status: 200, body: null, message: /пустой прогноз/ },
    { status: 200, body: { predictions: 'invalid' }, message: /пустой прогноз/ },
  ]
  for (const { status, body, message } of failures) {
    globalThis.fetch = async () => Response.json(body, { status })
    assert.equal(await store.submit(), false)
    assert.match(store.submitError, message)
    assert.equal(store.loading, false)
    assert.equal(store.submitted, false)
    assert.deepEqual(store.predictions, [])
  }
  globalThis.fetch = async () => new Response('<html>Proxy failure</html>', { status: 502 })
  assert.equal(await store.submit(), false)
  assert.match(store.submitError, /временно недоступен/)
  globalThis.fetch = async () => new Response('not JSON')
  assert.equal(await store.submit(), false)
  assert.match(store.submitError, /некорректный ответ/)
  globalThis.fetch = async () => { throw new TypeError('fetch failed') }
  assert.equal(await store.submit(), false)
  assert.match(store.submitError, /Не удалось связаться с сервером/)
  console.log('PASS: server problems, proxy errors, invalid responses and network failures produce useful messages')

  globalThis.fetch = async () => Response.json(prediction('city'))
  assert.equal(await store.submit(), true)
  assert.equal(store.submitError, '')
  store.reset()
  assert.equal(store.submitError, '')
  assert.equal(store.submitted, false)

  let rejectRequest!: (reason: Error) => void
  const pendingStore = fixture('city')
  globalThis.fetch = () => new Promise((_resolve, reject) => { rejectRequest = reject })
  const pending = pendingStore.submit()
  pendingStore.reset()
  rejectRequest(new Error('old request failed'))
  assert.equal(await pending, false)
  assert.equal(pendingStore.submitError, '')
  assert.equal(pendingStore.loading, false)
  console.log('PASS: retry clears errors and resetting the survey prevents stale failures')

  const regionStore = fixture('region')
  const doctor = useDoctorSurveyStore()
  doctor.loadConfig()
  Object.assign(doctor.answers, regionStore.answers)
  globalThis.fetch = async () => Response.json({ code: 'ML_SERVICE_ERROR' }, { status: 502 })
  assert.equal(await doctor.submit(), false)
  assert.match(doctor.submitError, /временно недоступен/)
  console.log('PASS: doctor surveys use the same error handling')
} finally {
  globalThis.fetch = originalFetch
  console.error = originalConsoleError
}
