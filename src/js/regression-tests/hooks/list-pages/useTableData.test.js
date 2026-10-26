import { renderHook } from '@testing-library/react-hooks';
import queryString from 'query-string';
import { useSelector } from 'react-redux';

import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));
jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: { get: jest.fn() },
}));

const URL = '/api/stockMovements';

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useTableData', () => {
  let session;
  let table;
  let getParams;
  let onFetchedData;

  // The data table normally attaches itself to tableRef before the hook's effects run;
  // attach a stub during render to reproduce that.
  const renderTableData = (overrides = {}) => renderHook((props) => {
    const hook = useTableData(props);
    if (!hook.tableRef.current) {
      hook.tableRef.current = table;
    }
    return hook;
  }, {
    initialProps: {
      filterParams: { status: 'PENDING' },
      url: URL,
      errorMessageId: 'react.error.label',
      defaultErrorMessage: 'Unable to fetch',
      getParams,
      onFetchedData,
      defaultSorting: { sort: 'dateCreated', order: 'desc' },
      ...overrides,
    },
  });

  beforeEach(() => {
    jest.clearAllMocks();
    session = { currentLocation: { id: 'loc-1', name: 'Main' } };
    useSelector.mockImplementation((selector) => selector({
      session,
      localize: { languages: [], translations: {}, options: {} },
    }));
    table = { state: { page: 0 }, fireFetchData: jest.fn(), onPageChange: jest.fn() };
    getParams = jest.fn(({ offset, sortingParams }) => ({ offset, ...sortingParams }));
    onFetchedData = jest.fn();
    apiClient.get.mockResolvedValue({ data: { data: [{ id: 'a' }], totalCount: 25 } });
  });

  it('starts empty and asks the table to fetch on mount', () => {
    const { result } = renderTableData();

    expect(result.current.loading).toBe(false);
    expect(result.current.tableData).toEqual({
      data: [], pages: -1, totalCount: 0, currentParams: {},
    });
    expect(table.fireFetchData).toHaveBeenCalledTimes(1);
    expect(table.onPageChange).not.toHaveBeenCalled();
  });

  it('resets the table to the first page instead of fetching when it is past page 0', () => {
    table.state.page = 3;
    renderTableData();

    expect(table.onPageChange).toHaveBeenCalledWith(0);
    expect(table.fireFetchData).not.toHaveBeenCalled();
  });

  it('refetches when filter params change and replaces the cancel token', () => {
    const { result, rerender } = renderTableData();
    const firstSource = result.current.sourceRef.current;

    rerender({
      filterParams: { status: 'SHIPPED' },
      url: URL,
      getParams,
      defaultSorting: {},
    });

    expect(table.fireFetchData).toHaveBeenCalledTimes(2);
    expect(result.current.sourceRef.current).not.toBe(firstSource);
  });

  it('fetches with offset, table sorting and the current location, then stores the page', async () => {
    const { result } = renderTableData();

    result.current.onFetchHandler({ page: 2, pageSize: 10, sorted: [{ id: 'name', desc: true }] });
    expect(result.current.loading).toBe(true);
    await flushPromises();

    expect(getParams).toHaveBeenCalledWith({
      offset: 20,
      currentLocation: session.currentLocation,
      state: { page: 2, pageSize: 10, sorted: [{ id: 'name', desc: true }] },
      sortingParams: { sort: 'name', order: 'desc' },
    });
    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe(URL);
    expect(config.params).toEqual({ offset: 20, sort: 'name', order: 'desc' });
    expect(config.cancelToken).toBe(result.current.sourceRef.current.token);
    expect(config.paramsSerializer({ a: [1, 2] })).toBe(queryString.stringify({ a: [1, 2] }));
    expect(result.current.tableData).toEqual({
      data: [{ id: 'a' }],
      totalCount: 25,
      pages: 3,
      currentParams: { offset: 20, sort: 'name', order: 'desc' },
    });
    expect(onFetchedData).toHaveBeenCalledWith({ data: [{ id: 'a' }], totalCount: 25 });
    expect(result.current.loading).toBe(false);
  });

  it('uses ascending order for an ascending column and the default sorting when unsorted', async () => {
    const { result } = renderTableData({ onFetchedData: undefined });

    result.current.onFetchHandler({ page: 0, pageSize: 5, sorted: [{ id: 'code', desc: false }] });
    result.current.onFetchHandler({ page: 0, pageSize: 5, sorted: [] });
    await flushPromises();

    expect(getParams.mock.calls[0][0].sortingParams).toEqual({ sort: 'code', order: 'asc' });
    expect(getParams.mock.calls[0][0].offset).toBe(0);
    expect(getParams.mock.calls[1][0].sortingParams).toEqual({ sort: 'dateCreated', order: 'desc' });
    expect(result.current.tableData.pages).toBe(5);
  });

  it('does not fetch while there are no filter params', () => {
    const { result } = renderTableData({ filterParams: {} });

    result.current.onFetchHandler({ page: 0, pageSize: 10 });

    expect(apiClient.get).not.toHaveBeenCalled();
    expect(result.current.loading).toBe(false);
  });

  it('rejects with the translated default message when the request fails', async () => {
    // The rejection is not returned to the caller (nothing can handle it), so capture it where
    // it is created and hand back a settled promise to keep the run free of unhandled rejections
    const rejections = [];
    const originalReject = Promise.reject.bind(Promise);
    apiClient.get.mockImplementation(() => originalReject(new Error('boom')));
    const rejectSpy = jest.spyOn(Promise, 'reject').mockImplementation((reason) => {
      rejections.push(reason);
      return Promise.resolve();
    });
    const { result } = renderTableData();

    result.current.onFetchHandler({ page: 0, pageSize: 10 });
    await flushPromises();
    rejectSpy.mockRestore();

    expect(result.current.loading).toBe(false);
    expect(result.current.tableData.data).toEqual([]);
    expect(onFetchedData).not.toHaveBeenCalled();
    expect(rejections.map((error) => error.message)).toEqual(['Unable to fetch']);
  });

  it('cancels the pending request on unmount when a location is set', () => {
    const { result, unmount } = renderTableData();
    const cancel = jest.spyOn(result.current.sourceRef.current, 'cancel');

    unmount();

    expect(cancel).toHaveBeenCalledWith('Fetching canceled');
  });

  it('does not cancel on unmount without a current location', () => {
    session.currentLocation = null;
    const { result, unmount } = renderTableData();
    const cancel = jest.spyOn(result.current.sourceRef.current, 'cancel');

    unmount();

    expect(cancel).not.toHaveBeenCalled();
  });
});
