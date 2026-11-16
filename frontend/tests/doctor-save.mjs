import assert from 'node:assert/strict'

export async function checkDoctorSaves(server) {
  const { sectionsForLevel, effectiveDoctorAccess } = await server.ssrLoadModule('/src/config/doctor-access.ts')
  const { saveDoctor, updateDoctorBlocked } = await server.ssrLoadModule('/src/services/doctors.ts')
  const { fetchDoctors } = await server.ssrLoadModule('/src/api/doctors.api.ts')
  const capabilities = { profiles: false, accessLevels: true }
  const record = { id: 7, email: 'old@example.ru', isBlocked: false, allowedSections: [] }
  for (const level of ['VIEW', 'EDIT', 'RETRAIN']) {
    const sections = sectionsForLevel(level)
    assert.equal(effectiveDoctorAccess({ ...record, allowedSections: sections.toReversed() }), level)
  }
  assert.equal(effectiveDoctorAccess(record), 'NONE')
  assert.equal(effectiveDoctorAccess({ ...record, allowedSections: ['RETRAIN'] }), 'CUSTOM')

  const originalFetch = globalThis.fetch
  const calls = []
  let failPassword = false
  let mismatchCreate = false
  let getBody = []
  globalThis.fetch = async (url, init = {}) => {
    const payload = init.body ? JSON.parse(init.body) : undefined
    calls.push({ url, method: init.method ?? 'GET', payload })
    if (!init.method) return Response.json(getBody)
    if (url.endsWith('/reset-password')) {
      return new Response(null, { status: failPassword ? 500 : 200 })
    }
    if (url.endsWith('/blocked')) return new Response(null, { status: 204 })
    if (init.method === 'POST' && url.endsWith('/doctors')) {
      return Response.json({
        ...record,
        email: payload.email,
        allowedSections: mismatchCreate ? [] : payload.allowedSections,
      }, { status: 201 })
    }
    if (url.endsWith('/sections')) return Response.json({ ...record, allowedSections: payload.allowedSections })
    throw new Error(`Unexpected operation ${init.method} ${url}`)
  }

  const draft = { name: 'Doctor', email: 'new@example.ru', password: 'test-password-123', accessLevel: 'VIEW' }
  try {
    assert.deepEqual((await fetchDoctors()).doctors, [])
    getBody = [{ ...record, allowedSections: ['NOT_A_SECTION'] }]
    await assert.rejects(fetchDoctors())
    getBody = [record]
    assert.equal((await fetchDoctors()).doctors[0].allowedSections.length, 0)
    calls.length = 0

    const created = await saveDoctor(null, draft, capabilities)
    assert.equal(created.id, 7)
    assert.equal(created.persistedEmail, draft.email)
    assert.equal(created.name, draft.name)
    assert.equal('password' in created, false)
    assert.deepEqual(calls[0].payload, {
      email: draft.email, password: draft.password, allowedSections: sectionsForLevel('VIEW'),
    })
    assert.equal('accessLevel' in calls[0].payload, false)
    assert.equal('name' in calls[0].payload, false)
    assert.equal(calls.length, 1)

    calls.length = 0
    const custom = { ...record, allowedSections: ['RETRAIN'] }
    const preview = await saveDoctor(custom, { ...draft, accessLevel: null, password: '' }, capabilities)
    assert.equal(calls.length, 0)
    assert.deepEqual(preview.allowedSections, ['RETRAIN'])
    assert.equal(preview.email, draft.email)
    assert.equal(preview.persistedEmail, record.email)
    assert.equal(preview.name, draft.name)
    assert.deepEqual(preview.pendingFields, ['name', 'email'])
    await saveDoctor(record, { ...draft, accessLevel: null, password: '' }, capabilities)
    assert.equal(calls.length, 0)

    let confirmed = record
    let password = draft.password
    const onProgress = progress => {
      confirmed = progress.doctor
      if (progress.passwordSaved) password = ''
    }
    failPassword = true
    await assert.rejects(saveDoctor(record, { ...draft, accessLevel: 'EDIT' }, capabilities, undefined, onProgress))
    assert.deepEqual(confirmed.allowedSections, sectionsForLevel('EDIT'))
    assert.equal(password, draft.password)
    assert.equal(calls.filter(call => call.url.endsWith('/sections')).length, 1)
    failPassword = false
    await saveDoctor(confirmed, { ...draft, password, accessLevel: 'EDIT' }, capabilities, undefined, onProgress)
    assert.equal(password, '')
    assert.equal(calls.filter(call => call.url.endsWith('/sections')).length, 1)

    const blocked = await updateDoctorBlocked(preview, true)
    assert.equal(blocked.isBlocked, true)
    assert.equal(blocked.name, draft.name)
    assert.equal(blocked.persistedEmail, record.email)
    assert.deepEqual(calls.at(-1).payload, { isBlocked: true })

    mismatchCreate = true
    confirmed = null
    await assert.rejects(saveDoctor(null, draft, capabilities, undefined, onProgress))
    assert.equal(confirmed.id, record.id, 'Keep created identity even when permissions do not match')
    console.log('PASS: section mapping, current API, creation, profile preview, custom/empty rights, partial save/retry and blocking')
  } finally {
    globalThis.fetch = originalFetch
  }
}
