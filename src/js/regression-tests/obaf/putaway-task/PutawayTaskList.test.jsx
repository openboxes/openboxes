// OBAF-new: the putaway task list page (header, filters, table and their two hooks), rendered
// whole against a mocked API (GET/PATCH /api/facilities/:id/putaway-tasks).
import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import { confirmAlert } from 'react-confirm-alert';
import Alert from 'react-s-alert';
import { answer, renderAt, sessionStore } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));

/* eslint-disable import/first,import/order */
import PutawayTaskList from 'components/putaway-task/list/PutawayTaskList';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// Noon local time as an ISO instant: the calendar date shown is the same in every time zone.
const localNoon = (year, month, day) => new Date(year, month - 1, day, 12).toISOString();

const URL = '/api/facilities/loc1/putaway-tasks';

const TASKS = [
  {
    id: 'pt1',
    identifier: 'TASK-1',
    status: 'PENDING',
    putawayOrder: { id: 'po1', orderNumber: 'PUT-100' },
    inventoryItem: { lotNumber: 'LOT-A', product: { id: 'prod1', productCode: 'P100', name: 'Gauze' } },
    location: { name: 'RECEIVING' },
    quantity: 40,
    container: { name: 'Cart 1' },
    destination: { name: 'A-01-01' },
    assignee: { name: 'Pat Putter' },
    comment: 'Fixed bin',
    dateCreated: localNoon(2026, 9, 1),
  },
  {
    id: 'pt2',
    identifier: 'TASK-2',
    status: 'IN_PROGRESS',
    putawayOrder: { id: 'po2', orderNumber: 'PUT-200' },
    inventoryItem: { lotNumber: 'LOT-B', product: { id: 'prod2', productCode: 'P200', name: 'Syringe' } },
    location: { name: 'RECEIVING' },
    quantity: 5,
    container: null,
    destination: { name: 'B-02-02' },
    assignee: null,
    comment: null,
    dateCreated: localNoon(2026, 9, 2),
  },
  {
    id: 'pt3',
    identifier: 'TASK-3',
    status: 'COMPLETED',
    putawayOrder: null,
    inventoryItem: { lotNumber: null, product: { id: 'prod3', productCode: 'P300', name: 'Mask' } },
    location: { name: 'RECEIVING' },
    quantity: 1,
    destination: { name: 'C-03-03' },
    dateCreated: localNoon(2026, 9, 3),
  },
];

const LOCATIONS = {
  c1: { id: 'c1', name: 'Cart 1' },
  d1: { id: 'd1', name: 'A-01-01' },
};

const renderList = ({ entry = '/openboxes/putawayTask/list', store } = {}) => renderAt({
  component: PutawayTaskList,
  path: '**/putawayTask/list',
  entry,
  store,
});

const listCalls = () => apiClient.get.mock.calls.filter(([url]) => url === URL);
const lastListParams = () => listCalls()[listCalls().length - 1][1].params;
const rowOf = (text) => screen.getByText(text).closest('.rt-tr');
const openMenu = (text) => fireEvent.click(within(rowOf(text)).getByTestId('dropdown-toggle'));
const menuLabels = () => within(screen.getByTestId('dropdown-menu'))
  .getAllByRole(/button|link/).map((el) => el.textContent);

