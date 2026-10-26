// OBAF-modified: the outbound pick step's Qty Allocated column, the editable Allocated column of
// the edit pick modal (prefill, totals, validation, saved payload), and mobile-picking aware
// crossing-out of changed suggestions. Set-up adapted from regression-tests/stock-movement-wizard/
// outboundPickPack.test.jsx (copied, not shared, so that file stays untouched).
import React from 'react';

import {
  configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';
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
// Tooltips render inline, exposing their title, so the row error messages can be read.
jest.mock('react-tippy', () => ({
  // eslint-disable-next-line react/prop-types
  Tooltip: ({ children, title }) => <div data-tooltip-title={title || undefined}>{children}</div>,
}));

// The steps render large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

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

let current;
let pickItems;
let pickDetails;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/reasonCodes', () => ({ data: [{ id: 'DAMAGED', name: 'Damaged' }] })],
  [/^\/api\/stockMovementItems\/ri\d\/details$/, () => ({ data: pickDetails })],
  [/^\/api\/stockMovementItems\/ri\d$/, (url) => ({
    data: pickItems.find((item) => url.endsWith(item['requisitionItem.id'])),
  })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({
    data: pickItems,
  })],
  ['/api/stockMovements/sm1/validatePicklist', () => ({})],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: 2 })],
  ['/api/stockMovements/createPickList/sm1', () => ({})],
  ['/api/stockMovements/exportPickListItems/sm1', () => 'pick-csv'],
  ['/api/stockMovements/picklistTemplate/sm1', () => 'pick-template'],
  ['/api/stockMovements/packlistTemplate/sm1', () => 'pack-template'],
];

let store;

const renderWizard = (options = {}) => {
  store = wizardStore(options);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
        <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
        <Route
          path="*"
          render={() => null}
        />
      </MemoryRouter>
    </Provider>,
  );
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const openEditModal = async (index = 0) => {
  fireEvent.click(screen.getAllByText('Pick', { selector: 'button *, button' })[index].closest('button'));
  return (await screen.findByText(/Product name:/)).closest('form');
};

const allocatedInputs = (dialog) => within(dialog).getAllByRole('cell', { name: 'Allocated' })
  .map((cell) => cell.querySelector('input'));
const pickedInputs = (dialog) => within(dialog).getAllByRole('cell', { name: 'Picked' })
  .map((cell) => cell.querySelector('input'));

