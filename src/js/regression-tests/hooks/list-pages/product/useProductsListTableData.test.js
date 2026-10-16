import { renderHook } from '@testing-library/react-hooks';
import fileDownload from 'js-file-download';
import queryString from 'query-string';

import useProductsListTableData from 'hooks/list-pages/product/useProductsListTableData';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';

jest.mock('js-file-download', () => jest.fn());
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/list-pages/useTableData', () => ({ __esModule: true, default: jest.fn() }));

describe('useProductsListTableData', () => {
  let tableArgs;
  const tableData = {
    data: [],
    currentParams: {
      offset: '10', max: '10', name: 'para', format: 'list',
    },
  };

  const renderTable = (filterParams = {}) => renderHook(
    () => useProductsListTableData(filterParams),
  );

  beforeEach(() => {
    jest.clearAllMocks();
    useTableData.mockImplementation((args) => {
      tableArgs = args;
      return {
        tableRef: { current: null }, loading: false, tableData, onFetchHandler: jest.fn(),
      };
    });
    apiClient.get.mockResolvedValue({ data: 'code,name' });
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('sorts by last update by default', () => {
    renderTable();

    expect(tableArgs).toMatchObject({
      url: '/api/products',
      defaultSorting: { sort: 'lastUpdated', order: 'desc' },
      errorMessageId: 'react.productsList.fetch.fail.label',
    });
  });

  it('requests the list format, maps multi selects to ids and drops false and empty values', () => {
    renderTable({
      name: 'para',
      includeInactive: true,
      includeCategoryChildren: false,
      catalogId: [{ id: 'c1' }],
      categoryId: [{ id: 'cat-1' }, { id: 'cat-2' }],
      tagId: [],
      glAccountsId: [{ id: 'gl-1' }],
      productFamilyId: [{ id: 'f1' }],
      handlingRequirementId: [{ id: 'cold' }],
      createdAfter: '',
    });

    expect(tableArgs.getParams({
      offset: 0, state: { pageSize: 10 }, sortingParams: { sort: 'name', order: 'asc' },
    })).toEqual({
      format: 'list',
      offset: '0',
      max: '10',
      sort: 'name',
      order: 'asc',
      name: 'para',
      includeInactive: true,
      catalogId: ['c1'],
      categoryId: ['cat-1', 'cat-2'],
      glAccountsId: ['gl-1'],
      productFamilyId: ['f1'],
      handlingRequirementId: ['cold'],
    });
  });

  it('exports the current results as csv without paging', async () => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date(2026, 9, 2, 14, 5, 9));
    const { result } = renderTable();

    await result.current.exportProducts();

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/products');
    expect(config.params).toEqual({ name: 'para', format: 'csv' });
    expect(config.paramsSerializer({ a: 1, b: [2, 3] }))
      .toBe(queryString.stringify({ a: 1, b: [2, 3] }));
    const [content, filename, mimeType] = fileDownload.mock.calls[0];
    expect(content).toBe('\uFEFFcode,name');
    expect(filename).toMatch(/^Products-2026/);
    expect(mimeType).toBe('text/csv');
  });

  it('exports all products or all products with attributes', async () => {
    const { result } = renderTable();

    await result.current.exportProducts(true);
    await result.current.exportProducts(false, true);

    expect(apiClient.get.mock.calls[0][1].params).toEqual({ format: 'csv' });
    expect(apiClient.get.mock.calls[1][1].params).toEqual({ format: 'csv', includeAttributes: true });
  });
});
