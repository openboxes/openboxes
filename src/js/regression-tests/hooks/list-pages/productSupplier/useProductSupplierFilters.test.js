import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchPreferenceTypes } from 'actions';
import useProductSupplierFilters from 'hooks/list-pages/productSupplier/useProductSupplierFilters';
import { fetchOrganization, fetchProduct } from 'utils/option-utils';

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
  fetchPreferenceTypes: jest.fn((config) => ({ type: 'FETCH_PREFERENCE_TYPES', config })),
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchOrganization: jest.fn(),
  fetchProduct: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const EMPTY_FILTERS = {
  product: '',
  supplier: '',
  defaultPreferenceTypes: '',
  createdFrom: '',
  createdTo: '',
  includeInactive: '',
  tab: 'details',
};

describe('useProductSupplierFilters', () => {
  let state;
  let history;
  let dispatch;

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      productSupplier: { preferenceTypes: [{ id: 'PRIMARY' }, { id: 'SECONDARY' }, { id: 'NONE' }] },
      session: { currentLocation: { id: 'loc-1' } },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/productSupplier/list', search: '' }, push: jest.fn() };
    dispatch = jest.fn();
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(dispatch);
    useHistory.mockReturnValue(history);
    fetchProduct.mockResolvedValue({ id: 'prod-1', productCode: 'P1', name: 'Paracetamol' });
    fetchOrganization.mockResolvedValue({ id: 'sup-1', displayName: 'Supplier One' });
  });

  it('fetches preference types including none and aborts the request on unmount', () => {
    const { unmount } = renderHook(() => useProductSupplierFilters([]));

    const [config] = fetchPreferenceTypes.mock.calls[0];
    expect(config.params).toEqual({ includeNone: true });
    expect(config.signal.aborted).toBe(false);
    expect(dispatch).toHaveBeenCalledWith({ type: 'FETCH_PREFERENCE_TYPES', config });

    unmount();
    expect(config.signal.aborted).toBe(true);
  });

  it('defaults to the details tab with empty filters', async () => {
    const { result } = renderHook(() => useProductSupplierFilters([]));
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual(EMPTY_FILTERS);
  });

  it('builds the defaults from the query, labelling the fetched product and supplier', async () => {
    history.location.search = 'searchTerm=abc&product=prod-1&supplier=sup-1&createdFrom=2026-01-01'
      + '&createdTo=2026-02-01&active=true&defaultPreferenceTypes=PRIMARY&defaultPreferenceTypes=NONE';
    const { result } = renderHook(() => useProductSupplierFilters([]));
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      ...EMPTY_FILTERS,
      searchTerm: 'abc',
      product: {
        id: 'prod-1', productCode: 'P1', name: 'Paracetamol', label: 'P1 - Paracetamol',
      },
      supplier: { id: 'sup-1', displayName: 'Supplier One', label: 'Supplier One' },
      createdFrom: '2026-01-01',
      createdTo: '2026-02-01',
      active: 'true',
      defaultPreferenceTypes: [{ id: 'PRIMARY' }, { id: 'NONE' }],
    });
    expect(fetchProduct).toHaveBeenCalledWith('prod-1');
    expect(fetchOrganization).toHaveBeenCalledWith('sup-1');
  });

  it('prefers the product display name and skips products and suppliers that are not found', async () => {
    fetchProduct.mockResolvedValueOnce({ productCode: 'P2', displayName: 'Shown', name: 'Raw' });
    history.location.search = 'product=prod-2';
    const first = renderHook(() => useProductSupplierFilters([]));
    await flushPromises();
    expect(first.result.current.defaultFilterValues.product.label).toBe('P2 - Shown');

    fetchProduct.mockResolvedValueOnce(null);
    fetchOrganization.mockResolvedValueOnce(undefined);
    history.location.search = 'product=gone&supplier=gone';
    const second = renderHook(() => useProductSupplierFilters([]));
    await flushPromises();
    expect(second.result.current.defaultFilterValues.product).toBe('');
    expect(second.result.current.defaultFilterValues.supplier).toBe('');
  });

  it('pushes the mapped filters and stores the params', () => {
    const { result } = renderHook(() => useProductSupplierFilters([]));
    const values = {
      searchTerm: 'abc',
      product: { id: 'prod-1' },
      tab: 'details',
      defaultPreferenceTypes: [{ id: 'PRIMARY' }],
      includeInactive: true,
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/productSupplier/list',
      search: 'defaultPreferenceTypes=PRIMARY&includeInactive=true&product=prod-1&searchTerm=abc&tab=details',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('keeps only the ignored query params when the filters are cleared on a rebuild', async () => {
    history.location.search = 'tab=details&searchTerm=abc&product=prod-1';
    const { result, rerender } = renderHook(() => useProductSupplierFilters(['tab']));
    await flushPromises();
    act(() => result.current.setFilterValues({}));
    expect(history.push).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/productSupplier/list', search: 'tab=details' });
  });
});
