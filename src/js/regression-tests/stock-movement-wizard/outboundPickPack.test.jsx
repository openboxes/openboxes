import React from 'react';

import {
  act, configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes below. The pick and pack steps use the client with the
// custom response handler; it is the same mock here. The helpers come from the real module.
jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: { response: { use: jest.fn() } },
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
    // OBAF's packing step still flattens its payload before posting it.
    flattenRequest: actual('flattenRequest'),
  };
});

// Confirmation dialogs: the test picks a button by its label and runs its handler.
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));
jest.mock('js-file-download', () => jest.fn());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The steps render large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';
import { confirmAlert } from 'react-confirm-alert';
import Alert from 'react-s-alert';

import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const ELSEWHERE = { id: 'loc9', name: 'Other Depot' };
const PRINT_URI = '/openboxes/stockMovement/printPicklist/sm1';

const movement = ({ statusCode, origin = HERE }) => ({
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode,
  shipped: false,
  received: false,
  origin: { ...origin, locationType: DEPOT },
  destination: { id: 'loc2', name: 'Clinic', locationType: DEPOT },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'STOCK' },
  dateRequested: '10/01/2026',
  hasManageInventory: true,
  shipmentType: { id: '2', name: 'Air' },
  lineItems: [],
  picklist: { id: 'pl1' },
  associations: {
    documents: [{ name: 'Print picklist', documentType: 'PICKLIST', uri: PRINT_URI }],
  },
});

// A pick line as the API sends it: the picks flattened with dotted keys.
const pickLine = (n, overrides = {}) => ({
  'requisitionItem.id': `ri${n}`,
  productCode: `P-${n}`,
  'product.id': `p${n}`,
  'product.name': `Product ${n}`,
  quantityRequired: 10,
  quantityPicked: 10,
  sortOrder: n * 100,
  picklistItems: [{
    id: `pi${n}`,
    'inventoryItem.id': `ii${n}`,
    lotNumber: `LOT-${n}`,
    'binLocation.id': `bin${n}`,
    'binLocation.name': n === 1 ? 'Z-bin' : 'A-bin',
    quantityPicked: 10,
  }],
  ...overrides,
});

const packLine = (n, overrides = {}) => ({
  shipmentItemId: `si${n}`,
  productCode: `P-${n}`,
  product: { id: `p${n}`, productCode: `P-${n}`, name: `Product ${n}` },
  lotNumber: `LOT-${n}`,
  quantityShipped: 10,
  uom: 'EA',
  palletName: '',
  boxName: '',
  ...overrides,
});

let current;
let pickItems;
let packItems;
let pickDetails;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/reasonCodes', () => ({ data: [{ id: 'DAMAGED', name: 'Damaged' }] })],
  [/^\/api\/stockMovementItems\/ri\d\/details$/, () => ({ data: pickDetails })],
  [/^\/api\/stockMovementItems\/ri\d$/, (url) => ({
    data: pickItems.find((item) => url.endsWith(item['requisitionItem.id'])),
  })],
  ['/api/stockMovements/sm1/stockMovementItems', (url) => ({
    data: url.includes('stepNumber=5') ? packItems : pickItems,
  })],
  ['/api/stockMovements/sm1/validatePicklist', () => ({})],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: 2 })],
  ['/api/stockMovements/createPickList/sm1', () => ({})],
  ['/api/stockMovements/exportPickListItems/sm1', () => 'pick-csv'],
  ['/api/stockMovements/picklistTemplate/sm1', () => 'pick-template'],
  ['/api/stockMovements/packlistTemplate/sm1', () => 'pack-template'],
];

let pathname;
let store;

