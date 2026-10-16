import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import _ from 'lodash';
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
const ENTRY = '/openboxes/replenishment/create/r1';

const pick = (id, bin, zone, quantity, extra = {}) => ({
  id,
  inventoryItem: { id: `ii-${id}` },
  binLocation: { id: bin, name: bin.toUpperCase() },
  zone: { name: zone },
  lotNumber: `LOT-${id}`,
  expirationDate: '01/15/2030',
  quantity,
  ...extra,
});

const ITEMS = [
  {
    id: 'ri1',
    product: { productCode: 'P1', name: 'Bandage' },
    currentBinLocation: { name: 'BIN-1' },
    quantity: 5,
    picklistItems: [pick('a', 'store-2', 'Zone B', 5)],
    // The autopick suggested another bin, so it shows crossed out above the user's pick.
    suggestedItems: [{
      inventoryItem: { id: 'ii-a' }, binLocation: { id: 'store-1', name: 'STORE-1' }, zone: { name: 'Zone A' }, quantityPicked: 5,
    }],
  },
  {
    id: 'ri2',
    product: { productCode: 'P2', name: 'Paracetamol' },
    currentBinLocation: null,
    quantity: 8,
    picklistItems: [pick('b', 'shelf-1', 'Aisle', 8)],
    suggestedItems: [{ inventoryItem: { id: 'ii-b' }, binLocation: { id: 'shelf-1' }, quantityPicked: 8 }],
  },
  {
    id: 'ri3',
    product: { productCode: 'P3', name: 'Gloves' },
    currentBinLocation: { name: 'BIN-3' },
    quantity: 2,
    picklistItems: [],
  },
];

let status;
const replenishment = () => ({
  id: 'r1', replenishmentNumber: 'RP-1', status, replenishmentItems: ITEMS,
});

const renderPicks = (routes = []) => {
  answer(apiClient.get, [
    ['/api/replenishments/r1', () => ({ data: replenishment() })],
    ...routes,
  ]);
  apiClient.post.mockImplementation((url, body) => {
    if (url === '/api/replenishments/r1' && body.status) {
      status = body.status;
    }
    return Promise.resolve({ data: {} });
  });
  return renderAt({ component: ReplenishmentWizard, path: PATH, entry: ENTRY });
};

// Row order as read in the table (the value tooltips keep copies of the names elsewhere).
const productOrder = () => {
  const text = document.querySelector('form').textContent;
  return _.sortBy(['Bandage', 'Paracetamol', 'Gloves'], (name) => text.indexOf(name));
};
const replenishmentFetches = () => apiClient.get.mock.calls
  .filter(([url]) => url === '/api/replenishments/r1').length;

