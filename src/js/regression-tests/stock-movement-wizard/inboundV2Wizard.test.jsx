import React from 'react';

import {
  configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes below; the location search calls the client directly.
jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  });
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));
jest.mock('js-file-download', () => jest.fn());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The wizard renders large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';
import Alert from 'react-s-alert';

import Inbound from 'components/stock-movement-wizard/inboundV2/Inbound';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const SUPPLIER = { locationTypeCode: 'SUPPLIER', description: 'Supplier' };

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'IN-0001',
  name: 'Inbound',
  description: 'Vendor delivery',
  statusCode: 'CREATED',
  displayStatus: { name: 'Pending' },
  origin: { id: 'sup1', name: 'Acme Supplies', locationType: SUPPLIER },
  destination: { ...HERE, locationType: DEPOT },
  requestedBy: { id: 'u1', name: 'Admin' },
  dateRequested: '09/15/2026',
  dateShipped: '2026-09-20T10:00:00',
  expectedDeliveryDate: '2026-09-25T00:00:00',
  shipmentType: { id: '2', name: 'Air', displayName: 'Air' },
  trackingNumber: 'TRK-1',
  driverName: '',
  comments: '',
  hasManageInventory: false,
  shipped: false,
  associations: { documents: [] },
  ...overrides,
});

