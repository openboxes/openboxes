import { renderHook } from '@testing-library/react-hooks';
import { confirmAlert } from 'react-confirm-alert';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useStockListTableData from 'hooks/list-pages/stock-list/useStockListTableData';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn() }));
jest.mock('hooks/list-pages/useTableData', () => ({ __esModule: true, default: jest.fn() }));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useStockListTableData', () => {
  let dispatch;
  let fireFetchData;
  let tableArgs;
  const tableData = { data: [], currentParams: { offset: '0', max: '10', name: 'abc' } };

  const renderTable = (filterParams = {}) => renderHook(() => useStockListTableData(filterParams));
  const confirmButtons = () => confirmAlert.mock.calls[0][0].buttons;

  beforeEach(() => {
    jest.clearAllMocks();
    dispatch = jest.fn();
    fireFetchData = jest.fn();
    useDispatch.mockReturnValue(dispatch);
    useSelector.mockImplementation((selector) => selector({
      localize: { languages: [], translations: {}, options: {} },
    }));
    useTableData.mockImplementation((args) => {
      tableArgs = args;
      return {
        tableRef: { current: null },
        fireFetchData,
        loading: false,
        tableData,
        onFetchHandler: jest.fn(),
      };
    });
  });

  it('configures the table for the stock list api', () => {
    const filterParams = { name: 'abc' };
    const { result } = renderTable(filterParams);

    expect(tableArgs).toMatchObject({
      filterParams,
      url: '/api/stocklists',
      errorMessageId: 'react.stocklists.fetch.fail.label',
      defaultErrorMessage: 'Unable to fetch stock transfers',
    });
    expect(result.current.tableData).toBe(tableData);
  });

  it('maps the filters to request params and drops empty values', () => {
    renderTable({
      name: 'abc',
      isPublished: true,
      origin: [{ id: 'o1' }, { id: 'o2' }],
      destination: [],
      other: '',
    });

    expect(tableArgs.getParams({
      offset: 20,
      state: { pageSize: 10 },
      sortingParams: { sort: 'name', order: 'asc' },
    })).toEqual({
      name: 'abc',
      offset: '20',
      max: '10',
      includeUnpublished: true,
      origin: ['o1', 'o2'],
      sort: 'name',
      order: 'asc',
    });
  });

  it('omits missing origin, destination and publication filters', () => {
    renderTable({});

    expect(tableArgs.getParams({ offset: 0, state: { pageSize: 5 }, sortingParams: undefined }))
      .toEqual({ offset: '0', max: '5' });
  });

  it('exports the current list and a single stock list', () => {
    const { result } = renderTable();

    result.current.exportStockList();
    result.current.exportStockListItems('sl-1');

    expect(exportFileFromAPI).toHaveBeenCalledWith({
      url: '/api/stocklists',
      params: { offset: '0', max: '10', name: 'abc' },
    });
    expect(exportFileFromAPI).toHaveBeenCalledWith({ url: '/api/stocklists/sl-1/export' });
  });

  it('deletes after confirmation and refreshes the table', async () => {
    apiClient.delete.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.onClickDeleteStocklists('sl-1');
    const [yes, no] = confirmButtons();
    expect(confirmAlert.mock.calls[0][0].title).toBe('Confirm delete of stock list');
    expect(yes.label).toBe('Yes');
    expect(no).toEqual({ label: 'No' });

    yes.onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/stocklists/sl-1');
    expect(Alert.success).toHaveBeenCalledWith('Stock List has been deleted successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls.map(([action]) => action.type))
      .toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('clears after confirmation and refreshes the table', async () => {
    apiClient.post.mockResolvedValue({ status: 200 });
    const { result } = renderTable();

    result.current.onClickClearStocklists('sl-2');
    expect(confirmAlert.mock.calls[0][0].message).toBe('Are you sure you want to clear this stock list ?');
    confirmButtons()[0].onClick();
    await flushPromises();

    expect(apiClient.post).toHaveBeenCalledWith('/api/stocklists/sl-2/clear');
    expect(Alert.success).toHaveBeenCalledTimes(1);
    expect(fireFetchData).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['cloneStocklists', '/api/stocklists/sl-3/clone', 'Stock List has been cloned successfully'],
    ['publishStocklists', '/api/stocklists/sl-3/publish', 'Stock List has been published successfully'],
    ['unpublishStocklists', '/api/stocklists/sl-3/unpublish', 'Stock List has been unpublished successfully'],
  ])('%s posts to the api, notifies and refreshes', async (action, url, message) => {
    apiClient.post.mockResolvedValue({ status: 200 });
    const { result } = renderTable();

    await result.current[action]('sl-3');

    expect(apiClient.post).toHaveBeenCalledWith(url);
    expect(Alert.success).toHaveBeenCalledWith(message);
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('does not notify or refresh on an unexpected status, and hides the spinner on failure', async () => {
    apiClient.post.mockResolvedValueOnce({ status: 202 });
    const { result } = renderTable();
    await result.current.publishStocklists('sl-4');
    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();

    dispatch.mockClear();
    apiClient.post.mockRejectedValueOnce(new Error('denied'));
    await expect(result.current.cloneStocklists('sl-4')).rejects.toThrow('denied');
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });
});
