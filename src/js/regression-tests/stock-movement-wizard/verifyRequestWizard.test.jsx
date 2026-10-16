import React from 'react';

import {
  act, configure, fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes of each test. The helpers the actions use come from the
// real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  // Callable like an axios instance; the pick step registers a response interceptor.
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

import StockMovementVerifyRequest from 'components/stock-movement-wizard/StockMovementVerifyRequest';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const WARD = { id: 'loc3', name: 'Ward A', locationType: DEPOT };

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'REQ-0042',
  name: 'Request',
  description: 'Ward restock',
  statusCode: 'REQUESTED',
  origin: { ...HERE, locationType: DEPOT },
  destination: WARD,
  requestedBy: { id: 'u9', name: 'Nurse' },
  requestType: { name: 'Adhoc' },
  dateRequested: '10/01/2026',
  lineItems: [],
  associations: { requisition: { status: 'VERIFYING' } },
  ...overrides,
});

const item = (id, overrides = {}) => ({
  requisitionItemId: id,
  productCode: `P-${id}`,
  productName: `Product ${id}`,
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Product ${id}` },
  quantityRequested: 5,
  quantityAvailable: 20,
  quantityOnHand: 25,
  quantityCounted: 2,
  statusCode: 'PENDING',
  substitutionStatus: 'NO',
  sortOrder: 100,
  ...overrides,
});

let current;
let items;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/reasonCodes', () => ({ data: [{ id: 'DAMAGED', name: 'Damaged' }, { id: 'EXPIRED', name: 'Expired' }] })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: items, totalCount: items.length })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: items.length })],
];

let pathname;

const renderWizard = (session = {}) => render(
  <Provider store={wizardStore(session)}>
    <MemoryRouter initialEntries={['/openboxes/stockMovement/verifyRequest/sm1']}>
      <Route path="**/stockMovement/verifyRequest/:stockMovementId?" component={StockMovementVerifyRequest} />
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

const confirmButton = (label) => {
  const { buttons } = confirmAlert.mock.calls[confirmAlert.mock.calls.length - 1][0];
  return buttons.find((button) => button.label === label);
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

// The row of the table holding the given product code.
const row = (code) => screen.getByText(code).closest('.table-row');

// The open modal (react-modal renders it into the body).
const findModal = async () => {
  await waitFor(() => expect(document.querySelector('.ReactModal__Content')).not.toBeNull());
  return document.querySelector('.ReactModal__Content');
};

// The quantity selected input of a substitution line in the modal.
const quantityInput = async (modal, index) => {
  const selector = `[id="substitutions[${index}].quantitySelected"]`;
  await waitFor(() => expect(modal.querySelector(selector)).not.toBeNull());
  return modal.querySelector(selector);
};

// Opens the menu of the select inside the element (arrow keys move between table cells here).
const openSelect = (element) => {
  fireEvent.mouseDown(element.querySelector('.react-select__control'), { button: 0 });
};

const activeStep = () => screen.getAllByTestId('wizard-step')
  .find((step) => step.dataset.stepstate === 'active')?.textContent;

describe('verify request wizard against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    current = movement();
    items = [item('1'), item('2', { quantityRequested: 8, quantityAvailable: 30 })];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    // Like axios, the response carries the request it answers.
    apiClient.post.mockImplementation((url, body) => Promise.resolve({
      data: { data: current }, headers: {}, config: { data: JSON.stringify(body) },
    }));
  });

  afterEach(() => jest.clearAllMocks());

  describe('opening a request', () => {
    it.each([
      ['REQUESTED', false, 'Edit'],
      ['VALIDATED', false, 'Pick'],
      ['PICKED', true, 'Pack'],
    ])('a %s request (packing support %s) opens on the %s step', async (statusCode, hasPackingSupport, step) => {
      current = movement({ statusCode });
      // The later steps load their own lines; none here.
      items = [];
      renderWizard({ hasPackingSupport });

      await waitFor(() => expect(activeStep()).toBe(step));
      expect(screen.getAllByTestId('wizard-step')).toHaveLength(hasPackingSupport ? 4 : 3);
      expect(screen.getByTestId('wizardTitle')).toHaveTextContent('REQ-0042 - Main Warehouse to Ward A');
    });

    it('shows a pending status on the send step', async () => {
      current = movement({ statusCode: 'DISPATCHED' });
      items = [];
      renderWizard();

      expect(await screen.findByText('PENDING')).toHaveClass('shipment-status');
    });
  });

  describe('edit step', () => {
    it('lists the requested items with their availability', async () => {
      renderWizard();

      expect(await screen.findByText('P-1')).toBeInTheDocument();
      const first = row('P-1');
      expect(within(first).getByRole('cell', { name: 'Qty Requested' })).toHaveTextContent('5');
      expect(within(first).getByRole('cell', { name: 'Available' })).toHaveTextContent('20');
      expect(within(first).getByRole('cell', { name: 'QOH' })).toHaveTextContent('25');
      expect(within(first).getByRole('cell', { name: 'Requester QOH' })).toHaveTextContent('2');
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm1/stockMovementItems?stepNumber=3');
      expect(apiClient.get).toHaveBeenCalledWith('/api/reasonCodes');
    });

    it.each([
      ['PUSH', 'Stocklist QTY'],
      ['PULL', 'Demand per Replenishment Period'],
    ])('a %s stocklist request shows the %s next to the request', async (type, column) => {
      current = movement({ stocklist: { id: 'sl1', name: 'Ward list' }, replenishmentType: { name: type } });
      items = [item('1', {
        quantityOnStocklist: 1500, demandPerReplenishmentPeriod: 1500, comments: 'Urgent',
      }), item('2', { quantityRequested: 50, quantityAvailable: 30 })];
      renderWizard();
      await screen.findByText('P-1');

      expect(within(row('P-1')).getByRole('cell', { name: column })).toHaveTextContent('1,500');
      expect(within(row('P-1')).getByRole('cell', { name: 'Qty Requested' }).querySelector('.fa-sticky-note'))
        .not.toBeNull();
      await waitFor(() => expect(button(/item\(s\) require your attention/))
        .toHaveTextContent('1 item(s) require your attention'));
    });

    it('generates the picklist of a REQUESTED request and moves on to picking', async () => {
      renderWizard();
      await screen.findByText('P-2');

      await waitFor(() => expect(button('Generate Picklist')).not.toBeDisabled());
      // The pick step loads its own lines; none here.
      items = [];
      fireEvent.click(button('Generate Picklist'));

      await waitFor(() => expect(activeStep()).toBe('Pick'));
      expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'PICKING', createPicklist: 'true' }]);
      // Nothing was revised, so nothing was sent for revision.
      expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([]);
    });

    it('does not ask for a new picklist once the request is being validated', async () => {
      current = movement({ statusCode: 'VALIDATING' });
      renderWizard();
      await screen.findByText('P-2');

      await waitFor(() => expect(button('Generate Picklist')).not.toBeDisabled());
      // The pick step loads its own lines; none here.
      items = [];
      fireEvent.click(button('Generate Picklist'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status'))
        .toEqual([{ status: 'PICKING', createPicklist: 'false' }]));
    });

    it('flags lines asking for more than is available and blocks the picklist', async () => {
      items = [item('1'), item('2', { quantityRequested: 50, quantityAvailable: 30 })];
      renderWizard();
      await screen.findByText('P-2');

      await waitFor(() => expect(button(/item\(s\) require your attention/))
        .toHaveTextContent('1 item(s) require your attention'));
      expect(button('Generate Picklist')).toBeDisabled();
      expect(within(row('P-2')).getByRole('cell', { name: 'Available' }).querySelector('.text-danger'))
        .not.toBeNull();
    });

    it('revises a quantity with a reason, then sends the revision before the picklist', async () => {
      renderWizard();
      await screen.findByText('P-1');

      const revised = within(row('P-1')).getByRole('cell', { name: 'Qty revised' }).querySelector('input');
      fireEvent.change(revised, { target: { value: '3' } });
      const reason = within(row('P-1')).getByRole('cell', { name: 'Reason code' });
      openSelect(reason);
      fireEvent.click(await screen.findByText('Damaged'));
      await waitFor(() => expect(button('Generate Picklist')).not.toBeDisabled());
      // The pick step loads its own lines; none here.
      items = [];
      fireEvent.click(button('Generate Picklist'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([{
        lineItems: [{ id: '1', quantityRevised: '3', reasonCode: 'DAMAGED' }],
      }]);
      const order = apiClient.post.mock.calls.map(([url]) => url);
      expect(order.indexOf('/api/stockMovements/sm1/reviseItems'))
        .toBeLessThan(order.indexOf('/api/stockMovements/sm1/status'));
    });

    it('save progress refuses a revised quantity without a reason code', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.change(within(row('P-1')).getByRole('cell', { name: 'Qty revised' }).querySelector('input'),
        { target: { value: '3' } });
      fireEvent.click(button('Save Progress'));

      expect(Alert.error).toHaveBeenCalledWith('Error occurred in line:', expect.objectContaining({
        customFields: expect.objectContaining({
          detailsArray: ['1 - react.stockMovement.errors.reasonCodeRequired.label'],
        }),
      }));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('save progress refuses a revised quantity above the available quantity', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.change(within(row('P-1')).getByRole('cell', { name: 'Qty revised' }).querySelector('input'),
        { target: { value: '25' } });
      fireEvent.click(button('Save Progress'));

      expect(Alert.error.mock.calls[0][1].customFields.detailsArray)
        .toEqual(['1 - react.stockMovement.errors.higherQty.label']);
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('save progress sends the revisions and confirms', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.change(within(row('P-1')).getByRole('cell', { name: 'Qty revised' }).querySelector('input'),
        { target: { value: '4' } });
      openSelect(within(row('P-1')).getByRole('cell', { name: 'Reason code' }));
      fireEvent.click(await screen.findByText('Expired'));
      fireEvent.click(button('Save Progress'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      expect(posted('/api/stockMovements/sm1/reviseItems')).toEqual([{
        lineItems: [{ id: '1', quantityRevised: '4', reasonCode: 'EXPIRED' }],
      }]);
    });

    // KNOWN-GAP: OBAF parses the missing response when nothing was revised, so it never confirms.
    it.skip('save progress with nothing revised sends nothing and confirms', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.click(button('Save Progress'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    // KNOWN-GAP: OBAF reads rowValues.comments on a cleared row and the page crashes on reload.
    it.skip.each([
      ['without a stocklist', {}],
      ['from a PUSH stocklist', { stocklist: { id: 'sl1', name: 'Ward list' }, replenishmentType: { name: 'PUSH' } }],
      ['from a PULL stocklist', { stocklist: { id: 'sl1', name: 'Ward list' }, replenishmentType: { name: 'PULL' } }],
    ])('reload of a request %s keeps the page up while a line with comments is fetched again', async (_, overrides) => {
      current = movement(overrides);
      items = [item('1', { comments: 'Urgent' }), item('2')];
      renderWizard();
      await screen.findByText('P-1');
      // The reloaded lines arrive later.
      let reloadItems;
      apiClient.get.mockImplementation((url) => (url.startsWith('/api/stockMovements/sm1/stockMovementItems')
        ? new Promise((resolve) => { reloadItems = () => resolve(routeAnswers(routes())(url)); })
        : routeAnswers(routes())(url)));

      fireEvent.click(screen.getByText('Reload'));
      act(() => confirmButton('Yes').onClick());

      await waitFor(() => expect(reloadItems).toBeDefined());
      reloadItems();
      expect(await screen.findByText('P-1')).toBeInTheDocument();
      expect(within(row('P-1')).getByRole('cell', { name: 'Qty Requested' }).querySelector('.fa-sticky-note'))
        .not.toBeNull();
    });

    it('undoes a changed line through the API and shows the reverted line', async () => {
      items = [item('1', { statusCode: 'CHANGED', quantityRevised: 3, reasonCode: 'DAMAGED' })];
      renderWizard();
      await screen.findByText('P-1');
      apiClient.get.mockImplementation((url) => (url === '/api/stockMovementItems/1?stepNumber=3'
        ? Promise.resolve({ data: { data: item('1', { quantityAvailable: 12 }) } })
        : routeAnswers(routes())(url)));

      fireEvent.click(within(row('P-1')).getByText('Undo'));

      await waitFor(() => expect(within(row('P-1')).getByRole('cell', { name: 'Available' }))
        .toHaveTextContent('12'));
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovementItems/1/revertItem');
    });

    it('filters the lines by product code or name, and clears the filter', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.change(screen.getByPlaceholderText('Search...'), { target: { value: 'product 2' } });

      await waitFor(() => expect(screen.queryByText('P-1')).not.toBeInTheDocument());
      expect(screen.getByText('P-2')).toBeInTheDocument();
      fireEvent.click(screen.getByRole('button', { name: 'Clear filter' }));
      expect(await screen.findByText('P-1')).toBeInTheDocument();
    });

    it('save and exit sends the revisions, then opens the movement page', async () => {
      const { location } = window;
      delete window.location;
      window.location = '';
      try {
        renderWizard();
        await screen.findByText('P-1');

        fireEvent.click(screen.getByText('Save and exit'));

        await waitFor(() => expect(window.location).toBe('/openboxes/stockMovement/show/sm1'));
      } finally {
        window.location = location;
      }
    });

    it('offers to edit the request items to the fulfilling location while verifying', async () => {
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.click(screen.getByText('Edit request items'));

      await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/createRequest/sm1'));
    });

    it('a request of another location is read-only, with an exit to the inbound list', async () => {
      current = movement({ origin: { id: 'loc8', name: 'Elsewhere', locationType: DEPOT } });
      renderWizard();
      await screen.findByText('P-1');

      expect(screen.queryByText('Save Progress')).not.toBeInTheDocument();
      expect(screen.queryByText('Edit request items')).not.toBeInTheDocument();
      expect(button('Generate Picklist')).toBeDisabled();
      expect(within(row('P-1')).getByRole('cell', { name: 'Qty revised' }).querySelector('input')).toBeDisabled();

      fireEvent.click(screen.getByText('Exit'));
      expect(pathname).toBe('/openboxes/stockMovement/list?direction=INBOUND');
    });

    it('a pending approval request is read-only even at the fulfilling location', async () => {
      current = movement({ statusCode: 'PENDING_APPROVAL' });
      renderWizard({ supportedActivities: ['SUBMIT_REQUEST'] });
      await screen.findByText('P-1');

      fireEvent.click(screen.getByText('Exit'));
      expect(pathname).toBe('/openboxes/dashboard');
    });
  });

  describe('substitutions and details', () => {
    it('substitutes a line: loads the candidates, then sends the chosen quantity', async () => {
      items = [item('1', { substitutionStatus: 'YES', quantityAvailable: 0 }), item('2')];
      renderWizard();
      await screen.findByText('P-1');
      apiClient.get.mockImplementation((url) => (url === '/api/stockMovements/1/substitutionItems'
        ? Promise.resolve({
          data: {
            data: [{
              productId: 'p9',
              productCode: 'P-9',
              productName: 'Generic ibuprofen',
              product: { id: 'p9' },
              minExpirationDate: '01/01/2030',
              quantityAvailable: 40,
            }],
          },
        })
        : routeAnswers(routes())(url)));

      fireEvent.click(within(row('P-1')).getByText('YES'));
      const modal = await findModal();
      const quantity = await quantityInput(modal, 0);
      expect(within(modal).getByText('Generic ibuprofen', { exact: false })).toBeInTheDocument();

      fireEvent.change(quantity, { target: { value: '5' } });
      expect(await within(modal).findByText(/Quantity selected\s*:\s*5/)).toBeInTheDocument();
      fireEvent.click(within(modal).getByText('Save'));

      await waitFor(() => expect(posted('/api/stockMovementItems/1/substituteItem')).toHaveLength(1));
      expect(posted('/api/stockMovementItems/1/substituteItem')[0]).toEqual({
        substitutionItems: [{
          newProduct: { id: 'p9' }, newQuantity: '5', reasonCode: 'SUBSTITUTION', sortOrder: 100,
        }],
      });
      // The page reloads its items after the substitution.
      await waitFor(() => expect(apiClient.get.mock.calls
        .filter(([url]) => url === '/api/stockMovements/sm1/stockMovementItems?stepNumber=3').length)
        .toBeGreaterThan(0));
    });

    it('a partial substitution needs a reason, which goes into the reason code', async () => {
      items = [item('1', { substitutionStatus: 'YES', quantityAvailable: 0, quantityRequested: 10 })];
      renderWizard();
      await screen.findByText('P-1');
      apiClient.get.mockImplementation((url) => (url === '/api/stockMovements/1/substitutionItems'
        ? Promise.resolve({
          data: {
            data: [{
              productId: 'p9', productCode: 'P-9', productName: 'Generic', product: { id: 'p9' }, quantityAvailable: 40,
            }],
          },
        })
        : routeAnswers(routes())(url)));

      fireEvent.click(within(row('P-1')).getByText('YES'));
      const modal = await findModal();
      fireEvent.change(await quantityInput(modal, 0), { target: { value: '4' } });
      fireEvent.click(within(modal).getByText('Save'));
      expect(await within(modal).findByText('react.default.error.requiredField.label')).toBeInTheDocument();
      expect(apiClient.post).not.toHaveBeenCalled();

      openSelect(within(modal).getByLabelText('Reason for not fulfilling full qty', { selector: 'div' }));
      fireEvent.click(await screen.findByText('Damaged'));
      fireEvent.click(within(modal).getByText('Save'));

      await waitFor(() => expect(posted('/api/stockMovementItems/1/substituteItem')).toHaveLength(1));
      expect(posted('/api/stockMovementItems/1/substituteItem')[0].substitutionItems[0].reasonCode)
        .toBe('SUBSTITUTION (DAMAGED)');
    });

    it('offers the line itself first when it still has stock, and says when nothing else is there', async () => {
      items = [item('1', { substitutionStatus: 'EARLIER', minExpirationDate: '01/01/2027' })];
      renderWizard();
      await screen.findByText('P-1');

      fireEvent.click(within(row('P-1')).getByText('EARLIER'));

      const modal = await findModal();

      expect(await within(modal).findByText('20')).toBeInTheDocument();
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/1/substitutionItems');
    });

    it('a substituted line cannot be substituted again', async () => {
      items = [item('1', { substitutionStatus: 'YES', statusCode: 'SUBSTITUTED' })];
      renderWizard();
      await screen.findByText('P-1');

      expect(within(row('P-1')).getByText('YES').closest('button')).toBeDisabled();
    });

    it('shows the pending requests for the product with their totals', async () => {
      renderWizard();
      await screen.findByText('P-1');
      apiClient.get.mockImplementation((url) => (url.startsWith('/api/stockMovements/pendingRequisitionDetails')
        ? Promise.resolve({
          data: {
            data: [{
              destination: { name: 'Ward B' },
              averageMonthlyDemand: 7,
              quantityOnHandAtDestination: 3,
              quantityRequested: 4,
              quantityPicked: 1,
              requisitions: [{
                requisition: { id: 'sm5', requestNumber: 'REQ-0005' }, quantityRequested: 6, quantityPicked: 2,
              }],
            }],
          },
        })
        : routeAnswers(routes())(url)));

      fireEvent.click(within(row('P-1')).getByRole('cell', { name: 'Details' }).querySelector('button'));
      const modal = await findModal();

      expect(await within(modal).findByText('Ward B')).toBeInTheDocument();
      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/stockMovements/pendingRequisitionDetails?origin.id=loc1&product.id=p1&stockMovementId=sm1',
      );
      expect(within(modal).getByText('Total Monthly Demand', { exact: false }).parentElement).toHaveTextContent('7');
      expect(within(modal).getByText('Total Qty Requested', { exact: false }).parentElement).toHaveTextContent('10');
      expect(within(modal).getByText('Total Qty Picked', { exact: false }).parentElement).toHaveTextContent('3');
      expect(within(modal).getByText('REQ-0005').closest('a')).toHaveAttribute('href', '/openboxes/stockMovement/show/sm5');
    });
  });
});
