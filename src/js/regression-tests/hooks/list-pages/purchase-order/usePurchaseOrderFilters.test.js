import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchBuyers, fetchPaymentTerms, fetchPurchaseOrderStatuses } from 'actions';
import usePurchaseOrderFilters from 'hooks/list-pages/purchase-order/usePurchaseOrderFilters';
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
  fetchBuyers: jest.fn(() => ({ type: 'FETCH_BUYERS' })),
  fetchPaymentTerms: jest.fn(() => ({ type: 'FETCH_PAYMENT_TERMS' })),
  fetchPurchaseOrderStatuses: jest.fn(() => ({ type: 'FETCH_PO_STATUSES' })),
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
  status: '',
  statusStartDate: '',
  statusEndDate: '',
  origin: '',
  destination: '',
  destinationParty: '',
  paymentTerm: '',
  orderedBy: '',
  createdBy: '',
};

describe('usePurchaseOrderFilters', () => {
  let state;
  let history;
  let dispatch;

  const currentUser = { id: 'user-1', name: 'Jane' };
  const currentLocationOption = {
    id: 'loc-1', value: 'loc-1', name: 'Main', label: 'Main',
  };
  const buyers = [{ id: 'org-1', name: 'Org 1' }, { id: 'org-2', name: 'Org 2' }];

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: {
        supportedActivities: [],
        currentLocation: { id: 'loc-1', name: 'Main', organization: { id: 'org-1' } },
        user: currentUser,
        activeLanguage: 'en',
        loading: false,
      },
      organizations: { buyers },
      purchaseOrder: {
        statuses: [{ id: 'PENDING', value: 'PENDING' }, { id: 'PLACED', value: 'PLACED' }],
        paymentTerms: [{ id: 'term-1' }, { id: 'term-2' }],
      },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/purchaseOrder/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchLocationById.mockImplementation(async (id) => ({ id, name: `Location ${id}` }));
    fetchUserById.mockImplementation(async (id) => ({ id, name: `User ${id}` }));
  });

  it('fetches buyers, statuses and payment terms on mount', () => {
    renderHook(() => usePurchaseOrderFilters());

    expect(fetchBuyers).toHaveBeenCalledTimes(1);
    expect(fetchPurchaseOrderStatuses).toHaveBeenCalledTimes(1);
    expect(fetchPaymentTerms).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls.map(([action]) => action.type)).toEqual(expect.arrayContaining([
      'FETCH_BUYERS', 'FETCH_PO_STATUSES', 'FETCH_PAYMENT_TERMS',
    ]));
  });

  it('defaults the destination to the current location without central purchasing', async () => {
    const { result } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();

    expect(result.current.isCentralPurchasingEnabled).toBe(false);
    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      destination: currentLocationOption,
    });
  });

  it('defaults the buyer to the location organization with central purchasing', async () => {
    state.session.supportedActivities = ['ENABLE_CENTRAL_PURCHASING'];
    const { result } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();

    expect(result.current.isCentralPurchasingEnabled).toBe(true);
    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      destinationParty: buyers[0],
    });
  });

  it('builds the defaults from the query parameters', async () => {
    history.location.search = 'status=PLACED&statusStartDate=2026-01-01&statusEndDate=2026-01-31'
      + '&origin=supplier-1&destination=loc-1&paymentTerm=term-2&paymentTerm=null'
      + '&destinationParty=org-2&orderedBy=user-1&createdBy=user-5';
    const { result } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      status: [{ id: 'PLACED', value: 'PLACED' }],
      statusStartDate: '2026-01-01',
      statusEndDate: '2026-01-31',
      origin: { id: 'supplier-1', name: 'Location supplier-1' },
      destination: currentLocationOption,
      paymentTerm: [{ id: 'term-2' }, { id: 'null', value: 'null' }],
      destinationParty: buyers[1],
      orderedBy: currentUser,
      createdBy: { id: 'user-5', name: 'User user-5' },
    });
    expect(fetchLocationById).toHaveBeenCalledTimes(1);
    expect(fetchUserById).toHaveBeenCalledTimes(1);
  });

  it('uses the current location as origin and fetches another destination', async () => {
    history.location.search = 'origin=loc-1&destination=loc-9&orderedBy=user-3&createdBy=user-1';
    const { result } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();

    expect(result.current.defaultFilterValues.origin).toEqual(currentLocationOption);
    expect(result.current.defaultFilterValues.destination).toEqual({ id: 'loc-9', name: 'Location loc-9' });
    expect(result.current.defaultFilterValues.orderedBy).toEqual({ id: 'user-3', name: 'User user-3' });
    expect(result.current.defaultFilterValues.createdBy).toBe(currentUser);
  });

  it('waits for the session and buyers before initializing', async () => {
    state.session.loading = true;
    state.organizations.buyers = undefined;
    const { result, rerender } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();
    expect(result.current.defaultFilterValues).toEqual({});

    state.session.loading = false;
    rerender();
    await flushPromises();
    expect(result.current.defaultFilterValues).toEqual({});

    state.organizations.buyers = buyers;
    rerender();
    await flushPromises();
    expect(result.current.defaultFilterValues.destination).toEqual(currentLocationOption);
  });

  it('pushes the mapped filters and stores the params', () => {
    const { result } = renderHook(() => usePurchaseOrderFilters());
    const values = {
      status: [{ id: 'PENDING' }],
      origin: { id: 'supplier-1' },
      paymentTerm: [{ id: 'term-1' }],
      statusStartDate: '2026-01-01',
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/purchaseOrder/list',
      search: 'origin=supplier-1&paymentTerm=term-1&status=PENDING&statusStartDate=2026-01-01',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('skips the url for empty values and clears it on a rebuild after initialization', async () => {
    const { result, rerender } = renderHook(() => usePurchaseOrderFilters());
    await flushPromises();
    act(() => result.current.setFilterValues({}));
    expect(history.push).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/purchaseOrder/list' });
  });
});