const item = (id, overrides = {}) => ({
  id,
  statusCode: 'PENDING',
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Bandage ${id}` },
  quantityRequested: 5,
  lotNumber: `L${id}`,
  expirationDate: null,
  palletName: '',
  boxName: '',
  recipient: null,
  inventoryItem: null,
  sortOrder: 100,
  ...overrides,
});

let current;
let items;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: items })],
  ['/api/stockMovements/sm1', () => ({ data: current })],
  ['/api/stocklists', () => ({ data: [{ id: 'sl1', name: 'Vendor list' }] })],
  ['/api/generic/shipmentType', () => ({ data: [{ id: '1', name: 'Default' }, { id: '2', name: 'Air' }] })],
  ['/openboxes/stockMovement/exportCsv/sm1', () => 'csv-content'],
];

let location;

const renderWizard = (path) => render(
  <Provider store={wizardStore()}>
    <MemoryRouter initialEntries={[path]}>
      <Route path="**/stockMovement/createInbound/:stockMovementId?" component={Inbound} />
      <Route
        path="*"
        render={({ location: routerLocation }) => {
          location = `${routerLocation.pathname}${routerLocation.search}`;
          return null;
        }}
      />
    </MemoryRouter>
  </Provider>,
);

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

// Confirmation dialogs render into the document; this clicks one of their buttons.
const confirm = async (label) => {
  await waitFor(() => expect(document.querySelector('.react-confirm-alert')).not.toBeNull());
  fireEvent.click(within(document.querySelector('.react-confirm-alert')).getByText(label));
};

describe('inbound stock movement wizard (v2) against a mocked API', () => {
  beforeEach(() => {
    location = null;
    current = movement();
    items = [item('1'), item('2', { quantityRequested: 3, sortOrder: 200 })];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation((url) => Promise.resolve({
      data: { data: url.endsWith('/updateItems') ? { ...current, lineItems: items } : current },
    }));
    apiClient.delete.mockResolvedValue({ status: 204 });
  });

  afterEach(() => {
    jest.clearAllMocks();
    document.body.innerHTML = '';
  });

  describe('create step', () => {
    it('a new inbound starts at the current location and refuses to go on without the required fields', async () => {
      renderWizard('/openboxes/stockMovement/createInbound?direction=INBOUND');

      await waitFor(() => expect(screen.getByLabelText('Destination')).toHaveTextContent('Main Warehouse [Depot]'));
      fireEvent.click(button('Next'));

      expect((await screen.findAllByText('This field is required')).length).toBeGreaterThanOrEqual(3);
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(button('Next')).toBeDisabled();
    });

    it('loads an existing inbound into the form and saves it before moving to the items step', async () => {
      renderWizard('/openboxes/stockMovement/createInbound/sm1?direction=INBOUND');

      expect(await screen.findByDisplayValue('Vendor delivery')).toBeInTheDocument();
      expect(screen.getByLabelText('Origin')).toHaveTextContent('Acme Supplies [Supplier]');
      expect(screen.getByText('IN-0001')).toBeInTheDocument();
      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stocklists', {
        params: { origin: 'sup1', destination: 'loc1' },
      }));

      fireEvent.change(screen.getByDisplayValue('Vendor delivery'), { target: { value: 'Second delivery' } });
      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0]).toEqual(expect.objectContaining({
        name: '',
        description: 'Second delivery',
        dateRequested: '09/15/2026',
        origin: { id: 'sup1' },
        destination: { id: 'loc1' },
        requestedBy: { id: 'u1' },
        stocklist: null,
      }));
      await waitFor(() => expect(location).toBe('/openboxes/stockMovement/createInbound/sm1?direction=INBOUND&step=ADD_ITEMS'));
    });

    it('asks before changing the stocklist of a saved inbound; no puts the saved values back', async () => {
      renderWizard('/openboxes/stockMovement/createInbound/sm1');
      await screen.findByDisplayValue('Vendor delivery');
      const stocklist = screen.getByLabelText('Stocklist').querySelector('input:not([type="hidden"])');
      await waitFor(() => expect(stocklist).not.toBeDisabled());

      fireEvent.keyDown(stocklist, { key: 'ArrowDown', keyCode: 40 });
      fireEvent.click(await screen.findByText('Vendor list'));
      fireEvent.click(button('Next'));

      await screen.findByText('Confirm change');
      expect(apiClient.post).not.toHaveBeenCalled();
      await confirm('No');
      await waitFor(() => expect(screen.queryByText('Vendor list')).not.toBeInTheDocument());
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('a stocklist change confirmed with yes is saved', async () => {
      renderWizard('/openboxes/stockMovement/createInbound/sm1');
      await screen.findByDisplayValue('Vendor delivery');
      const stocklist = screen.getByLabelText('Stocklist').querySelector('input:not([type="hidden"])');
      await waitFor(() => expect(stocklist).not.toBeDisabled());

      fireEvent.keyDown(stocklist, { key: 'ArrowDown', keyCode: 40 });
      fireEvent.click(await screen.findByText('Vendor list'));
      fireEvent.click(button('Next'));
      await confirm('Yes');

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0].stocklist).toEqual({ id: 'sl1' });
    });

    it('an inbound that cannot be loaded sends the user to a new inbound', async () => {
      apiClient.get.mockImplementation((url) => (url === '/api/stockMovements/sm1'
        ? Promise.reject(new Error('Not found'))
        : routeAnswers(routes())(url)));
      renderWizard('/openboxes/stockMovement/createInbound/sm1');

      await waitFor(() => expect(location).toBe('/openboxes/stockMovement/createInbound?direction=INBOUND'));
    });
  });

  describe('add items step', () => {
    const ADD_ITEMS = '/openboxes/stockMovement/createInbound/sm1?step=ADD_ITEMS';

    it('lists the saved items and goes on: nothing changed, so only the status moves to CHECKING', async () => {
      renderWizard(ADD_ITEMS);

      expect(await screen.findByDisplayValue('L1')).toBeInTheDocument();
      expect(screen.getByDisplayValue('L2')).toBeInTheDocument();
      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]));
      expect(posted('/api/stockMovements/sm1/updateItems')).toEqual([]);
      await waitFor(() => expect(location).toBe('/openboxes/stockMovement/createInbound/sm1?step=SEND_SHIPMENT'));
    });

    it('saves a changed quantity before going on', async () => {
      renderWizard(ADD_ITEMS);
      const quantity = await screen.findByDisplayValue('3');

      fireEvent.change(quantity, { target: { value: '9' } });
      fireEvent.blur(quantity);
      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1));
      const [{ lineItems }] = posted('/api/stockMovements/sm1/updateItems');
      expect(lineItems).toEqual([expect.objectContaining({
        id: '2', product: { id: 'p2' }, quantityRequested: 9, lotNumber: 'L2', sortOrder: 200,
      })]);
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]));
    });

    it('does not move the status of an inbound past CREATED again', async () => {
      current = movement({ statusCode: 'CHECKING' });
      renderWizard(ADD_ITEMS);

      fireEvent.click(await screen.findByText('Next'));

      await waitFor(() => expect(location).toContain('step=SEND_SHIPMENT'));
      expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
    });

    it('asks before going on with a zero quantity line', async () => {
      renderWizard(ADD_ITEMS);
      const quantity = await screen.findByDisplayValue('3');
      fireEvent.change(quantity, { target: { value: '0' } });
      fireEvent.blur(quantity);

      fireEvent.click(button('Next'));
      await screen.findByText('Confirm save');
      await confirm('Yes');

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
    });

    it('asks about a product entered twice; no stays on the step, yes goes on', async () => {
      current = movement({
        origin: { id: 'loc5', name: 'Depot B', locationType: DEPOT }, hasManageInventory: true,
      });
      items = [item('1'), item('2', { product: { id: 'p1', productCode: 'P-1', name: 'Bandage 1' } })];
      renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L2');

      fireEvent.click(button('Next'));
      expect(await screen.findByText('You have entered the same code twice. Do you want to continue?')).toBeInTheDocument();
      fireEvent.click(button('No'));
      await waitFor(() => expect(screen.queryByText('You have entered the same code twice. Do you want to continue?'))
        .not.toBeInTheDocument());
      expect(posted('/api/stockMovements/sm1/status')).toEqual([]);

      fireEvent.click(button('Next'));
      await screen.findByText('You have entered the same code twice. Do you want to continue?');
      fireEvent.click(button('Yes'));
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]));
    });

    it('saves the items and confirms it', async () => {
      renderWizard(ADD_ITEMS);
      const quantity = await screen.findByDisplayValue('5');
      fireEvent.change(quantity, { target: { value: '6' } });
      fireEvent.blur(quantity);

      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', expect.anything()));
      expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems)
        .toEqual([expect.objectContaining({ id: '1', quantityRequested: 6 })]);
    });

    it('deletes all items through the API', async () => {
      renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L1');

      fireEvent.click(button('Delete All'));

      await waitFor(() => expect(screen.queryByDisplayValue('L1')).not.toBeInTheDocument());
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm1/removeAllItems');
    });

    it('exports the items as a CSV named after the inbound', async () => {
      renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L1');

      fireEvent.click(button('Export template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('csv-content', 'ItemList-IN-0001.csv', 'text/csv'));
      expect(apiClient.get).toHaveBeenCalledWith('/openboxes/stockMovement/exportCsv/sm1', { responseType: 'blob' });
    });

    it('imports a CSV and shows only the imported items', async () => {
      const { container } = renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L1');
      items = [item('9', { lotNumber: 'IMPORTED' })];

      fireEvent.change(container.querySelector('input[type="file"]'), {
        target: { files: [new File(['code,qty'], 'items.csv', { type: 'text/csv' })] },
      });

      expect(await screen.findByDisplayValue('IMPORTED')).toBeInTheDocument();
      expect(screen.queryByDisplayValue('L1')).not.toBeInTheDocument();
      const [url, formData] = apiClient.post.mock.calls.find(([called]) => called.includes('importCsv'));
      expect(url).toBe('/openboxes/stockMovement/importCsv/sm1');
      expect(formData.get('importFile').type).toBe('text/csv');
    });

    it('previous goes back to the create step', async () => {
      renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L1');

      fireEvent.click(button('Previous'));

      await waitFor(() => expect(location).toContain('step=CREATE'));
    });

    it('adds a new empty line', async () => {
      renderWizard(ADD_ITEMS);
      await screen.findByDisplayValue('L1');
      const lots = () => screen.getAllByLabelText('Lot').length;
      const before = lots();

      fireEvent.click(button('Add line'));

      await waitFor(() => expect(lots()).toBe(before + 1));
    });
  });

  describe('send step', () => {
    const SEND = '/openboxes/stockMovement/createInbound/sm1?step=SEND_SHIPMENT';

    it('sends the shipment: shipment details saved, then DISPATCHED', async () => {
      renderWizard(SEND);
      expect(await screen.findByDisplayValue('TRK-1')).toBeInTheDocument();
      await waitFor(() => expect(button('Send shipment')).not.toBeDisabled());

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'DISPATCHED' }]));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toEqual([expect.objectContaining({
        destination: { id: 'loc1' },
        shipmentType: '2',
        trackingNumber: 'TRK-1',
        driverName: '',
        comments: '',
        expectedDeliveryDate: '09/25/2026',
        dateShipped: expect.stringMatching(/^09\/20\/2026 10:00/),
      })]);
      expect(screen.getByText('Pending')).toBeInTheDocument();
    });

    it('does not send without a shipment type', async () => {
      current = movement({ shipmentType: { id: '1', name: 'Default' } });
      renderWizard(SEND);
      await screen.findByDisplayValue('TRK-1');

      fireEvent.click(button('Send shipment'));

      expect(await screen.findByText('This field is required')).toBeInTheDocument();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('saves the shipment details and confirms it', async () => {
      renderWizard(SEND);
      await screen.findByDisplayValue('TRK-1');
      await waitFor(() => expect(button('Save')).not.toBeDisabled());

      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', expect.anything()));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
    });

    it('previous saves the shipment details and goes back to the items step', async () => {
      renderWizard(SEND);
      await screen.findByDisplayValue('TRK-1');

      fireEvent.click(button('Previous'));

      await waitFor(() => expect(location).toContain('step=ADD_ITEMS'));
      expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
    });

    it('rolls back a sent shipment at its destination and reloads it', async () => {
      current = movement({ statusCode: 'DISPATCHED', shipped: true, displayStatus: { name: 'Shipped' } });
      renderWizard(SEND);
      await screen.findByDisplayValue('TRK-1');
      expect(button('Send shipment')).toBeDisabled();
      await waitFor(() => expect(button('Rollback')).not.toBeDisabled());
      apiClient.get.mockClear();

      fireEvent.click(button('Rollback'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ rollback: true }]));
      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1', expect.anything()));
    });

    it('refuses a rollback from a location that is neither end of the shipment', async () => {
      current = movement({
        statusCode: 'DISPATCHED', shipped: true, destination: { id: 'loc9', name: 'Elsewhere', locationType: DEPOT },
      });
      renderWizard(SEND);
      await screen.findByDisplayValue('TRK-1');
      await waitFor(() => expect(button('Rollback')).not.toBeDisabled());

      fireEvent.click(button('Rollback'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalled());
      expect(apiClient.post).not.toHaveBeenCalled();
    });
  });
});
