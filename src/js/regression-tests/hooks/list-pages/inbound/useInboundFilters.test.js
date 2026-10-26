import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchShipmentStatusCodes, fetchShipmentTypes } from 'actions';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import ShipmentStatusCode from 'consts/shipmentStatusCode';
import useInboundFilters from 'hooks/list-pages/inbound/useInboundFilters';
import { fetchLocationById, fetchUserById } from 'utils/option-utils';

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
  fetchShipmentStatusCodes: jest.fn((options) => ({ type: 'FETCH_SHIPMENT_STATUSES', options })),
  fetchShipmentTypes: jest.fn(() => ({ type: 'FETCH_SHIPMENT_TYPES' })),
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchLocationById: jest.fn(),
  fetchUserById: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const EMPTY_FILTERS = {
  receiptStatusCode: '',
  origin: '',
  destination: '',
  shipmentType: '',
  requestedBy: '',
  createdBy: '',
  updatedBy: '',
  createdAfter: '',
  createdBefore: '',
};

describe('useInboundFilters', () => {
  let state;
  let history;
  let dispatch;

  const currentUser = { id: 'user-1', name: 'Jane Doe' };

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: {
        currentLocation: { id: 'loc-1', name: 'Main Depot' },
        user: currentUser,
        activeLanguage: 'en',
      },
      shipmentStatuses: { data: [{ id: 'SHIPPED', value: 'SHIPPED' }, { id: 'RECEIVED', value: 'RECEIVED' }] },
      stockMovementCommon: { shipmentTypes: [{ id: 'air' }, { id: 'land' }] },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/stockMovement/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchLocationById.mockResolvedValue({ id: 'origin-1', name: 'Supplier' });
    fetchUserById.mockImplementation(async (id) => ({ id, name: `User ${id}` }));
  });

  it('fetches shipment statuses without CREATED and shipment types on mount', () => {
    renderHook(() => useInboundFilters());

    expect(fetchShipmentStatusCodes).toHaveBeenCalledWith({
      excludedStatuses: [ShipmentStatusCode.CREATED],
    });
    expect(fetchShipmentTypes).toHaveBeenCalledTimes(1);
    expect(dispatch).toHaveBeenCalledWith({
      type: 'FETCH_SHIPMENT_STATUSES',
      options: { excludedStatuses: [ShipmentStatusCode.CREATED] },
    });
  });

  it('defaults the destination to the current location when the query has none', async () => {
    const { result } = renderHook(() => useInboundFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      direction: 'INBOUND',
      destination: {
        id: 'loc-1', value: 'loc-1', name: 'Main Depot', label: 'Main Depot',
      },
    });
  });

  it('builds the default values from the query parameters', async () => {
    history.location.search = 'destination=other&receiptStatusCode=RECEIVED&origin=origin-1'
      + '&requestedBy=user-9&createdBy=user-1&updatedBy=user-8'
      + '&createdAfter=2026-01-01&createdBefore=2026-02-01&shipmentType=air&shipmentType=land';
    const { result } = renderHook(() => useInboundFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      direction: 'INBOUND',
      receiptStatusCode: [{ id: 'RECEIVED', value: 'RECEIVED' }],
      origin: { id: 'origin-1', name: 'Supplier' },
      requestedBy: { id: 'user-9', name: 'User user-9' },
      createdBy: currentUser,
      updatedBy: { id: 'user-8', name: 'User user-8' },
      createdAfter: '2026-01-01',
      createdBefore: '2026-02-01',
      shipmentType: [{ id: 'air' }, { id: 'land' }],
    });
    expect(fetchLocationById).toHaveBeenCalledWith('origin-1');
    expect(fetchUserById.mock.calls).toEqual([['user-9'], ['user-8']]);
  });

  it('pushes mapped filters to the url and keeps the raw values as params', () => {
    const { result } = renderHook(() => useInboundFilters());
    const values = {
      direction: 'INBOUND',
      origin: { id: 'origin-1' },
      receiptStatusCode: [{ id: 'SHIPPED' }],
      shipmentType: [{ id: 'air' }],
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stockMovement/list',
      search: 'direction=INBOUND&origin=origin-1&receiptStatusCode=SHIPPED&shipmentType=air',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('does not push an empty query', () => {
    const { result } = renderHook(() => useInboundFilters());

    act(() => result.current.setFilterValues({ origin: null }));

    expect(history.push).not.toHaveBeenCalled();
    expect(result.current.filterParams).toEqual({ origin: null });
  });

  it('selects the current user as requester and creator', async () => {
    const { result } = renderHook(() => useInboundFilters());
    await flushPromises();

    act(() => result.current.selectFiltersForMyStockMovements());

    const userOption = {
      id: 'user-1', value: 'user-1', label: 'Jane Doe', name: 'Jane Doe',
    };
    expect(history.push).toHaveBeenCalledWith({
      pathname: STOCK_MOVEMENT_URL.list(),
      search: 'createdBy=user-1&direction=INBOUND&requestedBy=user-1',
    });
    expect(result.current.defaultFilterValues.requestedBy).toEqual(userOption);
    expect(result.current.defaultFilterValues.createdBy).toEqual(userOption);
    expect(result.current.defaultFilterValues.direction).toBe('INBOUND');
  });

  it('resets the url to the inbound direction on a rebuild after initialization', async () => {
    const { rerender } = renderHook(() => useInboundFilters());
    await flushPromises();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stockMovement/list',
      search: 'direction=INBOUND',
    });
  });
});
