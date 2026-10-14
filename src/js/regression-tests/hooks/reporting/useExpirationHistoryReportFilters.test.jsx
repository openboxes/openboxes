import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { createMemoryHistory } from 'history';
import { Router } from 'react-router-dom';

import { FILTER_FORM_PARAMS_BUILT } from 'actions/types';
import useExpirationHistoryReportFilters from 'hooks/reporting/useExpirationHistoryReportFilters';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));

const filterFields = { location: {}, startDate: {}, endDate: {} };

describe('useExpirationHistoryReportFilters', () => {
  let history;

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      session: { currentLocation: { id: 'loc-1', name: 'Depot' } },
      filterForm: { shouldRebuildParams: true },
    };
  });

  const renderFilters = (search = '') => {
    history = createMemoryHistory({ initialEntries: [`/report/expirationHistory${search}`] });
    const wrapper = ({ children }) => <Router history={history}>{children}</Router>;
    return renderHook(() => useExpirationHistoryReportFilters({ filterFields }), { wrapper });
  };

  it('defaults the location filter to the current location without fetching', () => {
    const { result } = renderFilters();

    expect(result.current.defaultFilterValues).toEqual({
      location: {
        id: 'loc-1', value: 'loc-1', name: 'Depot', label: 'Depot',
      },
      startDate: '',
      endDate: '',
    });
    expect(result.current.filtersInitialized).toBe(true);
    expect(result.current.shouldFetch).toBe(false);
    expect(mockDispatch.mock.calls.map(([action]) => action.type))
      .toEqual(['SHOW_SPINNER', 'HIDE_SPINNER', undefined]);
  });

  it('restores dates from the URL and fetches when both are present', () => {
    const { result } = renderFilters('?startDate=01/01/2026&endDate=01/31/2026');

    expect(result.current.defaultFilterValues.startDate).toBe('01/01/2026');
    expect(result.current.defaultFilterValues.endDate).toBe('01/31/2026');
    expect(result.current.shouldFetch).toBe(true);
  });

  it('does not fetch with only one date in the URL', () => {
    const { result } = renderFilters('?startDate=01/01/2026');

    expect(result.current.defaultFilterValues.startDate).toBe('01/01/2026');
    expect(result.current.shouldFetch).toBe(false);
  });

  it('skips initialization while no rebuild is requested', () => {
    mockState.filterForm.shouldRebuildParams = false;

    const { result } = renderFilters();

    expect(result.current.defaultFilterValues).toEqual({});
    expect(result.current.filtersInitialized).toBe(false);
  });

  it('writes the chosen dates to the URL and keeps the filter values', () => {
    const { result } = renderFilters();
    const values = { startDate: '01/01/2026', endDate: '', location: { id: 'loc-1' } };

    act(() => result.current.setFilterValues(values));

    expect(history.location.pathname).toBe('/report/expirationHistory');
    expect(history.location.search).toBe('?startDate=01%2F01%2F2026');
    expect(result.current.filterParams).toBe(values);
  });

  it('does not touch the URL for empty filter values', () => {
    const { result } = renderFilters('?startDate=01/01/2026');

    act(() => result.current.setFilterValues({}));

    expect(history.location.search).toBe('?startDate=01/01/2026');
    expect(result.current.filterParams).toEqual({});
  });

  it('clears the URL and stops fetching when filters are rebuilt for a new location', () => {
    const { result, rerender } = renderFilters('?startDate=01/01/2026&endDate=01/31/2026');
    expect(result.current.shouldFetch).toBe(true);

    mockState = {
      session: { currentLocation: { id: 'loc-2', name: 'Clinic' } },
      filterForm: { shouldRebuildParams: false },
    };
    rerender();
    mockState = { ...mockState, filterForm: { shouldRebuildParams: true } };
    rerender();

    expect(history.location.search).toBe('');
    expect(result.current.shouldFetch).toBe(false);
    expect(result.current.defaultFilterValues.location.id).toBe('loc-2');
    expect(result.current.defaultFilterValues.startDate).toBe('');
  });

  it('marks the filter params as built after initializing', () => {
    renderFilters();
    const thunk = mockDispatch.mock.calls.find(([action]) => typeof action === 'function')[0];
    const innerDispatch = jest.fn();

    thunk(innerDispatch);

    expect(innerDispatch).toHaveBeenCalledWith({ type: FILTER_FORM_PARAMS_BUILT });
  });
});
