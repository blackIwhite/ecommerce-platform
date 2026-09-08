import { defineConfig } from 'vitest/config'
import { resolve } from 'path'

// Workspace-level Vitest config.
// A single `pnpm test` at the repo root runs tests for all packages.
//
// Alias notes:
// - `@ecommerce/shared` mirrors the `paths` entry in packages/mall/tsconfig.json
//   and packages/admin/tsconfig.json ("@ecommerce/shared": ["../shared/src"]).
// - The `@/` alias is package-local (each package maps it to its own `src/`),
//   so it cannot be mirrored globally. Tests therefore use relative imports.
export default defineConfig({
  resolve: {
    alias: {
      '@ecommerce/shared': resolve(__dirname, 'packages/shared/src'),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    include: ['packages/*/src/**/*.{test,spec}.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      include: ['packages/*/src/**/*.{ts,vue}'],
      exclude: [
        '**/__tests__/**',
        '**/*.{test,spec}.ts',
        '**/*.d.ts',
        '**/types/**',
      ],
    },
  },
})