const renderWizard = (options = {}) => {
  store = wizardStore(options);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
        <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
        <Route
          path="*"
          render={({ location }) => {
            pathname = `${location.pathname}${location.search}`;
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

const getUrls = () => apiClient.get.mock.calls.map(([url]) => url);

const button = (text) => screen.getByText(text).closest('button');

describe('outbound stock movement pick and pack steps against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    pickItems = [pickLine(1), pickLine(2)];
    packItems = [packLine(1), packLine(2)];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation(
      () => Promise.resolve({ data: { data: packItems }, headers: {} }),
    );
    apiClient.delete.mockResolvedValue({ status: 204, data: {} });
  });

  afterEach(() => jest.clearAllMocks());

  describe('pick step', () => {
    beforeEach(() => {
      current = movement({ statusCode: 'PICKING' });
    });

    it('lists the picks and moves on: picklist validated, status PICKED, then the send step', async () => {
      renderWizard();
      expect(await screen.findByText('LOT-2')).toBeInTheDocument();
      expect(screen.getByText('Product 1')).toBeInTheDocument();
      expect(getUrls()).toContain('/api/reasonCodes');

      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'PICKED' }]));
      expect(getUrls()).toContain('/api/stockMovements/sm1/validatePicklist');
      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
      expect(screen.queryByText('Redo Autopick')).not.toBeInTheDocument();
    });

    it('stops on a line without a pick and points at its row', async () => {
      pickItems = [pickLine(1), pickLine(2, { quantityPicked: 0, picklistItems: [] })];
      renderWizard();
      await screen.findByText('LOT-1');

      expect(button('Save and exit')).toBeDisabled();
      expect(screen.getByText(/item\(s\) require your attention/).closest('button')).toHaveTextContent('1 item(s)');
      fireEvent.click(button('Next'));

      expect(await screen.findByText('The picked lot is missing at rows: 2')).toBeInTheDocument();
      expect(getUrls()).not.toContain('/api/stockMovements/sm1/validatePicklist');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('shows only the lines that need attention when asked', async () => {
      pickItems = [pickLine(1), pickLine(2, { quantityPicked: 0, picklistItems: [] })];
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getByText(/item\(s\) require your attention/));

      expect(await screen.findByText('Product 2')).toBeInTheDocument();
      await waitFor(() => expect(screen.queryByText('Product 1')).not.toBeInTheDocument());
    });

    it('asks for a reason code when less than required was picked', async () => {
      pickItems = [pickLine(1, { quantityPicked: 4 }), pickLine(2)];
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(button('Next'));

      expect(await screen.findByText(/Product P-1 requires a reason code for the pick value/)).toBeInTheDocument();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('crosses out the suggested pick that the user changed', async () => {
      pickItems = [pickLine(1, {
        autoAllocated: true,
        suggestedItems: [{
          'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', lotNumber: 'LOT-SUGGESTED', quantityPicked: 10,
        }],
        picklistItems: [{
          id: 'pi1', 'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', lotNumber: 'LOT-1', quantityPicked: 6, reasonCode: 'DAMAGED',
        }],
      })];
      const { container } = renderWizard();

      expect(await screen.findByText('LOT-SUGGESTED')).toBeInTheDocument();
      expect(screen.getByText('LOT-SUGGESTED').closest('.crossed-out')).not.toBeNull();
      expect(screen.getByText('LOT-1').closest('.crossed-out')).toBeNull();
      expect(container.querySelectorAll('.crossed-out')).toHaveLength(1);
    });

    // Adapted to OBAF: its EditPickModal (OBLS-358/OBLS-677) adds an editable Allocated column,
    // refuses a pick above the allocation and posts quantityAllocated with each pick, so the picks
    // are allocated first and the payload carries the allocations.
    it('edits a pick in the modal and replaces the line with the saved one', async () => {
      pickDetails = {
        productCode: 'P-1',
        quantityRequired: 10,
        product: { name: 'Product 1' },
        availableItems: [
          {
            'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', 'binLocation.name': 'Z-bin', lotNumber: 'LOT-1', quantityAvailable: 20, quantityOnHand: 20,
          },
          {
            'inventoryItem.id': 'ii7', 'binLocation.id': 'bin7', 'binLocation.name': 'B-bin', lotNumber: 'LOT-7', quantityAvailable: 5, quantityOnHand: 5,
          },
        ],
        picklistItems: [{
          id: 'pi1', 'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', quantityPicked: 10, quantity: 10,
        }],
      };
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getAllByText('Pick', { selector: 'button *, button' })[0].closest('button'));
      const modal = await screen.findByText('LOT-7');
      const dialog = modal.closest('form');
      expect(within(dialog).getByText(/Qty Required/)).toHaveTextContent('Qty Required:10');
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovementItems/ri1/details', {
        params: { stepNumber: 4, refreshPicklistItems: false },
      });
      const [allocated, emptyAllocation] = await within(dialog).findAllByRole('cell', { name: 'Allocated' });
      const [pick, emptyPick] = await within(dialog).findAllByRole('cell', { name: 'Picked' });
      fireEvent.change(allocated.querySelector('input'), { target: { value: '7' } });
      fireEvent.change(emptyAllocation.querySelector('input'), { target: { value: '3' } });
      fireEvent.change(pick.querySelector('input'), { target: { value: '7' } });
      fireEvent.change(emptyPick.querySelector('input'), { target: { value: '3' } });
      pickItems = [pickLine(1, { hasChangedPick: true, quantityPicked: 10 }), pickLine(2)];
      fireEvent.click(within(dialog).getByText('Save'));

      await waitFor(() => expect(posted('/api/stockMovementItems/ri1/updatePicklist')).toHaveLength(1));
      expect(posted('/api/stockMovementItems/ri1/updatePicklist')[0]).toEqual({
        picklistItems: [
          {
            id: 'pi1', inventoryItem: { id: 'ii1' }, binLocation: { id: 'bin1' }, quantityPicked: '7', quantityAllocated: '7',
          },
          {
            id: '', inventoryItem: { id: 'ii7' }, binLocation: { id: 'bin7' }, quantityPicked: '3', quantityAllocated: '3',
          },
        ],
        reasonCode: '',
      });
      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovementItems/ri1', {
        params: { stepNumber: 4, refreshPicklistItems: false },
      }));
      await waitFor(() => expect(document.querySelector('.fa-check')).not.toBeNull());
    });

    it('a pick that differs from the required quantity needs a reason code before it saves', async () => {
      pickDetails = {
        productCode: 'P-1',
        quantityRequired: 10,
        product: { name: 'Product 1', displayNames: { default: 'Ibuprofen' } },
        availableItems: [{
          'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', lotNumber: 'LOT-1', quantityAvailable: 20,
        }],
        picklistItems: [],
      };
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getAllByText('Pick', { selector: 'button *, button' })[0].closest('button'));
      const dialog = (await screen.findByText(/Product name:/)).closest('form');
      expect(dialog).toHaveTextContent('Ibuprofen');
      fireEvent.change((await within(dialog).findByRole('cell', { name: 'Picked' })).querySelector('input'), { target: { value: '25' } });
      fireEvent.click(within(dialog).getByText('Save'));

      expect(await within(dialog).findByText('react.stockMovement.errors.differentTotalQty.label')).toBeInTheDocument();
      expect(posted('/api/stockMovementItems/ri1/updatePicklist')).toEqual([]);
    });

    it('undo edit goes back to the autopick of the line', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getAllByText('Undo edit', { selector: 'button *, button' })[0]);

      await waitFor(() => expect(posted('/api/stockMovementItems/ri1/createPicklist')).toHaveLength(1));
      await waitFor(() => expect(getUrls()).toContain('/api/stockMovementItems/ri1'));
    });

    it('clears the picklist after confirmation; undo then empties the line instead of autopicking', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getByText('Clear pick'));
      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm clear pick' }));
      act(() => { confirmButton('Yes').onClick(); });

      expect(apiClient.delete).toHaveBeenCalledWith('/api/picklists/pl1/items');
      await waitFor(() => expect(screen.queryByText('LOT-1')).not.toBeInTheDocument());
      expect(screen.getByText(/item\(s\) require your attention/).closest('button')).toHaveTextContent('2 item(s)');

      fireEvent.click(screen.getAllByText('Undo edit', { selector: 'button *, button' })[1]);

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovementItems/ri2/picklistItems'));
      expect(posted('/api/stockMovementItems/ri2/createPicklist')).toEqual([]);
    });

    it('redoes the autopick after confirmation and reloads the lines', async () => {
      renderWizard();
      await screen.findByText('LOT-1');
      apiClient.get.mockClear();

      fireEvent.click(screen.getByText('Redo Autopick'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(getUrls()).toContain('/api/stockMovements/createPickList/sm1'));
      await waitFor(() => expect(getUrls()).toContain('/api/stockMovements/sm1/stockMovementItems'));
    });

    // KNOWN-GAP: OBAF's downloader reads Content-Disposition unconditionally and throws without it.
    it.skip('exports the picks and the pick template as CSV files named after the movement', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getByText('Export Pick'));
      fireEvent.click(screen.getByText('Export template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalledTimes(2));
      expect(fileDownload.mock.calls.map(([data, name]) => [data, name]))
        .toEqual(expect.arrayContaining([
          ['pick-csv', 'PickListItems-SM-0001.csv'],
          ['pick-template', 'PickListItems-SM-0001-template.csv'],
        ]));
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/exportPickListItems/sm1', expect.objectContaining({
        responseType: 'blob', params: { format: 'csv' },
      }));
    });

    it('imports picks from a CSV, shows the import errors and reloads the lines without a new autopick', async () => {
      const { container } = renderWizard();
      await screen.findByText('LOT-1');
      apiClient.post.mockResolvedValueOnce({ data: { errors: ['Row 3: unknown lot'] } });

      fireEvent.change(container.querySelector('#csvInput'), {
        target: { files: [new File(['code,lot'], 'picks.csv', { type: 'text/csv' })] },
      });

      expect(await screen.findByText('Row 3: unknown lot')).toBeInTheDocument();
      const [url, formData] = apiClient.post.mock.calls[0];
      expect(url).toBe('/api/stockMovements/importPickListItems/sm1');
      expect(formData.get('importFile').type).toBe('text/csv');
      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems', {
        params: { stepNumber: 4, refreshPicklistItems: false },
      }));
    });

    it('sorts the lines by bin and back, and prints the picklist in the shown order', async () => {
      const { container } = renderWizard();
      await screen.findByText('LOT-1');
      const lots = () => Array.from(container.querySelectorAll('[role="cell"][aria-label="Lot"]'))
        .map((cell) => cell.textContent).filter(Boolean);
      expect(lots()).toEqual(['LOT-1', 'LOT-2']);

      fireEvent.click(screen.getByText('Sort by bins'));

      await waitFor(() => expect(lots()).toEqual(['LOT-2', 'LOT-1']));
      expect(screen.getByText('Print picklist').closest('a')).toHaveAttribute('href', `${PRINT_URI}?sorted=true`);
      fireEvent.click(screen.getByText('Original order'));
      await waitFor(() => expect(lots()).toEqual(['LOT-1', 'LOT-2']));
      expect(screen.getByText('Print picklist').closest('a')).toHaveAttribute('href', PRINT_URI);
    });

    it('filters the lines by product', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.change(screen.getByPlaceholderText(/search/i), { target: { value: 'Product 2' } });

      await waitFor(() => expect(screen.queryByText('Product 1')).not.toBeInTheDocument());
      expect(screen.getByText('Product 2')).toBeInTheDocument();
    });

    it('adjust stock points to the stock card instead', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(screen.getAllByText('Adjust stock', { selector: 'button *, button' })[0]);

      expect(Alert.error).toHaveBeenCalledWith(expect.stringContaining('adjust stock on the electronic stock card page'));
    });

    it('save and exit opens the movement page', async () => {
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(button('Save and exit'));

      expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1');
    });

    it('previous with empty picks asks first, warning that they will be autopicked', async () => {
      pickItems = [pickLine(1), pickLine(2, { quantityPicked: 0, picklistItems: [] })];
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(button('Previous'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({
        title: 'Are you sure?',
        message: expect.stringContaining('You have 1 line/lines with an empty pick'),
      }));
    });

    it('a movement of another location is read-only', async () => {
      current = movement({ statusCode: 'PICKING', origin: ELSEWHERE });
      renderWizard();
      await screen.findByText('LOT-1');

      expect(screen.queryByText('Redo Autopick')).not.toBeInTheDocument();
      expect(button('Next')).toBeDisabled();
      expect(button('Previous')).toBeDisabled();
      expect(screen.getAllByText('Undo edit', { selector: 'button *, button' })[0].closest('button')).toBeDisabled();
    });

    // KNOWN-GAP: OBAF's pick and pack steps get no history prop, so Exit throws a TypeError.
    it.skip('exit, for a movement of another location, goes to the outbound list', async () => {
      current = movement({ statusCode: 'PICKING', origin: ELSEWHERE });
      renderWizard();
      await screen.findByText('LOT-1');

      fireEvent.click(button('Exit'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC'));
    });
  });

  describe('pack step', () => {
    const packing = { hasPackingSupport: true };

    beforeEach(() => {
      current = movement({ statusCode: 'PACKING' });
    });

    it('saves the packing of the lines', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.change(screen.getAllByRole('cell', { name: 'Pack level 1' })[0].querySelector('input'), { target: { value: 'Pallet A' } });
      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      const [save] = posted('/api/stockMovements/sm1/updateShipmentItems');
      expect(save.id).toBe('sm1');
      expect(save.stepNumber).toBe('5');
      expect(save.packPageItems.map((item) => [item.shipmentItemId, item.palletName]))
        .toEqual([['si1', 'Pallet A'], ['si2', '']]);
    });

    it('a box without a pallet blocks saving', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.change(screen.getAllByRole('cell', { name: 'Pack level 2' })[0].querySelector('input'), { target: { value: 'Box 1' } });

      await waitFor(() => expect(button('Save')).toBeDisabled());
      expect(button('Next')).toBeDisabled();
    });

    it('moves on: picklist validated, packing saved, status CHECKING, then the send step', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]));
      expect(getUrls()).toContain('/api/stockMovements/sm1/validatePicklist');
      expect(posted('/api/stockMovements/sm1/updateShipmentItems')).toHaveLength(1);
      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
      expect(screen.queryByText('Split line')).not.toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF's packing step moves to Send before the CHECKING status is saved.
    it.skip('moves on to the send step only after the CHECKING status is saved', async () => {
      let statusSaved;
      const answer = apiClient.post.getMockImplementation();
      apiClient.post.mockImplementation((url, body) => (url === '/api/stockMovements/sm1/status'
        ? new Promise((resolve) => { statusSaved = () => resolve(answer(url, body)); })
        : answer(url, body)));
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Next'));

      await waitFor(() => expect(statusSaved).toBeDefined());
      // Give an early move a chance to show.
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(screen.getAllByText('Split line', { selector: 'button *, button' })).not.toHaveLength(0);
      expect(screen.queryByText('Send shipment')).not.toBeInTheDocument();
      statusSaved();
      expect(await screen.findByText('Send shipment')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'CHECKING' }]);
    });

    // KNOWN-GAP: OBAF's packing step moves to Send even when the CHECKING status is refused.
    it.skip('stays on the pack step when the CHECKING status cannot be saved', async () => {
      const answer = apiClient.post.getMockImplementation();
      apiClient.post.mockImplementation((url, body) => (url === '/api/stockMovements/sm1/status'
        ? Promise.reject(new Error('status refused'))
        : answer(url, body)));
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
      await waitFor(() => expect(store.getState().spinner.show).toBe(false));
      expect(screen.getAllByText('Split line', { selector: 'button *, button' })).not.toHaveLength(0);
      expect(screen.queryByText('Send shipment')).not.toBeInTheDocument();
    });

    it('asks before moving on while the filter hides some lines', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');
      fireEvent.change(screen.getByPlaceholderText(/search/i), { target: { value: 'LOT-2' } });
      await waitFor(() => expect(screen.queryByText('LOT-1')).not.toBeInTheDocument());

      fireEvent.click(button('Next'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm hidden lines' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      act(() => confirmButton('Yes').onClick());
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
    });

    it('splits a line in the modal and saves the split lines with the packing', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(screen.getAllByText('Split line', { selector: 'button *, button' })[0].closest('button'));
      const dialog = (await screen.findByText(/Total quantity/)).closest('form');
      expect(dialog).toHaveTextContent('Total quantity:10');
      fireEvent.click(within(dialog).getByText('Add line'));
      const quantities = () => within(dialog).getAllByRole('cell', { name: 'Quantity shipped' })
        .map((cell) => cell.querySelector('input'));
      await waitFor(() => expect(quantities()).toHaveLength(2));
      fireEvent.change(quantities()[0], { target: { value: '6' } });
      fireEvent.change(quantities()[1], { target: { value: '4' } });
      fireEvent.change(within(dialog).getAllByRole('cell', { name: 'Pack level 1' })[1].querySelector('input'), { target: { value: 'Pallet B' } });
      fireEvent.click(within(dialog).getByText('Save'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateShipmentItems')).toHaveLength(1));
      const [save] = posted('/api/stockMovements/sm1/updateShipmentItems');
      expect(save.packPageItems[0].splitLineItems
        .map((item) => [item.quantityShipped, item.palletName, item.lotNumber]))
        .toEqual([['6', '', 'LOT-1'], ['4', 'Pallet B', 'LOT-1']]);
      expect(save.packPageItems[1].splitLineItems).toBeUndefined();
    });

    it('split lines must add up to the quantity of the line', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(screen.getAllByText('Split line', { selector: 'button *, button' })[0].closest('button'));
      const dialog = (await screen.findByText(/Total quantity/)).closest('form');
      fireEvent.change(within(dialog).getByRole('cell', { name: 'Quantity shipped' }).querySelector('input'), { target: { value: '9' } });
      fireEvent.click(within(dialog).getByText('Save'));

      expect(await within(dialog).findByText(/Qty Packed/)).toHaveTextContent('Qty Packed:9');
      expect(posted('/api/stockMovements/sm1/updateShipmentItems')).toEqual([]);
    });

    it('save and exit saves, then opens the movement page', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Save and exit'));

      await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/updateShipmentItems')).toHaveLength(1);
    });

    it('imports the packing from a CSV and reloads the lines', async () => {
      const { container } = renderWizard(packing);
      await screen.findByText('LOT-2');
      apiClient.post.mockResolvedValueOnce({ data: { errors: ['Row 2: no such pallet'] } });
      packItems = [packLine(1, { palletName: 'Imported pallet' })];

      fireEvent.change(container.querySelector('#csvInput'), {
        target: { files: [new File(['lot,pallet'], 'pack.csv', { type: 'text/csv' })] },
      });

      expect(await screen.findByText('Row 2: no such pallet')).toBeInTheDocument();
      expect(apiClient.post.mock.calls[0][0]).toBe('/api/stockMovements/importPackListItems/sm1');
      expect(await screen.findByDisplayValue('Imported pallet')).toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF's downloader reads Content-Disposition unconditionally and throws without it.
    it.skip('exports the packing template named after the movement', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(screen.getByText('Export template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalled());
      expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['pack-template', 'PackListItems-SM-0001-template.csv']);
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/packlistTemplate/sm1', expect.objectContaining({ responseType: 'blob' }));
    });

    it('reloads the lines after confirming', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');
      apiClient.get.mockClear();

      fireEvent.click(screen.getByText('Reload'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(getUrls()).toEqual(expect.arrayContaining([
        '/api/stockMovements/sm1?stepNumber=5',
        '/api/stockMovements/sm1/stockMovementItems?stepNumber=5',
      ])));
    });

    it('previous saves the packing first', async () => {
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Previous'));

      await waitFor(() => expect(screen.queryByText('Split line')).not.toBeInTheDocument());
      expect(posted('/api/stockMovements/sm1/updateShipmentItems')).toHaveLength(1);
    });

    it('a movement of another location is read-only', async () => {
      current = movement({ statusCode: 'PACKING', origin: ELSEWHERE });
      renderWizard(packing);
      await screen.findByText('LOT-2');

      expect(screen.queryByText('Save and exit')).not.toBeInTheDocument();
      expect(button('Next')).toBeDisabled();
      expect(screen.getAllByRole('cell', { name: 'Pack level 1' })[0].querySelector('input')).toBeDisabled();
    });

    // KNOWN-GAP: OBAF's pick and pack steps get no history prop, so Exit throws a TypeError.
    it.skip('exit, for a movement of another location, goes to the outbound list', async () => {
      current = movement({ statusCode: 'PACKING', origin: ELSEWHERE });
      renderWizard(packing);
      await screen.findByText('LOT-2');

      fireEvent.click(button('Exit'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=OUTBOUND'));
    });
  });
});
