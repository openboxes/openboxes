import {
  fireEvent, screen, waitFor,
} from '@testing-library/react';
import axios from 'axios';
import Alert from 'react-s-alert';
import {
  answer, HERE, renderAt,
} from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// OBAF still imports flattenRequest from utils/apiClient (upstream develop no longer has it).
jest.mock('utils/apiClient', () => ({
  // eslint-disable-next-line global-require
  ...require('regression-tests/__mocks__/screenApiClient').default(),
  flattenRequest: (...args) => jest.requireActual('utils/apiClient').flattenRequest(...args),
}));

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

jest.mock('react-s-alert', () => ({
  __esModule: true,
  default: {
    error: jest.fn(), success: jest.fn(), info: jest.fn(), warning: jest.fn(),
  },
}));

/* eslint-disable import/first,import/order */
import InboundReturnsWizard from 'components/returns/inbound/InboundReturnsWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const PATH = '**/stockTransfer/createInboundReturn/:inboundReturnId?';
const SUPPLIER = { id: 'loc7', name: 'Supplier' };

const ibuprofen = {
  id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg', lotAndExpiryControl: false,
};
const insulin = {
  id: 'p2', productCode: 'P-0002', name: 'Insulin', lotAndExpiryControl: true,
};

const line = (overrides = {}) => ({
  id: 'sti1',
  product: ibuprofen,
  lotNumber: 'LOT-A',
  expirationDate: '12/31/2027',
  quantity: 4,
  recipient: { id: 'u2', name: 'Nurse Joy' },
  sortOrder: 100,
  ...overrides,
});

const inboundReturn = (status, overrides = {}) => ({
  id: 'ir1',
  stockTransferNumber: 'IR-0001',
  description: 'Unused stock back from the ward',
  status,
  origin: SUPPLIER,
  destination: { id: HERE.id, name: HERE.name },
  stockTransferItems: [line()],
  shipmentType: { id: 'st1', name: 'Air|fr:Avion' },
  dateShipped: '10/02/2026',
  expectedDeliveryDate: '10/05/2026',
  ...overrides,
});

let current;
let inventoryExpiry;

const routes = () => [
  ['/api/stockTransfers/ir1', () => ({ data: current })],
  ['/api/persons', { data: [{ id: 'u2', name: 'Nurse Joy' }, { id: 'u3', name: 'Dr Who' }] }],
  ['/api/generic/shipmentType', { data: [{ id: 'st1', name: 'Air|fr:Avion' }, { id: 'st2', name: 'Sea' }] }],
  [(url) => url.includes('/inventoryItems/'), () => ({
    inventoryItem: inventoryExpiry ? { expirationDate: inventoryExpiry } : null,
  })],
];

const renderWizard = (id = 'ir1') => renderAt({
  component: InboundReturnsWizard,
  path: PATH,
  entry: `/openboxes/stockTransfer/createInboundReturn${id ? `/${id}` : ''}`,
});

// Form inputs carry their field name as id.
const field = (container, name) => container.querySelector(`[id="${name}"]`);

const posts = (url) => apiClient.post.mock.calls.filter(([called]) => called === url);

