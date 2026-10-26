import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import Alert from 'react-s-alert';
import { answer, renderAt, sessionStore } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import StockTransferList from 'components/stock-transfer/list/StockTransferList';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const STATUSES = [
  {
    id: 'PENDING', value: 'PENDING', label: 'Pending', variant: 'primary',
  },
  {
    id: 'COMPLETED', value: 'COMPLETED', label: 'Completed', variant: 'success',
  },
];

const TRANSFERS = [
  {
    id: 'st1', orderNumber: 'ST-0001', status: 'PENDING', orderItemsCount: 3, createdBy: 'Admin', dateCreated: '2026-09-01T10:00:00Z',
  },
  {
    id: 'st2', orderNumber: 'ST-0002', status: 'COMPLETED', orderItemsCount: 1, createdBy: 'Jane', dateCreated: '2026-09-02T10:00:00Z',
  },
];

const renderList = ({ entry = '/openboxes/stockTransfer/list', store } = {}) => renderAt({
  component: StockTransferList,
  path: '**/stockTransfer/list',
  entry,
  store,
});

const listCalls = () => apiClient.get.mock.calls.filter(([url]) => url === '/api/stockTransfers');
const lastListParams = () => listCalls()[listCalls().length - 1][1].params;
const rowOf = (text) => screen.getByText(text).closest('.rt-tr');

describe('stock transfer list', () => {
  beforeEach(() => {
    answer(apiClient.get, [
      ['/api/stockTransfers/statusOptions', { data: STATUSES }],
      ['/api/stockTransfers', { data: TRANSFERS, totalCount: 2 }],
    ]);
    answer(apiClient, [['/api/generic/person/u2', { data: { id: 'u2', name: 'Jane' } }]]);
    apiClient.delete.mockResolvedValue({ status: 204, data: {} });
    jest.spyOn(Alert, 'success').mockImplementation(() => null);
  });

  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  it('lists the transfers of the current location, newest first', async () => {
    renderList();

    expect(await screen.findByText('ST-0001')).toBeInTheDocument();
    expect(lastListParams()).toEqual({
      location: 'loc1', offset: '0', max: '10', sort: 'dateCreated', order: 'desc',
    });
    expect(within(rowOf('ST-0001')).getByText('3')).toBeInTheDocument();
    expect(within(rowOf('ST-0002')).getByText('Jane')).toBeInTheDocument();
    expect(await within(rowOf('ST-0002')).findByText('Completed')).toBeInTheDocument();
    expect(screen.getByText('ST-0001').closest('a')).toHaveAttribute('href', '/openboxes/stockTransfer/show/st1');
    expect(screen.getAllByText('List Stock Transfers').length).toBeGreaterThan(0);
    expect(screen.getByText('Create Stock Transfer').closest('a')).toHaveAttribute('href', '/openboxes/stockTransfer/create');
  });

  it('hides the create button from users who are not managers', async () => {
    renderList({ store: sessionStore({ session: { isUserManager: false } }) });

    await screen.findByText('ST-0001');
    expect(screen.queryByText('Create Stock Transfer')).not.toBeInTheDocument();
  });

  it('searches by transfer number', async () => {
    renderList();
    await screen.findByText('ST-0001');

    const search = screen.getByPlaceholderText('Search by transfer number');
    fireEvent.change(search, { target: { value: 'ST-0002' } });
    fireEvent.click(screen.getByText('Search').closest('button'));

    await waitFor(() => expect(lastListParams().q).toBe('ST-0002'));
  });

  it('starts from the filters in the address', async () => {
    const store = sessionStore();
    store.dispatch({ type: 'FETCH_STOCK_TRANSFER_STATUSES', payload: STATUSES });
    renderList({
      entry: '/openboxes/stockTransfer/list?status=PENDING&createdBy=u2&lastUpdatedStartDate=09/01/2026&lastUpdatedEndDate=09/30/2026',
      store,
    });

    await waitFor(() => expect(lastListParams()).toEqual(expect.objectContaining({
      status: ['PENDING'],
      createdBy: 'u2',
      lastUpdatedStartDate: '09/01/2026',
      lastUpdatedEndDate: '09/30/2026',
    })));
    expect(apiClient).toHaveBeenCalledWith('/api/generic/person/u2');
  });

  it('takes the signed-in user from the session when the address filters on them', async () => {
    renderList({ entry: '/openboxes/stockTransfer/list?createdBy=u1' });

    await waitFor(() => expect(lastListParams().createdBy).toBe('u1'));
    expect(apiClient).not.toHaveBeenCalled();
  });

  it('deletes a pending transfer after confirmation and reloads the list', async () => {
    renderList();
    await screen.findByText('ST-0001');
    const callsBefore = listCalls().length;

    fireEvent.click(rowOf('ST-0001').querySelector('.action-dots'));
    fireEvent.click(await screen.findByText('Delete'));
    expect(await screen.findByText('Are you sure you want to delete this stock transfer?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Yes'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransfers/st1'));
    await waitFor(() => expect(listCalls().length).toBeGreaterThan(callsBefore));
    expect(Alert.success).toHaveBeenCalledWith('Stock transfer has been deleted successfully');
  });

  it('offers only viewing for a completed transfer', async () => {
    renderList();
    await screen.findByText('ST-0002');

    fireEvent.click(rowOf('ST-0002').querySelector('.action-dots'));

    const view = await screen.findByText('View stock transfer');
    expect(view.closest('a')).toHaveAttribute('href', '/openboxes/stockTransfer/show/st2');
    expect(screen.queryByText('Delete')).not.toBeInTheDocument();
  });
});
