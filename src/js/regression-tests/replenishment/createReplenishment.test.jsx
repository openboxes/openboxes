import { fireEvent, screen, waitFor } from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// OBAF still imports flattenRequest from utils/apiClient (upstream develop no longer has it).
jest.mock('utils/apiClient', () => ({
  // eslint-disable-next-line global-require
  ...require('regression-tests/__mocks__/screenApiClient').default(),
  flattenRequest: (...args) => jest.requireActual('utils/apiClient').flattenRequest(...args),
}));
// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import ReplenishmentWizard from 'components/replenishment/ReplenishmentWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const PATH = '**/replenishment/create/:replenishmentId?';
const ENTRY = '/openboxes/replenishment/create';

const STATUS_OPTIONS = [
  { id: 'BELOW_MINIMUM', value: 'BELOW_MINIMUM', label: 'Below minimum' },
  { id: 'BELOW_MAXIMUM', value: 'BELOW_MAXIMUM', label: 'Below maximum' },
];

const REQUIREMENTS = [
  {
    product: { id: 'p1', productCode: 'P1', name: 'Paracetamol' },
    zone: 'Zone A',
    binLocation: { id: 'b1', name: 'BIN-1' },
    quantityInBin: 2,
    maxQuantity: 10,
    quantityAvailable: 50,
    quantityNeeded: 8,
  },
  {
    product: { id: 'p2', productCode: 'P2', name: 'Bandage' },
    binLocation: null,
    quantityInBin: 0,
    maxQuantity: 5,
    quantityAvailable: 3,
    quantityNeeded: 5,
  },
];

const renderCreate = (routes = []) => {
  answer(apiClient.get, [
    ['/api/replenishments/statusOptions', { data: STATUS_OPTIONS }],
    ['/api/requirements*', { data: REQUIREMENTS }],
    ...routes,
  ]);
  return renderAt({ component: ReplenishmentWizard, path: PATH, entry: ENTRY });
};

const field = (id) => document.querySelector(`[id="${id}"]`);
const errorOf = (id) => field(id).closest('[title]')?.getAttribute('title');
const nextButtons = () => screen.getAllByText('Next', { selector: 'button' });
const requirementCalls = () => apiClient.get.mock.calls.map(([url]) => url)
  .filter((url) => url.startsWith('/api/requirements'));

const pickStatus = (label) => {
  fireEvent.keyDown(screen.getByTestId('custom-select-element').querySelector('input'), { key: 'ArrowDown', keyCode: 40 });
  fireEvent.click(screen.getAllByText(label).find((el) => el.closest('[role="listitem"]')));
};

