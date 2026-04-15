---
paths:
  - "src/js/**"
---

# Frontend Testing — OpenBoxes

## Stack

- **Jest 29** — runner, `testEnvironment: jsdom`, config in `package.json`
- **@testing-library/react 12** — component queries and user interaction
- **@testing-library/react-hooks 8** — hook testing in isolation (React 16.8 compatible)
- **@testing-library/jest-dom** — DOM matchers (`toBeInTheDocument`, `toHaveAttribute`, …)
- **mock-local-storage** — auto-mocked via `setupFilesAfterEach`
- **redux-mock-store** — mock Redux stores in connected component tests

No Playwright, no Cypress, no Vitest — don't introduce new test runners.

## Where tests live

- Component tests: sibling `__tests__/` folder or `<name>.test.jsx` sibling file — match the neighbor's convention.
- Utility tests: `src/js/tests/utils/<name>.test.jsx`
- Hook tests: `src/js/tests/hooks/<name>.test.jsx` or inside the feature folder

Jest roots: `src/` (see `package.json` → `jest.roots`).

## Assertions

- **Assert concrete values.** Never `expect(value).toBeTruthy()` or `expect(result).toBeDefined()` when you can assert the exact value.
  ```js
  // BAD
  expect(result).toBeDefined();
  expect(screen.getByRole('button')).toBeTruthy();

  // GOOD
  expect(result).toEqual({ id: 1, name: 'Widget' });
  expect(screen.getByRole('button', { name: 'Save' })).toBeInTheDocument();
  ```
- **Use `describe` blocks** to group related tests by feature or scenario.
- **Extract constants** for repeated strings (class names, error messages, API paths).
- **Remove redundant tests** — if a stronger assertion exists, delete the weaker one.

## Component tests (RTL)

```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';

describe('QuantityInput', () => {
  it('calls onChange with the parsed integer', async () => {
    const onChange = jest.fn();
    render(<QuantityInput onChange={onChange} />);

    await userEvent.type(screen.getByRole('spinbutton'), '42');

    expect(onChange).toHaveBeenCalledWith(42);
  });
});
```

- **Query by role first**, then label, then text. Only fall back to `getByTestId` when nothing else works — test IDs are a last resort.
- **Use `userEvent` over `fireEvent`** for realistic interactions.
- **Never test implementation details** (state shape, internal method calls). Test what the user sees.

## Connected component tests (Redux)

```jsx
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

const mockStore = configureStore([thunk]);

const renderWithStore = (state, ui) => {
  const store = mockStore(state);
  return { store, ...render(<Provider store={store}>{ui}</Provider>) };
};
```

- **Assert on dispatched actions** for thunks: `expect(store.getActions()).toEqual([...])`.
- **Mock the `apiClient`** with `jest.mock('utils/apiClient')` rather than the whole axios.

## Hook tests

```js
import { renderHook, act } from '@testing-library/react-hooks';

describe('useInboundFilters', () => {
  it('resets filters', () => {
    const { result } = renderHook(() => useInboundFilters());
    act(() => result.current.reset());
    expect(result.current.filters).toEqual({});
  });
});
```

## API mocking

- **`jest.mock('utils/apiClient')`** at the top of the test file. Define per-test return values with `apiClient.get.mockResolvedValueOnce(...)`.
- **Never hit a real backend** in unit tests.
- For network-level tests, `axios-mock-adapter` is **not** currently in deps — don't add it without discussion.

## Test Quality Checklist

- [ ] Tests grouped under `describe` blocks
- [ ] Assertions use concrete values (`toEqual`/`toBe`), not `toBeDefined`/`toBeTruthy`
- [ ] No queries by `data-testid` when role/label/text would work
- [ ] Constants extracted for repeated strings
- [ ] No redundant tests that check a subset of a stronger test's assertions
- [ ] `apiClient` is mocked, not axios directly (unless testing `apiClient` itself)
- [ ] No `act()` warnings in the test output
- [ ] Coverage target: **80%+** on custom feature code (`src/js/custom/**`)

## Anti-Patterns

- **Snapshot tests for anything non-trivial.** They ossify implementation details and get ignored. Allowed only for tiny presentational components with stable output.
- **`setTimeout` in tests.** Use `waitFor` / `findBy*` / `act` instead.
- **`enzyme`.** Not in deps. Never introduce it — the project is committed to RTL.
- **Testing Redux reducers through the UI.** Test reducers directly with `reducer(state, action)` assertions, and test the UI behavior separately.
