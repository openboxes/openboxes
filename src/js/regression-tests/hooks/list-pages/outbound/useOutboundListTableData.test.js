import { renderHook } from '@testing-library/react-hooks';
import { confirmAlert } from 'react-confirm-alert';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useOutboundListTableData from 'hooks/list-pages/outbound/useOutboundListTableData';
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

describe('useOutboundListTableData', () => {
  let dispatch;
  let fireFetchData;
  let tableArgs;
  const tableData = { data: [], currentParams: { direction: 'OUTBOUND', max: '10' } };

  const renderTable = (filterParams = {}) => renderHook(
    () => useOutboundListTableData(filterParams),
  );
  const spinnerActions = () => dispatch.mock.calls.map(([action]) => action.type);

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
        loading: true,
        tableData,
        onFetchHandler: jest.fn(),
      };
    });
  });

  it('configures the table for the stock movement api', () => {
    const { result } = renderTable({ direction: 'OUTBOUND' });

    expect(tableArgs).toMatchObject({
      url: '/api/stockMovements',
      errorMessageId: 'react.stockMovement.outbound.fetching.error',
      defaultErrorMessage: 'Unable to fetch outbound movements',
    });
    expect(result.current.loading).toBe(true);
  });

  it('flattens option filters into ids and drops empty values', () => {
    renderTable({
      direction: 'OUTBOUND',
      requisitionStatusCode: [{ id: 'PENDING' }, { id: 'PICKING' }],
      requestType: { value: 'STOCK' },
      origin: { id: 'o1' },
      destination: { id: 'd1' },
      requestedBy: { id: 'u1' },
      createdBy: { id: 'u2' },
      updatedBy: { id: 'u3' },
      shipmentType: [{ id: 'air' }],
      approver: [{ id: 'a1' }, { id: 'null' }],
      createdAfter: '',
    });

    expect(tableArgs.getParams({
      offset: 10, state: { pageSize: 10 }, sortingParams: { sort: 'name', order: 'desc' },
    })).toEqual({
      direction: 'OUTBOUND',
      offset: '10',
      max: '10',
      requisitionStatusCode: ['PENDING', 'PICKING'],
      requestType: 'STOCK',
      origin: 'o1',
      destination: 'd1',
      requestedBy: 'u1',
      createdBy: 'u2',
      updatedBy: 'u3',
      shipmentType: ['air'],
      approver: ['a1', 'null'],
      sort: 'name',
      order: 'desc',
    });
  });

  it('drops empty multi selects', () => {
    renderTable({ requisitionStatusCode: [], shipmentType: [], approver: [] });

    expect(tableArgs.getParams({ offset: 0, state: { pageSize: 25 } }))
      .toEqual({ offset: '0', max: '25' });
  });

  it('exports the movements and the pending shipment items with the current params', () => {
    const { result } = renderTable();

    result.current.exportStockMovements();
    result.current.exportPendingShipmentItems();

    expect(exportFileFromAPI.mock.calls).toEqual([
      [{ url: '/api/stockMovements', params: tableData.currentParams }],
      [{ url: '/api/stockMovements/pendingRequisitionItems', params: tableData.currentParams }],
    ]);
  });

  it('deletes a movement after confirmation', async () => {
    apiClient.delete.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.deleteConfirmAlert('sm-1');
    const { title, message, buttons } = confirmAlert.mock.calls[0][0];
    expect(title).toBe('Are you sure?');
    expect(message).toBe('Are you sure you want to delete this Stock Movement?');
    expect(buttons[1]).toEqual({ label: 'No' });
    buttons[0].onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm-1');
    expect(Alert.success).toHaveBeenCalledWith('Stock Movement has been deleted successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('does not refresh when the delete returns another status', async () => {
    apiClient.delete.mockResolvedValue({ status: 200 });
    const { result } = renderTable();

    result.current.deleteConfirmAlert('sm-1');
    confirmAlert.mock.calls[0][0].buttons[0].onClick();
    await flushPromises();

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('approves a request', async () => {
    apiClient.post.mockResolvedValue({});
    const { result } = renderTable();

    await result.current.approveRequest('sm-2', 'REQ-2');

    expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-2/status', { status: 'APPROVED' });
    expect(Alert.success).toHaveBeenCalledWith(
      'You have successfully Approved the request REQ-2',
      expect.anything(),
    );
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('rejects a request with sender, recipient and comment', async () => {
    apiClient.post.mockResolvedValue({});
    const { result } = renderTable();

    await result.current.rejectRequest('sm-3', 'REQ-3')({
      sender: { id: 'u1' }, recipient: { id: 'u2' }, comment: 'Not needed',
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-3/status', {
      status: 'REJECTED', sender: 'u1', recipient: 'u2', comment: 'Not needed',
    });
    expect(Alert.success).toHaveBeenCalledWith(
      'You have successfully Rejected the request REQ-3',
      expect.anything(),
    );
    expect(fireFetchData).toHaveBeenCalledTimes(1);
  });

  it('rolls back an approval', async () => {
    apiClient.put.mockResolvedValue({});
    const { result } = renderTable();

    await result.current.rollbackRequest('sm-4');

    expect(apiClient.put).toHaveBeenCalledWith('/api/stockMovements/sm-4/rollbackApproval');
    expect(Alert.success).toHaveBeenCalledWith('Successfully rolled back approval', expect.anything());
    expect(fireFetchData).toHaveBeenCalledTimes(1);
  });

  it('hides the spinner without refreshing when an approval fails', async () => {
    apiClient.post.mockRejectedValue(new Error('forbidden'));
    const { result } = renderTable();

    await expect(result.current.approveRequest('sm-5', 'REQ-5')).rejects.toThrow('forbidden');

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });
});
