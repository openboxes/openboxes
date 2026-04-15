---
paths:
  - "src/js/**"
---

# Frontend Patterns — OpenBoxes

## Component Architecture

### Where components live

```
src/js/
├── components/
│   ├── <feature>/             # screens / pages for a feature
│   ├── form-elements/         # shared form inputs, table, modals
│   ├── Layout/                # header, footer, navbar, menu
│   ├── DataTable/             # shared table widgets
│   └── ...
├── custom/                    # EyeSeeTea customizations (NEW code goes here)
├── hooks/                     # custom hooks, mirrors feature folders
├── actions/                   # Redux action creators
├── reducers/                  # Redux reducers
├── selectors/                 # memoized selectors (reselect)
├── utils/                     # shared helpers (apiClient, form utils, etc.)
├── consts/                    # constants, enums
└── schemes/                   # zod validation schemas
```

**New custom code goes under `src/js/custom/<feature>/`** per the Upstream Compatibility rules. See `rules/custom-package-isolation.md`.

### Container vs presentational

- **Container components** own data loading (thunks, API calls, `useEffect`) and subscribe to Redux.
- **Presentational components** receive props, render UI, emit callbacks. No direct Redux access.
- Existing code mixes both patterns — when touching a mixed component, don't force a refactor. When creating new, prefer this split.

### Hook-first for new code

Class components still exist (`src/js/components/stock-movement-wizard/**/*.jsx`) — leave them alone unless you're actively working on one. **New components should be function components + hooks.**

## State Management

| State type | Where it lives | Tool |
|---|---|---|
| **Server data** (entities, lists) | Redux, populated via thunks | `redux-thunk`, `redux-promise` |
| **Form state** | react-final-form (legacy wizards) or react-hook-form (new) | see Forms below |
| **URL state** (filters, pagination, search) | Query params via `query-string` | `useHistory` from `react-router-dom` |
| **Ephemeral UI state** (modal open, hover) | `useState` inside the component | React |
| **Cross-component ephemeral** (current location, auth) | Redux | `react-redux` |

### Do not duplicate

- Never mirror server state into local `useState` — re-derive with selectors.
- Never mirror URL state into Redux — read it from `useLocation` / query-string.

### Selectors

Use `reselect` for derived values. Selector naming: `getXxx` for plain access, `selectXxxByY` for parameterized:

```js
import { createSelector } from 'reselect';

export const getProducts = (state) => state.products.byId;
export const getActiveProducts = createSelector(
  getProducts,
  (products) => Object.values(products).filter((p) => p.active),
);
```

## API Calls

- **Always** use the shared `apiClient` from `src/js/utils/apiClient.jsx`.
- **Thunk pattern** for async actions:
  ```js
  export const fetchProducts = () => async (dispatch) => {
    dispatch({ type: FETCH_PRODUCTS_REQUEST });
    try {
      const { data } = await apiClient.get('/openboxes/api/products');
      dispatch({ type: FETCH_PRODUCTS_SUCCESS, payload: data });
    } catch (err) {
      dispatch({ type: FETCH_PRODUCTS_FAILURE, payload: err });
    }
  };
  ```
- **Endpoints:** backend routes are under `/openboxes/api/...`. Do not hardcode hostnames — `apiClient` handles the base URL.
- **Query cancellation:** use `cwait` (already a dep) for concurrency limits on bulk operations.

## Forms

### Wizard pages (`react-final-form`)

The stock-movement wizards are the reference. Each step is a connected component that reads current form values and emits the next step:

```jsx
<Form onSubmit={onSubmit} initialValues={values}>
  {({ handleSubmit, values }) => (
    <form onSubmit={handleSubmit}>
      <Field name="product.id" component={ProductSelect} />
      <FieldArray name="lineItems">
        {({ fields }) => fields.map((name, i) => <LineItemRow key={i} name={name} />)}
      </FieldArray>
    </form>
  )}
</Form>
```

- **FieldArray** for repeatable rows.
- **Validators** go in `src/js/utils/validators.jsx` or a feature-specific file — never inline in JSX.

### New forms (`react-hook-form` + `zod`)

```jsx
const schema = z.object({
  name: z.string().min(1),
  quantity: z.number().int().positive(),
});

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema),
});
```

- **Schemas live in `src/js/schemes/<feature>.js`** — the project already follows this convention (`src/js/schemes/cycleCountSchemes.js`).

### Never mix libraries

One wizard, one form library. Don't introduce `react-hook-form` into an existing `react-final-form` wizard mid-flight.

## Tables

- **`react-table` v6** — older API, in use for most lists (`src/js/components/<feature>/...ListTable.jsx`)
- **`@tanstack/react-table` v8** — new API, used in newer tables (`src/js/components/DataTable/v2/*`)
- **`react-virtualized`** — used where thousands of rows are expected

Match the neighbor file. Don't introduce a fourth table library.

## Routing

- `react-router-dom` v5 (not v6). Use `<Route>`, `<Switch>`, `<Redirect>`, and the `useHistory` / `useLocation` / `useParams` hooks.
- **Lazy-loaded routes** via `react-loadable`. Keep the bundle-splitting boundary at the route level.

## Internationalization

- All user-facing text goes through `react-localize-redux`:
  ```jsx
  import { Translate } from 'react-localize-redux';
  <Translate id="stockMovement.createTitle" defaultMessage="Create Stock Movement" />
  ```
- New keys go in the translation JSON files under `src/js/locale/` (or wherever the project stores them — match the existing convention).

## Error Handling

- **Use the existing `AlertMessage` component** for user-facing errors.
- **`react-confirm-alert`** for destructive confirmations.
- **Sentry** (`@sentry/react`) catches uncaught errors; wrap risky async boundaries with an `ErrorBoundary`.
- **Never swallow errors silently.** An empty `.catch(() => {})` is a bug.

## Anti-Patterns

- **Mutating Redux state.** Always return new objects.
- **`useEffect` for derived state.** Compute at render time or use `useMemo`.
- **Props drilling past 2 levels.** Use context or connect the deeper component directly.
- **`key={index}`** in dynamic lists. Use a stable ID.
- **`useEffect(() => { ... }, [])` for data fetching without cleanup** — leaks on unmount.
- **`setState` inside render** — infinite loop.
