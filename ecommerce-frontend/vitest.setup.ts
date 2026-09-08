/**
 * Vitest global setup.
 *
 * Node >= 22 exposes an experimental, flag-gated `localStorage` global. When
 * the flag is off, that own property on globalThis can shadow jsdom's real
 * implementation and evaluate to `undefined` inside tests (and inside the
 * shared API layer, which reads `localStorage` directly). This setup makes
 * sure the jsdom-backed Storage instance is the one tests see.
 */
if (typeof globalThis.localStorage === 'undefined' || globalThis.localStorage === null) {
  const storage =
    typeof window !== 'undefined' && window.localStorage
      ? window.localStorage
      : createMemoryStorage()

  Object.defineProperty(globalThis, 'localStorage', {
    value: storage,
    writable: true,
    configurable: true,
    enumerable: true,
  })
}

function createMemoryStorage(): Storage {
  let store = new Map<string, string>()
  const api = {
    get length() {
      return store.size
    },
    clear: () => {
      store = new Map()
    },
    getItem: (key: string) => (store.has(key) ? (store.get(key) as string) : null),
    setItem: (key: string, value: string) => {
      store.set(key, String(value))
    },
    removeItem: (key: string) => {
      store.delete(key)
    },
    key: (index: number) => Array.from(store.keys())[index] ?? null,
  }
  return api as Storage
}
