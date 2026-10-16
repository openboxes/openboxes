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

// The API: GETs answer from the routes below. The helpers the actions use come from the real
// module, loaded on first use.
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
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The wizard renders large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import { confirmAlert } from 'react-confirm-alert';
import Alert from 'react-s-alert';

import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const CLINIC = { id: 'loc2', name: 'Clinic', locationType: DEPOT };

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode: 'CREATED',
  origin: { ...HERE, locationType: DEPOT },
  destination: CLINIC,
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'ADHOC' },
  dateRequested: '10/01/2026',
  hasManageInventory: true,
  lineItems: [],
  lastUpdated: '2026-10-01T10:00:00Z',
  ...overrides,
});

const product = (code, overrides = {}) => ({
  id: `p${code}`, productCode: `P-${code}`, name: `Product ${code}`, ...overrides,
});

// An item of the add items step.
const line = (id, overrides = {}) => ({
  id,
  statusCode: 'PENDING',
  product: product(id),
  productCode: `P-${id}`,
  quantityRequested: 5,
  sortOrder: Number(id) * 100,
  ...overrides,
});

// An item of the edit step.
const editItem = (id, overrides = {}) => ({
  requisitionItemId: `ri${id}`,
  productCode: `P-${id}`,
  productName: `Product ${id}`,
  product: product(id),
  quantityRequested: 10,
  quantityOnHand: 50,
  quantityAvailable: 40,
  quantityDemandFulfilling: 3,
  substitutionStatus: 'NO',
  statusCode: 'APPROVED',
  ...overrides,
});

let current;
let items;
let editItems;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems?stepNumber=3', () => ({ data: editItems, totalCount: editItems.length })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: items, totalCount: items.length })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: (current.statusCode === 'CREATED' ? items : editItems).length })],
  [/^\/api\/stockMovementItems\/ri\d\?stepNumber=3$/, (url) => ({ data: editItems.find((item) => url.includes(item.requisitionItemId)) })],
  ['/api/getRequestTypes', () => ({ data: [{ id: 'ADHOC', name: 'Adhoc' }, { id: 'STOCK', name: 'Stock' }] })],
  ['/api/stocklists', () => ({ data: [{ id: 'sl1', name: 'Clinic list' }] })],
  ['/api/reasonCodes', () => ({ data: [{ id: 'STOCKOUT', name: 'Stock out' }, { id: 'OTHER', name: 'Other' }] })],
  ['/api/persons', () => ({ data: [{ id: 'u1', name: 'Admin' }] })],
];

let pathname;
let store;

