import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { createPinia, setActivePinia } from 'pinia'
import { createSSRApp, h, nextTick } from 'vue'
import { renderToString } from 'vue/server-renderer'
import { createServer } from 'vite'
import vue from '@vitejs/plugin-vue'

const origin = process.argv[2] ?? 'http://localhost'
const root = fileURLToPath(new URL('../', import.meta.url))
const server = await createServer({
  root,
  configFile: false,
  envFile: false,
  plugins: [vue()],
  resolve: { alias: { '@': `${root}src` } },
  server: { middlewareMode: true, hmr: false, watch: null },
  appType: 'custom',
})
const originalFetch = globalThis.fetch
let request
globalThis.fetch = async (url, init) => {
  request = { url, payload: JSON.parse(init.body) }
  const response = await originalFetch(new URL(url, origin), init)
  assert.equal(response.status, 200, await response.clone().text())
  return response
}

try {
  const { useSurveyStore } = await server.ssrLoadModule('/src/stores/survey.store.ts')
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useSurveyStore()
  store.loadConfig()
  assert.equal(store.answers.chronicLungDisease, null)
  const fields = store.config.groups.flatMap(group => group.fields)
  const obsoleteFields = [
    'hipMeasurement', 'nonHdlCholesterol', 'vldlCholesterol', 'apolipoproteinA', 'apolipoproteinB',
    'stroke', 'strokeYear', 'heartFailure', 'heartFailureYear', 'cad', 'cadYear',
    'angina', 'anginaYear', 'myocardialInfarction', 'myocardialInfarctionYear',
    'arterialHypertension', 'arterialHypertensionYear', 'diagnoses',
  ]
  assert.ok(fields.every(field => !obsoleteFields.includes(field.key)))
  const lungGroup = store.config.groups.at(-1)
  assert.deepEqual(lungGroup.fields.map(field => field.key), ['chronicLungDisease'])
  assert.deepEqual(lungGroup.requiredFields, ['chronicLungDisease'])
  const { default: Step7 } = await server.ssrLoadModule('/src/components/survey/Step7.vue')
  const renderLungQuestion = () => renderToString(createSSRApp({
    render: () => h(Step7, { group: lungGroup, answers: store.answers, errors: {} }),
  }))
  let html = await renderLungQuestion()
  assert.equal((html.match(/type="radio"/g) ?? []).length, 2)
  assert.equal((html.match(/\schecked(?:\s|>)/g) ?? []).length, 0)
  store.answers.chronicLungDisease = 1
  html = await renderLungQuestion()
  assert.equal((html.match(/\schecked(?:\s|>)/g) ?? []).length, 1)
  assert.equal(html.includes('type="number"'), false)
  console.log('PASS lung question: explicit yes/no, no diagnosis dates')
  const schema = JSON.parse(await readFile(new URL('../../ml/app/data/dataset_schema.json', import.meta.url), 'utf8'))
  assert.deepEqual(
    fields.find(field => field.key === 'profession').options,
    Object.entries(schema.profession_categories).map(([id, label]) => ({ valueId: Number(id), label })),
  )

  Object.assign(store.answers, {
    modelKind: 'city', gender: 1, age: '55', height: '175', weight: '80',
    alcohol: 1, profession: 2, region: 5, chronicLungDisease: 0,
  })
  await nextTick()
  for (const key of obsoleteFields) store.answers[key] = 1
  for (const year of [2013, 2017, 2019, 2021]) {
    store.answers.year = year
    assert.equal(await store.submit(), true)
    assert.equal(request.url, '/api/survey/submit')
    assert.equal(request.payload.year, year)
    assert.equal(request.payload.age, 55)
    for (const key of obsoleteFields) {
      assert.equal(key in request.payload, false)
    }
    assert.equal('glucose' in request.payload, false)
    assert.equal(store.predictions.length, 1)
    assert.equal(store.predictions[0].modelKind, 'city')
    assert.ok(store.modelVersion)
    console.log(`PASS city ${year}: ${store.predictions[0].probability}%, ${store.modelVersion}`)
  }

  store.answers.modelKind = 'region'
  store.answers.chronicLungDisease = 1
  await nextTick()
  assert.equal(store.answers.year, null)
  assert.equal(await store.submit(), true)
  assert.equal(request.payload.year, null)
  assert.equal(request.payload.chronicLungDisease, 1)
  assert.equal(store.predictions[0].modelKind, 'region')
  assert.equal(store.loading, false)
  console.log(`PASS region: ${store.predictions[0].probability}%, ${store.modelVersion}`)

  store.loadConfig()
  assert.equal(store.modelVersion, null)
  assert.deepEqual(store.predictions, [])
  assert.equal(store.submitted, false)
  assert.equal(store.answers.chronicLungDisease, null)
  assert.equal(store.answers.modelKind, null)
  store.$dispose()
  console.log('PASS profession categories, model switch and reset')
} finally {
  globalThis.fetch = originalFetch
  await server.close()
}
