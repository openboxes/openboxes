import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useTableDataV2 from 'hooks/useTableDataV2';
import apiClient from 'utils/apiClient';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));
jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: { get: jest.fn() },
}));

const URL = '/api/stockMovements';

const createDeferred = () => {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
};

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useTableDataV2', () => {
  let session;
  let requests;
  let props;

  const renderTableData = (overrides) => renderHook((hookProps) => useTableDataV2(hookProps), {
    initialProps: { ...props, ...overrides },
  });

  const settle = async (callback) => {
    callback();
    await flushPromises();
  };

  beforeEach(() => {
    jest.clearAllMocks();
    session = { currentLocation: { id: 'loc-1' } };
    useSelector.mockImplementation((selector) => selector({
      session,
      localize: { languages: [], translations: {}, options: {} },
    }));
    requests = [];
    apiClient.get.mockImplementation(() => {
      const deferred = createDeferred();
      requests.push(deferred);
      return deferred.promise;
    });
    props = {
      url: URL,
      errorMessageId: 'react.stockMovement.fetch.error',
      defaultErrorMessage: 'Unable to fetch stock movements',
      getParams: jest.fn(({ sortingParams }) => ({ max: 10, offset: 0, ...sortingParams })),
      pageSize: 10,
      sort: 'dateCreated',
      order: 'desc',
      shouldFetch: true,
      serializedParams: '{"pageIndex":0}',
      setShouldFetch: jest.fn(),
      onFetchedData: jest.fn(),
    };
  });

  it('fetches the rows with the sorting and parameters of the table', () => {
    const { result } = renderTableData();

    expect(props.getParams).toHaveBeenCalledWith({ sortingParams: { sort: 'dateCreated', order: 'desc' } });
    expect(apiClient.get).toHaveBeenCalledWith(URL, expect.objectContaining({
      params: {
        max: 10, offset: 0, sort: 'dateCreated', order: 'desc',
      },
      cancelToken: expect.anything(),
    }));
    const { paramsSerializer } = apiClient.get.mock.calls[0][1];
    expect(paramsSerializer({ status: ['PENDING', 'SHIPPED'], q: 'a b' }))
      .toBe('q=a%20b&status=PENDING&status=SHIPPED');
    expect(result.current.loading).toBe(true);
  });

  it('shows the fetched rows and ends the loading', async () => {
    const { result } = renderTableData();
    const response = { data: [{ id: 'sm-1' }], totalCount: 31 };

    await settle(() => requests[0].resolve({ data: response }));

    expect(result.current.tableData).toEqual({ data: [{ id: 'sm-1' }], totalCount: 31 });
    expect(result.current.loading).toBe(false);
    expect(props.onFetchedData).toHaveBeenCalledWith(response);
    expect(props.setShouldFetch).toHaveBeenCalledWith(false);
  });

  it('keeps loading until the last pending request settles', async () => {
    const { result } = renderTableData();
    act(() => result.current.fetchData());

    await settle(() => requests[0].resolve({ data: { data: [{ id: 'old' }], totalCount: 1 } }));
    expect(result.current.loading).toBe(true);

    await settle(() => requests[1].resolve({ data: { data: [{ id: 'new' }], totalCount: 1 } }));
    expect(result.current.loading).toBe(false);
    expect(result.current.tableData.data).toEqual([{ id: 'new' }]);
  });

  it('cancels the pending request when a new one starts', () => {
    const { rerender } = renderTableData();
    const firstToken = apiClient.get.mock.calls[0][1].cancelToken;

    rerender({ ...props, order: 'asc' });

    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(firstToken.reason.message).toBe('Cancelled due to new request');
    expect(apiClient.get.mock.calls[1][1].params.order).toBe('asc');
    expect(apiClient.get.mock.calls[1][1].cancelToken.reason).toBeUndefined();
  });

  it.each([
    ['the page size', { pageSize: 25 }],
    ['the sorted column', { sort: 'name' }],
    ['the parameters', { serializedParams: '{"pageIndex":1}' }],
  ])('fetches again when %s changes', (description, change) => {
    const { rerender } = renderTableData();

    rerender({ ...props, ...change });

    expect(apiClient.get).toHaveBeenCalledTimes(2);
  });

  it('does not fetch again on a render without changes', () => {
    const { rerender } = renderTableData();

    rerender({ ...props });

    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });

  it('does not fetch until asked to, but shows the loader meanwhile', () => {
    const { result } = renderTableData({ shouldFetch: false });

    expect(apiClient.get).not.toHaveBeenCalled();
    expect(result.current.loading).toBe(true);
  });

  it('does not show the loader before the first fetch when told not to', () => {
    const { result } = renderTableData({ shouldFetch: false, disableInitialLoading: true });

    expect(result.current.loading).toBe(false);
  });

  it('waits for the filters to be initialized', () => {
    const { rerender } = renderTableData({ filtersInitialized: false });
    expect(apiClient.get).not.toHaveBeenCalled();

    rerender({ ...props, filtersInitialized: true });

    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });

  it('keeps the rows and ends the loading when the request fails', async () => {
    const { result } = renderTableData();
    act(() => result.current.setTableData({ data: [{ id: 'kept' }], totalCount: 1 }));

    await settle(() => requests[0].reject(new Error('Network Error')));

    expect(result.current.tableData).toEqual({ data: [{ id: 'kept' }], totalCount: 1 });
    expect(result.current.loading).toBe(false);
    expect(props.setShouldFetch).toHaveBeenCalledWith(false);
    expect(props.onFetchedData).not.toHaveBeenCalled();
  });

  it('cancels the pending request when the location changes', () => {
    const { result, rerender } = renderTableData();
    const { token } = result.current.sourceRef.current;

    session = { currentLocation: { id: 'loc-2' } };
    rerender({ ...props });

    expect(token.reason.message).toBe('Fetching canceled');
  });

  it('works without the optional callbacks', async () => {
    const { result } = renderTableData({ setShouldFetch: undefined, onFetchedData: undefined });

    await settle(() => requests[0].resolve({ data: { data: [], totalCount: 0 } }));

    expect(result.current.tableData).toEqual({ data: [], totalCount: 0 });
  });
});