describe('inbound return wizard', () => {
  beforeEach(() => {
    inventoryExpiry = null;
    answer(apiClient.get, routes());
    // OBAF's ProductApi.getInventoryItem calls axios directly:
    // route it to the same mocked answers.
    jest.spyOn(axios, 'get').mockImplementation((...args) => apiClient.get(...args));
    const echo = (url, body) => Promise.resolve({ data: { data: { ...current, ...body } } });
    apiClient.post.mockImplementation(echo);
    apiClient.put.mockImplementation(echo);
    apiClient.delete.mockResolvedValue({ data: {} });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('create step', () => {
    it('prefills the destination with the current location and requires the rest', async () => {
      renderWizard('');
      await screen.findByText('Main Warehouse [Depot]');

      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(screen.getAllByText('react.default.error.requiredField.label')).toHaveLength(2));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('creates the return from an origin found among return locations and opens add items', async () => {
      answer(apiClient, [['/api/locations', {
        data: [{ id: 'loc7', name: 'Supplier', locationType: { description: 'Vendor' } }],
      }]]);
      current = inboundReturn('PENDING', { stockTransferItems: [] });
      const { container, currentPath } = renderWizard('');
      await screen.findByText('Main Warehouse [Depot]');
      fireEvent.change(field(container, 'description'), { target: { value: 'Unused stock back from the ward' } });

      fireEvent.change(container.querySelector('[aria-label="Origin"] input'), { target: { value: 'Sup' } });
      fireEvent.click(await screen.findByText('Supplier [Vendor]'));
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/', {
        description: 'Unused stock back from the ward',
        origin: { id: 'loc7' },
        destination: { id: 'loc1' },
        type: 'RETURN_ORDER',
      }));
      expect(apiClient).toHaveBeenCalledWith('/api/locations', expect.objectContaining({
        params: expect.objectContaining({ name: 'Sup', isReturnOrder: true }),
      }));
      await waitFor(() => expect(currentPath()).toBe('/openboxes/stockTransfer/createInboundReturn/ir1'));
      // A return without items starts with one empty line.
      expect(await screen.findByText('Add line')).toBeInTheDocument();
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(1));
    });
  });

  describe('add items step', () => {
    beforeEach(() => {
      current = inboundReturn('PENDING');
    });

    it('lists the items of the return with the title of the return', async () => {
      const { container } = renderWizard();

      await waitFor(() => expect(field(container, 'returnItems[0].lotNumber')).toHaveValue('LOT-A'));
      expect(screen.getByText('IR-0001')).toBeInTheDocument();
      expect(screen.getByText('Supplier')).toBeInTheDocument();
      expect(field(container, 'returnItems[0].quantity')).toHaveValue(4);
      expect(field(container, 'returnItems[0].expirationDate')).toHaveValue('12/31/2027');
      expect(apiClient.get).toHaveBeenCalledWith('/api/persons', { params: { status: true } });
    });

    it('saves the edited lines and reports success', async () => {
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(4));

      fireEvent.change(field(container, 'returnItems[0].quantity'), { target: { value: '7' } });
      fireEvent.click(screen.getByText('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', expect.anything()));
      const [[url, body]] = posts('/api/stockTransfers/ir1');
      expect(url).toBe('/api/stockTransfers/ir1');
      expect(body.status).toBe('PENDING');
      expect(body.stockTransferItems).toEqual([expect.objectContaining({ id: 'sti1', quantity: '7' })]);
      // The inventory item is checked before saving a line with lot and expiry.
      expect(apiClient.get).toHaveBeenCalledWith(expect.stringContaining('/products/p1/inventoryItems/LOT-A'));
    });

    it('asks before changing the expiry of an existing lot, and saves only when confirmed', async () => {
      inventoryExpiry = '06/30/2027';
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(4));

      // Declined from save and exit: nothing is saved and the page stays open.
      fireEvent.click(screen.getByText('Save and exit'));
      expect(await screen.findByText('Confirm save')).toBeInTheDocument();
      expect(screen.getByText('This will update the expiry date across all depots...')).toBeInTheDocument();
      fireEvent.click(screen.getByText('No'));

      await waitFor(() => expect(screen.queryByText('Confirm save')).not.toBeInTheDocument());
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(screen.getByText('Add line')).toBeInTheDocument();

      fireEvent.click(screen.getByText('Save'));
      fireEvent.click(await screen.findByText('Yes'));

      await waitFor(() => expect(posts('/api/stockTransfers/ir1')).toHaveLength(1));
    });

    it('flags lines that break the lot and expiry rules and blocks saving', async () => {
      current = inboundReturn('PENDING', {
        stockTransferItems: [
          line({
            id: 'sti1', product: insulin, lotNumber: '', expirationDate: '',
          }),
          line({ id: 'sti2', lotNumber: '', expirationDate: '12/31/2027' }),
          line({ id: 'sti3', quantity: '' }),
        ],
      });
      const { container } = renderWizard();
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(3));

      // Errors show once a field has been touched.
      ['returnItems[0].lotNumber', 'returnItems[1].lotNumber', 'returnItems[2].quantity'].forEach((name) => {
        fireEvent.blur(field(container, name));
      });

      await waitFor(() => expect(field(container, 'returnItems[0].lotNumber').closest('.has-error')).not.toBeNull());
      expect(field(container, 'returnItems[1].lotNumber').closest('.has-error')).not.toBeNull();
      expect(field(container, 'returnItems[2].quantity').closest('.has-error')).not.toBeNull();
      expect(screen.getByText('Save').closest('button')).toBeDisabled();
      expect(screen.getByText('Save and exit').closest('button')).toBeDisabled();
    });

    it('deletes a saved line through the API, and all lines at once', async () => {
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(4));

      fireEvent.click(container.querySelector('.delete-icon'));

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransferItems/sti1'));
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(0));

      fireEvent.click(screen.getByText('Add line'));
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(1));
      fireEvent.click(screen.getByText('Delete all'));

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransfers/ir1/removeAllItems'));
      // The return is loaded again after removing everything.
      await waitFor(() => expect(apiClient.get.mock.calls.filter(([url]) => url === '/api/stockTransfers/ir1').length).toBeGreaterThan(2));
    });

    it('a new line is removed without a request', async () => {
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(4));

      fireEvent.click(screen.getByText('Add line'));
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(2));
      fireEvent.click(container.querySelectorAll('.delete-icon')[1]);

      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(1));
      expect(apiClient.delete).not.toHaveBeenCalled();
    });

    it('the recipient header sets the recipient of every line', async () => {
      current = inboundReturn('PENDING', {
        stockTransferItems: [line({ recipient: null }), line({ id: 'sti2', recipient: null, sortOrder: 200 })],
      });
      const { container } = renderWizard();
      await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(2));
      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/persons', expect.anything()));
      const header = screen.getByText('Recipient', { selector: '.react-select__placeholder' });

      fireEvent.keyDown(header.closest('.react-select__control').querySelector('input'), { key: 'ArrowDown', keyCode: 40 });
      fireEvent.click(await screen.findByText('Dr Who'));
      fireEvent.click(screen.getByText('Save'));

      await waitFor(() => expect(posts('/api/stockTransfers/ir1')).toHaveLength(1));
      const [[, body]] = posts('/api/stockTransfers/ir1');
      expect(body.stockTransferItems.map((item) => item.recipient.id)).toEqual(['u3', 'u3']);
    });

    it('Next places the return and opens the send step', async () => {
      renderWizard();
      await screen.findByText('IR-0001');
      await waitFor(() => expect(screen.getByText('Next').closest('button')).not.toBeDisabled());

      fireEvent.click(screen.getByText('Next'));

      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
      const [[, body]] = posts('/api/stockTransfers/ir1');
      expect(body.status).toBe('PLACED');
    });

    describe('leaving the step', () => {
      let location;

      beforeEach(() => {
        ({ location } = window);
        delete window.location;
        window.location = {};
      });

      afterEach(() => {
        window.location = location;
      });

      it('save and exit saves and opens the movement page', async () => {
        const { container } = renderWizard();
        await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(4));

        fireEvent.click(screen.getByText('Save and exit'));

        await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/ir1'));
        expect(posts('/api/stockTransfers/ir1')).toHaveLength(1);
      });

      it('Previous with an invalid line can go back after dropping the empty lines', async () => {
        current = inboundReturn('PENDING', {
          stockTransferItems: [line(), line({ id: 'sti2', quantity: '', sortOrder: 200 })],
        });
        const { container } = renderWizard();
        await waitFor(() => expect(container.querySelectorAll('.table-row')).toHaveLength(2));

        fireEvent.click(screen.getByText('Previous'));
        fireEvent.click(await screen.findByText('Continue (lose unsaved work)'));
        // Lines without a quantity are dropped on save, after confirming.
        fireEvent.click(await screen.findByText('Yes'));

        await waitFor(() => expect(field(container, 'description')).toHaveValue('Unused stock back from the ward'));
        const [[, body]] = posts('/api/stockTransfers/ir1');
        expect(body.stockTransferItems.map((item) => item.id)).toEqual(['sti1']);
        expect(field(container, 'description')).toBeDisabled();

        // On an existing return, Next on the first step goes straight back to the items.
        fireEvent.click(screen.getByText('Next'));
        expect(await screen.findByText('Add line')).toBeInTheDocument();
        expect(posts('/api/stockTransfers/')).toHaveLength(0);
      });

      it('Previous with an invalid line stays to correct the error', async () => {
        current = inboundReturn('PENDING', { stockTransferItems: [line({ quantity: '' })] });
        renderWizard();
        await screen.findByText('IR-0001');

        fireEvent.click(screen.getByText('Previous'));
        fireEvent.click(await screen.findByText('Correct error'));

        await waitFor(() => expect(screen.queryByText('Correct error')).not.toBeInTheDocument());
        expect(screen.getByText('Add line')).toBeInTheDocument();
        expect(apiClient.post).not.toHaveBeenCalled();
      });
    });
  });

  describe('send step', () => {
    let location;

    beforeEach(() => {
      ({ location } = window);
      delete window.location;
      window.location = { reload: jest.fn() };
    });

    afterEach(() => {
      window.location = location;
    });

    it('shows the shipping details, then saves them and sends the shipment', async () => {
      current = inboundReturn('PLACED');
      apiClient.post.mockResolvedValue({ data: {} });
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'dateShipped')).toHaveValue('10/02/2026'));
      expect(field(container, 'origin.name')).toHaveValue('Supplier');
      // The English half of a bilingual shipment type name.
      expect(screen.getByText('Air')).toBeInTheDocument();
      expect(screen.getByText('Nurse Joy')).toBeInTheDocument();

      fireEvent.change(field(container, 'comments'), { target: { value: 'Fragile' } });
      fireEvent.click(screen.getByText('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/ir1'));
      expect(apiClient.put).toHaveBeenCalledWith('/api/stockTransfers/ir1', expect.objectContaining({
        comments: 'Fragile', shipmentType: { id: 'st1' }, trackingNumber: '', driverName: '',
      }));
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/ir1/sendShipment', expect.objectContaining({ comments: 'Fragile' }));
    });

    it('requires a ship date and blocks sending without one', async () => {
      current = inboundReturn('PLACED', { dateShipped: null });
      renderWizard();
      await screen.findByText('Send shipment');

      fireEvent.click(screen.getByText('Send shipment'));

      expect(await screen.findAllByText('react.default.error.requiredField.label')).not.toHaveLength(0);
      expect(apiClient.put).not.toHaveBeenCalled();
    });

    it('Save keeps the step open with the saved values', async () => {
      current = inboundReturn('PLACED');
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'driverName')).not.toBeNull());
      fireEvent.change(field(container, 'driverName'), { target: { value: 'Sam' } });

      fireEvent.click(screen.getByText('Save'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalledWith('/api/stockTransfers/ir1', expect.objectContaining({ driverName: 'Sam' })));
      await waitFor(() => expect(field(container, 'driverName')).toHaveValue('Sam'));
    });

    it('save and exit asks first when the details are invalid', async () => {
      current = inboundReturn('PLACED', { expectedDeliveryDate: '10/01/2026' });
      renderWizard();
      await screen.findByText('Save and exit');

      fireEvent.click(screen.getByText('Save and exit'));
      fireEvent.click(await screen.findByText('No'));

      expect(window.location).toEqual({ reload: expect.any(Function) });
      expect(apiClient.put).not.toHaveBeenCalled();
    });

    it('Previous saves valid details and returns to the items', async () => {
      current = inboundReturn('PLACED');
      renderWizard();
      await screen.findByText('Send shipment');

      fireEvent.click(screen.getByText('Previous'));

      expect(await screen.findByText('Add line')).toBeInTheDocument();
      expect(apiClient.put).toHaveBeenCalledWith('/api/stockTransfers/ir1', expect.objectContaining({ id: 'ir1' }));
    });

    it('a received return can be rolled back only at its destination', async () => {
      current = inboundReturn('COMPLETED');
      apiClient.post.mockResolvedValue({ data: {} });
      renderWizard();
      fireEvent.click(await screen.findByText('Rollback'));

      await waitFor(() => expect(window.location.reload).toHaveBeenCalled());
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/ir1/rollback');
    });

    it('refuses the rollback away from the destination', async () => {
      current = inboundReturn('COMPLETED', { destination: { id: 'loc9', name: 'Other Depot' } });
      renderWizard();
      fireEvent.click(await screen.findByText('Rollback'));

      expect(Alert.error).toHaveBeenCalledWith('You are not able to rollback shipment from your location.');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('Exit on a received return opens the movement page', async () => {
      current = inboundReturn('COMPLETED');
      renderWizard();

      fireEvent.click(await screen.findByText('Exit'));

      expect(window.location).toBe('/openboxes/stockMovement/show/ir1');
    });
  });
});