describe('outbound pick step, OBAF allocation', () => {
  beforeEach(() => {
    pickItems = [pickLine(1, { quantityAllocated: 1234 }), pickLine(2)];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation(() => Promise.resolve({ data: { data: [] }, headers: {} }));
    current = movement({ statusCode: 'PICKING' });
  });

  afterEach(() => jest.clearAllMocks());

  it('shows a Qty Allocated column on the pick lines', async () => {
    renderWizard();
    await screen.findByText('LOT-1');

    expect(screen.getAllByText('Qty Allocated').length).toBeGreaterThan(0);
    expect(screen.getByText('1,234')).toBeInTheDocument();
  });

  describe('edit pick modal', () => {
    beforeEach(() => {
      pickDetails = {
        productCode: 'P-1',
        quantityRequired: 10,
        quantityAllocated: 10,
        product: { name: 'Product 1' },
        availableItems: [
          {
            'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', 'binLocation.name': 'Z-bin', lotNumber: 'LOT-1', quantityAvailable: 20, quantityOnHand: 20,
          },
          {
            'inventoryItem.id': 'ii7', 'binLocation.id': 'bin7', 'binLocation.name': 'B-bin', lotNumber: 'LOT-7', quantityAvailable: 5, quantityOnHand: 5,
          },
          {
            'inventoryItem.id': 'ii8', 'binLocation.id': 'bin8', 'binLocation.name': 'C-bin', lotNumber: 'LOT-8', quantityAvailable: 0, quantityOnHand: 0,
          },
        ],
        // Not yet picked (quantityPicked 0): Picked shows the quantity to pick, Allocated the
        // planned quantity.
        picklistItems: [{
          id: 'pi1', 'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', quantity: 8, quantityPicked: 0, quantityToPick: 8,
        }],
      };
    });

    it('prefills Allocated from the planned quantity and totals it', async () => {
      renderWizard();
      await screen.findByText('LOT-1');
      const dialog = await openEditModal();
      await within(dialog).findByText('LOT-7');

      const allocated = allocatedInputs(dialog);
      expect(allocated.map((input) => input.value)).toEqual(['8', '', '']);
      expect(pickedInputs(dialog)[0].value).toBe('8');
      expect(within(dialog).getByText(/Qty Allocated/)).toHaveTextContent('Qty Allocated:8');
      // A lot with nothing available and nothing allocated cannot be allocated.
      expect(allocated[2]).toBeDisabled();
      expect(allocated[1]).not.toBeDisabled();
    });

    it('prefills Allocated from the picked quantity once a reason code was given', async () => {
      pickDetails.picklistItems = [{
        id: 'pi1', 'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', quantity: 10, quantityPicked: 6, reasonCode: 'DAMAGED',
      }];
      renderWizard();
      await screen.findByText('LOT-1');
      const dialog = await openEditModal();
      await within(dialog).findByText('LOT-7');

      expect(allocatedInputs(dialog)[0].value).toBe('6');
      expect(pickedInputs(dialog)[0].value).toBe('6');
    });

    it('saves the allocated quantities with the picks', async () => {
      renderWizard();
      await screen.findByText('LOT-1');
      const dialog = await openEditModal();
      await within(dialog).findByText('LOT-7');

      fireEvent.change(allocatedInputs(dialog)[0], { target: { value: '7' } });
      fireEvent.change(pickedInputs(dialog)[0], { target: { value: '7' } });
      fireEvent.change(allocatedInputs(dialog)[1], { target: { value: '3' } });
      fireEvent.change(pickedInputs(dialog)[1], { target: { value: '3' } });
      expect(within(dialog).getByText(/Qty Allocated/)).toHaveTextContent('Qty Allocated:10');
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
          {
            id: '', inventoryItem: { id: 'ii8' }, binLocation: { id: 'bin8' }, quantityPicked: '', quantityAllocated: '',
          },
        ],
        reasonCode: '',
      });
    });

    // The row errors of the modal show as a has-error field whose tooltip carries the message.
    const fieldError = (input) => {
      const field = input.closest('[data-testid="form-field"]');
      if (!field.classList.contains('has-error')) {
        return null;
      }
      const titled = field.closest('[data-tooltip-title]');
      return titled ? titled.getAttribute('data-tooltip-title') : 'has-error';
    };

    it.each([
      ['allocating more than available', [['0', '0'], ['0p', '0'], ['1', '6']], '1', 'react.stockMovement.errors.higherThanAvailable.label'],
      ['a negative allocation', [['0', '-1']], '0', 'react.stockMovement.errors.negativeQtyAllocated.label'],
      ['picking more than allocated', [['0', '6'], ['0p', '7']], '0p', 'react.stockMovement.errors.pickedHigherThanAllocated.label'],
      ['allocating more than required in total', [['0', '10'], ['1', '5']], '1', 'react.stockMovement.errors.allocatedHigherThanRequired.label'],
      ['picking more than required in total', [['0', '10'], ['1', '5'], ['0p', '6'], ['1p', '5']], '1p', 'react.stockMovement.errors.pickedHigherThanRequired.label'],
    ])('refuses %s', async (name, edits, errorAt, error) => {
      renderWizard();
      await screen.findByText('LOT-1');
      const dialog = await openEditModal();
      await within(dialog).findByText('LOT-7');

      const inputAt = (target) => {
        const index = Number.parseInt(target, 10);
        return target.endsWith('p') ? pickedInputs(dialog)[index] : allocatedInputs(dialog)[index];
      };
      edits.forEach(([target, value]) => fireEvent.change(inputAt(target), { target: { value } }));
      fireEvent.click(within(dialog).getByText('Save'));

      await waitFor(() => expect(fieldError(inputAt(errorAt))).toBe(error));
      expect(posted('/api/stockMovementItems/ri1/updatePicklist')).toEqual([]);
    });
  });

  describe('suggested picks with mobile picking', () => {
    const suggested = (picklistItem) => pickLine(1, {
      autoAllocated: true,
      suggestedItems: [{
        'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', lotNumber: 'LOT-SUGGESTED', quantityPicked: 10,
      }],
      picklistItems: [{
        id: 'pi1', 'inventoryItem.id': 'ii1', 'binLocation.id': 'bin1', lotNumber: 'LOT-1', ...picklistItem,
      }],
    });

    it('compares the planned quantity, so an unchanged allocation is not crossed out', async () => {
      current = movement({
        statusCode: 'PICKING',
        origin: { ...HERE, supportedActivities: ['REQUIRE_MOBILE_PICKING'] },
      });
      pickItems = [suggested({ quantity: 10, quantityPicked: 0 })];
      const { container } = renderWizard();

      await screen.findByText('LOT-1');
      expect(screen.queryByText('LOT-SUGGESTED')).toBeNull();
      expect(container.querySelectorAll('.crossed-out')).toHaveLength(0);
    });

    it('crosses out the suggestion when the planned quantity changed', async () => {
      current = movement({
        statusCode: 'PICKING',
        origin: { ...HERE, supportedActivities: ['REQUIRE_MOBILE_PICKING'] },
      });
      pickItems = [suggested({ quantity: 6, quantityPicked: 0 })];
      renderWizard();

      expect(await screen.findByText('LOT-SUGGESTED')).toBeInTheDocument();
      expect(screen.getByText('LOT-SUGGESTED').closest('.crossed-out')).not.toBeNull();
    });

    it('without mobile picking, still compares the picked quantity', async () => {
      pickItems = [suggested({ quantity: 10, quantityPicked: 0 })];
      renderWizard();

      expect(await screen.findByText('LOT-SUGGESTED')).toBeInTheDocument();
      expect(screen.getByText('LOT-SUGGESTED').closest('.crossed-out')).not.toBeNull();
    });
  });
});
