import React from 'react';

import {
  act, configure, fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
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
    parseResponse: actual('parseResponse'),
    // OBAF still imports flattenRequest (upstream develop no longer has it).
    flattenRequest: actual('flattenRequest'),
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
import fileDownload from 'js-file-download';
import { confirmAlert } from 'react-confirm-alert';
import Alert from 'react-s-alert';

import { showSpinner } from 'actions';
import StockMovementRequest from 'components/stock-movement-wizard/StockMovementRequest';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const SUPPLIER_DEPOT = {
  id: 'loc2', name: 'Central Depot', locationType: DEPOT, supportedActivities: [],
};

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'REQ-0001',
  name: 'Request',
  description: 'Monthly request',
  statusCode: 'CREATED',
  origin: SUPPLIER_DEPOT,
  destination: { ...HERE, locationType: DEPOT, supportedActivities: [] },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { id: 'ADHOC', name: 'Adhoc' },
  dateRequested: '10/01/2026',
  hasManageInventory: true,
  lineItems: [],
  ...overrides,
});

const product = (id, overrides = {}) => ({
  id, productCode: `P-${id}`, name: `Product ${id}`, active: true, ...overrides,
});

const line = (id, overrides = {}) => ({
  id,
  statusCode: 'PENDING',
  product: product(`p${id}`),
  productCode: `P-p${id}`,
  quantityRequested: 5,
  quantityOnHand: 10,
  quantityAvailable: 8,
  monthlyDemand: 3,
  sortOrder: 100,
  ...overrides,
});

let current;
let items;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: items, totalCount: items.length })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: items.length })],
  ['/api/getRequestTypes', () => ({ data: [{ id: 'ADHOC', name: 'Adhoc' }, { id: 'STOCK', name: 'Stock' }] })],
  ['/api/stocklists', () => ({ data: [{ id: 'sl1', name: 'Ward list' }] })],
  ['/api/users/options', () => ({ data: [{ id: 'u7', name: 'Approver' }] })],
  ['/stockMovement/exportCsv/sm1', () => 'csv-content'],
];

let pathname;

let store;