describe('replenishment, pick and check steps', () => {
  const originalLocation = window.location;

  beforeEach(() => {
    status = 'PENDING';
    // The screens leave for server pages by assigning window.location.
    delete window.location;
    window.location = { href: 'http://localhost/' };
  });

  afterEach(() => {
    window.location = originalLocation;
    jest.clearAllMocks();
  });

  it('shows the picks of a pending replenishment, crossing out changed suggestions', async () => {
    renderPicks();

    expect(await screen.findByText('RP-1')).toBeInTheDocument();
    expect(await screen.findByText('Bandage')).toBeInTheDocument();
    expect(productOrder()).toEqual(['Bandage', 'Paracetamol', 'Gloves']);

    const crossedOut = document.querySelectorAll('.crossed-out');
    expect(crossedOut).toHaveLength(1);
    expect(crossedOut[0]).toHaveTextContent('STORE-1');
    expect(screen.getByText('STORE-2')).toBeInTheDocument();
    expect(screen.getByText('LOT-a')).toBeInTheDocument();
    // Paracetamol has no destination bin, so it goes to the default one.
    expect(screen.getByText('DEFAULT')).toBeInTheDocument();
  });

  it('sorts the lines by the bins they are picked from and back', async () => {
    renderPicks();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getByText('Sort by bins'));
    await waitFor(() => expect(productOrder()).toEqual(['Paracetamol', 'Bandage', 'Gloves']));

    fireEvent.click(screen.getByText('Original order'));
    await waitFor(() => expect(productOrder()).toEqual(['Bandage', 'Paracetamol', 'Gloves']));
    expect(screen.getByText('Sort by bins')).toBeInTheDocument();
  });

  it('approves the replenishment and completes it on the check step', async () => {
    renderPicks();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getByText('Next', { selector: 'button' }));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/replenishments/r1', { status: 'APPROVED' }));
    expect(await screen.findByText('Complete Stock Transfer')).toBeInTheDocument();
    expect(screen.getByText('Current Bin Location')).toBeInTheDocument();
    await waitFor(() => expect(replenishmentFetches()).toBe(3));

    fireEvent.click(screen.getByText('Complete Stock Transfer'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/replenishments/r1', { status: 'COMPLETED' }));
    await waitFor(() => expect(window.location).toBe('/openboxes/stockTransfer/show/r1'));
  });

  it('opens an approved replenishment on the check step and goes back to the picks', async () => {
    status = 'APPROVED';
    renderPicks();

    expect(await screen.findByText('Complete Stock Transfer')).toBeInTheDocument();
    expect(await screen.findByText('Gloves')).toBeInTheDocument();

    fireEvent.click(screen.getByText('Previous'));

    expect(await screen.findByText('Sort by bins')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Next', { selector: 'button' }));
    // Already approved: no second approval is sent.
    expect(await screen.findByText('Complete Stock Transfer')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('does not complete a replenishment that is not approved', async () => {
    status = 'COMPLETED';
    renderPicks();
    await screen.findByText('Gloves');

    await waitFor(() => expect(replenishmentFetches()).toBe(3));
    fireEvent.click(screen.getByText('Complete Stock Transfer'));

    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('prints the transfer order and leaves through save and exit', async () => {
    const open = jest.spyOn(window, 'open').mockImplementation(() => null);
    renderPicks();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getByText('Print Transfer Order'));
    expect(open).toHaveBeenCalledWith('/openboxes/replenishment/print/r1', '_blank');

    fireEvent.click(screen.getByText('Save and exit'));
    expect(window.location).toBe('/openboxes/stockTransfer/show/r1');
    open.mockRestore();
  });

  it('redoes the autopick only after confirming', async () => {
    renderPicks();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getByText('Reload'));
    fireEvent.click(await screen.findByText('No'));
    expect(apiClient.post).not.toHaveBeenCalled();

    fireEvent.click(screen.getByText('Reload'));
    expect(await screen.findByText('This button will redo the autopick on all items. Are you sure you want to continue?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Yes'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/replenishments/r1/picklists'));
    await waitFor(() => expect(replenishmentFetches()).toBe(3));
  });

  it('undoes the edits of a line', async () => {
    renderPicks();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getAllByText('Undo edit').map((label) => label.closest('button')).filter(Boolean)[0]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/replenishments/ri1/picklistItem', {}));
    await waitFor(() => expect(replenishmentFetches()).toBe(3));
  });

  it('edits the pick of a line in the modal', async () => {
    renderPicks([
      ['/api/replenishments/ri1/picklists', {
        data: {
          productCode: 'P1',
          'product.name': 'Bandage',
          quantityRequired: 5,
          availableItems: [
            {
              'inventoryItem.id': 'ii-a', 'binLocation.id': 'store-1', binLocation: { name: 'STORE-1' }, lotNumber: 'LOT-a', quantityAvailable: 10, status: 'AVAILABLE',
            },
            {
              'inventoryItem.id': 'ii-a', 'binLocation.id': 'store-2', binLocation: { name: 'STORE-2' }, lotNumber: 'LOT-a', quantityAvailable: 4, status: 'AVAILABLE',
            },
            {
              'inventoryItem.id': 'ii-x', 'binLocation.id': 'store-9', binLocation: { name: 'STORE-9' }, lotNumber: 'LOT-x', quantityAvailable: 0, status: 'NOT_AVAILABLE',
            },
          ],
          picklistItems: [{
            id: 'pi-a',
            'inventoryItem.id': 'ii-a',
            'binLocation.id': 'store-2',
            'binLocation.name': 'STORE-2',
            'binLocation.zoneId': 'zb',
            'binLocation.zoneName': 'Zone B',
            quantityPicked: 4,
          }],
        },
      }],
    ]);
    apiClient.put.mockResolvedValue({ data: {} });
    await screen.findByText('Bandage');

    fireEvent.click(screen.getAllByText('Edit', { selector: 'button' })[0]);

    const modal = within(document.querySelector('.modal-content-custom'));
    expect(await modal.findByText('Edit Pick')).toBeInTheDocument();
    await waitFor(() => expect(modal.getByText(/Qty Required/)).toHaveTextContent('Qty Required:5'));
    expect(modal.getByText(/Product code/)).toHaveTextContent('P1');
    expect(await modal.findByText('NOT_AVAILABLE')).toBeInTheDocument();
    expect(modal.getByText('Zone B')).toBeInTheDocument();
    const picked = (index) => document.querySelector(`[id="availableItems[${index}].quantityPicked"]`);
    expect(picked(1)).toHaveValue(4);
    expect(picked(2)).toBeDisabled();

    // 4 picked of 5 required: the totals differ.
    expect(picked(0).closest('[title]')).toHaveAttribute('title', 'react.stockMovement.errors.differentTotalQty.label');
    fireEvent.change(picked(0), { target: { value: '20' } });
    await waitFor(() => expect(picked(0).closest('[title]')).toHaveAttribute('title', 'react.stockMovement.errors.higherTyPicked.label'));
    fireEvent.change(picked(0), { target: { value: '-1' } });
    await waitFor(() => expect(picked(0).closest('[title]')).toHaveAttribute('title', 'react.stockMovement.errors.negativeQtyPicked.label'));

    fireEvent.change(picked(0), { target: { value: '1' } });
    await waitFor(() => expect(modal.getByText(/^Qty Picked:/)).toHaveTextContent('Qty Picked:5'));
    fireEvent.click(modal.getByText('Save'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    const [url, payload] = apiClient.put.mock.calls[0];
    expect(url).toBe('/api/replenishments/ri1/picklists');
    expect(payload.picklistItems).toEqual([
      {
        id: '', 'inventoryItem.id': 'ii-a', 'binLocation.id': 'store-1', quantityPicked: '1',
      },
      {
        id: 'pi-a', 'inventoryItem.id': 'ii-a', 'binLocation.id': 'store-2', quantityPicked: 4,
      },
      {
        id: '', 'inventoryItem.id': 'ii-x', 'binLocation.id': 'store-9', quantityPicked: '',
      },
    ]);
    await waitFor(() => expect(replenishmentFetches()).toBe(3));
  });

  it('closes the pick modal without saving on cancel', async () => {
    renderPicks([
      ['/api/replenishments/ri3/picklists', new Error('down')],
    ]);
    await screen.findByText('Gloves');

    fireEvent.click(screen.getAllByText('Edit', { selector: 'button' })[2]);
    const modal = within(document.querySelector('.modal-content-custom'));
    fireEvent.click(await modal.findByText('Cancel'));

    await waitFor(() => expect(document.querySelector('.modal-content-custom')).toBeNull());
    expect(apiClient.put).not.toHaveBeenCalled();
  });
});
