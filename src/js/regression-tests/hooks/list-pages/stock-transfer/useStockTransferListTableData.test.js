import { renderHook } from '@testing-library/react-hooks';
import { confirmAlert } from 'react-confirm-alert';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useStockTransferListTableData from 'hooks/list-pages/stock-transfer/useStockTransferListTableData';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/list-pages/useTableData', () => ({ __esModule: true, default: jest.fn() }));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useStockTransferListTableData', () => {
  let dispatch;
  let fireFetchData;
  let tableArgs;

  const renderTable = (filterParams = {}) => renderHook(
    () => useStockTransferListTableData(filterParams),
  );

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
        tableData: {},
        onFetchHandler: jest.fn(),
      };
    });
  });

  it('sorts by creation date by default', () => {
    renderTable();

    expect(tableArgs).toMatchObject({
      url: '/api/stockTransfers',
      defaultSorting: { sort: 'dateCreated', order: 'desc' },
      errorMessageId: 'react.stockTransfer.fetch.fail.label',
    });
  });

  it('scopes the request to the current location and maps the filters', () => {
    renderTable({
      createdBy: { id: 'u1' },
      status: [{ value: 'PENDING' }, { value: 'COMPLETED' }],
      lastUpdatedStartDate: '2026-01-01',
      lastUpdatedEndDate: '',
    });

    expect(tableArgs.getParams({
      offset: 30,
      currentLocation: { id: 'loc-1' },
      state: { pageSize: 15 },
      sortingParams: { sort: 'dateCreated', order: 'desc' },
    })).toEqual({
      location: 'loc-1',
      offset: '30',
      max: '15',
      sort: 'dateCreated',
      order: 'desc',
      createdBy: 'u1',
      status: ['PENDING', 'COMPLETED'],
      lastUpdatedStartDate: '2026-01-01',
    });
  });

  it('drops the location and empty filters when they are missing', () => {
    renderTable({ status: [] });

    expect(tableArgs.getParams({
      offset: 0, currentLocation: null, state: { pageSize: 5 }, sortingParams: {},
    }))
      .toEqual({ offset: '0', max: '5' });
  });

  it('deletes after confirmation, notifies and refreshes', async () => {
    apiClient.delete.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.deleteHandler('st-1');
    const { buttons, message } = confirmAlert.mock.calls[0][0];
    expect(message).toBe('Are you sure you want to delete this stock transfer?');
    expect(buttons[1]).toEqual({ label: 'No' });
    buttons[0].onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransfers/st-1');
    expect(Alert.success).toHaveBeenCalledWith('Stock transfer has been deleted successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('refreshes even when the delete does not succeed', async () => {
    apiClient.delete.mockResolvedValue({ status: 400 });
    const { result } = renderTable();

    result.current.deleteHandler('st-2');
    confirmAlert.mock.calls[0][0].buttons[0].onClick();
    await flushPromises();

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).toHaveBeenCalledTimes(1);
  });
});
