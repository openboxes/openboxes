import { fireEvent, screen, waitFor } from '@testing-library/react';
import moment from 'moment';
import { answer } from 'regression-tests/__mocks__/screenHarness';
import {
  bodyOf, button, candidates, field, item, open, RECEIVING, serve, stubLocation,
} from 'regression-tests/receiving/receivingFixtures';

import '@testing-library/jest-dom';

// OBAF still exports flattenRequest (an identity function in OBAF); the shared mock predates
// it.
jest.mock('utils/apiClient', () => ({
  // eslint-disable-next-line global-require
  ...require('regression-tests/__mocks__/screenApiClient').default(),
  flattenRequest: (...args) => jest.requireActual('utils/apiClient').flattenRequest(...args),
}));

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

jest.setTimeout(30000);

const STEP_2 = `${RECEIVING}?stepNumber=2`;
const delivered = (days) => moment().add(days, 'days').format('MM/DD/YYYY HH:mm Z');

const binA = { id: 'b1', name: 'Bin A', zoneName: 'Zone 1' };

const checked = (overrides = {}) => candidates({
  dateDelivered: delivered(-1),
  containers: [{
    container: { id: 'c1', name: 'Box 1' },
    shipmentItems: [
      item({
        receiptItemId: 'ri1', quantityReceiving: 6, quantityRemaining: 4, binLocation: binA,
      }),
      item({
        shipmentItemId: 'si2',
        receiptItemId: 'ri2',
        lotNumber: 'L2',
        quantityShipped: 5,
        quantityReceiving: 5,
        quantityRemaining: 0,
        binLocation: { id: 'b3', name: 'Dock' },
      }),
    ],
  }],
  ...overrides,
});

/** Opens the receiving step with one line already in the receipt and goes on to the check. */
const openCheck = async (step2 = checked(), options = {}) => {
  serve(candidates({
    containers: [{
      container: { id: 'c1', name: 'Box 1' },
      shipmentItems: [item({ receiptItemId: 'ri1', quantityReceiving: 6 })],
    }],
  }), { step2 });
  const utils = await open(options);
  await waitFor(() => expect(button('Next')).toBeEnabled());
  fireEvent.click(button('Next'));
  await screen.findByText('Shipment information');
  await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith(`${STEP_2}&sort=shipment`));
  if (!(step2 instanceof Error)) {
    await screen.findAllByText('P-1');
  }
  apiClient.post.mockClear();
  return utils;
};

