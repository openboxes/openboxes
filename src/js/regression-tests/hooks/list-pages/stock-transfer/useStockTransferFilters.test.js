import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchStockTransferStatuses } from 'actions';
import useStockTransferFilters from 'hooks/list-pages/stock-transfer/useStockTransferFilters';
import { fetchUserById } from 'utils/option-utils';

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
  fetchStockTransferStatuses: jest.fn(() => ({ type: 'FETCH_STOCK_TRANSFER_STATUSES' })),
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchUserById: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useStockTransferFilters', () => {
  let state;
  let history;
  let dispatch;
  const currentUser = { id: 'user-1', name: 'Jane' };

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      stockTransfer: { statuses: [{ id: 'PENDING', value: 'PENDING' }, { id: 'COMPLETED', value: 'COMPLETED' }] },
      session: { currentLocation: { id: 'loc-1' }, user: currentUser, activeLanguage: 'en' },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/stockTransfer/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchUserById.mockImplementation(async (id) => ({ id, name: 'Other' }));
  });

  it('fetches the stock transfer statuses on mount', () => {
    renderHook(() => useStockTransferFilters());

    expect(fetchStockTransferStatuses).toHaveBeenCalledTimes(1);
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_STOCK_TRANSFER_STATUSES' });
  });

  it('starts from empty defaults', async () => {
    const { result } = renderHook(() => useStockTransferFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      status: '', createdBy: '', lastUpdatedStartDate: '', lastUpdatedEndDate: '',
    });
  });

  it('builds the defaults from the query, fetching other users', async () => {
    history.location.search = 'status=COMPLETED&createdBy=user-2'
      + '&lastUpdatedStartDate=2026-01-01&lastUpdatedEndDate=2026-01-31';
    const { result } = renderHook(() => useStockTransferFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      status: [{ id: 'COMPLETED', value: 'COMPLETED' }],
      createdBy: { id: 'user-2', name: 'Other' },
      lastUpdatedStartDate: '2026-01-01',
      lastUpdatedEndDate: '2026-01-31',
    });
    expect(fetchUserById).toHaveBeenCalledWith('user-2');
  });

  it('uses the current user without fetching when they created the transfer', async () => {
    history.location.search = 'createdBy=user-1';
    const { result } = renderHook(() => useStockTransferFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues.createdBy).toBe(currentUser);
    expect(fetchUserById).not.toHaveBeenCalled();
  });

  it('pushes the filters to the url and stores a copy as params', () => {
    const { result } = renderHook(() => useStockTransferFilters());
    const values = { status: [{ id: 'PENDING' }], createdBy: { id: 'user-1' }, lastUpdatedStartDate: '2026-01-01' };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stockTransfer/list',
      search: 'createdBy=user-1&lastUpdatedStartDate=2026-01-01&status=PENDING',
    });
    expect(result.current.filterParams).toEqual(values);
    expect(result.current.filterParams).not.toBe(values);
  });

  it('skips the url for empty values and clears it on a rebuild after initialization', async () => {
    const { result, rerender } = renderHook(() => useStockTransferFilters());
    await flushPromises();
    act(() => result.current.setFilterValues({}));
    expect(history.push).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/stockTransfer/list' });
  });
});
