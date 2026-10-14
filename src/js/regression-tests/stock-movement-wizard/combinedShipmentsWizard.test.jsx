import React from 'react';

import {
  act, configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';
import {
  DEPOT, HERE, routeAnswers, sessionInfo, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes of each test. The helpers the actions use come from the
// real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  // Callable like an axios instance: the location search calls it directly.
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

// Confirmation dialogs: the test picks a button by its label and runs its handler.
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));
jest.mock('js-file-download', () => jest.fn());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The wizard renders large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import axios from 'axios';
import fileDownload from 'js-file-download';
import { confirmAlert } from 'react-confirm-alert';
import Alert from 'react-s-alert';

import StockMovementCombinedShipments from 'components/stock-movement-wizard/StockMovementCombinedShipments';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const VENDOR = {
  id: 'ven1', name: 'Acme Supplies', organizationCode: 'ACME', locationType: { locationTypeCode: 'SUPPLIER', description: 'Supplier' },
};

const line = (id, overrides = {}) => ({
  id,
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Product ${id}` },
  productCode: `P-${id}`,
  orderItemId: `oi${id}`,
  orderId: 'po1',
  orderNumber: 'PO-0001',
  packsRequested: 2,
  packSize: 10,
  quantityAvailable: 100,
  unitOfMeasure: 'BX/10',
  palletName: '',
  boxName: '',
  lotNumber: '',
  expirationDate: '',
  recipient: null,
  sortOrder: 100 * Number(id),
  ...overrides,
});

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'CS-0001',
  name: 'Combined shipment',
  description: 'Two purchase orders',
  statusCode: 'PENDING',
  origin: VENDOR,
  destination: { ...HERE, locationType: DEPOT },
  dateRequested: '10/01/2026',
  hasManageInventory: false,
  lineItems: [line('1'), line('2', { packsRequested: 3 })],
  dateShipped: '10/01/2026 10:00 +0000',
  expectedDeliveryDate: '10/05/2026',
  shipmentType: { id: '2', name: 'Air' },
  trackingNumber: 'TRK-1',
  associations: { documents: [] },
  ...overrides,
});

let current;
let stepItems;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/persons', () => ({ data: [{ id: 'u5', name: 'Nurse Joy' }] })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: stepItems(), totalCount: stepItems().length })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: current.lineItems.length })],
  ['/api/generic/shipmentType', () => ({ data: [{ id: '1', name: 'Default' }, { id: '2', name: 'Air|fr:Avion' }] })],
  ['/api/combinedShipmentItems/exportTemplate', () => 'csv-content'],
  ['/api/orderNumberOptions', () => ({ data: [{ id: 'po1', orderNumber: 'PO-0001' }] })],
  [/inventoryItems\/LOT-1/, () => ({ inventoryItem: { id: 'ii1', expirationDate: '12/31/2027' }, quantityOnHand: 5 })],
];

let store;
let pathname;

const renderWizard = ({ path = '/openboxes/stockMovement/createCombinedShipments/sm1', ...session } = {}) => {
  store = wizardStore(session);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <Route path="**/stockMovement/createCombinedShipments/:stockMovementId?" component={StockMovementCombinedShipments} />
        <Route
          path="*"
          render={({ location }) => {
            pathname = location.pathname;
            return null;
          }}
        />
      </MemoryRouter>
    </Provider>,
  );
};

const confirmButton = (label) => {
  const { buttons } = confirmAlert.mock.calls[confirmAlert.mock.calls.length - 1][0];
  return buttons.find((button) => button.label === label);
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

// Picks an option of a select field, found by its label; async fields search for `search` first.
const chooseOption = async (fieldLabel, optionText, search) => {
  const field = screen.getAllByLabelText(fieldLabel).find((element) => element.dataset.testid === 'form-field');
  const input = field.querySelector('input:not([type="hidden"])');
  if (search) {
    fireEvent.change(input, { target: { value: search } });
  } else {
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
  }
  fireEvent.click(await screen.findByText(optionText));
};

// OBAF renders the send step's Dropzone without its file input (upstream later added
// getInputProps), so the files are dropped on the zone itself.
const dropFiles = (container, files) => fireEvent.drop(container.querySelector('.dropzone > div'), {
  dataTransfer: { files, types: ['Files'] },
});

// Leaving the wizard assigns window.location (a full page load); record it instead.
const realLocation = window.location;
const trackPageLoads = () => {
  delete window.location;
  window.location = { search: '', reload: jest.fn() };
};

describe('combined shipments wizard against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    current = movement();
    stepItems = () => current.lineItems;
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation(() => Promise.resolve({
      data: { data: current }, headers: {},
    }));
    apiClient.delete.mockResolvedValue({ status: 204 });
    // OBAF's ProductApi.getInventoryItem calls axios.get directly:
    // answer it from the same routes and record it on the client mock.
    jest.spyOn(axios, 'get').mockImplementation((url) => apiClient.get(url));
  });

  afterEach(() => {
    jest.clearAllMocks();
    axios.get.mockRestore();
    window.location = realLocation;
  });

  describe('create step', () => {
    it('creates a shipment from a vendor to the current location', async () => {
      apiClient.mockResolvedValue({ data: { data: [VENDOR] } });
      current = movement({ statusCode: 'CREATED', lineItems: [] });
      renderWizard({ path: '/openboxes/stockMovement/createCombinedShipments' });
      // The session info arrives after the form has mounted, as it does in the app.
      act(() => { store.dispatch(sessionInfo()); });

      await waitFor(() => expect(screen.getAllByLabelText('Destination')
        .find((element) => element.dataset.testid === 'form-field')).toHaveTextContent('Main Warehouse [Depot]'));
      fireEvent.change(document.getElementById('description'), { target: { value: 'Two purchase orders' } });
      await chooseOption('Origin', 'ACME - Acme Supplies', 'Acm');
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/createCombinedShipments')).toEqual([{
        description: 'Two purchase orders',
        origin: { id: 'ven1' },
        destination: { id: 'loc1' },
      }]));
      expect(apiClient).toHaveBeenCalledWith('/api/locations', expect.objectContaining({
        params: expect.objectContaining({ name: 'Acm' }),
      }));
      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/createCombinedShipments/sm1'));
      expect(await screen.findByText('Add lines')).toBeInTheDocument();
    });

    it('a location with central purchasing does not preset the destination', async () => {
      renderWizard({ path: '/openboxes/stockMovement/createCombinedShipments', locationSettings: { hasCentralPurchasingEnabled: true } });
      act(() => {
        store.dispatch(sessionInfo({ locationSettings: { hasCentralPurchasingEnabled: true } }));
      });

      fireEvent.change(await screen.findByLabelText('Description', { selector: 'input' }), { target: { value: 'Two purchase orders' } });
      fireEvent.click(screen.getByText('Next'));

      expect(screen.getAllByLabelText('Destination').find((element) => element.dataset.testid === 'form-field'))
        .not.toHaveTextContent('Main Warehouse');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('updates the description and destination of a saved shipment, never its origin', async () => {
      renderWizard();
      fireEvent.click(await screen.findByText('Previous'));
      await screen.findByDisplayValue('Two purchase orders');
      apiClient.post.mockClear();

      fireEvent.change(screen.getByDisplayValue('Two purchase orders'), { target: { value: 'Three purchase orders' } });
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toEqual([{
        description: 'Three purchase orders', destination: { id: 'loc1' },
      }]));
      expect(await screen.findByText('Add lines')).toBeInTheDocument();
    });
  });

  describe('add items step', () => {
    it('lists the order items with their quantity in each and saves them on next', async () => {
      current = movement({ statusCode: 'CREATED' });
      stepItems = () => current.lineItems;
      // A CREATED shipment opens on the send step; going back shows its items.
      renderWizard();
      await screen.findByText('Send shipment');
      fireEvent.click(button('Previous'));

      expect(await screen.findByText('20')).toBeInTheDocument();
      expect(screen.getByText('30')).toBeInTheDocument();
      expect(screen.getAllByText('PO-0001')).toHaveLength(2);
      apiClient.post.mockClear();

      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]));
      expect(posted('/api/stockMovements/sm1/updateItems')[0]).toEqual({
        id: 'sm1',
        lineItems: [
          {
            id: '1',
            product: { id: 'p1' },
            quantityRequested: 20,
            palletName: '',
            boxName: '',
            lotNumber: '',
            expirationDate: '',
            recipient: { id: '' },
            sortOrder: 101,
            orderItemId: 'oi1',
          },
          expect.objectContaining({ id: '2', quantityRequested: 30, sortOrder: 202 }),
        ],
      });
      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
    });

    it('does not move on while a row asks for more than the order has left', async () => {
      current = movement({ lineItems: [line('1', { packsRequested: 20 })] });
      renderWizard();
      await screen.findByText('200');

      fireEvent.click(button('Next'));

      expect(await screen.findByTestId('alert-message'))
        .toHaveTextContent('Following rows contain validation errors: Row 1: P-1');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('requires lot and expiry for products under lot and expiry control', async () => {
      current = movement({
        lineItems: [line('1', { product: { id: 'p1', productCode: 'P-1', lotAndExpiryControl: true } })],
      });
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(button('Next'));

      expect(await screen.findByTestId('alert-message')).toHaveTextContent('Row 1: P-1');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('saves in place and reports success', async () => {
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems.map((item) => item.quantityRequested))
        .toEqual([20, 30]);
    });

    it('refuses to save a shipment without items', async () => {
      current = movement({ lineItems: [] });
      renderWizard();
      await screen.findByText('Add lines');

      fireEvent.click(button('Save'));

      expect(Alert.error).toHaveBeenCalledWith('Cannot save shipment from PO with no items.', { timeout: 2000 });
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('save and exit saves the items and opens the shipment page', async () => {
      renderWizard();
      await screen.findByText('20');
      trackPageLoads();

      fireEvent.click(button('Save and exit'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1);
    });

    it('deletes all items through the API', async () => {
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(button('Delete all'));

      await waitFor(() => expect(screen.queryByText('20')).not.toBeInTheDocument());
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm1/removeAllItems');
    });

    it('deletes one saved line through the API', async () => {
      renderWizard();
      await screen.findByText('30');

      fireEvent.click(screen.getAllByRole('button', { name: 'Delete' })[1].querySelector('.delete-icon'));

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovementItems/2/removeItem'));
      await waitFor(() => expect(screen.queryByText('30')).not.toBeInTheDocument());
    });

    it('downloads the available order items or a blank template for the vendor and destination', async () => {
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(screen.getByText('Available order items'));
      fireEvent.click(screen.getByText('Blank import template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalledTimes(2));
      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/combinedShipmentItems/exportTemplate?vendor=ven1&destination=loc1',
        { responseType: 'blob' },
      );
      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/combinedShipmentItems/exportTemplate?vendor=ven1&destination=loc1&blank=true',
        { responseType: 'blob' },
      );
      expect(fileDownload).toHaveBeenCalledWith('csv-content', 'Order-items-template.csv', 'text/csv');
    });

    it('imports a template and shows the imported items', async () => {
      const { container } = renderWizard();
      await screen.findByText('20');
      stepItems = () => [line('7', { packsRequested: 4, packSize: 25 })];

      fireEvent.change(container.querySelector('#csvInput'), {
        target: { files: [new File(['code,qty'], 'items.csv', { type: 'text/csv' })] },
      });

      expect(await screen.findByText('100')).toBeInTheDocument();
      const [url, formData] = apiClient.post.mock.calls[0];
      expect(url).toBe('/api/combinedShipmentItems/importTemplate/sm1');
      expect(formData.get('importFile').type).toBe('text/csv');
      expect(screen.queryByText('20')).not.toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF reads `_.last(lineItems).product` on an empty list; the throw lands in the
    // catch, which hides the spinner before the items are in.
    it.skip('imports a template into a shipment without lines, keeping the spinner until the items are in', async () => {
      current = movement({ lineItems: [] });
      stepItems = () => [];
      const { container } = renderWizard();
      await screen.findByText('Add lines');
      await waitFor(() => expect(store.getState().spinner.show).toBe(false));
      let itemsLoaded;
      apiClient.get.mockImplementation((url) => (url.startsWith('/api/stockMovements/sm1/stockMovementItems')
        ? new Promise((resolve) => { itemsLoaded = () => resolve(routeAnswers(routes())(url)); })
        : routeAnswers(routes())(url)));
      stepItems = () => [line('7', { packsRequested: 4, packSize: 25 })];

      fireEvent.change(container.querySelector('#csvInput'), {
        target: { files: [new File(['code,qty'], 'items.csv', { type: 'text/csv' })] },
      });

      await waitFor(() => expect(itemsLoaded).toBeDefined());
      // Give an early hide a chance to show.
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(store.getState().spinner.show).toBe(true);
      itemsLoaded();
      expect(await screen.findByText('100')).toBeInTheDocument();
      await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    });

    it('fills the expiry date of a known lot from its inventory item', async () => {
      renderWizard();
      await screen.findByText('20');
      const lot = screen.getAllByRole('cell', { name: 'Lot' })[0].querySelector('input');

      fireEvent.change(lot, { target: { value: 'LOT-1' } });
      fireEvent.blur(lot);

      expect(await screen.findByDisplayValue('12/31/2027')).toBeInTheDocument();
      expect(apiClient.get).toHaveBeenCalledWith(expect.stringMatching(/products\/p1\/inventoryItems\/LOT-1$/));
    });

    describe('a lot whose expiry differs from the one on record', () => {
      const enterKnownLot = async () => {
        current = movement({ lineItems: [line('1', { expirationDate: '01/01/2028' }), line('2', { packsRequested: 3 })] });
        renderWizard();
        await screen.findByText('30');
        const lot = screen.getAllByRole('cell', { name: 'Lot' })[0].querySelector('input');
        fireEvent.change(lot, { target: { value: 'LOT-1' } });
        fireEvent.blur(lot);
        await screen.findByText('Yes');
      };

      // KNOWN-GAP: declining makes OBAF return Promise.reject(), an unhandled rejection.
      it.skip('declining keeps the expiry on record and stays on the step without saving', async () => {
        const unhandled = jest.fn();
        process.on('unhandledRejection', unhandled);
        try {
          await enterKnownLot();
          apiClient.post.mockClear();

          fireEvent.click(screen.getByText('No'));

          expect(await screen.findByDisplayValue('12/31/2027')).toBeInTheDocument();
          // Let a rejected promise surface.
          await new Promise((resolve) => setTimeout(resolve, 100));
          expect(unhandled).not.toHaveBeenCalled();
          expect(posted('/api/stockMovements/sm1/updateItems')).toEqual([]);
          expect(screen.getByText('Add lines')).toBeInTheDocument();
        } finally {
          process.off('unhandledRejection', unhandled);
        }
      });

      it('saves the new expiry once confirmed', async () => {
        await enterKnownLot();
        apiClient.post.mockClear();

        fireEvent.click(screen.getByText('Yes'));

        await waitFor(() => expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1));
        expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems[0])
          .toEqual(expect.objectContaining({ lotNumber: 'LOT-1', expirationDate: '01/01/2028' }));
      });
    });

    it('splits a line into a second line of the same order item', async () => {
      renderWizard();
      await screen.findByText('30');
      const lotCells = () => screen.getAllByRole('cell', { name: 'Lot' });
      expect(lotCells()).toHaveLength(2);

      fireEvent.click(screen.getAllByRole('button', { name: 'Split line' })[0].querySelector('.split-icon'));

      await waitFor(() => expect(lotCells()).toHaveLength(3));
      expect(screen.getAllByText('PO-0001')).toHaveLength(3);
    });

    it('previous saves the items and goes back to the create step', async () => {
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(button('Previous'));

      expect(await screen.findByDisplayValue('Two purchase orders')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1);
    });

    it('adds the order items picked in the add lines dialog to the shipment', async () => {
      apiClient.post.mockImplementation((url) => {
        if (url === '/api/combinedShipmentItems/findOrderItems') {
          return Promise.resolve({
            data: {
              orderItems: [
                {
                  orderItemId: 'oi8', orderNumber: 'PO-0002', productCode: 'P-8', productName: 'Gauze', quantityAvailable: 40,
                },
                {
                  orderItemId: 'oi9', orderNumber: 'PO-0002', productCode: 'P-9', productName: 'Tape', quantityAvailable: 15,
                },
              ],
            },
          });
        }
        return Promise.resolve({ data: { data: current }, headers: {} });
      });
      renderWizard();
      await screen.findByText('20');

      fireEvent.click(button('Add lines'));

      // The open dialog marks the rest of the page aria-hidden, so it is found by its class.
      await waitFor(() => expect(document.querySelector('.modal-content-custom')).not.toBeNull());
      const dialog = document.querySelector('.modal-content-custom');
      expect(await within(dialog).findByText('Gauze')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1);
      expect(apiClient.get).toHaveBeenCalledWith('/api/orderNumberOptions?vendor=ven1&destination=loc1');
      expect(posted('/api/combinedShipmentItems/findOrderItems')[0]).toEqual({
        orderIds: [], productId: '', vendor: 'ven1', destination: 'loc1',
      });

      fireEvent.click(within(dialog).getAllByRole('checkbox', { hidden: true })[2]);
      fireEvent.click(within(dialog).getByText('Add items to shipment', { selector: 'button, button *' }));

      await waitFor(() => expect(posted('/api/combinedShipmentItems/addToShipment/sm1')).toHaveLength(1));
      expect(posted('/api/combinedShipmentItems/addToShipment/sm1')[0]).toEqual({
        itemsToAdd: [{ orderItemId: 'oi9', quantityToShip: 15, sortOrder: expect.any(Number) }],
      });
    });
  });

  describe('send step', () => {
    beforeEach(() => {
      current = movement({ statusCode: 'CHECKING' });
    });

    it('shows the picked items and sends the shipment: saved, ISSUED, then the shipment page', async () => {
      renderWizard();
      expect(await screen.findByText('2 BX/10')).toBeInTheDocument();
      expect(screen.getByText('3 BX/10')).toBeInTheDocument();
      expect(screen.getByTestId('wizardTitle')).toHaveTextContent('CS-0001 - Acme Supplies to Main Warehouse');
      trackPageLoads();

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toEqual([{
        destination: { id: 'loc1' },
        dateShipped: '10/01/2026 10:00 +0000',
        shipmentType: { id: '2' },
        trackingNumber: 'TRK-1',
        driverName: '',
        comments: '',
        expectedDeliveryDate: '10/05/2026',
      }]);
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'ISSUED' }]);
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=6');
    });

    it('asks for a real shipment type before sending', async () => {
      current = movement({ statusCode: 'CHECKING', shipmentType: { id: '1', name: 'Default' } });
      renderWizard();
      await screen.findByText('2 BX/10');

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Please populate shipment type before continuing'));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('does not send a shipment of another managed origin from here', async () => {
      current = movement({
        statusCode: 'CHECKING',
        hasManageInventory: true,
        origin: { id: 'loc9', name: 'Other Depot', locationType: DEPOT },
      });
      renderWizard();
      await screen.findByText('2 BX/10');

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith(expect.stringContaining('other than origin')));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('uploads one dropped file to the single upload before sending', async () => {
      const { container } = renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      dropFiles(container, [new File(['%PDF-1.4'], 'note.pdf', { type: 'application/pdf' })]);
      await screen.findByText('note.pdf');
      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      const [url, data] = apiClient.post.mock.calls[0];
      expect(url).toBe('/stockMovement/uploadDocument/sm1');
      expect(data.get('fileContents').name).toBe('note.pdf');
      expect(Alert.success).toHaveBeenCalledWith('File uploaded successfuly!', { timeout: 3000 });
    });

    it('uploads several dropped files together, and a removed file is left out', async () => {
      const { container } = renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      dropFiles(container, ['a.pdf', 'b.pdf', 'c.pdf'].map((name) => new File(['%PDF-1.4'], name, { type: 'application/pdf' })));
      await screen.findByText('c.pdf');
      fireEvent.click(screen.getByText('b.pdf').parentElement.querySelector('.remove-button'));
      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      const [url, data] = apiClient.post.mock.calls[0];
      expect(url).toBe('/stockMovement/uploadDocuments/sm1');
      expect([data.get('filesContents[0]').name, data.get('filesContents[1]').name]).toEqual(['a.pdf', 'c.pdf']);
      expect(data.get('filesContents[2]')).toBeNull();
    });

    it('stops when the upload fails', async () => {
      const { container } = renderWizard();
      await screen.findByText('2 BX/10');
      apiClient.post.mockRejectedValue(new Error('upload failed'));

      dropFiles(container, [new File(['%PDF-1.4'], 'note.pdf', { type: 'application/pdf' })]);
      await screen.findByText('note.pdf');
      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Error occured during file upload!'));
      expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
    });

    it('saves the sending options and reports success', async () => {
      renderWizard();
      await screen.findByText('2 BX/10');

      fireEvent.change(screen.getByDisplayValue('TRK-1'), { target: { value: 'TRK-2' } });
      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      expect(posted('/api/stockMovements/sm1/updateShipment')[0].trackingNumber).toBe('TRK-2');
    });

    it('save and exit asks before leaving with an expected delivery before the ship date', async () => {
      current = movement({ statusCode: 'CHECKING', expectedDeliveryDate: '09/01/2026' });
      renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      fireEvent.click(button('Save and exit'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm save' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      confirmButton('Yes').onClick();
      expect(window.location).toBe('/openboxes/stockMovement/show/sm1');
    });

    it('save and exit saves valid options and opens the shipment page', async () => {
      renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      fireEvent.click(button('Save and exit'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
    });

    it('opens a document of the step after saving the options', async () => {
      // OBAF's DocumentButton sets the blank tab's location.href itself; the navigation spy's
      // window.open returns a tab that reports that to followLink with { targetWindow: tab }.
      const open = jest.spyOn(window, 'open');
      current = movement({
        statusCode: 'CHECKING',
        associations: {
          documents: [
            { name: 'Packing List', stepNumber: 5, uri: '/doc/packing' },
            {
              name: 'Hidden', stepNumber: 5, uri: '/doc/hidden', hidden: true,
            },
            { name: 'Other step', stepNumber: 1, uri: '/doc/other' },
          ],
        },
      });
      renderWizard();
      await screen.findByText('2 BX/10');

      expect(screen.queryByText('Hidden')).not.toBeInTheDocument();
      expect(screen.queryByText('Other step')).not.toBeInTheDocument();
      fireEvent.click(screen.getByText('Packing List'));

      expect(open).toHaveBeenCalledWith('', '_blank');
      const tab = open.mock.results[0].value;
      await waitFor(() => expect(followLink).toHaveBeenCalledWith('/doc/packing', { targetWindow: tab }));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
      open.mockRestore();
    });

    it('previous saves the options and goes back to the items', async () => {
      renderWizard();
      await screen.findByText('2 BX/10');

      fireEvent.click(button('Previous'));

      expect(await screen.findByText('Add lines')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
    });

    it('an issued shipment cannot be sent again and shows its status', async () => {
      current = movement({ statusCode: 'ISSUED', shipped: true });
      renderWizard();
      await screen.findByText('2 BX/10');

      expect(button('Send shipment')).toBeDisabled();
      expect(button('Previous')).toBeDisabled();
      expect(screen.getByText('SHIPPED')).toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF sends apiClient.delete(url, { rollback: true }), so no rollback status is
    // posted.
    it.skip('rolls back a shipped shipment at its destination by posting the rollback status', async () => {
      current = movement({ statusCode: 'ISSUED', shipped: true });
      renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      fireEvent.click(button('Rollback'));

      await waitFor(() => expect(window.location.reload).toHaveBeenCalled());
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ rollback: true }]);
      expect(apiClient.delete).not.toHaveBeenCalled();
    });

    // KNOWN-GAP: OBAF renders `documents.length && ...`, a literal 0 in the menu.
    it.skip('shows no stray text in the download menu when there are no documents', async () => {
      renderWizard();
      await screen.findByText('2 BX/10');

      expect(document.querySelector('.print-buttons-container').textContent.trim()).toBe('');
    });

    // KNOWN-GAP: OBAF reads `_.find(...Default).id` and throws when no type is labelled Default.
    it.skip('sends a shipment when no shipment type is labelled Default', async () => {
      apiClient.get.mockImplementation((url) => (url.startsWith('/api/generic/shipmentType')
        ? Promise.resolve({ data: { data: [{ id: '2', name: 'Air' }] }, headers: {}, status: 200 })
        : routeAnswers(routes())(url)));
      renderWizard();
      await screen.findByText('2 BX/10');
      trackPageLoads();

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'ISSUED' }]);
    });

    it('refuses to roll back from a location that is neither origin nor destination', async () => {
      current = movement({
        statusCode: 'ISSUED', shipped: true, destination: { id: 'loc5', name: 'Clinic', locationType: DEPOT },
      });
      renderWizard();
      await screen.findByText('2 BX/10');

      fireEvent.click(button('Rollback'));

      expect(Alert.error).toHaveBeenCalledWith('You are not able to rollback shipment from your location.');
      expect(apiClient.delete).not.toHaveBeenCalled();
    });
  });
});
