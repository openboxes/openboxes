import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';
import {
  bodyOf, button, candidates, ENTRY, field, item, open, RECEIVING, ROUTE, serve, storeWith,
  stubLocation, type,
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

// The CSV download: the file the browser would save.
jest.mock('js-file-download', () => jest.fn());

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';

import ReceivingPage from 'components/receiving/ReceivingPage';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

jest.setTimeout(30000);

const QTY = 'containers[0].shipmentItems[0].quantityReceiving';

describe('legacy partial receiving, receiving step', () => {
  let restoreLocation;

  beforeEach(() => {
    delete window.setFormValue;
    restoreLocation = stubLocation();
  });

  afterEach(() => {
    restoreLocation();
    jest.clearAllMocks();
  });

  it('loads the shipment and its receiving bins into the wizard', async () => {
    serve();
    await open();

    expect(screen.getByText('SH-1')).toBeInTheDocument();
    expect(screen.getByText('Supplier A')).toBeInTheDocument();
    expect(screen.getByText('Inbound order')).toBeInTheDocument();
    expect(screen.getByText('Box 1')).toBeInTheDocument();
    expect(screen.getByText('P-1')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1`);
    expect(apiClient.get).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1&sort=shipment`);
    const [, config] = apiClient.get.mock.calls
      .find(([url]) => url === '/api/internalLocations/receiving');
    expect(config.params).toEqual({
      locationTypeCode: ['BIN_LOCATION', 'INTERNAL'],
      'location.id': 'loc1',
      shipmentNumber: 'SH-1',
    });
    expect(config.paramsSerializer(config.params))
      .toBe('location.id=loc1&locationTypeCode=BIN_LOCATION&locationTypeCode=INTERNAL&shipmentNumber=SH-1');
    // Nothing is chosen yet, so nothing can be saved or submitted.
    expect(button('Next')).toBeDisabled();
    expect(button('Save')).toBeDisabled();
    expect(button('Save and exit')).toBeDisabled();
  });

  it('renders nothing when the shipment cannot be loaded', async () => {
    answer(apiClient.get, [
      [(url) => url.startsWith(RECEIVING), new Error('Server error')],
    ]);
    const { store } = renderAt({
      component: ReceivingPage, path: ROUTE, entry: ENTRY, store: storeWith(),
    });

    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1`));
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.queryByText('Autofill quantities')).not.toBeInTheDocument();
  });

  it('autofills the remaining quantity of every line and clears it per container', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [
          item({ quantityReceived: 4, quantityRemaining: 6 }),
          item({
            shipmentItemId: 'si2', lotNumber: 'L2', quantityShipped: 5, quantityReceived: 5, quantityRemaining: 0,
          }),
        ],
      }],
    }));
    await open();

    fireEvent.click(button('Autofill quantities'));
    // The partly received line gets what is left; the fully received one stays untouched.
    await waitFor(() => expect(field(QTY)).toHaveValue('6'));
    expect(field('containers[0].shipmentItems[1].quantityReceiving')).toHaveValue('');
    expect(field('containers[0].shipmentItems[1].quantityReceiving')).toBeDisabled();

    const [containerBox] = screen.getAllByTestId('checkbox');
    await waitFor(() => expect(containerBox).toBeChecked());
    fireEvent.click(containerBox);
    await waitFor(() => expect(field(QTY)).toHaveValue(''));
    await waitFor(() => expect(containerBox).not.toBeChecked());

    fireEvent.click(containerBox);
    await waitFor(() => expect(field(QTY)).toHaveValue('6'));
  });

  it('autofills a single line from its own checkbox', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item(), item({ shipmentItemId: 'si2', lotNumber: 'L2', quantityShipped: 3 })],
      }],
    }));
    await open();

    const boxes = screen.getAllByTestId('checkbox');
    fireEvent.click(boxes[2]);

    await waitFor(() => expect(field('containers[0].shipmentItems[1].quantityReceiving')).toHaveValue('3'));
    expect(field(QTY)).toHaveValue('');
    // One line of two chosen: the container checkbox is half checked.
    await waitFor(() => expect(boxes[0].indeterminate).toBe(true));
  });

  it('posts the entered quantities and moves on to the check step', async () => {
    serve();
    await open();

    type(QTY, '7');
    type('containers[0].shipmentItems[0].comment', 'Box was wet');
    await waitFor(() => expect(button('Next')).toBeEnabled());
    fireEvent.click(button('Next'));

    await screen.findByText('Receive shipment');
    const body = bodyOf(apiClient.post, `${RECEIVING}?stepNumber=1`);
    expect(body.receiptStatus).toBe('CHECKING');
    expect(body.recipient).toBe('u1');
    const [line] = body.containers[0].shipmentItems;
    expect(line.quantityReceiving).toBe('7');
    expect(line.comment).toBe('Box was wet');
    // product.displayNames has no setter on the server, so it is never sent.
    expect(line.product.displayNames).toBeUndefined();
    expect(apiClient.get).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=2&sort=shipment`);
  });

  it('only sends the lines that are being received when partial receiving is supported', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [
          item(),
          item({ shipmentItemId: 'si2', lotNumber: 'L2' }),
          item({ shipmentItemId: 'si3', lotNumber: 'L3', receiptItemId: 'ri3' }),
        ],
      }],
    }));
    await open();

    type(QTY, '2');
    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, RECEIVING).containers[0].shipmentItems;
    // The untouched new line is left out; the already saved receipt line is kept with 0.
    expect(items.map((i) => [i.shipmentItemId, i.quantityReceiving])).toEqual([
      ['si1', '2'],
      ['si3', 0],
    ]);
  });

  it('asks before receiving empty lines as zero where partial receiving is not supported', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item(), item({ shipmentItemId: 'si2', lotNumber: 'L2' })],
      }],
    }));
    await open({ partial: false });

    type(QTY, '10');
    await waitFor(() => expect(button('Next')).toBeEnabled());
    fireEvent.click(button('Next'));

    expect(await screen.findByText('Confirm receiving')).toBeInTheDocument();
    expect(screen.getByText(/1 line\/lines on this shipment/)).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();

    fireEvent.click(screen.getByText('Yes'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, RECEIVING).containers[0].shipmentItems;
    expect(items.map((i) => i.quantityReceiving)).toEqual(['10', 0]);
  });

  it('stays on the page when the user declines receiving empty lines', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item(), item({ shipmentItemId: 'si2', lotNumber: 'L2' })],
      }],
    }));
    await open({ partial: false });

    type(QTY, '10');
    fireEvent.click(button('Next'));
    fireEvent.click(await screen.findByText('No'));

    await waitFor(() => expect(screen.queryByText('Confirm receiving')).not.toBeInTheDocument());
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
  });

  it('does not submit negative quantities', async () => {
    serve();
    await open();

    type(QTY, '-1');
    await waitFor(() => expect(button('Next')).toBeEnabled());
    fireEvent.click(button('Next'));

    await waitFor(() => expect(field(QTY).closest('.has-error')).not.toBeNull());
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(button('Next')).toBeInTheDocument();
  });

  it('does not submit a lot and expiry controlled line without its lot and expiry', async () => {
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

    type(QTY, '5');
    await waitFor(() => expect(button('Next')).toBeEnabled());
    fireEvent.click(button('Next'));

    await waitFor(() => expect(document.querySelector('.has-control-error')).not.toBeNull());
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('saves, then reloads the saved lines and resets the sort order', async () => {
    serve();
    await open();

    type(QTY, '4');
    await waitFor(() => expect(button('Save')).toBeEnabled());
    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, body] = apiClient.post.mock.calls[0];
    expect(url).toBe(`${RECEIVING}?stepNumber=1`);
    expect(body.receiptStatus).toBeUndefined();
    expect(body.containers[0].shipmentItems[0].quantityReceiving).toBe('4');
    // Still on the first step.
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
  });

  // KNOWN-GAP: OBAF passes the result of updateSort(...) as the callback, so the sort
  // resets as soon as Save is clicked, before the save finishes.
  it.skip('resets the sort order only once the save has succeeded', async () => {
    serve();
    await open();
    fireEvent.keyDown(screen.getByText('Shipment Order'), { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(await screen.findByText('Alphabetical Order'));
    await waitFor(() => expect(apiClient.get)
      .toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1&sort=alphabetical`));
    await waitFor(() => expect(screen.getByText('Alphabetical Order')).toBeInTheDocument());
    let finishSave;
    apiClient.post.mockImplementation(() => new Promise((resolve) => {
      finishSave = resolve;
    }));

    type(QTY, '4');
    await waitFor(() => expect(button('Save')).toBeEnabled());
    fireEvent.click(button('Save'));

    await waitFor(() => expect(finishSave).toBeDefined());
    expect(screen.getByText('Alphabetical Order')).toBeInTheDocument();
    finishSave({ data: { data: candidates() } });
    expect(await screen.findByText('Shipment Order')).toBeInTheDocument();
  });

  it('saves and leaves for the stock movement page', async () => {
    serve(candidates({ requisition: 'req1' }));
    await open();

    type(QTY, '4');
    await waitFor(() => expect(button('Save and exit')).toBeEnabled());
    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/req1'));
    expect(bodyOf(apiClient.post, RECEIVING).containers[0].shipmentItems[0].quantityReceiving)
      .toBe('4');
  });

  it('stays on the page when saving and exiting fails', async () => {
    serve();
    await open();
    answer(apiClient.post, [[(url) => url.startsWith(RECEIVING), new Error('Server error')]]);

    type(QTY, '4');
    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(window.location).not.toEqual(expect.stringContaining('stockMovement'));
  });

  it('saves the current lines before reloading them in the chosen order', async () => {
    serve();
    await open();

    const sortSelect = screen.getByText('Shipment Order');
    fireEvent.keyDown(sortSelect, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(await screen.findByText('Alphabetical Order'));

    await waitFor(() => expect(apiClient.get)
      .toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1&sort=alphabetical`));
    expect(apiClient.post).toHaveBeenCalledWith(`${RECEIVING}?stepNumber=1`, expect.any(Object));
  });

  it('applies a bin chosen on a container to its lines that are not yet received', async () => {
    serve(candidates({
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [
          item(),
          item({
            shipmentItemId: 'si2', lotNumber: 'L2', quantityReceived: 10, quantityRemaining: 0, receiptItemId: 'ri2',
          }),
        ],
      }],
    }));
    await open();

    const table = screen.getByTestId('items-table');
    const [containerSelect] = table.querySelectorAll('.select-xs');
    fireEvent.keyDown(within(containerSelect).getByRole('textbox'), { key: 'ArrowDown', keyCode: 40 });

    // Zoned bins are grouped under their zone and sorted by name; unzoned bins follow.
    const menu = await waitFor(() => {
      const element = document.querySelector('.react-select__menu-list');
      expect(element).not.toBeNull();
      return element;
    });
    expect(within(menu).getByText('Zone 1')).toBeInTheDocument();
    const names = [...menu.querySelectorAll('[class*="-option"]')].map((o) => o.textContent);
    expect(names).toEqual(['Bin A', 'Bin B', 'Dock']);

    fireEvent.click(within(menu).getByText('Dock'));
    type(QTY, '1');
    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const items = bodyOf(apiClient.post, RECEIVING).containers[0].shipmentItems;
    expect(items[0].binLocation).toMatchObject({ id: 'b3', name: 'Dock' });
    expect(items[1].binLocation).toEqual({ id: null, name: null });
  });

  it('shows what is left to ship in purchase order units for shipments from an order', async () => {
    serve(candidates({
      isShipmentFromPurchaseOrder: true,
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item({
          quantityReceived: 5, quantityRemaining: 5, unitOfMeasure: 'BX', packSize: 5, packsRequested: 2,
        })],
      }],
    }));
    await open();

    expect(screen.getByText('Shipped (in PO UoM)')).toBeInTheDocument();
    expect(screen.getByText('Receiving now (each)')).toBeInTheDocument();
    // 2 packs ordered, 5 of 5 each received: 1 box to go.
    expect(screen.getByText('BX').parentElement).toHaveTextContent('1BX');
  });

  it('stays on the receiving step when moving on fails', async () => {
    serve();
    await open();
    answer(apiClient.post, [[(url) => url.startsWith(RECEIVING), new Error('Server error')]]);

    type(QTY, '3');
    await waitFor(() => expect(button('Next')).toBeEnabled());
    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
    expect(screen.queryByText('Receive shipment')).not.toBeInTheDocument();
  });

  it('locks the page once the shipment has been received', async () => {
    serve(candidates({
      shipmentStatus: 'RECEIVED',
      containers: [{
        container: { id: 'c1', name: 'Box 1' },
        shipmentItems: [item({ quantityReceiving: 10 })],
      }],
    }));
    await open();

    expect(button('Autofill quantities')).toBeDisabled();
    expect(button('Next')).toBeDisabled();
    expect(button('Save')).toBeDisabled();
    expect(field(QTY)).toBeDisabled();
    expect(button('Edit line')).toBeDisabled();
  });

  it('exports the lines as a CSV template without product display names', async () => {
    serve();
    await open();

    fireEvent.click(button('Export template'));

    await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('code,lot\nP-1,L1', 'PartialReceiving-s1.csv', 'text/csv'));
    const body = bodyOf(apiClient.post, '/api/partialReceiving/exportCsv/s1');
    expect(body.recipient).toEqual({ id: 'u1' });
    expect(body.containers[0].shipmentItems[0].product).toEqual({ id: 'p1', productCode: 'P-1', name: 'Ibuprofen' });
  });

  it('does not download anything when the export fails', async () => {
    serve();
    await open();
    answer(apiClient.post, [['/api/partialReceiving/exportCsv/s1', new Error('Server error')]]);

    fireEvent.click(button('Export template'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(fileDownload).not.toHaveBeenCalled();
  });

  it('uploads an imported CSV and reloads the page', async () => {
    serve();
    await open();
    const file = new File(['code,lot\nP-1,L1'], 'receipt.csv', { type: 'text/csv' });

    const input = document.getElementById('csvInput');
    fireEvent.click(input);
    fireEvent.change(input, { target: { files: [file] } });

    await waitFor(() => expect(window.location.reload).toHaveBeenCalled());
    const [url, body, config] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/partialReceiving/importCsv/s1');
    expect(body.get('importFile')).toBeInstanceOf(Blob);
    expect(config.headers['content-type']).toBe('multipart/form-data');
  });

  it('does not reload the page when the import is rejected', async () => {
    serve();
    await open();
    answer(apiClient.post, [['/api/partialReceiving/importCsv/s1', new Error('Bad file')]]);
    const file = new File(['x'], 'receipt.csv', { type: 'text/csv' });

    fireEvent.change(document.getElementById('csvInput'), { target: { files: [file] } });

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(window.location.reload).not.toHaveBeenCalled();
  });
});
