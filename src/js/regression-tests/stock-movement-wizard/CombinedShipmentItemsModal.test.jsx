import React from 'react';

import {
  configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { wizardStore } from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  });
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    apiClientCustomResponseHandler: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import CombinedShipmentItemsModal from 'components/stock-movement-wizard/modals/CombinedShipmentItemsModal';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const ORDER_ITEMS = [
  {
    orderItemId: 'oi8', orderId: 'po2', orderNumber: 'PO-0002', productCode: 'P-8', productName: 'Gauze', quantityAvailable: 40,
  },
  {
    orderItemId: 'oi9', orderId: 'po2', orderNumber: 'PO-0002', productCode: 'P-9', productName: 'Tape', quantityAvailable: 15,
  },
];

let onOpen;
let onResponse;
let overrideFormValue;

const renderModal = () => render(
  <Provider store={wizardStore()}>
    <CombinedShipmentItemsModal
      shipment="sm1"
      vendor="ven1"
      destination="loc1"
      onOpen={onOpen}
      onResponse={onResponse}
      overrideFormValue={overrideFormValue}
      btnOpenText="react.default.button.addLines.label"
      btnOpenDefaultText="Add lines"
    />
  </Provider>,
);

const findOrderItemsCalls = () => apiClient.post.mock.calls
  .filter(([url]) => url === '/api/combinedShipmentItems/findOrderItems').map(([, body]) => body);

// The open dialog marks the rest of the page aria-hidden, so queries inside it include
// hidden nodes.
const openDialog = async () => {
  fireEvent.click(screen.getByText('Add lines'));
  await waitFor(() => expect(document.querySelector('.modal-content-custom')).not.toBeNull());
  const dialog = document.querySelector('.modal-content-custom');
  await within(dialog).findByText('Tape');
  return dialog;
};

const saveButton = (dialog) => within(dialog).getByText('Add items to shipment', { selector: 'button *, button' }).closest('button');

describe('CombinedShipmentItemsModal', () => {
  beforeEach(() => {
    onOpen = jest.fn(() => Promise.resolve());
    onResponse = jest.fn();
    overrideFormValue = jest.fn();
    apiClient.get.mockResolvedValue({ data: { data: [{ id: 'po2', orderNumber: 'PO-0002' }] } });
    apiClient.post.mockImplementation((url) => Promise.resolve(url === '/api/combinedShipmentItems/findOrderItems'
      ? { data: { orderItems: ORDER_ITEMS } }
      : { data: {} }));
  });

  afterEach(() => jest.clearAllMocks());

  it('loads the open order items of the vendor and destination once the shipment has been saved', async () => {
    renderModal();

    const dialog = await openDialog();

    expect(onOpen).toHaveBeenCalled();
    expect(apiClient.get).toHaveBeenCalledWith('/api/orderNumberOptions?vendor=ven1&destination=loc1');
    expect(findOrderItemsCalls()).toEqual([{
      orderIds: [], productId: '', vendor: 'ven1', destination: 'loc1',
    }]);
    expect(within(dialog).getByText('Gauze')).toBeInTheDocument();
    expect(saveButton(dialog)).toBeDisabled();
  });

  it('select all picks every item with its full available quantity and adds them to the shipment', async () => {
    renderModal();
    const dialog = await openDialog();

    fireEvent.click(within(dialog).getAllByRole('checkbox', { hidden: true })[0]);
    expect(within(dialog).getByDisplayValue('40')).toBeInTheDocument();
    fireEvent.click(saveButton(dialog));

    await waitFor(() => expect(onResponse).toHaveBeenCalledWith(overrideFormValue));
    const [, payload] = apiClient.post.mock.calls.find(([url]) => url === '/api/combinedShipmentItems/addToShipment/sm1');
    expect(payload.itemsToAdd
      .map(({ orderItemId, quantityToShip }) => [orderItemId, quantityToShip]))
      .toEqual([['oi8', 40], ['oi9', 15]]);
  });

  it('select all again clears the selection', async () => {
    renderModal();
    const dialog = await openDialog();
    const selectAll = within(dialog).getAllByRole('checkbox', { hidden: true })[0];

    fireEvent.click(selectAll);
    fireEvent.click(selectAll);

    expect(within(dialog).queryByDisplayValue('40')).not.toBeInTheDocument();
    expect(saveButton(dialog)).toBeDisabled();
  });

  it('a checked row is added with its full available quantity, the others are left out', async () => {
    renderModal();
    const dialog = await openDialog();
    const [, , tape] = within(dialog).getAllByRole('checkbox', { hidden: true });

    fireEvent.click(tape);
    expect(within(dialog).getByDisplayValue('15')).toBeInTheDocument();
    expect(within(dialog).queryByDisplayValue('40')).not.toBeInTheDocument();
    fireEvent.click(saveButton(dialog));

    await waitFor(() => expect(onResponse).toHaveBeenCalled());
    const [, payload] = apiClient.post.mock.calls.find(([url]) => url === '/api/combinedShipmentItems/addToShipment/sm1');
    expect(payload.itemsToAdd).toEqual([{ orderItemId: 'oi9', quantityToShip: 15, sortOrder: expect.any(Number) }]);
  });

  it('filtering by order asks for the items of the chosen orders', async () => {
    renderModal();
    const dialog = await openDialog();

    const orderSelect = within(dialog).getByText('Select orders...').closest('[data-testid="custom-select-element"]');
    fireEvent.keyDown(orderSelect.querySelector('input'), { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(await within(document.body).findByText('PO-0002', { selector: '[class*="option"], [class*="option"] *' }));

    await waitFor(() => expect(findOrderItemsCalls()).toHaveLength(2));
    expect(findOrderItemsCalls()[1].orderIds).toEqual(['po2']);
  });
});
