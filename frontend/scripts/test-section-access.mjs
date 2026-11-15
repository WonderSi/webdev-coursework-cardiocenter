import { build } from 'esbuild'
import { readFile, mkdtemp, rm } from 'node:fs/promises'
import { existsSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join, resolve } from 'node:path'
import { pathToFileURL, fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('../', import.meta.url))
const temporary = await mkdtemp(join(tmpdir(), 'cardio-access-tests-'))
try {
  const output = join(temporary, 'tests.mjs')
  await build({
    absWorkingDir: root,
    entryPoints: [process.argv.includes('--dashboard') ? 'tests/dashboard.test.ts' : 'tests/section-access.test.ts'],
    outfile: output,
    bundle: true,
    platform: 'node',
    format: 'esm',
    define: { 'process.env.NODE_ENV': '"test"', 'import.meta.env': '{}' },
    plugins: [{
      name: 'headless-vue-router',
      setup(context) {
        context.onResolve({ filter: /^@\// }, ({ path }) => {
          const base = resolve(root, 'src', path.slice(2))
          return { path: [base + '.ts', join(base, 'index.ts'), base].find(existsSync) }
        })

        context.onLoad({ filter: /\.vue$/ }, () => ({ contents: 'export default {}' }))
        context.onLoad({ filter: /src[\\/]router[\\/]index\.ts$/ }, async ({ path }) => ({
          contents: (await readFile(path, 'utf8')).replaceAll('createWebHistory', 'createMemoryHistory'),
          loader: 'ts',
        }))
      },
    }],
  })
  await import(pathToFileURL(output).href)
} finally {
  await rm(temporary, { recursive: true, force: true })
}
