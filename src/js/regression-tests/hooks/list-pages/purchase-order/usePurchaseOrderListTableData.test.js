import { act, renderHook } from '@testing-library/react-hooks';
import { confirmAlert } from 'react-confirm-alert';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import { ORDER_URL } from 'consts/applicationUrls';
import usePurchaseOrderListTableData from 'hooks/list-pages/purchase-order/usePurchaseOrderListTableData';
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

// OBAF opens the print page with window.open directly; the spy reports it to followLink.
installNavigationSpy();

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('usePurchaseOrderListTableData', () => {
  let dispatch;
  let fireFetchData;
  let tableArgs;
  let session;
  const tableData = {
    data: [
      { id: 'po-1', status: 'pending', shipmentsCount: 0 },
      { id: 'po-2', status: 'PLACED', shipmentsCount: 2 },
      { id: 'po-3', status: 'PLACED', shipmentsCount: 0 },
    ],
    currentParams: { offset: '0', max: '10', status: ['PLACED'] },
  };

  const renderTable = (filterParams = {}) => renderHook(
    () => usePurchaseOrderListTableData(filterParams),
  );
  const spinnerActions = () => dispatch.mock.calls.map(([action]) => action.type);

  beforeEach(() => {
    jest.clearAllMocks();
    dispatch = jest.fn();
    fireFetchData = jest.fn();
    session = { isUserApprover: true };
    useDispatch.mockReturnValue(dispatch);
    useSelector.mockImplementation((selector) => selector({
      session,
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

  it('sorts by order date and exposes the total price of the last fetch', () => {
    const { result } = renderTable();
    expect(tableArgs).toMatchObject({
      url: '/api/purchaseOrders',
      defaultSorting: { sort: 'dateOrdered', order: 'desc' },
      errorMessageId: 'react.purchaseOrder.error.purchaseOrderList.label',
    });
    expect(result.current.tableData.totalPrice).toBe(0);

    act(() => tableArgs.onFetchedData({ totalPrice: 1234.5 }));

    expect(result.current.tableData).toEqual({ ...tableData, totalPrice: 1234.5 });
  });

  it('maps the filters to request params', () => {
    renderTable({
      status: [{ value: 'PLACED' }, { value: 'PENDING' }],
      origin: { id: 'o1' },
      destination: { id: 'd1' },
      orderedBy: { id: 'u1' },
      createdBy: { id: 'u2' },
      destinationParty: { id: 'org-1' },
      paymentTerm: [{ id: 'term-1' }, { id: 'null' }],
      statusStartDate: '2026-01-01',
      statusEndDate: '',
    });

    expect(tableArgs.getParams({
      offset: 20, state: { pageSize: 10 }, sortingParams: { sort: 'dateOrdered', order: 'desc' },
    })).toEqual({
      offset: '20',
      max: '10',
      sort: 'dateOrdered',
      order: 'desc',
      status: ['PLACED', 'PENDING'],
      origin: 'o1',
      destination: 'd1',
      orderedBy: 'u1',
      createdBy: 'u2',
      destinationParty: 'org-1',
      paymentTerm: ['term-1', 'null'],
      statusStartDate: '2026-01-01',
    });
  });

  it('keeps an unset destination as undefined and drops other empty filters', () => {
    renderTable({ status: [], paymentTerm: [] });

    expect(tableArgs.getParams({ offset: 0, state: { pageSize: 5 }, sortingParams: {} }))
      .toEqual({ offset: '0', max: '5', destination: undefined });
  });

  it('downloads orders or order lines without paging params', () => {
    const { result } = renderTable();

    result.current.downloadOrders(false);
    result.current.downloadOrders(true);

    expect(exportFileFromAPI.mock.calls).toEqual([
      [{ url: '/api/purchaseOrders', filename: 'Orders', params: { status: ['PLACED'], orderItems: false } }],
      [{ url: '/api/purchaseOrders', filename: 'OrdersLineDetails', params: { status: ['PLACED'], orderItems: true } }],
    ]);
  });

  it('deletes after confirmation and refreshes', async () => {
    apiClient.delete.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.deleteHandler('po-1');
    const { title, buttons } = confirmAlert.mock.calls[0][0];
    expect(title).toBe('Are you sure?');
    expect(buttons[1]).toEqual({ label: 'No' });
    buttons[0].onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/purchaseOrders/po-1');
    expect(Alert.success).toHaveBeenCalledWith('Purchase order has been deleted successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('refreshes after a delete that did not succeed', async () => {
    apiClient.delete.mockResolvedValue({ status: 500 });
    const { result } = renderTable();

    result.current.deleteHandler('po-1');
    confirmAlert.mock.calls[0][0].buttons[0].onClick();
    await flushPromises();

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).toHaveBeenCalledTimes(1);
  });

  it('refuses a rollback for users who are not approvers', () => {
    session.isUserApprover = false;
    const { result } = renderTable();

    result.current.rollbackHandler('po-3');

    expect(Alert.error).toHaveBeenCalledWith('You do not have permissions to perform this action');
    expect(confirmAlert).not.toHaveBeenCalled();
  });

  it('refuses a rollback of an order with shipments', () => {
    const { result } = renderTable();

    result.current.rollbackHandler('po-2');

    expect(Alert.error).toHaveBeenCalledWith('Cannot rollback order with associated shipments');
    expect(confirmAlert).not.toHaveBeenCalled();
  });

  it('rolls back after confirmation and refreshes', async () => {
    apiClient.post.mockResolvedValue({ status: 200 });
    const { result } = renderTable();

    result.current.rollbackHandler('po-3');
    const { message, buttons } = confirmAlert.mock.calls[0][0];
    expect(message).toBe('Are you sure you want to rollback this order?');
    buttons[0].onClick();
    await flushPromises();

    expect(apiClient.post).toHaveBeenCalledWith('/api/purchaseOrders/po-3/rollback');
    expect(Alert.success).toHaveBeenCalledWith('Rollback of order status has been done successfully');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(spinnerActions()).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('does not refresh after a rollback with an unexpected status', async () => {
    apiClient.post.mockResolvedValue({ status: 204 });
    const { result } = renderTable();

    result.current.rollbackHandler('unknown-order');
    confirmAlert.mock.calls[0][0].buttons[0].onClick();
    await flushPromises();

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();
  });

  it('prints placed orders in a new tab but not pending ones', () => {
    const { result } = renderTable();

    result.current.printOrder('po-1');
    expect(Alert.error).toHaveBeenCalledWith('Order must be placed in order to print');
    expect(followLink).not.toHaveBeenCalled();

    result.current.printOrder('po-3');
    expect(followLink).toHaveBeenCalledWith(ORDER_URL.print('po-3'), { newTab: true });
  });

  it('reports cancelling as not supported', () => {
    const { result } = renderTable();

    result.current.cancelOrder();

    expect(Alert.error).toHaveBeenCalledWith('This feature is not currently supported');
  });
});
