import { afterEach, beforeEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import { effectScope, nextTick, type EffectScope } from 'vue'
import { fetchDashboardStats, type DashboardStatsResponse, type DashboardModelKind } from '@/api/dashboard.api'
import { useDashboardCharts } from '@/composables/useDashboardsCharts'

let scope: EffectScope
const sample = (modelKind: DashboardModelKind): DashboardStatsResponse => ({
  modelKind, totalRecords: modelKind === 'city' ? 2 : 3, averageAge: 45,
  genderDistribution: [{ name: 'Мужской', count: 2, percent: 100 }],
  ageGender: [{ ageGroup: '40–49', maleCount: 2, femaleCount: 0 }],
  bmi: [{ name: 'Нет данных', count: 1, percent: 50 }],
  alcohol: [{ name: 'Никогда не употреблял', count: 2, percent: 100 }],
  professions: [{ name: 'Другое', count: 2, percent: 100 }],
  districts: [{ name: 'Рудничный', count: 2, percent: 100 }],
  lungDisease: [{ name: 'Да', count: 1, percent: 50 }, { name: 'Нет', count: 1, percent: 50 }],
  labCoverage: [{ field: 'glucose', label: 'Глюкоза', filledCount: 1, missingCount: 1, percent: 50 }],
  diagnosedRecords: 0, recordedDiagnoses: [],
})
const json = (data: unknown, status = 200) => new Response(JSON.stringify(data), { status })
const settle = async () => { await nextTick(); await new Promise(resolve => setImmediate(resolve)) }
const mount = () => scope.run(() => useDashboardCharts())!

beforeEach(() => {
  setActivePinia(createPinia())
  scope = effectScope()
  globalThis.fetch = async input => json(sample(String(input).includes('region') ? 'region' : 'city'))
})
afterEach(() => scope.stop())

test('request includes selected model kind', async () => {
  let url = ''
  globalThis.fetch = async input => { url = String(input); return json(sample('region')) }
  assert.equal((await fetchDashboardStats('region')).modelKind, 'region')
  assert.equal(url, '/api/admin/dashboard?modelKind=region')
})

test('chart counts use observations without inventing diagnosis categories', async () => {
  const state = mount()
  await settle()
  assert.equal(state.stats.value?.totalRecords, 2)
  assert.equal(state.stats.value?.diagnosedRecords, 0)
  assert.deepEqual(state.chartProfessions.value.series[0].data, [2])
  assert.deepEqual(state.chartAgeGender.value.series.map(item => item.name), ['Мужчины', 'Женщины'])
  assert.deepEqual(state.chartLabs.value.series.map(item => item.data), [[1], [1]])
  assert.equal(state.chartWeight.value.yAxis.data[0], 'Нет данных')
})

test('switch clears previous data and late old response cannot replace selected model', async () => {
  let finishCity!: (response: Response) => void
  globalThis.fetch = async input => String(input).includes('city')
    ? new Promise<Response>(resolve => { finishCity = resolve }) : json(sample('region'))
  const state = mount()
  state.modelKind.value = 'region'
  await settle()
  assert.equal(state.stats.value?.modelKind, 'region')
  finishCity(json(sample('city')))
  await settle()
  assert.equal(state.stats.value?.modelKind, 'region')
  assert.equal(state.stats.value?.totalRecords, 3)
})

test('failure after switching never displays previous model data', async () => {
  const state = mount()
  await settle()
  globalThis.fetch = async () => json({}, 500)
  state.modelKind.value = 'region'
  await settle()
  assert.equal(state.stats.value, null)
  assert.ok(state.errorMessage.value)
  assert.equal(state.loading.value, false)
})

test('empty selection returns empty chart datasets and null average', async () => {
  globalThis.fetch = async () => json({ ...sample('city'), totalRecords: 0, averageAge: null,
    alcohol: [], genderDistribution: [], professions: [], districts: [], bmi: [], lungDisease: [],
    ageGender: [], labCoverage: [] })
  const state = mount()
  await settle()
  assert.equal(state.stats.value?.totalRecords, 0)
  assert.equal(state.stats.value?.averageAge, null)
  assert.deepEqual(state.chartAlcohol.value.series[0].data, [])
  assert.deepEqual(state.chartLabs.value.series[0].data, [])
})

test('retry loads data after an error', async () => {
  globalThis.fetch = async () => { throw new Error('Connection unavailable') }
  const state = mount()
  await settle()
  assert.ok(state.errorMessage.value)
  globalThis.fetch = async () => json(sample('city'))
  await state.load()
  assert.equal(state.errorMessage.value, '')
  assert.equal(state.stats.value?.totalRecords, 2)
})