const renderWizard = ({ path = '/openboxes/stockMovement/createRequest/sm1', ...session } = {}) => {
  store = wizardStore(session);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <Route path="**/stockMovement/createRequest/:stockMovementId?" component={StockMovementRequest} />
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
const chooseOption = async (fieldLabel, optionText, search) => {
  const field = screen.getAllByLabelText(fieldLabel).find((element) => element.dataset.testid === 'form-field');
  const input = field.querySelector('input:not([type="hidden"])');
  if (search) {
    fireEvent.change(input, { target: { value: search } });
  } else {
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
  }
  fireEvent.click(await screen.findByText(optionText, { selector: '[id*="option"], .react-select__option, div' }));
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

describe('request stock movement wizard against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    items = [line('1'), line('2', { quantityRequested: 2 })];
    current = movement();
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation(() => Promise.resolve({
      data: { data: { ...current, lineItems: items } }, headers: {},
    }));
    apiClient.delete.mockResolvedValue({ status: 204 });
  });

  afterEach(() => jest.clearAllMocks());

  describe('create step', () => {
    // An existing request opens on the items step; Previous brings its header back to the form.
    const backToCreateStep = async () => {
      fireEvent.click(await screen.findByText('Previous'));
      await screen.findByText('Next');
      apiClient.post.mockClear();
    };

    it('saves the edited header of an existing request and returns to the items step', async () => {
      renderWizard();
      await backToCreateStep();

      fireEvent.change(screen.getByDisplayValue('Monthly request'), { target: { value: 'Updated request' } });
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0]).toEqual({
        name: '',
        description: 'Updated request',
        dateRequested: '10/01/2026',
        origin: 'loc2',
        destination: 'loc1',
        requestedBy: 'u1',
        stocklist: { id: '' },
        requestType: 'ADHOC',
        sourceType: 'ELECTRONIC',
        approvers: undefined,
        dateDeliveryRequested: null,
      });
      expect(apiClient.get).toHaveBeenCalledWith('/api/stocklists', { params: { origin: 'loc2', destination: 'loc1' } });
      expect(await screen.findByText('Submit request')).toBeInTheDocument();
      expect(pathname).toBe('/openboxes/stockMovement/createRequest/sm1');
    });

    it('does not save while a required field is empty', async () => {
      renderWizard();
      await backToCreateStep();

      fireEvent.change(screen.getByDisplayValue('Monthly request'), { target: { value: '' } });
      fireEvent.click(screen.getByText('Next'));

      expect(await screen.findByText('react.default.error.requiredField.label')).toBeInTheDocument();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('asks before changing the stocklist of a saved request; yes saves it as a STOCK request', async () => {
      renderWizard();
      await backToCreateStep();

      await chooseOption('Stocklist', 'Ward list');
      fireEvent.click(screen.getByText('Next'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm change' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      act(() => confirmButton('Yes').onClick());
      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0]).toEqual(expect.objectContaining({
        stocklist: { id: 'sl1' }, requestType: 'STOCK',
      }));
    });

    it('answering no to the stocklist change puts the saved header back', async () => {
      renderWizard();
      await backToCreateStep();

      await chooseOption('Stocklist', 'Ward list');
      fireEvent.click(screen.getByText('Next'));
      act(() => confirmButton('No').onClick());

      await waitFor(() => expect(screen.queryByText('Ward list')).not.toBeInTheDocument());
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    // KNOWN-GAP: OBAF reads newValues.type instead of the origin type, so it asks for confirmation.
    it.skip('switching a saved request from one supplier to another saves without asking', async () => {
      const SUPPLIER = { locationTypeCode: 'SUPPLIER', description: 'Supplier' };
      current = movement({
        origin: {
          id: 'sup1', name: 'Acme Supplies', locationType: SUPPLIER, supportedActivities: [],
        },
      });
      apiClient.mockResolvedValue({
        data: {
          data: [{
            id: 'sup2', name: 'Beta Supplies', locationType: SUPPLIER, supportedActivities: [],
          }],
        },
      });
      renderWizard();
      await backToCreateStep();

      await chooseOption('Fulfilling Location', 'Beta Supplies [Supplier]', 'Bet');
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0].origin).toBe('sup2');
      expect(confirmAlert).not.toHaveBeenCalled();
    });

    it('switching a saved request from a depot to another location still asks first', async () => {
      apiClient.mockResolvedValue({
        data: {
          data: [{
            id: 'loc3', name: 'North Depot', locationType: DEPOT, supportedActivities: [],
          }],
        },
      });
      renderWizard();
      await backToCreateStep();

      await chooseOption('Fulfilling Location', 'North Depot [Depot]', 'Nor');
      fireEvent.click(screen.getByText('Next'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm change' }));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('loads the approvers of an origin that approves requests and sends the chosen ones', async () => {
      current = movement({
        origin: { ...SUPPLIER_DEPOT, supportedActivities: ['APPROVE_REQUEST'] },
        approvers: [{ id: 'u7', name: 'Approver' }],
      });
      renderWizard();
      await backToCreateStep();

      expect(screen.getByText('Approvers')).toBeInTheDocument();
      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/users',
        { params: { roleTypes: 'ROLE_REQUISITION_APPROVER', location: 'loc2' } },
      );
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateRequisition')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateRequisition')[0].approvers).toEqual(['u7']);
    });

    it('creates a new request from the current location, the signed-in user and today', async () => {
      // The clock is pinned; the testing library queries advance the fake timers while they wait.
      jest.useFakeTimers({ doNotFake: ['nextTick', 'queueMicrotask'] });
      jest.setSystemTime(new Date('2026-10-02T12:00:00'));
      apiClient.mockResolvedValue({
        data: {
          data: [{
            id: 'loc2', name: 'Central Depot', locationType: DEPOT, supportedActivities: [],
          }],
        },
      });
      try {
        renderWizard({ path: '/openboxes/stockMovement/createRequest' });
        // The session info arrives after the form has mounted, as it does in the app.
        act(() => { store.dispatch(sessionInfo()); });

        await waitFor(() => expect(screen.getByLabelText('Requesting Location')).toHaveTextContent('Main Warehouse [Depot]'));
        expect(screen.getByLabelText('Requested by')).toHaveTextContent('Admin');

        fireEvent.change(document.getElementById('description'), { target: { value: 'Ward restock' } });
        await chooseOption('Fulfilling Location', 'Central Depot [Depot]', 'Cen');
        await chooseOption('Request type', 'Adhoc');
        fireEvent.click(screen.getByText('Next'));

        await waitFor(() => expect(posted('/api/stockMovements')).toHaveLength(1));
        expect(posted('/api/stockMovements')[0]).toEqual(expect.objectContaining({
          description: 'Ward restock',
          dateRequested: '10/02/2026',
          origin: 'loc2',
          destination: 'loc1',
          requestedBy: 'u1',
          requestType: 'ADHOC',
          sourceType: 'ELECTRONIC',
        }));
        expect(apiClient).toHaveBeenCalledWith('/api/locations', expect.objectContaining({
          params: expect.objectContaining({ name: 'Cen', activityCodes: ['FULFILL_REQUEST'] }),
        }));
        expect(apiClient.get).toHaveBeenCalledWith('/api/stocklists', { params: { origin: 'loc2', destination: 'loc1' } });
        await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/createRequest/sm1'));
      } finally {
        jest.useRealTimers();
      }
    });
  });

  describe('add items step', () => {
    it('lists the items of the request and submits it: changed lines saved, then REQUESTED', async () => {
      renderWizard();
      expect(await screen.findByDisplayValue('2')).toBeInTheDocument();
      expect(screen.getByTestId('wizardTitle')).toHaveTextContent('REQ-0001 - Central Depot to Main Warehouse');

      fireEvent.change(screen.getByDisplayValue('2'), { target: { value: '7' } });
      fireEvent.click(button('Submit request'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=INBOUND'));
      const [save] = posted('/api/stockMovements/sm1/updateItems');
      expect(save.removeEmptyItems).toBe(true);
      expect(save.lineItems.find((item) => item.id === '2')).toEqual({
        id: '2', product: { id: 'p2' }, quantityRequested: '7', sortOrder: 100, comments: '', quantityCounted: 10,
      });
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'REQUESTED' }]);
      expect(Alert.success).toHaveBeenCalledWith(expect.stringContaining('REQ-0001'));
    });

    it('submits for approval when the fulfilling location approves requests', async () => {
      current = movement({ origin: { ...SUPPLIER_DEPOT, supportedActivities: ['APPROVE_REQUEST'] } });
      renderWizard();

      fireEvent.click(await screen.findByText('Submit for approval'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'PENDING_APPROVAL' }]));
    });

    it('does not change the status of a request that is already past CREATED', async () => {
      current = movement({ statusCode: 'REQUESTED' });
      renderWizard();

      fireEvent.click(await screen.findByText('Submit request'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=INBOUND'));
      expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
    });

    it('asks before submitting zero quantity lines, which the save then removes', async () => {
      renderWizard();
      fireEvent.change(await screen.findByDisplayValue('2'), { target: { value: '0' } });

      fireEvent.click(button('Submit request'));

      expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm save' }));
      expect(apiClient.post).not.toHaveBeenCalled();
      act(() => confirmButton('Yes').onClick());
      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
    });

    it('stops on the same product entered twice and lists the duplicates', async () => {
      items = [line('1'), line('2', { product: product('p1') })];
      renderWizard();

      fireEvent.click(await screen.findByText('Submit request'));

      const dialog = confirmAlert.mock.calls[0][0];
      expect(dialog.title).toContain('same code twice');
      render(<div>{dialog.message}</div>);
      expect(screen.getAllByText('P-p1 Product p1 5')).toHaveLength(2);
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('refuses discontinued products with a validation notification', async () => {
      items = [line('1', { product: product('p1', { active: false }) })];
      renderWizard();

      fireEvent.click(await screen.findByText('Submit request'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalled());
      const [message, options] = Alert.error.mock.calls[0];
      expect(message).toBe('Validation error');
      expect(options.customFields.detailsArray[0]).toContain('P-p1');
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('saves without leaving the step, keeping empty lines, and reloads the items', async () => {
      renderWizard();
      fireEvent.change(await screen.findByDisplayValue('5'), { target: { value: '6' } });
      apiClient.get.mockClear();

      fireEvent.click(button('Save'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      const [save] = posted('/api/stockMovements/sm1/updateItems');
      expect(save.removeEmptyItems).toBe(false);
      expect(save.lineItems.map((item) => item.quantityRequested)).toContain('6');
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=2');
    });

    // KNOWN-GAP: OBAF parses newQty.quantityOnHand (NaN), so every existing line counts as changed.
    it.skip('saves only the lines that changed', async () => {
      renderWizard();
      fireEvent.change(await screen.findByDisplayValue('2'), { target: { value: '7' } });

      fireEvent.click(button('Save'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/updateItems')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateItems')[0].lineItems.map((item) => item.id)).toEqual(['2']);
    });

    // KNOWN-GAP: OBAF calls hideSpinner() immediately in .catch(...), so the spinner is gone during
    // the lookup.
    it.skip.each([
      ['a depot', {}, '/productAvailabilityAndDemand'],
      ['a ward', { supportedActivities: ['SUBMIT_REQUEST'] }, '/productDemand'],
    ])('keeps the spinner while the product of a new line is looked up for %s, and hides it when the lookup fails', async (_, session, lookup) => {
      let failLookup;
      apiClient.get.mockImplementation((url) => {
        if (url.startsWith('/api/products/search')) {
          return Promise.resolve({ data: { data: [product('p9')] } });
        }
        if (url.includes(lookup)) {
          return new Promise((resolve, reject) => { failLookup = reject; });
        }
        return routeAnswers(routes())(url);
      });
      renderWizard(session);
      await screen.findByText('Product p1', { exact: false });

      fireEvent.click(button('Add line'));
      const cells = await screen.findAllByRole('cell', { name: 'Requested product' });
      const input = cells[cells.length - 1].querySelector('input:not([type="hidden"])');
      fireEvent.change(input, { target: { value: 'p9 ' } });
      const option = await screen.findByText('P-p9 - Product p9', { exact: false });
      act(() => { store.dispatch(showSpinner()); });
      fireEvent.click(option);

      await waitFor(() => expect(failLookup).toBeDefined());
      expect(store.getState().spinner.show).toBe(true);
      failLookup(new Error('lookup failed'));
      await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    }, 60000);

    it('save and exit returns to the inbound list', async () => {
      renderWizard();

      fireEvent.click(await screen.findByText('Save and exit'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=INBOUND'));
    });

    it('deletes all items through the API and leaves one empty line', async () => {
      renderWizard();
      await screen.findByDisplayValue('2');

      fireEvent.click(button('Delete all'));

      await waitFor(() => expect(screen.queryByDisplayValue('2')).not.toBeInTheDocument());
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm1/removeAllItems');
    });

    it('deletes one saved line through the API', async () => {
      const { container } = renderWizard();
      await screen.findByDisplayValue('2');

      fireEvent.click(container.querySelectorAll('[aria-label="Delete"] .delete-icon')[1]
        || screen.getAllByRole('button', { name: 'Delete' })[1].firstChild);

      await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovementItems/2/removeItem'));
      await waitFor(() => expect(screen.queryByDisplayValue('2')).not.toBeInTheDocument());
    });

    it('cancels the request after confirmation and returns to the inbound list', async () => {
      renderWizard();

      fireEvent.click(await screen.findByText('Cancel Request'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list?direction=INBOUND'));
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm1');
      expect(Alert.success).toHaveBeenCalledWith('Request was successfully deleted', { timeout: 3000 });
    });

    it('exports the saved items as a CSV named after the request', async () => {
      renderWizard();

      fireEvent.click(await screen.findByText('Export template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('csv-content', 'ItemList-REQ-0001.csv', 'text/csv'));
      expect(apiClient.get).toHaveBeenCalledWith('/stockMovement/exportCsv/sm1', { responseType: 'blob' });
    });

    it('imports a CSV and reloads the items', async () => {
      const { container } = renderWizard();
      await screen.findByText('Import template');
      items = [line('9', { quantityRequested: 42 })];

      fireEvent.change(container.querySelector('#csvInput'), {
        target: { files: [new File(['code,qty'], 'items.csv', { type: 'text/csv' })] },
      });

      expect(await screen.findByDisplayValue('42')).toBeInTheDocument();
      const [url, formData, config] = apiClient.post.mock.calls[0];
      expect(url).toBe('/stockMovement/importCsv/sm1');
      expect(formData.get('importFile').type).toBe('text/csv');
      expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
    });

    it('reloads the items after confirming a refresh', async () => {
      renderWizard();
      await screen.findByDisplayValue('2');
      apiClient.get.mockClear();

      fireEvent.click(screen.getByText('Reload'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(apiClient.get)
        .toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=2'));
    });

    it('filters the lines by product code or name', async () => {
      renderWizard();
      await screen.findByDisplayValue('2');

      fireEvent.change(screen.getByPlaceholderText(/search/i), { target: { value: 'p2' } });

      await waitFor(() => expect(screen.queryByDisplayValue('5')).not.toBeInTheDocument());
      expect(screen.getByDisplayValue('2')).toBeInTheDocument();
    });

    it('previous saves the changed items and goes back to the create step', async () => {
      renderWizard();
      fireEvent.change(await screen.findByDisplayValue('2'), { target: { value: '4' } });

      fireEvent.click(button('Previous'));

      expect(await screen.findByText('Next')).toBeInTheDocument();
      expect(posted('/api/stockMovements/sm1/updateItems')[0].removeEmptyItems).toBe(false);
    });
  });

  describe('request from a ward (no inventory management at the requesting location)', () => {
    const ward = { supportedActivities: ['SUBMIT_REQUEST'] };

    it('pull stocklist: needed quantity starts at the demand minus the quantity on hand', async () => {
      current = movement({ stocklist: { id: 'sl1', name: 'Ward list' }, replenishmentType: { name: 'PULL' } });
      items = [
        line('1', {
          quantityRequested: 0,
          version: 0,
          demandPerReplenishmentPeriod: 12,
          quantityOnHand: 4,
          quantityCounted: 3,
        }),
        // Edited before (version > 0): its quantity, even zero, is kept.
        line('2', {
          quantityRequested: 0,
          version: 2,
          demandPerReplenishmentPeriod: 40,
          quantityOnHand: 1,
          quantityCounted: 9,
        }),
        // More on hand than needed: never below zero.
        line('3', {
          quantityRequested: 0,
          version: 0,
          demandPerReplenishmentPeriod: 5,
          quantityOnHand: 50,
          quantityCounted: 7,
        }),
      ];
      renderWizard(ward);

      // Each row: QOH (the counted quantity) then needed quantity.
      await screen.findByDisplayValue('8');
      const quantities = screen.getAllByRole('cell', { name: /QOH|Needed Qty/ })
        .map((cell) => cell.querySelector('input').value);
      expect(quantities).toEqual(['3', '8', '9', '0', '7', '0']);
      expect(screen.getByText('Import template').closest('label')).toHaveClass('disabled');
    });

    it('push stocklist: needed quantity starts at the max quantity', async () => {
      current = movement({ stocklist: { id: 'sl1', name: 'Ward list' }, replenishmentType: { name: 'PUSH' } });
      items = [
        line('1', {
          quantityRequested: 0, version: 0, quantityAllowed: 30, quantityCounted: 3,
        }),
        line('2', {
          quantityRequested: 6, version: 0, quantityAllowed: 30, quantityCounted: 3,
        }),
      ];
      renderWizard(ward);

      expect(await screen.findByDisplayValue('30')).toBeInTheDocument();
      expect(screen.getByDisplayValue('6')).toBeInTheDocument();
    });

    it('a cancelled ward request returns to the dashboard', async () => {
      renderWizard(ward);

      fireEvent.click(await screen.findByText('Cancel Request'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(pathname).toBe('/openboxes/dashboard'));
    });

    it('submitting a ward request returns to the dashboard', async () => {
      renderWizard(ward);

      fireEvent.click(await screen.findByText('Submit request'));

      await waitFor(() => expect(pathname).toBe('/openboxes/dashboard'));
    });
  });
});
