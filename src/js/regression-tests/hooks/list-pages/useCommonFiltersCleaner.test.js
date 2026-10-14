import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { setShouldRebuildFilterParams } from 'actions';
import useCommonFilters from 'hooks/list-pages/useCommonFilters';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('react-router-dom', () => ({
  useHistory: jest.fn(),
}));
jest.mock('actions', () => ({
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));

describe('useCommonFiltersCleaner', () => {
  let state;
  let dispatch;
  let clearFilterValues;
  let initializeDefaultFilterValues;

  const renderCleaner = (filtersInitialized = false) => renderHook(
    (props) => useCommonFiltersCleaner(props),
    { initialProps: { clearFilterValues, initializeDefaultFilterValues, filtersInitialized } },
  );

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: { currentLocation: { id: 'loc-1' } },
      filterForm: { shouldRebuildParams: true },
    };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    clearFilterValues = jest.fn();
    initializeDefaultFilterValues = jest.fn();
  });

  it('initializes the default values and resets the rebuild flag on the first rebuild', () => {
    renderCleaner(false);

    expect(clearFilterValues).not.toHaveBeenCalled();
    expect(initializeDefaultFilterValues).toHaveBeenCalledTimes(1);
    expect(setShouldRebuildFilterParams).toHaveBeenCalledWith(false);
    expect(dispatch).toHaveBeenCalledWith({ type: 'REBUILD', flag: false });
  });

  it('clears existing values first once the filters were initialized', () => {
    renderCleaner(true);

    expect(clearFilterValues).toHaveBeenCalledTimes(1);
    expect(initializeDefaultFilterValues).toHaveBeenCalledTimes(1);
  });

  it('skips initialization without a current location but still resets the flag', () => {
    state.session.currentLocation = null;
    renderCleaner(false);

    expect(initializeDefaultFilterValues).not.toHaveBeenCalled();
    expect(dispatch).toHaveBeenCalledWith({ type: 'REBUILD', flag: false });
  });

  it('does nothing until the rebuild flag is raised, and reacts only to its change', () => {
    state.filterForm.shouldRebuildParams = false;
    const { rerender } = renderCleaner(false);
    expect(initializeDefaultFilterValues).not.toHaveBeenCalled();
    expect(dispatch).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = true;
    rerender({ clearFilterValues, initializeDefaultFilterValues, filtersInitialized: true });
    expect(clearFilterValues).toHaveBeenCalledTimes(1);
    expect(initializeDefaultFilterValues).toHaveBeenCalledTimes(1);

    rerender({ clearFilterValues, initializeDefaultFilterValues, filtersInitialized: true });
    expect(initializeDefaultFilterValues).toHaveBeenCalledTimes(1);
  });
});

describe('useCommonFilters', () => {
  it('exposes the filter state, history and dispatch', () => {
    const history = { push: jest.fn() };
    const dispatch = jest.fn();
    useHistory.mockReturnValue(history);
    useDispatch.mockReturnValue(dispatch);
    const { result } = renderHook(() => useCommonFilters());

    expect(result.current.filterParams).toEqual({});
    expect(result.current.defaultFilterValues).toEqual({});
    expect(result.current.filtersInitialized).toBe(false);
    expect(result.current.history).toBe(history);
    expect(result.current.dispatch).toBe(dispatch);

    act(() => {
      result.current.setFilterParams({ a: 1 });
      result.current.setDefaultFilterValues({ b: 2 });
      result.current.setFiltersInitialized(true);
    });

    expect(result.current.filterParams).toEqual({ a: 1 });
    expect(result.current.defaultFilterValues).toEqual({ b: 2 });
    expect(result.current.filtersInitialized).toBe(true);
  });
});
