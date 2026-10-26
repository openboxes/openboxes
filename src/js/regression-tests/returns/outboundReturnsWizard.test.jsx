import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import Alert from 'react-s-alert';
import {
  answer, HERE, renderAt, sessionStore,
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
  default: { error: jest.fn(), success: jest.fn(), info: jest.fn() },
}));

/* eslint-disable import/first,import/order */
import OutboundReturnsWizard from 'components/returns/outbound/OutboundReturnsWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const PATH = '**/stockTransfer/createOutboundReturn/:outboundReturnId?';
const CLINIC = { id: 'loc2', name: 'Clinic' };

const picked = {
  id: 'pi1',
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  lotNumber: 'LOT-A',
  expirationDate: '12/31/2027',
  originZone: 'Zone 1',
  originBinLocation: { id: 'bin1', name: 'A-01' },
  quantity: 4,
};

const transferItem = {
  id: 'sti1',
  productAvailabilityId: 'pa1',
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  lotNumber: 'LOT-A',
  quantityOnHand: 10,
  quantityNotPicked: 6,
  quantity: 4,
  picklistItems: [picked],
};

const outboundReturn = (status, overrides = {}) => ({
  id: 'or1',
  stockTransferNumber: 'OR-0001',
  description: 'Expired stock back to supplier',
  status,
  origin: { id: HERE.id, name: HERE.name },
  destination: CLINIC,
  stockTransferItems: [transferItem],
  shipmentType: { id: 'st1', name: 'Air' },
  dateShipped: '10/02/2026',
  expectedDeliveryDate: '10/05/2026',
  documents: [{ documentType: 'PICKLIST', uri: '/openboxes/picklist/or1' }],
  ...overrides,
});

let current;

const routes = () => [
  ['/api/stockTransfers/or1', () => ({ data: current })],
  ['/api/internalLocations', {
    data: [
      { id: 'bin2', name: 'B-02', zoneName: 'Zone 1' },
      { id: 'bin1', name: 'A-01', zoneName: 'Zone 1' },
      { id: 'recv', name: 'Receiving' },
    ],
  }],
  ['/api/generic/shipmentType', { data: [{ id: 'st1', name: 'Air' }, { id: 'st2', name: 'Sea' }] }],
  ['/api/stockMovements/or1/documents', {
    data: [{ name: 'Picklist', documentType: 'PICKLIST', uri: '/openboxes/picklist/or1' }],
  }],
];

const renderWizard = (id = 'or1', store) => renderAt({
  component: OutboundReturnsWizard,
  path: PATH,
  entry: `/openboxes/stockTransfer/createOutboundReturn${id ? `/${id}` : ''}`,
  store,
});

// Form inputs carry their field name as id.
const field = (container, name) => container.querySelector(`[id="${name}"]`);

const putBody = () => apiClient.put.mock.calls[apiClient.put.mock.calls.length - 1][1];

