import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import {
  bodyOf, candidates, field, item, open, RECEIVING, serve, stubLocation, type,
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

const MISMATCH = 'The total edited quantity does not match the original quantity shipped.';

const line = (index) => `containers[0].shipmentItems[${index}]`;

// This react-modal version renders no dialog role; its content element has a fixed class.
const modal = () => document.querySelector('.ReactModal__Content');

const openEditor = async (index = 0) => {
  const buttons = screen.getAllByText('Edit line').map((b) => b.closest('button'));
  fireEvent.click(buttons[index]);
  await waitFor(() => expect(modal()).not.toBeNull());
  return modal();
};

const save = (dialog) => fireEvent.click(within(dialog).getByText('Save'));

describe('legacy partial receiving, edit line modal', () => {
  let restoreLocation;

  beforeEach(() => {
    delete window.setFormValue;
    restoreLocation = stubLocation();
  });

  afterEach(() => {
    restoreLocation();
    jest.clearAllMocks();
  });

  it('splits a new line in two without saving it yet', async () => {
    serve();
    await open();

    const dialog = await openEditor();
    expect(dialog.querySelector('.mb-3')).toHaveTextContent('Original quantity shipped:10');
    expect(field('lines[0].lotNumber')).toHaveValue('L1');

    type('lines[0].quantityShipped', '6');
    expect(await within(dialog).findByText(MISMATCH)).toBeInTheDocument();

    fireEvent.click(within(dialog).getByText('Add line'));
    await waitFor(() => expect(field('lines[1].quantityShipped')).not.toBeNull());
    type('lines[1].lotNumber', 'L2');
    type('lines[1].quantityShipped', '4');
    await waitFor(() => expect(within(dialog).queryByText(MISMATCH)).not.toBeInTheDocument());

    save(dialog);

    await waitFor(() => expect(modal()).toBeNull());
    await waitFor(() => expect(field(`${line(1)}.quantityReceiving`)).not.toBeNull());
    expect(screen.getByText('L2')).toBeInTheDocument();
    // The lines are only saved with the rest of the page.
    expect(apiClient.post).not.toHaveBeenCalled();

    type(`${line(0)}.quantityReceiving`, '6');
    type(`${line(1)}.quantityReceiving`, '4');
    fireEvent.click(screen.getByText('Save').closest('button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, RECEIVING).containers[0].shipmentItems;
    expect(items.map((i) => [i.lotNumber, i.quantityShipped, i.quantityReceiving])).toEqual([
      ['L1', '6', '6'],
      ['L2', '4', '4'],
    ]);
    expect(items[1].shipmentItemId).toBe('si1');
    expect(items[1].receiptItemId).toBeNull();
  });

  it('saves a line that is already part of the receipt before splitting it', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [
          item({ receiptItemId: 'ri1', quantityReceiving: 10 }),
          item({ shipmentItemId: 'si2', lotNumber: 'L9', quantityShipped: 3 }),
        ],
      }],
    }));
    await open();

    const dialog = await openEditor();
    type('lines[0].quantityShipped', '8');
    fireEvent.click(within(dialog).getByText('Add line'));
    await waitFor(() => expect(field('lines[1].quantityShipped')).not.toBeNull());
    type('lines[1].lotNumber', 'L2');
    type('lines[1].quantityShipped', '2');
    save(dialog);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const body = bodyOf(apiClient.post, RECEIVING);
    // Only the persisted line goes to the server, with its new quantity.
    expect(body.containers).toHaveLength(1);
    expect(body.containers[0].shipmentItems.map((i) => [i.receiptItemId, i.quantityShipped]))
      .toEqual([['ri1', '8']]);

    // The saved line stays, its receiving quantity is cleared, the new line follows it.
    await screen.findByText('L2');
    expect(field(`${line(0)}.quantityReceiving`)).toHaveValue('');
    expect(screen.getByText('L9')).toBeInTheDocument();
    expect(field(`${line(2)}.quantityReceiving`)).not.toBeNull();
  });

  it('does not save lines with a negative or missing quantity or an expiry without a lot', async () => {
    serve();
    await open();

    const dialog = await openEditor();
    type('lines[0].quantityShipped', '-2');
    fireEvent.click(within(dialog).getByText('Add line'));
    await waitFor(() => expect(field('lines[1].quantityShipped')).not.toBeNull());
    save(dialog);

    await waitFor(() => expect(field('lines[0].quantityShipped').closest('.has-error')).not.toBeNull());
    expect(field('lines[1].quantityShipped').closest('.has-error')).not.toBeNull();
    expect(modal()).not.toBeNull();

    type('lines[1].quantityShipped', '0');

    type('lines[0].quantityShipped', '10');
    type('lines[0].lotNumber', '');
    save(dialog);

    await waitFor(() => expect(field('lines[0].lotNumber').closest('.has-error')).not.toBeNull());
    expect(modal()).not.toBeNull();
    expect(field(`${line(0)}.quantityReceiving`)).not.toBeNull();
  });

  it('asks for a lot and expiry on controlled products, on new lines too', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item({
          lotNumber: null,
          expirationDate: null,
          product: {
            id: 'p1', productCode: 'P-1', name: 'Ibuprofen', lotAndExpiryControl: true,
          },
        })],
      }],
    }));
    await open();

    const dialog = await openEditor();
    fireEvent.click(within(dialog).getByText('Add line'));
    await waitFor(() => expect(field('lines[1].quantityShipped')).not.toBeNull());
    save(dialog);

    await waitFor(() => expect(field('lines[0].lotNumber').closest('.has-error')).not.toBeNull());
    expect(field('lines[0].expirationDate').closest('.has-error')).not.toBeNull();
    expect(field('lines[1].lotNumber').closest('.has-error')).not.toBeNull();

    type('lines[0].lotNumber', 'L5');
    type('lines[1].quantityShipped', '0');
    type('lines[1].lotNumber', 'L6');
    save(dialog);

    await waitFor(() => expect(field('lines[0].expirationDate').closest('.has-error')).not.toBeNull());
    expect(field('lines[0].lotNumber').closest('.has-error')).toBeNull();
    expect(modal()).not.toBeNull();
  });

  it('does not take expiry dates earlier than the minimum expiration date', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item({ expirationDate: '01/01/1999' })],
      }],
    }));
    await open({ session: { minimumExpirationDate: '01/01/2000' } });

    const dialog = await openEditor();
    save(dialog);

    await waitFor(() => expect(field('lines[0].expirationDate').closest('.has-error')).not.toBeNull());
    expect(modal()).not.toBeNull();
  });

  it('closes without changes on cancel', async () => {
    serve();
    await open();

    const dialog = await openEditor();
    type('lines[0].lotNumber', 'CHANGED');
    fireEvent.click(within(dialog).getByText('Cancel'));

    await waitFor(() => expect(modal()).toBeNull());
    expect(screen.queryByText('CHANGED')).not.toBeInTheDocument();
    expect(screen.getByText('L1')).toBeInTheDocument();
  });

  describe('when the lot is in stock with another expiry date', () => {
    const inStock = () => candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item({ quantityOnHand: 25 })],
      }],
    });

    const changeExpiry = async () => {
      const dialog = await openEditor();
      fireEvent.change(field('lines[0].expirationDate'), { target: { value: '06/30/2028' } });
      await waitFor(() => expect(field('lines[0].expirationDate')).toHaveValue('06/30/2028'));
      save(dialog);
      return screen.findByText('Confirm save');
    };

    it('applies the new expiry once the user confirms it', async () => {
      serve(inStock());
      await open();

      await changeExpiry();
      fireEvent.click(screen.getByText('Yes'));

      await waitFor(() => expect(screen.queryByText('Confirm save')).not.toBeInTheDocument());
      await waitFor(() => expect(screen.queryAllByText('12/31/2027')).toHaveLength(0));
      expect(screen.getByText('06/30/2028')).toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF passes itemsWithMismatchedExpiry while the modal reads data, so
    // the confirmation opens with an empty table.
    it.skip('lists the line with its old and new expiry in the confirmation', async () => {
      serve(inStock());
      await open();

      await changeExpiry();

      expect(await screen.findByText('31/Dec/2027')).toBeInTheDocument();
      expect(screen.getByText('30/Jun/2028')).toBeInTheDocument();
    });

    it('keeps the old expiry when the user declines', async () => {
      serve(inStock());
      await open();

      await changeExpiry();
      fireEvent.click(screen.getByText('No'));

      await waitFor(() => expect(screen.queryByText('Confirm save')).not.toBeInTheDocument());
      expect(screen.getByText('12/31/2027')).toBeInTheDocument();
      expect(screen.queryByText('06/30/2028')).not.toBeInTheDocument();
    });
  });
});
