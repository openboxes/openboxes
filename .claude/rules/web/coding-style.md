---
paths:
  - "src/js/**"
---

# Frontend Coding Style — OpenBoxes

## Language Floor

- **Node 14**, **React 16.8**, **ES2020 via Babel**. Avoid features that require newer runtimes:
  - **No optional chaining (`?.`) or nullish coalescing (`??`) in Webpack config or build scripts** — they run on raw Node 14. Use them freely in application code (Babel transpiles them).
  - **No top-level `await`.** Wrap in an async IIFE if needed.
  - **No private class fields (`#foo`)** — Babel preset in this project doesn't guarantee them.
- **No TypeScript.** OpenBoxes is JS + JSX. Don't introduce `.ts`/`.tsx` files. Use JSDoc `@param` / `@returns` for non-trivial utilities if documentation helps.

## File Extensions

- Files containing JSX → `.jsx`
- Files with only plain JS → `.js`
- Match the existing convention in the neighbor directory.

## Modules

- `import` / `export` only. No `require()` in `src/js/`.
- Absolute imports resolve from `src/js/` via the `moduleDirectories` Jest config.
- Use **named imports** for large libraries to preserve tree-shaking:
  ```js
  // GOOD
  import { isEmpty, debounce } from 'lodash';
  // BAD
  import _ from 'lodash';
  ```
- **Do not import from `lodash/fp`** — not in dependencies.
- Import order (enforced by `eslint-plugin-simple-import-sort`): builtin → external → absolute → relative.

## Variables

- `const` by default, `let` when reassignment is required.
- **No `var`.** Existing uses are legacy; don't add more.
- Prefer destructuring for props and state.

## Naming

- **Components:** `PascalCase` (`StockMovementList`, `AddItemsPage`)
- **Hooks:** `useCamelCase` (`useInboundFilters`)
- **Redux action types:** `SCREAMING_SNAKE_CASE` constants
- **Action creators / reducers:** `camelCase`
- **File names:** match the default export — `StockMovementList.jsx`, `useInboundFilters.jsx`
- **CSS classes:** kebab-case, scoped via SCSS module or component folder

## React Components

- **Functional components + hooks** for new code. Existing class components can stay until touched; don't mass-convert.
- **Default-export the component**; use named exports for helpers in the same file sparingly.
- **Props destructuring** at the top of the function body:
  ```jsx
  const AddItemsPage = ({ items, onSubmit, translate }) => {
    // ...
  };
  ```
- **PropTypes** for public components (the project uses `prop-types`). For new internal components, PropTypes are still preferred but not mandatory — match the surrounding file.

## Hooks Rules

- **Rules of hooks:** only call hooks at the top level, never inside loops or conditions.
- **Exhaustive dependency arrays.** If `eslint-plugin-react-hooks` flags a missing dep, fix the dep — don't suppress the rule.
- **Custom hooks** live in `src/js/hooks/` mirroring the feature structure (`hooks/list-pages/<feature>/`).
- **No side effects in render.** Move to `useEffect` or event handlers.

## Redux

- **Existing pattern:** actions in `src/js/actions/`, reducers in `src/js/reducers/`, selectors in `src/js/selectors/`. Follow it.
- **Immutable updates.** Use `immutability-helper` (already in deps) for deeply nested updates, or return a new object via spread.
- **Do not mutate state.** `state.items.push(...)` is a bug.
- **Selectors should be memoized** via `reselect` (`createSelector`) when they compute derived values.
- **`redux-persist` whitelist** — see `rules/web/security.md`. Default new state to **not** persisted.

## Forms

- **New forms:** prefer `react-hook-form` + `zod` schemas. Put the schema in a sibling `schemes/` or `validation.js` file, not inline (the existing convention in this codebase is `schemes/`, e.g. `src/js/utils/form-values/schemes/cycleCountSchemes.js` — do not create a parallel `schemas/` folder).
- **Existing wizards:** `react-final-form` + `react-final-form-arrays`. Don't mix libraries inside a single wizard.
- **Never inline form validators** that are reused across steps — extract to `src/js/utils/validation.js` or a feature-specific file.

## Styling

- **SCSS** in a sibling file (`Component.scss`) imported at the top of the component.
- **Bootstrap 4.6** utility classes first (`d-flex`, `align-items-center`, `col-md-6`). Custom CSS only when utilities don't cover the case.
- **No inline styles** except for dynamic values that can't be expressed as a class (computed colors, pixel offsets from refs). Even then, use `className` conditionals where possible.
- **No CSS-in-JS libraries.** The stack doesn't use emotion/styled-components and adding them is overkill.

## Comments

- Default to **no comments** — well-named identifiers document intent.
- Comment only the **why** of non-obvious code: workarounds, browser quirks, subtle data-shape assumptions.
- Never reference issue numbers, the current task, or callers (`// added for OBPIH-7718`). That information belongs in git history.

## Functional Style

- **`.map` / `.filter` / `.reduce` / `.flatMap`** over `for` loops. The codebase already leans this way ~75:1; match it.
- **`Array.from(Object.entries(...))` + `.reduce`** over `for...in`.
- **Avoid mutating function arguments.** Return a new value.

## Anti-Patterns

- **No `moment` for new date code** — the project has `date-fns` v4. `moment` is still a dep for legacy reasons; don't add new `moment` calls.
- **No `jquery` in React code.** It's present for legacy GSP pages only.
- **No `eval`, no `new Function(...)`, no `with {}`.**
- **No `console.log` in committed code.** Use the logger pattern (Sentry + `apiClient` error interceptor).
