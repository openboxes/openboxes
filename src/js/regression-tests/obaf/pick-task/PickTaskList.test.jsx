// OBAF-new: the "Ready to be Staged" pick task list (GET
// /api/facilities/:id/pick-tasks?status=PICKED).
import { screen, waitFor, within } from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import PickTaskList from 'components/pick-task/list/PickTaskList';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const URL = '/api/facilities/loc1/pick-tasks';

const TASKS = [
  {
    id: 'pt1',
    requisitionId: 'req1',
    requisitionNumber: 'REQ-001',
    product: { name: 'Gauze 10cm' },
    quantityPicked: 12,
    hasStockIssue: true,
    outboundContainer: { name: 'Tote 7' },
    location: { name: 'A-01' },
    pickedBy: { name: 'Jane Picker' },
  },
  {
    id: 'pt2',
    requisitionId: null,
    requisitionNumber: 'REQ-002',
    product: { name: 'Syringe 5ml' },
    quantityPicked: 3,
    hasStockIssue: false,
    outboundContainer: null,
    location: { name: 'B-02' },
    pickedBy: { name: 'Joe Picker' },
  },
];

const renderList = () => renderAt({
  component: PickTaskList,
  path: '**/pickTask/list',
  entry: '/openboxes/pickTask/list',
});

const listCalls = () => apiClient.get.mock.calls.filter(([url]) => url === URL);
const rowOf = (text) => screen.getByText(text).closest('.rt-tr');

describe('PickTaskList (OBAF)', () => {
  afterEach(() => jest.clearAllMocks());

  it('fetches only PICKED tasks of the current facility with paging params', async () => {
    answer(apiClient.get, [[URL, { data: TASKS, totalCount: 2 }]]);
    renderList();

    expect(await screen.findByText('REQ-001')).toBeInTheDocument();
    // The table's first onFetchData plus useTableData's mount fetch; the stable FILTER_PARAMS
    // must not make it loop.
    await new Promise((resolve) => setTimeout(resolve, 200));
    expect(listCalls().length).toBeGreaterThan(0);
    expect(listCalls().length).toBeLessThanOrEqual(2);
    listCalls().forEach(([, config]) => {
      expect(config.params).toEqual({ offset: '0', max: '10', status: 'PICKED' });
    });
  });

  it('renders the columns: order link, product, qty, container-or-location, picker', async () => {
    answer(apiClient.get, [[URL, { data: TASKS, totalCount: 2 }]]);
    renderList();

    await screen.findByText('REQ-001');
    expect(screen.getByText('Ready to be Staged')).toBeInTheDocument();
    ['Order', 'Product', 'Qty Picked', 'Location', 'Picker'].forEach((header) => {
      expect(screen.getAllByText(header).length).toBeGreaterThan(0);
    });

    const first = rowOf('REQ-001');
    expect(screen.getByText('REQ-001').closest('a')).toHaveAttribute('href', '/openboxes/stockMovement/show/req1');
    expect(within(first).getByText('Gauze 10cm')).toBeInTheDocument();
    expect(within(first).getByText('12')).toBeInTheDocument();
    // The outbound container wins over the bin location.
    expect(within(first).getByText('Tote 7')).toBeInTheDocument();
    expect(within(first).queryByText('A-01')).not.toBeInTheDocument();
    expect(within(first).getByText('Jane Picker')).toBeInTheDocument();

    const second = rowOf('REQ-002');
    // No requisition id: no link.
    expect(screen.getByText('REQ-002').closest('a')).toBeNull();
    expect(within(second).getByText('B-02')).toBeInTheDocument();
  });

  it('flags only tasks with a stock issue', async () => {
    answer(apiClient.get, [[URL, { data: TASKS, totalCount: 2 }]]);
    renderList();

    await screen.findByText('REQ-001');
    const icons = screen.getAllByTestId('pick-task-stock-issue-icon');
    expect(icons).toHaveLength(1);
    expect(within(rowOf('REQ-001')).getByTestId('pick-task-stock-issue-icon')).toBeInTheDocument();
    expect(within(rowOf('REQ-002')).queryByTestId('pick-task-stock-issue-icon')).toBeNull();
  });

  it('shows the empty-state message when there is no staging work', async () => {
    answer(apiClient.get, [[URL, { data: [], totalCount: 0 }]]);
    renderList();

    await waitFor(() => expect(listCalls().length).toBeGreaterThan(0));
    expect(await screen.findByText('There is no outstanding staging work')).toBeInTheDocument();
  });
});