describe('outbound return wizard', () => {
  beforeEach(() => {
    answer(apiClient.get, routes());
    apiClient.put.mockImplementation((url, body) => Promise.resolve({
      data: { data: { ...current, ...body } },
    }));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('create step', () => {
    it('prefills the origin with the current location and shows no title yet', async () => {
      renderWizard('');

      expect(await screen.findByText('Main Warehouse [Depot]')).toBeInTheDocument();
      expect(screen.queryByText('OR-0001')).not.toBeInTheDocument();
      expect(apiClient.get).not.toHaveBeenCalledWith('/api/stockTransfers/or1');
    });

    it('does not post while the description and destination are empty', async () => {
      renderWizard('');
      await screen.findByText('Main Warehouse [Depot]');

      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(screen.getAllByText('react.default.error.requiredField.label')).toHaveLength(2));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('creates the return with the location picked from the search and moves to add items', async () => {
      answer(apiClient, [['/api/locations', {
        data: [{ id: 'loc2', name: 'Clinic', locationType: { description: 'Ward' } }],
      }]]);
      current = outboundReturn('PENDING');
      apiClient.post.mockResolvedValue({ data: { data: current } });
      const { container, currentPath } = renderWizard('');
      await screen.findByText('Main Warehouse [Depot]');
      fireEvent.change(field(container, 'description'), { target: { value: 'Expired stock back to supplier' } });

      fireEvent.change(container.querySelector('[aria-label="Destination"] input'), { target: { value: 'Cli' } });
      fireEvent.click(await screen.findByText('Clinic [Ward]'));
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers', {
        description: 'Expired stock back to supplier',
        origin: { id: 'loc1' },
        destination: { id: 'loc2' },
        type: 'RETURN_ORDER',
      }));
      expect(apiClient).toHaveBeenCalledWith('/api/locations', expect.objectContaining({
        params: expect.objectContaining({ name: 'Cli' }),
      }));
      await waitFor(() => expect(currentPath()).toBe('/openboxes/stockTransfer/createOutboundReturn/or1'));
      expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    });
  });

  describe('opens an existing return on the step for its status', () => {
    it('shows the title with number, origin and destination', async () => {
      current = outboundReturn('APPROVED');
      renderWizard();

      expect(await screen.findByText('OR-0001')).toBeInTheDocument();
      expect(screen.getByText('Main Warehouse')).toBeInTheDocument();
      expect(screen.getByText('Clinic')).toBeInTheDocument();
    });

    it('a PENDING return lists its items, checked, on the add items step', async () => {
      current = outboundReturn('PENDING');
      const { container } = renderWizard();

      expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
      expect(screen.getByText('LOT-A')).toBeInTheDocument();
      // The quantity already picked counts as available again: 6 not picked + 4 picked.
      expect(container.querySelector('[role="cell"][aria-label="Qty Available to Return"]')).toHaveTextContent('10');
      expect(field(container, 'returnItems[0].quantity')).toHaveValue(4);
      // Bins come from the location, sorted, grouped by zone.
      expect(apiClient.get).toHaveBeenCalledWith('/api/internalLocations', expect.objectContaining({
        params: { locationTypeCode: ['BIN_LOCATION', 'INTERNAL'], 'location.id': 'loc1' },
      }));
    });

    it('an APPROVED return shows its picked lines and the picklist link', async () => {
      current = outboundReturn('APPROVED');
      renderWizard();

      expect(await screen.findByText('A-01')).toBeInTheDocument();
      expect(screen.getByText('Print picklist').closest('a')).toHaveAttribute('href', '/openboxes/picklist/or1');
    });

    it('a PLACED return opens on the send step with its shipping details', async () => {
      current = outboundReturn('PLACED', { trackingNumber: 'TRK-1' });
      const { container } = renderWizard();

      await waitFor(() => expect(field(container, 'trackingNumber')).toHaveValue('TRK-1'));
      expect(field(container, 'origin.name')).toHaveValue('Main Warehouse');
      expect(screen.getByText('Send shipment').closest('button')).not.toBeDisabled();
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/or1/documents');
    });

    it('stays on the first step when the return cannot be loaded', async () => {
      answer(apiClient.get, [['/api/stockTransfers/or1', new Error('Network Error')]]);
      renderWizard();

      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockTransfers/or1'));
      expect(await screen.findByText('Next')).toBeInTheDocument();
      expect(screen.queryByText('OR-0001')).not.toBeInTheDocument();
    });
  });

  describe('add items step', () => {
    beforeEach(() => {
      current = outboundReturn('PENDING');
    });

    it('searches return candidates by lot number and lists them', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          data: [{
            productAvailabilityId: 'pa2',
            product: { id: 'p2', productCode: 'P-0002', name: 'Paracetamol 500mg' },
            lotNumber: 'LOT-B',
            quantityOnHand: 8,
            quantityNotPicked: 8,
            onHold: true,
          }],
        },
      });
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');

      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'LOT-B' } });

      expect(await screen.findByText('Paracetamol 500mg')).toBeInTheDocument();
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/candidates', {
        productId: '',
        lotNumber: 'LOT-B',
        expirationDate: '',
        binLocationId: undefined,
        locationId: 'loc1',
      });
      // An on-hold candidate is marked as such; it is not selected yet.
      expect(container.querySelector('.on-hold')).not.toBeNull();
      expect(field(container, 'returnItems[0].quantity')).toHaveValue(null);
    });

    it('filters candidates by bin and lot together and marks recalled stock', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          data: [
            {
              productAvailabilityId: 'pa3', product: { id: 'p3', name: 'Amoxicillin' }, quantityNotPicked: 2, recalled: true,
            },
            {
              productAvailabilityId: 'pa4', product: { id: 'p4', name: 'Insulin' }, quantityNotPicked: 1, recalled: true, onHold: true,
            },
          ],
        },
      });
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');
      const binInput = screen.getByText('Select bin location...').closest('.react-select__control').querySelector('input');

      fireEvent.keyDown(binInput, { key: 'ArrowDown', keyCode: 40 });
      fireEvent.click(await screen.findByText('B-02'));

      expect(await screen.findByText('Amoxicillin')).toBeInTheDocument();
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/candidates', expect.objectContaining({
        binLocationId: 'bin2', productId: '', lotNumber: '',
      }));
      expect(container.querySelector('.recalled')).toHaveTextContent('Amoxicillin');
      expect(container.querySelector('.recalled-and-on-hold')).toHaveTextContent('Insulin');

      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'LOT' } });
      await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(2));
      expect(apiClient.post.mock.calls[1][1]).toEqual(expect.objectContaining({ binLocationId: 'bin2', lotNumber: 'LOT' }));
    });

    it('does not search for a lot number shorter than the minimum search length', async () => {
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');

      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'L' } });

      expect(container.querySelector('#selectedLotNumber')).toHaveValue('L');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('selecting a candidate fills its quantity, and Next saves the selection as APPROVED', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          data: [{
            productAvailabilityId: 'pa2',
            product: { id: 'p2', productCode: 'P-0002', name: 'Paracetamol 500mg' },
            quantityOnHand: 8,
            quantityNotPicked: 8,
          }],
        },
      });
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');
      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'LOT-B' } });
      await screen.findByText('Paracetamol 500mg');

      fireEvent.click(container.querySelector('input[type="checkbox"]'));
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(8));
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
      expect(apiClient.put.mock.calls[0][0]).toBe('/api/stockTransfers/or1');
      expect(putBody().status).toBe('APPROVED');
      expect(putBody().stockTransferItems.map((it) => [it.productAvailabilityId, it.quantity]))
        .toEqual([['pa1', 4], ['pa2', 8]]);
    });

    // KNOWN-GAP: OBAF stores quantityOnHand on selection, not the quantityNotPicked the field
    // shows.
    it.skip('selecting a partly picked candidate saves the quantity the field shows, not the quantity on hand', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          data: [{
            productAvailabilityId: 'pa2',
            product: { id: 'p2', productCode: 'P-0002', name: 'Paracetamol 500mg' },
            quantityOnHand: 10,
            quantityNotPicked: 6,
          }],
        },
      });
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');
      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'LOT-B' } });
      await screen.findByText('Paracetamol 500mg');

      fireEvent.click(container.querySelector('input[type="checkbox"]'));
      await waitFor(() => expect(field(container, 'returnItems[0].quantity')).toHaveValue(6));
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
      expect(putBody().stockTransferItems.map((it) => [it.productAvailabilityId, it.quantity]))
        .toEqual([['pa1', 4], ['pa2', 6]]);
    });

    // KNOWN-GAP: OBAF updates the row with the values from before the keystroke, so the line stays
    // unchecked.
    it.skip('typing a quantity keeps the typed value, checks the line and saves it', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          data: [{
            productAvailabilityId: 'pa2',
            product: { id: 'p2', productCode: 'P-0002', name: 'Paracetamol 500mg' },
            quantityOnHand: 8,
            quantityNotPicked: 8,
          }],
        },
      });
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');
      fireEvent.change(container.querySelector('#selectedLotNumber'), { target: { value: 'LOT-B' } });
      await screen.findByText('Paracetamol 500mg');

      fireEvent.change(field(container, 'returnItems[0].quantity'), { target: { value: '3' } });

      await waitFor(() => expect(container.querySelector('input[type="checkbox"]')).toBeChecked());
      expect(field(container, 'returnItems[0].quantity')).toHaveValue(3);
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
      expect(putBody().stockTransferItems.map((it) => [it.productAvailabilityId, it.quantity]))
        .toEqual([['pa1', 4], ['pa2', '3']]);
    });

    it('unchecking the only line clears its quantity and leaves nothing to save', async () => {
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');
      expect(screen.getByText('Next').closest('button')).not.toBeDisabled();

      fireEvent.click(container.querySelector('input[type="checkbox"]'));

      await waitFor(() => expect(screen.getByText('Next').closest('button')).toBeDisabled());
      expect(field(container, 'returnItems[0].quantity')).toHaveValue(null);
    });

    it('Previous saves the items without changing the status; the first step then updates the return', async () => {
      const { container } = renderWizard();
      await screen.findByText('Ibuprofen 200mg');

      fireEvent.click(screen.getByText('Previous'));

      await waitFor(() => expect(field(container, 'description')).toHaveValue('Expired stock back to supplier'));
      expect(putBody().status).toBe('PENDING');
      expect(putBody().stockTransferItems).toHaveLength(1);
      // The description of an existing return is fixed; Next updates it instead of creating one.
      expect(field(container, 'description')).toBeDisabled();

      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(2));
      expect(apiClient.put.mock.calls[1][0]).toBe('/api/stockTransfers/or1');
      expect(putBody()).toEqual(expect.objectContaining({ description: 'Expired stock back to supplier' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    });
  });

  describe('pick step', () => {
    it('Next places the return and moves to the send step', async () => {
      current = outboundReturn('APPROVED');
      renderWizard();
      await screen.findByText('A-01');

      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalledWith(
        '/api/stockTransfers/or1',
        expect.objectContaining({ status: 'PLACED', id: 'or1' }),
      ));
      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
    });

    it('a bin-less pick shows the default bin, and Previous returns to the add items step', async () => {
      current = outboundReturn('APPROVED', {
        documents: [],
        stockTransferItems: [{
          ...transferItem,
          picklistItems: [{ ...picked, originBinLocation: null }],
        }],
      });
      renderWizard();

      expect(await screen.findByText('DEFAULT')).toBeInTheDocument();
      expect(screen.getByText('Print picklist').closest('a')).toHaveAttribute('href', '/');

      fireEvent.click(screen.getByText('Previous'));

      expect(await screen.findByPlaceholderText('Enter lot number...')).toBeInTheDocument();
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

    it('saves the shipping details, sends the shipment and opens the movement page', async () => {
      current = outboundReturn('PLACED');
      apiClient.post.mockResolvedValue({ data: {} });
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'dateShipped')).toHaveValue('10/02/2026'));
      fireEvent.change(field(container, 'trackingNumber'), { target: { value: 'TRK-9' } });

      fireEvent.click(screen.getByText('Send shipment'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/or1'));
      expect(putBody()).toEqual(expect.objectContaining({
        shipmentType: { id: 'st1' },
        trackingNumber: 'TRK-9',
        driverName: '',
        comments: '',
      }));
      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/stockTransfers/or1/sendShipment',
        expect.objectContaining({ trackingNumber: 'TRK-9' }),
      );
    });

    it('does not send while the expected delivery date is before the ship date', async () => {
      current = outboundReturn('PLACED', { expectedDeliveryDate: '10/01/2026' });
      renderWizard();
      await screen.findByText('Send shipment');

      fireEvent.click(screen.getByText('Send shipment'));

      expect(await screen.findAllByText('react.stockMovement.error.deliveryDateBeforeShipDate.label')).not.toHaveLength(0);
      expect(apiClient.put).not.toHaveBeenCalled();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('Save stores the details and stays on the step', async () => {
      current = outboundReturn('PLACED');
      const { container } = renderWizard();
      await waitFor(() => expect(field(container, 'driverName')).not.toBeNull());
      fireEvent.change(field(container, 'driverName'), { target: { value: 'Sam' } });

      fireEvent.click(screen.getByText('Save'));

      await waitFor(() => expect(apiClient.put).toHaveBeenCalledWith(
        '/api/stockTransfers/or1',
        expect.objectContaining({ driverName: 'Sam', shipmentType: { id: 'st1' } }),
      ));
      await waitFor(() => expect(field(container, 'driverName')).toHaveValue('Sam'));
      expect(window.location).toEqual({ reload: expect.any(Function) });
    });

    it('save and exit with valid details saves and opens the movement page', async () => {
      current = outboundReturn('PLACED');
      renderWizard();
      await screen.findByText('Save and exit');

      fireEvent.click(screen.getByText('Save and exit'));

      await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/or1'));
      expect(apiClient.put).toHaveBeenCalledTimes(1);
    });

    it('save and exit with invalid details asks first and leaves without saving on yes', async () => {
      current = outboundReturn('PLACED', { dateShipped: null });
      renderWizard();
      await screen.findByText('Save and exit');

      fireEvent.click(screen.getByText('Save and exit'));
      fireEvent.click(await screen.findByText('Yes'));

      expect(window.location).toBe('/openboxes/stockMovement/show/or1');
      expect(apiClient.put).not.toHaveBeenCalled();
    });

    it('Previous with invalid details asks first and can go back losing the changes', async () => {
      current = outboundReturn('PLACED', { dateShipped: null });
      renderWizard();
      await screen.findByText('Send shipment');

      fireEvent.click(screen.getByText('Previous'));
      fireEvent.click(await screen.findByText('Continue (lose unsaved work)'));

      expect(await screen.findByText('Print picklist')).toBeInTheDocument();
      expect(apiClient.put).not.toHaveBeenCalled();
    });

    it('Previous with valid details saves them and goes back to the pick step', async () => {
      current = outboundReturn('PLACED');
      renderWizard();
      await screen.findByText('Send shipment');

      fireEvent.click(screen.getByText('Previous'));

      expect(await screen.findByText('Print picklist')).toBeInTheDocument();
      expect(apiClient.put).toHaveBeenCalledWith('/api/stockTransfers/or1', expect.objectContaining({ id: 'or1' }));
    });

    it('a sent return is read-only; an admin at the origin can roll it back', async () => {
      current = outboundReturn('COMPLETED');
      apiClient.post.mockResolvedValue({ data: {} });
      const { container } = renderWizard();
      await screen.findByText('Rollback');

      expect(screen.getByText('Send shipment').closest('button')).toBeDisabled();
      expect(screen.queryByText('Save and exit')).not.toBeInTheDocument();
      expect(field(container, 'trackingNumber')).toBeDisabled();

      fireEvent.click(screen.getByText('Rollback'));

      await waitFor(() => expect(window.location.reload).toHaveBeenCalled());
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockTransfers/or1/rollback');
    });

    it('refuses the rollback away from the origin', async () => {
      current = outboundReturn('COMPLETED', { origin: { id: 'loc7', name: 'Supplier' } });
      renderWizard('or1', sessionStore());
      await screen.findByText('Rollback');

      fireEvent.click(screen.getByText('Rollback'));

      expect(Alert.error).toHaveBeenCalledWith('You are not able to rollback shipment from your location.');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('Exit on a sent return opens the movement page', async () => {
      current = outboundReturn('COMPLETED');
      renderWizard();
      const exit = await screen.findByText('Exit');

      fireEvent.click(within(exit.closest('button')).getByText('Exit'));

      expect(window.location).toBe('/openboxes/stockMovement/show/or1');
    });
  });
});
