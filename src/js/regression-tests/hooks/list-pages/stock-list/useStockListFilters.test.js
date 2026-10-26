import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import useStockListFilters from 'hooks/list-pages/stock-list/useStockListFilters';
import apiClient from 'utils/apiClient';
import { fetchLocationById } from 'utils/option-utils';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: jest.fn(),
}));
jest.mock('actions', () => ({
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: { get: jest.fn() },
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchLocationById: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useStockListFilters', () => {
  let state;
  let history;

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: { currentLocation: { id: 'loc-1' } },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/stocklist/list', search: '' }, push: jest.fn() };
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(jest.fn());
    useHistory.mockReturnValue(history);
    apiClient.get.mockResolvedValue({ data: { data: [{ id: 'loc-1' }, { id: 'loc-2' }] } });
    fetchLocationById.mockImplementation(async (id) => ({ id, name: `Location ${id}`, extra: true }));
  });

  it('loads the location options on mount', async () => {
    const { result } = renderHook(() => useStockListFilters());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/locations?presentation=toBaseJson');
    expect(result.current.locations).toEqual([{ id: 'loc-1' }, { id: 'loc-2' }]);
  });

  it('starts from empty defaults without query parameters', async () => {
    const { result } = renderHook(() => useStockListFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({ origin: '', destination: '', isPublished: '' });
  });

  it('resolves single and repeated origin and destination ids into options', async () => {
    history.location.search = 'origin=a&destination=b&destination=c&isPublished=false';
    const { result } = renderHook(() => useStockListFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      origin: [{
        id: 'a', name: 'Location a', value: 'a', label: 'Location a',
      }],
      destination: [
        {
          id: 'b', name: 'Location b', value: 'b', label: 'Location b',
        },
        {
          id: 'c', name: 'Location c', value: 'c', label: 'Location c',
        },
      ],
      isPublished: false,
    });
    expect(fetchLocationById.mock.calls.map(([id]) => id)).toEqual(['a', 'b', 'c']);
  });

  it('reads isPublished=true as a boolean', async () => {
    history.location.search = 'isPublished=true';
    const { result } = renderHook(() => useStockListFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues.isPublished).toBe(true);
  });

  it('pushes the filters to the url whenever any value is given', () => {
    const { result } = renderHook(() => useStockListFilters());
    const values = { origin: [{ id: 'a' }, { id: 'b' }], destination: { id: 'c' }, isPublished: true };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stocklist/list',
      search: 'destination=c&isPublished=true&origin=a&origin=b',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('does not push for an empty filter object', () => {
    const { result } = renderHook(() => useStockListFilters());

    act(() => result.current.setFilterValues({}));

    expect(history.push).not.toHaveBeenCalled();
    expect(result.current.filterParams).toEqual({});
  });

  it('clears the query on a rebuild after initialization', async () => {
    history.location.search = 'isPublished=true';
    const { rerender } = renderHook(() => useStockListFilters());
    await flushPromises();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/stocklist/list' });
  });
});