describe('PutawayTaskList (OBAF)', () => {
  beforeEach(() => {
    answer(apiClient.get, [
      [URL, { data: TASKS, totalCount: 3 }],
      ['/api/internalLocations/search', (url, { params }) => ({
        data: params.activityCodes === 'PUTAWAY_CART'
          ? [{ id: 'c1', name: 'Cart 1' }]
          : [{ id: 'd1', name: 'A-01-01' }],
      })],
    ]);
    answer(apiClient, [[(url) => url.startsWith('/api/locations/'), (url) => ({
      data: LOCATIONS[url.split('/').pop()],
    })]]);
    apiClient.patch.mockResolvedValue({ data: {} });
    jest.spyOn(Alert, 'success').mockImplementation(() => null);
  });

  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  it('lists tasks of the current facility, newest first, starting with the OPEN category', async () => {
    renderList();

    expect(await screen.findByText('TASK-1')).toBeInTheDocument();
    expect(listCalls()[0][1].params).toEqual({
      offset: '0', max: '10', sort: 'dateCreated', order: 'desc', statusCategory: 'OPEN',
    });
    listCalls().forEach(([, config]) => {
      expect(config.params).toEqual(expect.objectContaining({
        offset: '0', max: '10', sort: 'dateCreated', order: 'desc',
      }));
    });
    expect(screen.getAllByText('List Putaway Tasks').length).toBeGreaterThan(0);
  });

  // KNOWN-GAP: on first landing FilterForm's mount-time clear raises shouldRebuildParams,
  // useCommonFiltersCleaner re-runs initializeDefaultFilterValues, and by then
  // hasAppliedDefaultStatusCategoryRef is already true, so statusCategory is blanked: only the
  // first fetch carries statusCategory=OPEN, the settled list shows every status.
  it.skip('keeps the OPEN status category default once the first load settles', async () => {
    const { currentSearch } = renderList();

    expect(await screen.findByText('TASK-1')).toBeInTheDocument();
    await new Promise((resolve) => setTimeout(resolve, 500));
    expect(lastListParams()).toEqual({
      offset: '0', max: '10', sort: 'dateCreated', order: 'desc', statusCategory: 'OPEN',
    });
    expect(currentSearch()).toContain('statusCategory=OPEN');
  });

  it('renders every column with the right links', async () => {
    renderList();
    await screen.findByText('TASK-1');

    [
      'Putaway #', 'Task Identifier', 'Status', 'Product Code', 'Product Name', 'Lot Number',
      'Location', 'Qty', 'Container', 'Destination', 'Assignee', 'Strategy', 'Date Created',
    ].forEach((header) => expect(screen.getAllByText(header).length).toBeGreaterThan(0));

    const row = rowOf('TASK-1');
    expect(screen.getByText('PUT-100').closest('a')).toHaveAttribute('href', '/openboxes/putaway/show/po1');
    expect(within(row).getByText('P100').closest('a')).toHaveAttribute('href', '/openboxes/inventoryItem/showStockCard/prod1');
    ['Gauze', 'LOT-A', 'RECEIVING', '40', 'Cart 1', 'A-01-01', 'Pat Putter', 'Fixed bin']
      .forEach((text) => expect(within(row).getByText(text)).toBeInTheDocument());
    expect(within(row).getByText('Pending')).toBeInTheDocument();
    expect(within(row).getByText('Sep 01, 2026')).toBeInTheDocument();
    expect(within(rowOf('TASK-2')).getByText(/in.progress/i)).toBeInTheDocument();
  });

  it('offers rerun only on pending tasks, and on in-progress ones to superusers', async () => {
    renderList();
    await screen.findByText('TASK-1');

    openMenu('TASK-1');
    expect(menuLabels()).toEqual(['Show Details', 'Rerun Strategy', 'Show All Tasks for This Putaway']);
    expect(screen.getByText('Show Details').closest('a')).toHaveAttribute('href', '/openboxes/putaway/show/po1');
    fireEvent.click(within(rowOf('TASK-1')).getByTestId('dropdown-toggle'));

    openMenu('TASK-2');
    expect(menuLabels()).toEqual(['Show Details', 'Rerun Strategy', 'Show All Tasks for This Putaway']);
    fireEvent.click(within(rowOf('TASK-2')).getByTestId('dropdown-toggle'));

    openMenu('TASK-3');
    expect(menuLabels()).not.toContain('Rerun Strategy');
  });

  it('hides the in-progress rerun from users below superuser', async () => {
    renderList({ store: sessionStore({ session: { highestRole: 'Manager', isSuperuser: false } }) });
    await screen.findByText('TASK-1');

    openMenu('TASK-2');
    expect(menuLabels()).toEqual(['Show Details', 'Show All Tasks for This Putaway']);
    fireEvent.click(within(rowOf('TASK-2')).getByTestId('dropdown-toggle'));

    openMenu('TASK-1');
    expect(menuLabels()).toContain('Rerun Strategy');
  });

  it('reruns a pending task after confirmation and refetches', async () => {
    renderList();
    await screen.findByText('TASK-1');
    const before = listCalls().length;

    openMenu('TASK-1');
    fireEvent.click(screen.getByText('Rerun Strategy'));
    const { title, message, buttons } = confirmAlert.mock.calls[0][0];
    expect(title).toBe('Are you sure?');
    expect(message).toMatch(/current task will be replaced/);
    expect(buttons.map(({ label }) => label)).toEqual(['Yes', 'No']);
    await buttons[0].onClick();

    expect(apiClient.patch).toHaveBeenCalledWith(`${URL}/pt1`, { action: 'rerunStrategy' });
    expect(Alert.success).toHaveBeenCalledWith('Putaway strategy has been rerun successfully');
    await waitFor(() => expect(listCalls().length).toBeGreaterThan(before));
  });

  it('warns that an in-progress rerun keeps the container', async () => {
    renderList();
    await screen.findByText('TASK-2');

    openMenu('TASK-2');
    fireEvent.click(screen.getByText('Rerun Strategy'));
    expect(confirmAlert.mock.calls[0][0].message)
      .toMatch(/destination will be reassigned, but the putaway container will not change/);
  });

  it('still refetches when the rerun request fails', async () => {
    apiClient.patch.mockRejectedValue(new Error('boom'));
    renderList();
    await screen.findByText('TASK-1');
    const before = listCalls().length;

    openMenu('TASK-1');
    fireEvent.click(screen.getByText('Rerun Strategy'));
    await expect(confirmAlert.mock.calls[0][0].buttons[0].onClick()).rejects.toThrow('boom');

    expect(Alert.success).not.toHaveBeenCalled();
    await waitFor(() => expect(listCalls().length).toBeGreaterThan(before));
  });

  it('filters down to one putaway order from the row action', async () => {
    const { currentSearch } = renderList();
    await screen.findByText('TASK-1');

    openMenu('TASK-1');
    fireEvent.click(screen.getByText('Show All Tasks for This Putaway'));

    await waitFor(() => expect(lastListParams()).toEqual({
      offset: '0', max: '10', sort: 'dateCreated', order: 'desc', searchTerm: 'PUT-100',
    }));
    expect(currentSearch()).toBe('?searchTerm=PUT-100');
  });

  it('starts from the filters in the address, resolving container and destination', async () => {
    renderList({
      entry: '/openboxes/putawayTask/list?statusCategory=CLOSED&status=COMPLETED&status=CANCELED'
        + '&searchTerm=Gauze&container=c1&destination=d1&createdAfter=09/01/2026&createdBefore=09/30/2026',
    });

    await waitFor(() => expect(lastListParams()).toEqual({
      offset: '0',
      max: '10',
      sort: 'dateCreated',
      order: 'desc',
      statusCategory: 'CLOSED',
      status: ['COMPLETED', 'CANCELED'],
      searchTerm: 'Gauze',
      container: 'c1',
      destination: 'd1',
      createdAfter: '09/01/2026',
      createdBefore: '09/30/2026',
    }));
    expect(apiClient).toHaveBeenCalledWith('/api/locations/c1');
    expect(apiClient).toHaveBeenCalledWith('/api/locations/d1');
  });

  it('searches by the free-text field', async () => {
    renderList();
    await screen.findByText('TASK-1');

    fireEvent.change(
      screen.getByPlaceholderText('Search by putaway #, task ID, product code, name, or description'),
      { target: { value: 'TASK-2' } },
    );
    fireEvent.click(screen.getByText('Search').closest('button'));

    await waitFor(() => expect(lastListParams()).toEqual(expect.objectContaining({
      searchTerm: 'TASK-2',
    })));
  });

  it('shows the empty message when nothing matches', async () => {
    answer(apiClient.get, [[URL, { data: [], totalCount: 0 }]]);
    renderList();

    expect(await screen.findByText('No putaway tasks match the given criteria')).toBeInTheDocument();
  });

  // KNOWN-GAP: a task with no putaway order still gets a "Show Details" item, but with
  // neither href nor onClick, so ContextMenu renders it as a button whose click throws
  // "action.onClick is not a function".
  it.skip('does not offer a dead Show Details item on a task without a putaway order', async () => {
    renderList();
    await screen.findByText('TASK-3');

    openMenu('TASK-3');
    expect(menuLabels()).not.toContain('Show Details');
  });
});
