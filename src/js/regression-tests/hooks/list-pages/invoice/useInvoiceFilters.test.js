import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchInvoiceStatuses, fetchInvoiceTypeCodes, fetchSuppliers } from 'actions';
import useInvoiceFilters from 'hooks/list-pages/invoice/useInvoiceFilters';
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
  fetchInvoiceStatuses: jest.fn(() => ({ type: 'FETCH_INVOICE_STATUSES' })),
  fetchInvoiceTypeCodes: jest.fn(() => ({ type: 'FETCH_INVOICE_TYPE_CODES' })),
  fetchSuppliers: jest.fn((params) => ({ type: 'FETCH_SUPPLIERS', params })),
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

describe('useInvoiceFilters', () => {
  let state;
  let history;
  let dispatch;
  let setFilterParams;
  const currentUser = { id: 'user-1', name: 'Jane' };
  const buyerOrganization = {
    id: 'org-1', value: 'org-1', name: 'Org', label: 'Org (display)',
  };

  const renderFilters = () => renderHook(() => useInvoiceFilters({ setFilterParams }));

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      invoices: {
        statuses: [{ id: 'PENDING', value: 'PENDING' }, { id: 'POSTED', value: 'POSTED' }],
        typeCodes: [{ id: 'INVOICE', value: 'INVOICE' }, { id: 'PREPAYMENT', value: 'PREPAYMENT' }],
      },
      organizations: { suppliers: [{ id: 'sup-1', value: 'sup-1' }] },
      session: {
        currentLocation: { id: 'loc-1', organization: { id: 'org-1', name: 'Org', displayName: 'Org (display)' } },
        user: currentUser,
        activeLanguage: 'en',
      },
      localize: { languages: [], translations: {}, options: {} },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/invoice/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    setFilterParams = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchUserById.mockImplementation(async (id) => ({ id, name: 'Other' }));
  });

  it('fetches statuses, type codes and suppliers sorted by name on mount', () => {
    renderFilters();

    expect(fetchInvoiceStatuses).toHaveBeenCalledTimes(1);
    expect(fetchInvoiceTypeCodes).toHaveBeenCalledTimes(1);
    expect(fetchSuppliers).toHaveBeenCalledWith({ sort: 'name', order: 'asc' });
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_SUPPLIERS', params: { sort: 'name', order: 'asc' } });
  });

  it('defaults the buyer organization to the current location organization', async () => {
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultValues).toEqual({
      buyerOrganization,
      status: '',
      vendor: '',
      invoiceTypeCode: '',
      dateInvoiced: '',
      createdBy: '',
    });
  });

  it('selects the query values from the loaded options', async () => {
    history.location.search = 'status=POSTED&vendor=sup-1&invoiceTypeCode=PREPAYMENT'
      + '&dateInvoiced=2026-03-01&createdBy=user-7';
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultValues).toEqual({
      buyerOrganization,
      status: { id: 'POSTED', value: 'POSTED' },
      vendor: { id: 'sup-1', value: 'sup-1' },
      invoiceTypeCode: { id: 'PREPAYMENT', value: 'PREPAYMENT' },
      dateInvoiced: '2026-03-01',
      createdBy: { id: 'user-7', name: 'Other' },
    });
  });

  it('uses the current user as creator without fetching', async () => {
    history.location.search = 'createdBy=user-1';
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultValues.createdBy).toBe(currentUser);
    expect(fetchUserById).not.toHaveBeenCalled();
  });

  it('pushes the mapped filters and forwards them to setFilterParams', () => {
    const { result } = renderFilters();
    const values = {
      buyerOrganization: { id: 'org-1' },
      status: { id: 'POSTED' },
      dateInvoiced: '2026-03-01',
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/invoice/list',
      search: 'buyerOrganization=org-1&dateInvoiced=2026-03-01&status=POSTED',
    });
    expect(setFilterParams).toHaveBeenCalledWith(values);
  });

  it('forwards empty values without pushing, and clears the url on a rebuild', async () => {
    const { result, rerender } = renderFilters();
    await flushPromises();

    act(() => result.current.setFilterValues({}));
    expect(history.push).not.toHaveBeenCalled();
    expect(setFilterParams).toHaveBeenCalledWith({});

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/invoice/list' });
  });
});
