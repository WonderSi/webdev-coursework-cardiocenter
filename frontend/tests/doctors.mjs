import assert from 'node:assert/strict'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createServer } from 'vite'

const root = fileURLToPath(new URL('..', import.meta.url))
const require = createRequire(path.join(root, 'package.json'))
const { shallowRef, effectScope, nextTick } = require('vue')
const server = await createServer({
  root,
  server: { middlewareMode: true },
  optimizeDeps: { noDiscovery: true, include: [] },
  appType: 'custom',
})
const scopes = []
const settle = () => new Promise(resolve => setImmediate(resolve))

async function checkDirectoryLifecycle(useDoctorsDirectory) {
  const allowed = shallowRef(true)
  const requests = []
  const scope = effectScope()
  scopes.push(scope)
  const fetchDirectory = signal => new Promise((resolve, reject) => {
    requests.push({ signal, resolve, reject })
  })
  const directory = scope.run(() => useDoctorsDirectory(() => allowed.value, fetchDirectory))
  assert.equal(requests.length, 1)

  const second = directory.loadDoctors()
  assert.equal(requests[0].signal.aborted, true)
  const doctor = {
    id: 1,
    name: 'Последний',
    email: 'last@example.ru',
    isBlocked: false,
    accessLevel: 'VIEW',
  }
  requests[1].resolve({
    doctors: [doctor],
    capabilities: { profiles: true, accessLevels: true },
  })
  await second
  requests[0].resolve({
    doctors: [{ ...doctor, name: 'Устаревший' }],
    capabilities: { profiles: false, accessLevels: false },
  })
  await settle()
  assert.equal(directory.doctors.value[0].name, 'Последний')
  assert.equal(directory.loaded.value, true)

  allowed.value = false
  await nextTick()
  assert.deepEqual(directory.doctors.value, [])
  assert.equal(directory.loaded.value, false)
  assert.equal(directory.loading.value, false)
  assert.equal(directory.capabilities.value.profiles, false)
  directory.upsertDoctor(doctor)
  assert.equal(directory.doctors.value.length, 0)

  allowed.value = true
  await nextTick()
  assert.equal(requests.length, 3)
  scope.stop()
  assert.equal(requests[2].signal.aborted, true)
  requests[2].resolve({
    doctors: [doctor],
    capabilities: { profiles: true, accessLevels: true },
  })
  await settle()
  assert.equal(directory.doctors.value.length, 0)
  console.log('PASS: newest response wins; access loss clears state; disposed requests and late updates are ignored')
}

function checkFilters(useDoctorFilters) {
  const records = shallowRef([
    { id: 1, name: 'Борис', email: 'b@example.ru', isBlocked: false, accessLevel: 'EDIT' },
    { id: 2, name: 'Анна', email: 'a@example.ru', isBlocked: true, accessLevel: 'VIEW' },
    { id: 3, email: 'legacy@example.ru', isBlocked: false },
  ])
  const capabilities = shallowRef({ profiles: false, accessLevels: false })
  const scope = effectScope()
  scopes.push(scope)
  const filters = scope.run(() => useDoctorFilters(() => records.value, () => capabilities.value))
  const visibleIds = () => filters.visibleRows.value.map(row => row.doctor.id)

  assert.deepEqual(visibleIds(), [2, 1, 3])
  filters.query.value = ' B@EXAMPLE.RU '
  assert.deepEqual(visibleIds(), [1])
  filters.resetFilters()
  filters.accessFilter.value = 'RETRAIN'
  assert.deepEqual(visibleIds(), [3])
  filters.resetFilters()
  filters.statusFilter.value = 'blocked'
  assert.deepEqual(visibleIds(), [2])
  assert.deepEqual(filters.statusOptions.value.map(status => status.count), [3, 2, 1])
  filters.query.value = 'не существует'
  assert.equal(filters.visibleRows.value.length, 0)
  filters.resetFilters()
  assert.equal(filters.hasFilters.value, false)
  records.value = []
  assert.equal(filters.visibleRows.value.length, 0)
  console.log('PASS: combined filters, Russian sorting, case-insensitive search, legacy access, counts and empty directory')
}

try {
  const { useDoctorsDirectory } = await server.ssrLoadModule('/src/composables/useDoctorsDirectory.ts')
  const { useDoctorFilters } = await server.ssrLoadModule('/src/composables/useDoctorFilters.ts')
  await checkDirectoryLifecycle(useDoctorsDirectory)
  checkFilters(useDoctorFilters)
} finally {
  scopes.forEach(scope => scope.stop())
  await server.close()
}