const renderWizard = ({ path = '/openboxes/stockMovement/createOutbound/sm1', ...session } = {}) => {
  store = wizardStore(session);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
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

// Picks an option of a select field, found by its label; async fields search for `search` first.
const chooseOption = async (fieldLabel, optionText, { search, within: scope = screen } = {}) => {
  const field = scope.getAllByLabelText(fieldLabel).find((element) => element.dataset.testid === 'form-field');
  const input = field.querySelector('input:not([type="hidden"])');
  if (search) {
    fireEvent.change(input, { target: { value: search } });
  } else {
    // Opened with the mouse: in table rows the arrow keys move between cells.
    fireEvent.mouseDown(field.querySelector('.react-select__control'), { button: 0 });
  }
  const matches = await screen.findAllByText(optionText);
  fireEvent.click(matches[matches.length - 1]);
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

// The quantity inputs of the add items step, in row order.
const quantityInputs = () => screen.getAllByRole('cell', { name: 'Quantity' }).map((cell) => cell.querySelector('input'));

describe('outbound stock movement wizard steps against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    current = movement();
    items = [line('1'), line('2', { quantityRequested: 2 })];
    editItems = [editItem('1'), editItem('2', { quantityRequested: 60, quantityAvailable: 40 })];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation((url, body) => Promise.resolve({
      data: { data: { ...current, lineItems: items } },
      config: { data: JSON.stringify(body) },
      headers: {},
    }));
    apiClient.delete.mockResolvedValue({ status: 204 });
  });

  afterEach(() => jest.clearAllMocks());

  describe('create step', () => {
    // An existing movement opens on the add items step; Previous brings its header back.
    const backToCreateStep = async () => {
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));
      fireEvent.mouseDown(button('Previous'));
      await screen.findByText('Next');
      apiClient.post.mockClear();
    };

    it('saves the edited header of an existing movement and returns to the add items step', async () => {
      renderWizard();
      await backToCreateStep();

      fireEvent.change(screen.getByDisplayValue('Weekly restock'), { target: { value: 'Urgent restock' } });
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0]).toEqual({
        name: '',
        description: 'Urgent restock',
        dateRequested: '10/01/2026',
        origin: { id: 'loc1' },
        destination: { id: 'loc2' },
        requestedBy: { id: 'u1' },
        stocklist: { id: '' },
        requestType: undefined,
      });
      expect(apiClient.get).toHaveBeenCalledWith('/api/stocklists?origin=loc1&destination=loc2');
      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/createOutbound/sm1'));
      expect(await screen.findByText('Export template')).toBeInTheDocument();
    });

    it('rejects a request date in the future', async () => {
      current = movement({ dateRequested: '01/01/2099' });
      renderWizard();
      await backToCreateStep();

      fireEvent.click(screen.getByText('Next'));

      expect(await screen.findByText('react.stockMovement.error.futureDate.label')).toBeInTheDocument();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('asks before switching a saved movement to a stocklist; yes saves it', async () => {
      renderWizard();
      await backToCreateStep();

      await chooseOption('Stocklist', 'Clinic list');
      fireEvent.click(screen.getByText('Next'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm change' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      act(() => confirmButton('Yes').onClick());
      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0]).toEqual(expect.objectContaining({
        stocklist: { id: 'sl1' }, requestType: 'STOCK',
      }));
    });

    // KNOWN-GAP: OBAF reads newValues.type, so it asks for confirmation instead of saving.
    it.skip('switching a saved movement from one supplier to another saves without asking', async () => {
      const SUPPLIER = { locationTypeCode: 'SUPPLIER', description: 'Supplier' };
      current = movement({ origin: { id: 'sup1', name: 'Acme Supplies', locationType: SUPPLIER } });
      apiClient.mockResolvedValue({
        data: {
          data: [{
            id: 'sup2', name: 'Beta Supplies', locationType: SUPPLIER, supportedActivities: [],
          }],
        },
      });
      // Signed in at the supplier, so the movement can be edited.
      renderWizard({ location: { id: 'sup1', name: 'Acme Supplies' } });
      await backToCreateStep();

      await chooseOption('Origin', 'Beta Supplies [Supplier]', { search: 'Bet' });
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0].origin).toEqual({ id: 'sup2' });
      expect(confirmAlert).not.toHaveBeenCalled();
    });

    it('a new movement starts at the current location and looks up destinations to load stocklists', async () => {
      apiClient.mockResolvedValue({ data: { data: [{ ...CLINIC, supportedActivities: [] }] } });
      renderWizard({ path: '/openboxes/stockMovement/createOutbound' });
      // The session info arrives after the form has mounted, as it does in the app.
      act(() => { store.dispatch(sessionInfo()); });

      await waitFor(() => expect(screen.getAllByLabelText('Origin')
        .find((element) => element.dataset.testid === 'form-field')).toHaveTextContent('Main Warehouse [Depot]'));
      await chooseOption('Destination', 'Clinic [Depot]', { search: 'Cli' });

      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stocklists?origin=loc1&destination=loc2'));
      expect(apiClient).toHaveBeenCalledWith('/api/locations', expect.objectContaining({
        params: expect.objectContaining({ name: 'Cli' }),
      }));
      fireEvent.click(screen.getByText('Next'));
      // Requested by, request type and date are still missing.
      expect(await screen.findAllByText('react.default.error.requiredField.label')).not.toHaveLength(0);
      expect(apiClient.post).not.toHaveBeenCalled();
    });
  });

  describe('add items step', () => {
    it('saves the changed lines and moves the movement to REQUESTED, on to the edit step', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.change(quantityInputs()[1], { target: { value: '7' } });
      fireEvent.mouseDown(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'REQUESTED' }]));
      expect(posted('/api/stockMovements/sm1/updateItems')).toEqual([{
        id: 'sm1',
        lineItems: [{
          id: '2', product: { id: 'p2' }, quantityRequested: '7', recipient: { id: '' }, sortOrder: 200,
        }],
        removeEmptyItems: true,
      }]);
      expect(await screen.findByText('item(s) require your attention', { exact: false })).toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF calls nextPage at once, before the REQUESTED status is saved.
    it.skip('moves on to the edit step only once the REQUESTED status is saved', async () => {
      let statusSaved;
      const answer = apiClient.post.getMockImplementation();
      apiClient.post.mockImplementation((url, body) => (url === '/api/stockMovements/sm1/status'
        ? new Promise((resolve) => { statusSaved = () => resolve(answer(url, body)); })
        : answer(url, body)));
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.mouseDown(button('Next'));

      await waitFor(() => expect(statusSaved).toBeDefined());
      expect(screen.getByText('Export template')).toBeInTheDocument();
      statusSaved();
      expect(await screen.findByText('item(s) require your attention', { exact: false })).toBeInTheDocument();
      expect(screen.queryByText('Export template')).not.toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF moves on to the edit step even when the status save fails (and leaves
    // the rejection unhandled).
    it.skip('stays on the add items step when the REQUESTED status cannot be saved', async () => {
      const answer = apiClient.post.getMockImplementation();
      apiClient.post.mockImplementation((url, body) => (url === '/api/stockMovements/sm1/status'
        ? Promise.reject(new Error('status refused'))
        : answer(url, body)));
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.mouseDown(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
      await waitFor(() => expect(store.getState().spinner.show).toBe(false));
      expect(screen.getByText('Export template')).toBeInTheDocument();
    });

    it('asks before going on with zero quantity lines and goes on only after yes', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));
      fireEvent.change(quantityInputs()[1], { target: { value: '0' } });

      fireEvent.mouseDown(button('Next'));

      const dialog = confirmAlert.mock.calls[0][0];
      expect(dialog.title).toBe('Confirm save');
      dialog.afterClose();
      expect(apiClient.post).not.toHaveBeenCalled();
      act(() => confirmButton('Yes').onClick());
      dialog.afterClose();
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems)
        .toEqual([expect.objectContaining({ id: '2', quantityRequested: '0' })]);
    });

    it('warns about the same product on two lines and goes on after yes', async () => {
      items = [line('1'), line('2', { product: product('1'), productCode: 'P-1' })];
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.mouseDown(button('Next'));

      const dialog = confirmAlert.mock.calls[0][0];
      expect(dialog.title).toBe('You have entered the same code twice. Do you want to continue?');
      render(<div>{dialog.message}</div>);
      expect(screen.getAllByText('P-1 Product 1 5')).toHaveLength(2);
      act(() => confirmButton('Yes').onClick());
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
    });

    it('saves in place, then reloads the items and confirms', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));
      fireEvent.change(quantityInputs()[0], { target: { value: '9' } });
      apiClient.get.mockClear();

      fireEvent.mouseDown(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems)
        .toEqual([expect.objectContaining({ id: '1', quantityRequested: '9' })]);
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=2');
    });

    it('save and exit, without changes, opens the movement page', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.mouseDown(button('Save and exit'));

      await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
      expect(posted('/api/stockMovements/sm1/updateItems')).toEqual([]);
    });

    it('deletes all items through the API and leaves one empty line', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.click(button('Delete all'));

      await waitFor(() => expect(quantityInputs()).toHaveLength(1));
      expect(quantityInputs()[0]).toHaveValue(null);
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm1/removeAllItems');
    });

    it('deletes one saved line through the API', async () => {
      renderWizard();
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.mouseDown(screen.getAllByRole('button', { name: 'Delete' })[1]);
      fireEvent.click(screen.getAllByRole('button', { name: 'Delete' })[1]);

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/stockMovementItems/2/removeItem',
        { data: { stockMovementId: 'sm1' } },
      ));
    });

    it('autosave: an edited quantity is saved in the background', async () => {
      const autosave = sessionInfo();
      autosave.payload.data.data.isAutosaveEnabled = true;
      renderWizard();
      act(() => { store.dispatch(autosave); });
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));

      fireEvent.change(quantityInputs()[0], { target: { value: '12' } });

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateItems')[0]).toEqual({
        id: 'sm1',
        lineItems: [expect.objectContaining({ id: '1', quantityRequested: '12' })],
        removeEmptyItems: true,
      });
    });

    it('autosave: next waits while a line is still unsaved', async () => {
      const autosave = sessionInfo();
      autosave.payload.data.data.isAutosaveEnabled = true;
      apiClient.post.mockImplementation(() => new Promise(() => {}));
      renderWizard();
      act(() => { store.dispatch(autosave); });
      await waitFor(() => expect(quantityInputs()).toHaveLength(2));
      fireEvent.change(quantityInputs()[0], { target: { value: '12' } });

      fireEvent.mouseDown(button('Next'));

      expect(Alert.info).toHaveBeenCalledWith('Please wait while your line items are being saved.', expect.anything());
      expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
    });
  });

  describe('add items step of a stocklist movement', () => {
    beforeEach(() => {
      current = movement({ stocklist: { id: 'sl1', name: 'Clinic list' } });
      items = [line('1', { quantityRequested: 0, quantityAllowed: 20 }), line('2', { quantityAllowed: 10 })];
    });

    it('shows the max quantities and keeps the stocklist products fixed', async () => {
      renderWizard();

      await waitFor(() => expect(screen.getAllByRole('cell', { name: 'Needed Qty' })).toHaveLength(2));
      expect(screen.getAllByRole('cell', { name: 'Max Qty' }).map((cell) => cell.textContent)).toEqual(['20', '10']);
    });

    it('autosave: zero quantity lines of the stocklist are cleaned up as soon as it loads', async () => {
      const autosave = sessionInfo();
      autosave.payload.data.data.isAutosaveEnabled = true;
      renderWizard();
      act(() => { store.dispatch(autosave); });

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1));
      const [save] = posted('/api/stockMovements/sm1/updateItems');
      expect(save.removeEmptyItems).toBe(true);
      expect(save.lineItems).toContainEqual({
        id: '1', product: { id: 'p1' }, quantityRequested: 0, recipient: { id: '' }, sortOrder: 100,
      });
    });
  });

  describe('edit step', () => {
    beforeEach(() => {
      current = movement({ statusCode: 'REQUESTED' });
    });

    const rows = () => screen.getAllByRole('cell', { name: 'Code' }).map((cell) => cell.parentElement);

    it('flags the lines that cannot be fulfilled and can show only those', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));

      const toggle = await screen.findByText('item(s) require your attention', { exact: false });
      await waitFor(() => expect(toggle).toHaveTextContent('1 item(s) require your attention'));
      fireEvent.click(toggle);

      await waitFor(() => {
        expect(screen.queryByText('P-1')).not.toBeInTheDocument();
        expect(screen.getByText('P-2')).toBeInTheDocument();
      });
    });

    it('saves a revised quantity with its reason code', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));
      const secondRow = rows()[1];

      fireEvent.change(within(secondRow).getByRole('cell', { name: 'Qty revised' }).querySelector('input'), { target: { value: '30' } });
      await chooseOption('Reason code', 'Stock out', { within: within(secondRow) });
      fireEvent.click(button('Save'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([{
        lineItems: [{ id: 'ri2', quantityRevised: '30', reasonCode: 'STOCKOUT' }],
      }]));
      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
    });

    it('refuses to save a revised quantity without a reason code', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));

      fireEvent.change(within(rows()[0]).getByRole('cell', { name: 'Qty revised' }).querySelector('input'), { target: { value: '4' } });
      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalled());
      expect(Alert.error.mock.calls[0][0]).toBe('Error occurred in line:');
      expect(Alert.error.mock.calls[0][1].customFields.detailsArray)
        .toEqual(['1 - react.stockMovement.errors.reasonCodeRequired.label']);
      expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([]);
    });

    it('next starts picking with an automatic picklist for a REQUESTED movement', async () => {
      editItems = [editItem('1')];
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(1));

      await waitFor(() => expect(button('Next')).not.toBeDisabled());
      fireEvent.click(button('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status'))
        .toEqual([{ status: 'PICKING', createPicklist: 'true' }]));
    });

    it('undo reverts a changed line through the API and reloads it', async () => {
      editItems = [editItem('1', { statusCode: 'CHANGED', quantityRevised: 4, reasonCode: 'STOCKOUT' })];
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(1));
      editItems = [editItem('1')];

      const undo = () => screen.getAllByText('Undo').map((text) => text.closest('button')).find(Boolean);
      // Lines that were not changed have no visible undo button.
      expect(undo()).toBeVisible();

      fireEvent.click(undo());

      await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovementItems/ri1?stepNumber=3'));
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovementItems/ri1/revertItem');
      await waitFor(() => expect(undo()).not.toBeVisible());
    });

    it('reload asks first, then fetches the items again', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));
      apiClient.get.mockClear();

      fireEvent.click(screen.getByText('Reload'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(apiClient.get)
        .toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=3'));
    });

    it('previous with a line in error asks before leaving without saving', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));

      fireEvent.click(button('Previous'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Validation error' }));
      act(() => confirmButton('Continue (lose unsaved work)').onClick());
      expect(await screen.findByText('Export template')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([]);
    });

    it('save and exit opens the movement page', async () => {
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(2));

      fireEvent.click(button('Save and exit'));

      await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
    });

    it('previous returns to the add items step', async () => {
      editItems = [editItem('1')];
      renderWizard();
      await waitFor(() => expect(rows()).toHaveLength(1));

      fireEvent.click(button('Previous'));

      expect(await screen.findByText('Export template')).toBeInTheDocument();
    });
  });
});