describe('legacy partial receiving, check step', () => {
  let restoreLocation;

  beforeEach(() => {
    delete window.setFormValue;
    restoreLocation = stubLocation();
  });

  afterEach(() => {
    restoreLocation();
    jest.clearAllMocks();
  });

  it('shows the shipment and what is being received', async () => {
    await openCheck();

    expect(field('origin.name')).toHaveValue('Supplier A');
    expect(field('origin.name')).toBeDisabled();
    expect(field('destination.name')).toHaveValue('Main Warehouse');
    // A zoned bin shows its zone first.
    expect(screen.getByText(': Bin A').previousSibling).toHaveTextContent('Zone 1');
    expect(screen.getByText('Dock')).toBeInTheDocument();
    expect(screen.getByText('6')).toBeInTheDocument();
    // What is left on a line is marked until it is cancelled.
    expect(screen.getByText('4').closest('.text-danger')).not.toBeNull();
    expect(screen.getByText('Cancel all remaining')).toBeInTheDocument();
    expect(button('Receive shipment')).toBeEnabled();
  });

  it('completes the receipt and leaves for the stock movement page', async () => {
    await openCheck(checked({ orderId: 'po1' }));

    fireEvent.click(button('Receive shipment'));

    await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/po1'));
    const body = bodyOf(apiClient.post, STEP_2);
    expect(body.receiptStatus).toBe('COMPLETED');
    expect(body.recipient).toBe('u1');
    expect(body.containers[0].shipmentItems).toHaveLength(2);
    expect(body.containers[0].shipmentItems[0].product.displayNames).toBeUndefined();
    await waitFor(() => expect(button('Receive shipment')).toBeDisabled());
  });

  it('asks before completing a receipt with lines that have no bin', async () => {
    const step2 = checked();
    step2.containers[0].shipmentItems[1].binLocation = { id: null, name: null };
    await openCheck(step2);

    fireEvent.click(button('Receive shipment'));
    expect(await screen.findByText(/some lines with empty bin locations/)).toBeInTheDocument();
    fireEvent.click(screen.getByText('No'));

    await waitFor(() => expect(screen.queryByText(/empty bin locations/)).not.toBeInTheDocument());
    expect(apiClient.post).not.toHaveBeenCalled();

    fireEvent.click(button('Receive shipment'));
    fireEvent.click(await screen.findByText('Yes'));

    await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/s1'));
    expect(bodyOf(apiClient.post, STEP_2).receiptStatus).toBe('COMPLETED');
  });

  it('does not ask about empty bins where the location has no bins', async () => {
    const step2 = checked();
    step2.containers[0].shipmentItems[1].binLocation = { id: null, name: null };
    await openCheck(step2, { bins: false });

    expect(screen.queryByText('Bin Location')).not.toBeInTheDocument();
    fireEvent.click(button('Receive shipment'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(screen.queryByText(/empty bin locations/)).not.toBeInTheDocument();
    expect(bodyOf(apiClient.post, STEP_2).receiptStatus).toBe('COMPLETED');
  });

  it('cancels every remaining quantity and saves it', async () => {
    await openCheck();

    const boxes = () => document.querySelectorAll('input[type="checkbox"]');
    expect([...boxes()].map((b) => [b.checked, b.disabled]))
      .toEqual([[false, false], [false, true]]);

    fireEvent.click(button('Cancel all remaining'));
    await waitFor(() => expect(boxes()[0]).toBeChecked());
    await waitFor(() => expect(screen.getByText('4').closest('.strike-through')).not.toBeNull());

    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, STEP_2).containers[0].shipmentItems;
    expect(items.map((i) => i.cancelRemaining)).toEqual([true, false]);
    expect(bodyOf(apiClient.post, STEP_2).receiptStatus).toBeUndefined();
    // Saving keeps the user on the check step.
    expect(screen.getByText('Shipment information')).toBeInTheDocument();
  });

  it('cancels what remains and hides partial lines where partial receiving is not supported', async () => {
    const step2 = checked();
    step2.containers[0].shipmentItems.push(item({
      shipmentItemId: 'si3', lotNumber: 'L3', quantityReceiving: null,
    }));
    await openCheck(step2, { partial: false });

    expect(screen.queryByText('Cancel all remaining')).not.toBeInTheDocument();
    expect(screen.queryByText('L3')).not.toBeInTheDocument();

    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, STEP_2).containers[0].shipmentItems;
    expect(items.map((i) => [i.shipmentItemId, i.cancelRemaining])).toEqual([
      ['si1', true],
      ['si2', undefined],
    ]);
  });

  it('saves and goes back to the receiving step', async () => {
    await openCheck();
    apiClient.get.mockClear();

    fireEvent.click(button('Back to edit'));

    expect(await screen.findByText('Autofill quantities')).toBeInTheDocument();
    expect(apiClient.post).toHaveBeenCalledWith(STEP_2, expect.any(Object));
    expect(apiClient.get).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1&sort=shipment`);
  });

  // KNOWN-GAP: OBAF's prevPage has no catch, so a failed save leaves the spinner up
  // (and the rejection unhandled).
  it.skip('hides the spinner and stays on the check step when going back fails', async () => {
    const { store } = await openCheck();
    answer(apiClient.post, [[(url) => url.startsWith(RECEIVING), new Error('Server error')]]);

    fireEvent.click(button('Back to edit'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.getByText('Shipment information')).toBeInTheDocument();
  });

  it('saves and leaves for the stock movement page', async () => {
    await openCheck(checked({ requisition: 'req1' }));

    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/req1'));
    expect(bodyOf(apiClient.post, STEP_2).receiptStatus).toBeUndefined();
  });

  it('stays on the check step when saving and exiting fails', async () => {
    await openCheck();
    answer(apiClient.post, [[(url) => url.startsWith(RECEIVING), new Error('Server error')]]);

    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(window.location).not.toEqual(expect.stringContaining('stockMovement'));
    expect(screen.getByText('Shipment information')).toBeInTheDocument();
  });

  it('does not complete a receipt delivered in the future', async () => {
    await openCheck(checked({ dateDelivered: delivered(3) }));

    fireEvent.click(button('Receive shipment'));

    await waitFor(() => expect(field('dateDelivered').closest('.has-error')).not.toBeNull());
    expect(screen.getByText('react.partialReceiving.error.futureDate.label')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('does not complete a receipt without a delivery date', async () => {
    await openCheck(checked({ dateDelivered: null }));

    fireEvent.click(button('Receive shipment'));

    await waitFor(() => expect(field('dateDelivered').closest('.has-error')).not.toBeNull());
    expect(screen.getByText('react.default.error.requiredField.label')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('cannot be saved or completed when the check step does not load', async () => {
    const step2 = new Error('Server error');
    await openCheck(step2);

    expect(button('Receive shipment')).toBeDisabled();
    expect(button('Save')).toBeDisabled();
    expect(button('Cancel all remaining')).toBeDisabled();
  });
});
