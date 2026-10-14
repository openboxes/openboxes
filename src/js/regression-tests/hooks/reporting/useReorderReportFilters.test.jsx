import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { createMemoryHistory } from 'history';
import { Router } from 'react-router-dom';

import { LOCATION_API, REORDER_REPORT } from 'api/urls';
import { EXPIRATION_FILTER, INVENTORY_LEVEL_STATUS } from 'consts/filterOptions';
import useReorderReportFilters from 'hooks/reporting/useReorderReportFilters';
import apiClient from 'utils/apiClient';
import fileDownloadUtil from 'utils/file-download-util';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/file-download-util', () => jest.fn(() => Promise.resolve()));

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const categories = [{ id: 'c1', label: 'Drugs' }, { id: 'c2', label: 'Supplies' }];
const tags = [{ id: 't1', label: 'Cold chain' }];
const locations = [
  { id: 'loc-1', name: 'Main' },
  { id: 'loc-2', name: 'Annex' },
  { id: 'loc-3', name: 'Ward' },
];

describe('useReorderReportFilters', () => {
  let history;

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      session: { currentLocation: { id: 'loc-1', name: 'Main' } },
      filterForm: { shouldRebuildParams: true },
    };
    apiClient.get.mockImplementation((url) => Promise.resolve({
      data: { data: url === '/api/categoryOptions' ? categories : tags },
    }));
    apiClient.mockResolvedValue({ data: { data: locations } });
  });

  const renderFilters = async (search = '') => {
    history = createMemoryHistory({ initialEntries: [`/report/reorder${search}`] });
    const wrapper = ({ children }) => <Router history={history}>{children}</Router>;
    const rendered = renderHook(() => useReorderReportFilters(), { wrapper });
    await flushPromises();
    return rendered;
  };

  it('loads the option lists, leaving out the current location', async () => {
    const { result } = await renderFilters();

    expect(apiClient.get).toHaveBeenCalledWith('/api/categoryOptions');
    expect(apiClient.get).toHaveBeenCalledWith('/api/tagOptions', { params: {} });
    expect(apiClient).toHaveBeenCalledWith(LOCATION_API, expect.objectContaining({
      params: { activityCodes: ['MANAGE_INVENTORY'] },
    }));
    expect(result.current.categories).toEqual(categories);
    expect(result.current.tags).toEqual(tags);
    expect(result.current.locations).toEqual([
      { id: 'loc-2', value: 'loc-2', label: 'Annex' },
      { id: 'loc-3', value: 'loc-3', label: 'Ward' },
    ]);
  });

  it('selects the default product and expiry options when the URL has no filters', async () => {
    const { result } = await renderFilters();
    const values = result.current.defaultFilterValues;

    expect(Object.keys(values).sort()).toEqual([
      'additionalInventoryLocations', 'categories', 'expiredStock', 'filterProducts', 'tags',
    ]);
    expect(values.filterProducts.id).toBe(INVENTORY_LEVEL_STATUS.ALL_PRODUCTS);
    expect(values.expiredStock.id).toBe(EXPIRATION_FILTER.SUBTRACT_EXPIRED_STOCK);
    expect(values.categories).toBeNull();
    expect(values.additionalInventoryLocations).toBeNull();
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'HIDE_SPINNER', payload: false });
  });

  it('restores selected options from the URL', async () => {
    const { result } = await renderFilters(
      '?categories=c2&tags=t1&additionalInventoryLocations=loc-3&additionalInventoryLocations=loc-2'
      + `&filterProducts=${INVENTORY_LEVEL_STATUS.BELOW_MINIMUM}`
      + `&expiredStock=${EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_YEAR}`,
    );
    const values = result.current.defaultFilterValues;

    expect(values.categories).toEqual([{
      id: 'c2', label: 'Supplies', name: 'Supplies', value: 'c2',
    }]);
    expect(values.tags).toEqual([{
      id: 't1', label: 'Cold chain', name: 'Cold chain', value: 't1',
    }]);
    expect(values.additionalInventoryLocations.map(({ id }) => id)).toEqual(['loc-2', 'loc-3']);
    expect(values.filterProducts.id).toBe(INVENTORY_LEVEL_STATUS.BELOW_MINIMUM);
    expect(values.expiredStock.id).toBe(EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_YEAR);
  });

  it('falls back to the default product and expiry options for unknown values', async () => {
    const { result } = await renderFilters('?filterProducts=UNKNOWN&expiredStock=UNKNOWN');
    const values = result.current.defaultFilterValues;

    expect(values.filterProducts.id).toBe(INVENTORY_LEVEL_STATUS.ALL_PRODUCTS);
    expect(values.expiredStock.id).toBe(EXPIRATION_FILTER.SUBTRACT_EXPIRED_STOCK);
    expect(values.categories).toBeNull();
    expect(values.tags).toBeNull();
  });

  it('writes selected filter ids to the URL', async () => {
    const { result } = await renderFilters();
    const values = {
      categories: [{ id: 'c1' }, { id: 'c2' }],
      filterProducts: { id: INVENTORY_LEVEL_STATUS.BELOW_REORDER },
      expiredStock: null,
    };

    act(() => result.current.setFilterValues(values));

    expect(history.location.pathname).toBe('/report/reorder');
    expect(history.location.search).toBe('?categories=c1&categories=c2&filterProducts=BELOW_REORDER');
    expect(result.current.filterParams).toBe(values);
  });

  it('keeps the URL for empty filter values', async () => {
    const { result } = await renderFilters('?tags=t1');

    act(() => result.current.setFilterValues({}));

    expect(history.location.search).toBe('?tags=t1');
  });

  it('downloads the report for the current location with the mapped parameters', async () => {
    const { result } = await renderFilters();
    mockDispatch.mockClear();

    await result.current.downloadCsv({
      expiredStock: { id: EXPIRATION_FILTER.DO_NOT_SUBTRACT_EXPIRED_STOCK },
      filterProducts: { id: INVENTORY_LEVEL_STATUS.BELOW_MAXIMUM },
      categories: [{ id: 'c1' }],
      tags: [],
      additionalInventoryLocations: [{ id: 'loc-2' }],
    });

    expect(fileDownloadUtil).toHaveBeenCalledWith({
      url: REORDER_REPORT('loc-1'),
      params: {
        expiration: EXPIRATION_FILTER.DO_NOT_SUBTRACT_EXPIRED_STOCK,
        inventoryLevelStatus: INVENTORY_LEVEL_STATUS.BELOW_MAXIMUM,
        categories: ['c1'],
        tags: [],
        additionalLocations: ['loc-2'],
      },
    });
    expect(mockDispatch.mock.calls.map(([action]) => action.type)).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('omits unset parameters from the download', async () => {
    const { result } = await renderFilters();

    await result.current.downloadCsv({});

    expect(fileDownloadUtil).toHaveBeenCalledWith({ url: REORDER_REPORT('loc-1'), params: {} });
  });

  it('clears the URL before rebuilding filters for a new location', async () => {
    const { rerender } = await renderFilters('?tags=t1');

    mockState = {
      session: { currentLocation: { id: 'loc-2', name: 'Annex' } },
      filterForm: { shouldRebuildParams: false },
    };
    rerender();
    mockState = { ...mockState, filterForm: { shouldRebuildParams: true } };
    rerender();
    await flushPromises();

    expect(history.location.search).toBe('');
    expect(apiClient).toHaveBeenCalledTimes(2);
  });
});
