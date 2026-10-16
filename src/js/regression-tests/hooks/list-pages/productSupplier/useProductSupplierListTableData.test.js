import { renderHook } from '@testing-library/react-hooks';

import useProductSupplierListTableData from 'hooks/list-pages/productSupplier/useProductSupplierListTableData';
import useTableData from 'hooks/list-pages/useTableData';

jest.mock('hooks/list-pages/useTableData', () => ({ __esModule: true, default: jest.fn() }));

describe('useProductSupplierListTableData', () => {
  let tableArgs;
  const hookResult = {
    tableRef: { current: null },
    loading: false,
    tableData: { data: [] },
    onFetchHandler: jest.fn(),
    fireFetchData: jest.fn(),
  };

  beforeEach(() => {
    useTableData.mockImplementation((args) => {
      tableArgs = args;
      return hookResult;
    });
  });

  it('sorts by creation date and passes the table helpers through', () => {
    const { result } = renderHook(() => useProductSupplierListTableData({}));

    expect(tableArgs).toMatchObject({
      url: '/api/productSuppliers',
      defaultSorting: { sort: 'dateCreated', order: 'desc' },
      errorMessageId: 'react.productSupplier.error.productSupplierList.label',
      defaultErrorMessage: 'Unable to fetch product sources',
    });
    expect(result.current.fireFetchData).toBe(hookResult.fireFetchData);
    expect(result.current.tableData).toBe(hookResult.tableData);
  });

  it('maps product, supplier and preference types to ids and drops false and empty values', () => {
    renderHook(() => useProductSupplierListTableData({
      searchTerm: 'abc',
      product: { id: 'prod-1' },
      supplier: { id: 'sup-1' },
      defaultPreferenceTypes: [{ id: 'PRIMARY' }, { id: 'NONE' }],
      includeInactive: false,
      createdFrom: '',
      tab: 'details',
    }));

    expect(tableArgs.getParams({
      offset: 50, state: { pageSize: 25 }, sortingParams: { sort: 'code', order: 'asc' },
    })).toEqual({
      offset: '50',
      max: '25',
      sort: 'code',
      order: 'asc',
      searchTerm: 'abc',
      product: 'prod-1',
      supplier: 'sup-1',
      defaultPreferenceTypes: ['PRIMARY', 'NONE'],
      tab: 'details',
    });
  });

  it('keeps an enabled boolean filter', () => {
    renderHook(() => useProductSupplierListTableData({ includeInactive: true }));

    expect(tableArgs.getParams({ offset: 0, state: { pageSize: 10 }, sortingParams: {} }))
      .toEqual({ offset: '0', max: '10', includeInactive: true });
  });
});