describe('replenishment, create step', () => {
  afterEach(() => jest.clearAllMocks());

  it('lists the bins below their minimum with every line selected', async () => {
    const { store } = renderCreate();

    expect(await screen.findByText('Paracetamol')).toBeInTheDocument();
    expect(requirementCalls()).toEqual(['/api/requirements?location.id=loc1']);
    expect(screen.getByText('Below minimum')).toBeInTheDocument();
    expect(screen.getByText('Zone A')).toBeInTheDocument();
    expect(screen.getByText('BIN-1')).toBeInTheDocument();
    // A requirement without a bin is for the default bin.
    expect(screen.getByText('DEFAULT')).toBeInTheDocument();
    expect(field('requirements[0].checked')).toBeChecked();
    expect(field('requirements[1].checked')).toBeChecked();
    expect(field('requirements[0].quantity')).toHaveValue(8);
    expect(store.getState().infoBarVisibility.STOCK_REPLENISHMENT_DESCRIPTION).toBe(true);

    // Bandage needs 5 but only 3 are available.
    expect(errorOf('requirements[1].quantity')).toBe('react.replenishment.error.quantity.greaterThanQATP.label');
    nextButtons().forEach((button) => expect(button).toBeDisabled());
  });

  it('creates the replenishment from the selected lines and opens it', async () => {
    const { currentPath, store } = renderCreate([
      ['/api/replenishments/r1', {
        data: {
          id: 'r1', replenishmentNumber: 'RP-1', status: 'PENDING', replenishmentItems: [],
        },
      }],
    ]);
    apiClient.post.mockResolvedValue({ data: { data: { id: 'r1', replenishmentNumber: 'RP-1', status: 'PENDING' } } });
    await screen.findByText('Bandage');

    fireEvent.click(field('requirements[1].checked'));
    await waitFor(() => expect(nextButtons()[0]).toBeEnabled());
    fireEvent.click(nextButtons()[0]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/replenishments/');
    expect(payload.replenishmentItems).toHaveLength(1);
    expect(payload.replenishmentItems[0]).toMatchObject({
      product: { id: 'p1' }, quantity: 8, checked: true,
    });

    await waitFor(() => expect(currentPath()).toBe('/openboxes/replenishment/create/r1'));
    expect(await screen.findByText('RP-1')).toBeInTheDocument();
    expect(screen.getByText('Sort by bins')).toBeInTheDocument();
    expect(store.getState().infoBarVisibility.STOCK_REPLENISHMENT_DESCRIPTION).toBe(false);
  });

  it('validates the quantity of selected lines only', async () => {
    renderCreate();
    await screen.findByText('Paracetamol');

    fireEvent.change(field('requirements[0].quantity'), { target: { value: '' } });
    await waitFor(() => expect(errorOf('requirements[0].quantity')).toBe('react.replenishment.error.emptyQtyToTransfer.label'));

    fireEvent.change(field('requirements[0].quantity'), { target: { value: '0' } });
    await waitFor(() => expect(errorOf('requirements[0].quantity')).toBe('react.replenishment.error.quantity.label'));

    // Unselected lines are not validated.
    fireEvent.click(field('requirements[1].checked'));
    await waitFor(() => expect(errorOf('requirements[1].quantity')).toBeFalsy());
  });

  it('requires at least one selected line', async () => {
    renderCreate();
    await screen.findByText('Paracetamol');
    const selectAll = document.querySelector('.table-form input[type="checkbox"]:not([id])');

    fireEvent.click(selectAll);
    await waitFor(() => expect(field('requirements[0].checked')).not.toBeChecked());
    expect(field('requirements[1].checked')).not.toBeChecked();
    expect(errorOf('requirements[0].checked')).toBe('react.replenishment.error.selected.label');
    nextButtons().forEach((button) => expect(button).toBeDisabled());

    fireEvent.click(selectAll);
    await waitFor(() => expect(field('requirements[0].checked')).toBeChecked());
    expect(field('requirements[1].checked')).toBeChecked();
  });

  it('reloads the requirements for another stock level', async () => {
    renderCreate();
    await screen.findByText('Paracetamol');

    pickStatus('Below maximum');

    await waitFor(() => expect(requirementCalls()).toEqual([
      '/api/requirements?location.id=loc1',
      '/api/requirements?location.id=loc1&inventoryLevelStatus=BELOW_MAXIMUM',
    ]));
  });

  it('asks before dropping edited quantities when the stock level changes', async () => {
    renderCreate();
    await screen.findByText('Paracetamol');

    fireEvent.change(field('requirements[0].quantity'), { target: { value: '6' } });
    fireEvent.blur(field('requirements[0].quantity'));

    pickStatus('Below maximum');
    expect(await screen.findByText('Confirm inventory level status change')).toBeInTheDocument();
    fireEvent.click(screen.getByText('No'));
    expect(requirementCalls()).toHaveLength(1);
    expect(field('requirements[0].quantity')).toHaveValue(6);

    pickStatus('Below maximum');
    fireEvent.click(await screen.findByText('Yes'));
    await waitFor(() => expect(requirementCalls()).toHaveLength(2));
    await waitFor(() => expect(field('requirements[0].quantity')).toHaveValue(8));
  });

  it('stays on the create step when the replenishment cannot be created', async () => {
    const { currentPath } = renderCreate();
    apiClient.post.mockRejectedValue(new Error('nope'));
    await screen.findByText('Bandage');

    fireEvent.click(field('requirements[1].checked'));
    await waitFor(() => expect(nextButtons()[1]).toBeEnabled());
    fireEvent.click(nextButtons()[1]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(currentPath()).toBe(ENTRY);
    expect(screen.getByText('Paracetamol')).toBeInTheDocument();
  });

  it('shows no lines when the requirements cannot be loaded', async () => {
    answer(apiClient.get, [
      ['/api/replenishments/statusOptions', new Error('down')],
      ['/api/requirements*', new Error('down')],
    ]);
    renderAt({ component: ReplenishmentWizard, path: PATH, entry: ENTRY });

    await waitFor(() => expect(requirementCalls()).toHaveLength(1));
    expect(screen.queryByText('Paracetamol')).not.toBeInTheDocument();
    expect(screen.getByText('Create Replenishment')).toBeInTheDocument();
  });
});
