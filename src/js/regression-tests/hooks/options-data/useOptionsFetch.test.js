import { renderHook } from '@testing-library/react-hooks';

import { FETCH_UNIT_OF_MEASURE_QUANTITY } from 'actions/types';
import { UNIT_OF_MEASURE_OPTIONS } from 'api/urls';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import useQuantityUnitOfMeasureOptions from 'hooks/options-data/useQuantityUnitOfMeasureOptions';
import apiClient from 'utils/apiClient';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const setSession = (currentLocation, activeLanguage) => {
  mockState = { session: { currentLocation, activeLanguage }, unitOfMeasure: { quantity: ['EA'] } };
};

describe('useOptionsFetch', () => {
  const fetchA = jest.fn((config) => ({ type: 'A', config }));
  const fetchB = jest.fn((config) => ({ type: 'B', config }));

  beforeEach(() => {
    jest.clearAllMocks();
    setSession({ id: 'loc-1' }, 'en');
  });

  it('dispatches every fetcher with an abort signal and aborts it on unmount', () => {
    const { unmount } = renderHook(() => useOptionsFetch([fetchA, fetchB]));

    expect(mockDispatch.mock.calls.map(([action]) => action.type)).toEqual(['A', 'B']);
    const { signal } = fetchA.mock.calls[0][0];
    expect(fetchB.mock.calls[0][0].signal).toBe(signal);
    expect(signal.aborted).toBe(false);

    unmount();

    expect(signal.aborted).toBe(true);
  });

  it('refetches on location and locale changes by default', () => {
    const { rerender } = renderHook(() => useOptionsFetch([fetchA]));

    const otherLocation = { id: 'loc-2' };
    setSession(otherLocation, 'en');
    rerender();
    expect(fetchA).toHaveBeenCalledTimes(2);

    // The same location object, so only the locale changes.
    setSession(otherLocation, 'fr');
    rerender();
    expect(fetchA).toHaveBeenCalledTimes(3);
  });

  it('skips the refetch for the triggers that are turned off', () => {
    const { rerender } = renderHook(() => useOptionsFetch(
      [fetchA],
      { refetchOnLocaleChange: false, refetchOnLocationChange: false },
    ));

    setSession({ id: 'loc-2' }, 'fr');
    rerender();

    expect(fetchA).toHaveBeenCalledTimes(1);
  });
});

describe('useQuantityUnitOfMeasureOptions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    setSession({ id: 'loc-1' }, 'en');
  });

  it('returns the quantity units and fetches them by type, ignoring location changes', async () => {
    const { result, rerender } = renderHook(() => useQuantityUnitOfMeasureOptions());

    expect(result.current.quantityUom).toEqual(['EA']);
    expect(mockDispatch).toHaveBeenCalledTimes(1);

    apiClient.get.mockResolvedValue({ data: [{ id: 'EA' }] });
    const innerDispatch = jest.fn();
    mockDispatch.mock.calls[0][0](innerDispatch);
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(UNIT_OF_MEASURE_OPTIONS, expect.objectContaining({
      params: { type: 'QUANTITY' },
      signal: expect.any(Object),
    }));
    expect(innerDispatch).toHaveBeenCalledWith({ type: FETCH_UNIT_OF_MEASURE_QUANTITY, payload: [{ id: 'EA' }] });

    setSession({ id: 'loc-2' }, 'en');
    rerender();
    expect(mockDispatch).toHaveBeenCalledTimes(1);
  });
});
