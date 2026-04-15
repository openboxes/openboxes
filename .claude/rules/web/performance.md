---
paths:
  - "src/js/**"
  - "webpack.config.js"
---

# Frontend Performance — OpenBoxes

OpenBoxes is an **admin SPA** used 8 hours a day with large datasets (10k+ rows, nested wizards). Performance priorities are **render responsiveness, render count, and bundle size** — not landing-page Core Web Vitals.

## Bundle

- **Webpack 5 production build** (`npm run bundle`) is the authoritative bundle. Inspect output size when adding dependencies.
- **Route-level code splitting** via `react-loadable` is the existing pattern. New feature screens should be lazy-loaded at the route boundary.
- **Avoid full-library imports:**
  ```js
  // BAD — imports all of lodash (~70kb)
  import _ from 'lodash';
  // GOOD — tree-shakeable
  import { isEmpty, debounce } from 'lodash';
  ```
- **Large libraries to watch:** `moment` (legacy — prefer `date-fns`), `chart.js`, `react-virtualized`, `@tanstack/react-virtual`.

## Tables and Long Lists

This is the single biggest performance topic for OpenBoxes.

- **Virtualize anything > 100 rows.** Use `react-virtualized`, `@tanstack/react-virtual`, or the existing virtualized components (`src/js/components/cycleCount/toCountTab/VirtualizedTablesList.jsx`).
- **`react-table-hoc-fixed-columns`** is in use for wide tables with sticky columns — don't reinvent.
- **Server-side pagination** for lists that could exceed a few hundred rows. Never load 10k rows and then paginate client-side.
- **Debounce search/filter inputs** (`lodash.debounce`, 250–400ms) so every keystroke doesn't refire queries.

## Render Count

- **`React.memo`** on presentational list row components — a 10k-row table re-rendering every row on hover is the #1 perf bug.
- **`useCallback` / `useMemo`** for props passed to memoized children. Inline arrow functions in props defeat memoization.
- **Stable keys.** `key={index}` on a filterable list causes React to reconcile everything. Use a stable ID.
- **Reselect** for derived Redux state — avoids recalculating derived values on every state change.

## API Call Efficiency

- **Batch reference data** (categories, locations, UoMs) at app boot or via a shared Redux slice — don't refetch per component.
- **No N+1 fetches.** If you find yourself calling `GET /api/product/:id` inside a `.map(...)`, batch via a `GET /api/product?ids=...` endpoint or via a `Promise.all` if the backend supports it.
- **`cwait`** for concurrency limits on bulk operations (already a dependency).
- **Cancel in-flight requests on unmount.** Use axios `CancelToken` or the newer `AbortController` pattern; the shared `apiClient` supports it.

## Forms

- **`react-final-form`** re-renders aggressively. Use `<FormSpy subscription={{ values: true }}>` to limit what triggers a re-render.
- **Field arrays** (`react-final-form-arrays`) with 100+ rows are expensive — consider chunking or virtualizing.
- **Zod validation** on every keystroke can be slow — debounce or validate on blur for large forms.

## Charts

- **`chart.js` v2.9** is in use via `react-chartjs-2`. Don't upgrade chart.js without testing; v3+ is a breaking change.
- **Destroy chart instances on unmount** to avoid canvas memory leaks.

## Webpack / Build

- **Node 14** for the build toolchain. Don't upgrade Webpack without confirming Node 14 compatibility.
- **No optional chaining (`?.`) in `webpack.config.js`** — it runs on raw Node 14 outside of Babel.
- **Watch mode:** `npm run watch` uses HMR where possible; re-running full webpack is slow (~30–60s).

## Anti-Patterns

- **Loading full product/location catalogs into Redux at app boot.** Lazy-load by page.
- **Unbounded `useEffect` dependencies** causing infinite re-renders.
- **Synchronous JSON parsing** of 5MB+ API responses — chunk on the server instead.
- **`setState` inside `useEffect` without a guard** — causes re-renders in a loop.
- **Inline object/array literals as Redux selector results** — breaks shallow equality and re-renders everything subscribed.

## Perf Review Checklist

- [ ] New list views virtualize beyond 100 rows
- [ ] New deps are tree-shaken or imported by name
- [ ] No N+1 API calls inside `.map()` / loops
- [ ] `React.memo` + `useCallback` on row components in large tables
- [ ] Debounced search/filter inputs
- [ ] `apiClient` requests cancelled on unmount
- [ ] Reselect used for derived Redux state
- [ ] Bundle size inspected after adding a dep (`npm run bundle && ls -la src/main/webapp/webpack`)
