import { renderHook } from '@testing-library/react-hooks';
import { confirmAlert } from 'react-confirm-alert';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useInboundListTableData from 'hooks/list-pages/inbound/useInboundListTableData';
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

describe('useInboundListTableData', () => {
  let dispatch;
  let fireFetchData;
  let tableArgs;
  const tableData = { data: [], currentParams: { direction: 'INBOUND' } };

  const renderTable = (filterParams = {}) => renderHook(
    () => useInboundListTableData(filterParams),
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
        tableData,
        onFetchHandler: jest.fn(),
      };
    });
  });

  it('configures the table for the stock movement api', () => {
    renderTable();

    expect(tableArgs).toMatchObject({
      url: '/api/stockMovements',
      errorMessageId: 'react.stockMovement.inbound.fetching.error',
      defaultErrorMessage: 'Unable to fetch inbound movements',
    });
  });

  it('flattens option filters into ids and drops empty values', () => {
    renderTable({
      direction: 'INBOUND',
      receiptStatusCode: [{ id: 'SHIPPED' }],
      origin: { id: 'o1' },
      destination: { id: 'd1' },
      requestedBy: { id: 'u1' },
      createdBy: null,
      updatedBy: { id: 'u3' },
      shipmentType: [{ id: 'sea' }],
      createdBefore: '',
    });

    expect(tableArgs.getParams({
      offset: 0, state: { pageSize: 20 }, sortingParams: { sort: 'dateCreated', order: 'asc' },
    })).toEqual({
      direction: 'INBOUND',
      offset: '0',
      max: '20',
      receiptStatusCode: ['SHIPPED'],
      origin: 'o1',
      destination: 'd1',
      requestedBy: 'u1',
      updatedBy: 'u3',
      shipmentType: ['sea'],
      sort: 'dateCreated',
      order: 'asc',
    });
  });

  it('exports the movements and the incoming items with the current params', () => {
    const { result } = renderTable();

    result.current.exportStockMovements();
    result.current.exportAllIncomingItems();

    expect(exportFileFromAPI.mock.calls).toEqual([
      [{ url: '/api/stockMovements', params: tableData.currentParams }],
      [{ url: '/api/stockMovements/shippedItems', params: tableData.currentParams }],
    ]);
  });

  it('deletes a movement after confirmation and refreshes the list', async () => {
    apiClient.delete.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.deleteConfirmAlert('sm-1');
    const { buttons, title } = confirmAlert.mock.calls[0][0];
    expect(title).toBe('Are you sure?');
    expect(buttons.map(({ label }) => label)).toEqual(['Yes', 'No']);
    buttons[0].onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm-1');
    expect(Alert.success).toHaveBeenCalledWith('Stock Movement has been deleted successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('hides the spinner without refreshing when the delete fails', async () => {
    apiClient.delete.mockRejectedValue(new Error('locked'));
    const { result } = renderTable();

    result.current.deleteConfirmAlert('sm-2');
    const pending = confirmAlert.mock.calls[0][0].buttons[0].onClick();
    await expect(pending).rejects.toThrow('locked');

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });
});
