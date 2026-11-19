import { act, renderHook } from '@testing-library/react';
import queryString from 'query-string';
import { BrowserRouter, useLocation } from 'react-router-dom';

import useCycleCountFilters from 'hooks/cycleCount/useCycleCountFilters';
import useSwitchTabs from 'hooks/useSwitchTabs';

vi.mock('react-redux', () => ({
  useSelector: vi.fn().mockImplementation(() => ({
    currentLocation: { id: 'loc-123' },
    debounceTime: 500,
    minSearchLength: 3,
    shouldRebuildParams: false,
  })),
  useDispatch: () => vi.fn(),
  connect: vi.fn(() => (Component) => Component),
}));
vi.mock('hooks/useTranslate', () => ({ default: vi.fn(() => (key) => key) }));

const useCycleCountTabs = () => {
  const { switchTab } = useSwitchTabs({ defaultTab: 'ALL_PRODUCTS' });
  const { resetForm } = useCycleCountFilters({ filterFields: { categories: {} } });
  const { search } = useLocation();

  return { switchTab, resetForm, search };
};

describe('useCycleCountFilters', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/cycleCount?tab=ALL_PRODUCTS');
  });

  it('keeps the new tab when the filters are reset right after a tab switch', async () => {
    const { result } = renderHook(useCycleCountTabs, { wrapper: BrowserRouter });

    await act(async () => {
      result.current.switchTab('TO_COUNT', result.current.resetForm);
    });

    expect(queryString.parse(result.current.search).tab).toEqual('TO_COUNT');
    expect(queryString.parse(window.location.search).tab).toEqual('TO_COUNT');
  });
});
