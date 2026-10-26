import { renderHook } from '@testing-library/react-hooks';
import { useHistory } from 'react-router-dom';

import cycleCountReportingIndicators from 'consts/cycleCountReportingIndicators';
import useIndicatorsTab from 'hooks/cycleCount/useIndicatorsTab';
import {
  fetchIndicatorInventoryAccuracy,
  fetchIndicatorInventoryShrinkage,
  fetchIndicatorProductsInventoried,
} from 'utils/option-utils';

jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({ location: { id: 'loc-1' } })),
}));
jest.mock('selectors', () => ({
  getCurrentLocation: (state) => state.location,
}));
jest.mock('react-router-dom', () => ({ useHistory: jest.fn() }));
jest.mock('utils/option-utils', () => ({
  fetchIndicatorInventoryAccuracy: jest.fn(),
  fetchIndicatorInventoryShrinkage: jest.fn(),
  fetchIndicatorProductsInventoried: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useIndicatorsTab', () => {
  let history;
  let setShouldFetch;

  const renderIndicators = (overrides = {}) => renderHook((props) => useIndicatorsTab(props), {
    initialProps: {
      filterParams: { startDate: '2026-01-01', endDate: '2026-01-31' },
      shouldFetch: true,
      setShouldFetch,
      filtersInitialized: true,
      ...overrides,
    },
  });

  beforeEach(() => {
    jest.clearAllMocks();
    history = {
      location: { pathname: '/openboxes/cycleCount/reporting', search: '?tab=indicators' },
      replace: jest.fn(),
    };
    useHistory.mockReturnValue(history);
    setShouldFetch = jest.fn();
    fetchIndicatorProductsInventoried.mockResolvedValue({ name: 'productsInventoried', firstValue: 5 });
    fetchIndicatorInventoryAccuracy.mockResolvedValue({ name: 'inventoryAccuracy', firstValue: 90 });
    fetchIndicatorInventoryShrinkage.mockResolvedValue({ name: 'somethingUnknown', firstValue: 1 });
  });

  it('writes the selected dates into the url, keeping the other query params', () => {
    renderIndicators({ shouldFetch: false });
    expect(history.replace).toHaveBeenCalledWith({
      pathname: '/openboxes/cycleCount/reporting',
      search: 'endDate=31%2FJan%2F2026&startDate=01%2FJan%2F2026&tab=indicators',
    });
  });

  it('leaves the date params out of the url when a date is missing', () => {
    renderIndicators({ filterParams: { startDate: '2026-01-01' } });
    expect(history.replace).toHaveBeenCalledWith({
      pathname: '/openboxes/cycleCount/reporting',
      search: 'tab=indicators',
    });
    expect(fetchIndicatorProductsInventoried).not.toHaveBeenCalled();
  });

  it('fetches all indicators and keeps only the known ones merged with their config', async () => {
    const { result } = renderIndicators();
    expect(result.current.loading).toBe(true);

    const expectedParams = { startDate: '01/01/2026', endDate: '01/31/2026', facility: 'loc-1' };
    expect(fetchIndicatorProductsInventoried).toHaveBeenCalledWith(expectedParams);
    expect(fetchIndicatorInventoryAccuracy).toHaveBeenCalledWith(expectedParams);
    expect(fetchIndicatorInventoryShrinkage).toHaveBeenCalledWith(expectedParams);

    await flushPromises();

    expect(result.current.loading).toBe(false);
    expect(result.current.tiles).toEqual([
      { ...cycleCountReportingIndicators.productsInventoried, name: 'productsInventoried', firstValue: 5 },
      { ...cycleCountReportingIndicators.inventoryAccuracy, name: 'inventoryAccuracy', firstValue: 90 },
    ]);
    expect(setShouldFetch).toHaveBeenCalledWith(false);
  });

  it('stops loading and resets the fetch flag when a request fails', async () => {
    fetchIndicatorInventoryAccuracy.mockRejectedValue(new Error('down'));
    const { result } = renderIndicators();
    await flushPromises();

    expect(result.current.loading).toBe(false);
    expect(result.current.tiles).toEqual([]);
    expect(setShouldFetch).toHaveBeenCalledWith(false);
  });

  it('waits for the filters to be initialized and for the fetch flag', () => {
    const { rerender } = renderIndicators({ filtersInitialized: false });
    expect(fetchIndicatorProductsInventoried).not.toHaveBeenCalled();

    rerender({
      filterParams: { startDate: '2026-01-01', endDate: '2026-01-31' },
      shouldFetch: false,
      setShouldFetch,
      filtersInitialized: true,
    });
    expect(fetchIndicatorProductsInventoried).not.toHaveBeenCalled();
  });
});
