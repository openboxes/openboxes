import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchAvailableApprovers, fetchRequisitionStatusCodes, fetchShipmentTypes } from 'actions';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import useOutboundFilters from 'hooks/list-pages/outbound/useOutboundFilters';
import { fetchLocationById, fetchPriorityLevels, fetchUserById } from 'utils/option-utils';

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
  fetchShipmentTypes: jest.fn(() => ({ type: 'FETCH_SHIPMENT_TYPES' })),
  fetchRequisitionStatusCodes: jest.fn((sourceType) => ({ type: 'FETCH_REQUISITION_STATUSES', sourceType })),
  fetchAvailableApprovers: jest.fn(() => ({ type: 'FETCH_APPROVERS' })),
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchLocationById: jest.fn(),
  fetchPriorityLevels: jest.fn(),
  fetchUserById: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const EMPTY_FILTERS = {
  requisitionStatusCode: '',
  origin: '',
  destination: '',
  shipmentType: '',
  requestedBy: '',
  createdBy: '',
  updatedBy: '',
  requestType: '',
  priorityLevel: '',
  createdAfter: '',
  createdBefore: '',
};

describe('useOutboundFilters', () => {
  let state;
  let history;
  let dispatch;

  const currentUser = { id: 'user-1', name: 'Jane Doe' };
  const originOption = {
    id: 'loc-1', value: 'loc-1', name: 'Main Depot', label: 'Main Depot',
  };
  const priorityLevelOptions = [
    { id: 'NORMAL', value: 'NORMAL', label: 'Normal (0)' },
    { id: 'HIGH', value: 'HIGH', label: 'High (50-99)' },
    { id: 'CRITICAL', value: 'CRITICAL', label: 'Critical (100+)' },
  ];

  const renderFilters = (sourceType) => renderHook((type) => useOutboundFilters(type), {
    initialProps: sourceType,
  });

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      requisitionStatuses: {
        data: [{ id: 'PENDING', value: 'PENDING' }, { id: 'ISSUED', value: 'ISSUED' }],
      },
      session: {
        currentLocation: { id: 'loc-1', name: 'Main Depot' },
        user: currentUser,
        activeLanguage: 'en',
      },
      stockMovementCommon: { shipmentTypes: [{ id: 'air' }, { id: 'sea' }] },
      approvers: { data: [{ id: 'appr-1' }, { id: 'appr-2' }] },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/stockMovement/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchLocationById.mockResolvedValue({ id: 'dest-1', name: 'Clinic' });
    fetchUserById.mockImplementation(async (id) => ({ id, name: `User ${id}` }));
    fetchPriorityLevels.mockResolvedValue(priorityLevelOptions);
  });

  it('fetches shipment types, requisition statuses for the source type and approvers on mount', () => {
    renderFilters('ELECTRONIC');

    expect(fetchShipmentTypes).toHaveBeenCalledTimes(1);
    expect(fetchRequisitionStatusCodes).toHaveBeenCalledWith('ELECTRONIC');
    expect(fetchAvailableApprovers).toHaveBeenCalledTimes(1);
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_SHIPMENT_TYPES' });
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_REQUISITION_STATUSES', sourceType: 'ELECTRONIC' });
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_APPROVERS' });
  });

  it('defaults the origin to the current location and the direction to outbound', async () => {
    const { result } = renderFilters(undefined);
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      origin: originOption,
      direction: 'OUTBOUND',
    });
    expect(fetchLocationById).not.toHaveBeenCalled();
  });

  it('exposes the fetched priority levels as filter options', async () => {
    const { result } = renderFilters(undefined);
    await flushPromises();

    expect(fetchPriorityLevels).toHaveBeenCalledTimes(1);
    expect(result.current.priorityLevels).toEqual(priorityLevelOptions);
  });

  it('builds the default values from every supported query parameter', async () => {
    history.location.search = [
      'requisitionStatusCode=PENDING',
      'requisitionStatusCode=UNKNOWN',
      'receiptStatusCode=RECEIVED',
      'destination=dest-1',
      'requestedBy=user-1',
      'createdBy=user-2',
      'updatedBy=user-3',
      'createdAfter=01/01/2026',
      'createdBefore=02/01/2026',
      'shipmentType=sea',
      'priorityLevel=HIGH',
      'priorityLevel=CRITICAL',
      'priorityLevel=UNKNOWN',
      'approver=appr-2',
      'approver=null',
    ].join('&');
    const { result } = renderFilters('ELECTRONIC');
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      approver: [{ id: 'appr-2' }, { id: 'null', value: 'null' }],
      origin: originOption,
      direction: 'OUTBOUND',
      sourceType: 'ELECTRONIC',
      requisitionStatusCode: [{ id: 'PENDING', value: 'PENDING' }],
      receiptStatusCode: ['RECEIVED'],
      destination: { id: 'dest-1', name: 'Clinic' },
      requestedBy: currentUser,
      createdBy: { id: 'user-2', name: 'User user-2' },
      updatedBy: { id: 'user-3', name: 'User user-3' },
      createdAfter: '01/01/2026',
      createdBefore: '02/01/2026',
      shipmentType: [{ id: 'sea' }],
      priorityLevel: [priorityLevelOptions[1], priorityLevelOptions[2]],
    });
    expect(fetchLocationById).toHaveBeenCalledWith('dest-1');
    expect(fetchUserById).toHaveBeenCalledTimes(2);
    expect(fetchUserById).not.toHaveBeenCalledWith('user-1');
  });

  it('resolves created and updated by the current user without fetching, and ignores approvers for non-requests', async () => {
    history.location.search = 'createdBy=user-1&updatedBy=user-1&approver=appr-1';
    const { result } = renderFilters(undefined);
    await flushPromises();

    expect(result.current.defaultFilterValues.createdBy).toBe(currentUser);
    expect(result.current.defaultFilterValues.updatedBy).toBe(currentUser);
    expect(result.current.defaultFilterValues.approver).toBeUndefined();
    expect(fetchUserById).not.toHaveBeenCalled();
  });

  it('pushes the filters to the url and stores them as filter params', () => {
    const { result } = renderFilters('ELECTRONIC');
    const values = {
      direction: 'OUTBOUND',
      sourceType: 'ELECTRONIC',
      destination: { id: 'dest-1' },
      requisitionStatusCode: [{ id: 'PENDING' }, { id: 'ISSUED' }],
      createdAfter: '01/01/2026',
      ignored: 'value',
      shipmentType: [],
      priorityLevel: [{ id: 'HIGH' }, { id: 'CRITICAL' }],
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stockMovement/list',
      search: 'createdAfter=01%2F01%2F2026&destination=dest-1&direction=OUTBOUND'
        + '&priorityLevel=HIGH&priorityLevel=CRITICAL'
        + '&requisitionStatusCode=PENDING&requisitionStatusCode=ISSUED&sourceType=ELECTRONIC',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('stores the filter params without touching the url when nothing maps to a query', () => {
    const { result } = renderFilters(undefined);

    act(() => result.current.setFilterValues({ createdAfter: '' }));

    expect(history.push).not.toHaveBeenCalled();
    expect(result.current.filterParams).toEqual({ createdAfter: '' });
  });

  it('selects the current user as requester and creator', async () => {
    const { result } = renderFilters(undefined);
    await flushPromises();

    act(() => result.current.selectFiltersForMyStockMovements());

    const userOption = {
      id: 'user-1', value: 'user-1', label: 'Jane Doe', name: 'Jane Doe',
    };
    expect(history.push).toHaveBeenCalledWith({
      pathname: STOCK_MOVEMENT_URL.list(),
      search: 'createdBy=user-1&direction=OUTBOUND&requestedBy=user-1',
    });
    expect(result.current.defaultFilterValues).toMatchObject({
      origin: originOption,
      requestedBy: userOption,
      createdBy: userOption,
    });
  });

  it('resets the url to the outbound direction when the filters are rebuilt after initialization', async () => {
    const { rerender } = renderFilters(undefined);
    await flushPromises();
    expect(history.push).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = false;
    rerender(undefined);
    state.filterForm.shouldRebuildParams = true;
    rerender(undefined);

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/stockMovement/list',
      search: 'direction=OUTBOUND',
    });
  });
});
