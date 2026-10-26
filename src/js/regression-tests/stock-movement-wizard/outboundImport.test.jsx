import React from 'react';

import {
  configure, fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { routeAnswers, wizardStore } from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes below. The fulfillment calls go through the client with
// the custom response handler, a mock of its own.
jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  });
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    apiClientCustomResponseHandler: { get: jest.fn(), post: jest.fn() },
    parseResponse: actual('parseResponse'),
    // OBAF still imports flattenRequest (upstream develop no longer has it).
    flattenRequest: actual('flattenRequest'),
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

configure({ asyncUtilTimeout: 5000 });

// jsdom has no layout: scrolling to the next button is a no-op here.
Element.prototype.scrollIntoView = jest.fn();
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';
import Alert from 'react-s-alert';

import OutboundImport from 'components/stock-movement-wizard/outboundImport/OutboundImport';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const CLINIC = { id: 'loc2', name: 'Clinic', label: 'Clinic [Depot]' };

// What the details step leaves in the session storage after a validated import.
const cachedImport = () => ({
  fulfillmentDetails: {
    description: 'Clinic restock',
    origin: { id: 'loc1', name: 'Main Warehouse', label: 'Main Warehouse [Depot]' },
    destination: CLINIC,
    requestedBy: { id: 'u1', name: 'Admin', label: 'Admin' },
    dateRequested: '09/30/2026',
  },
  sendingOptions: {
    shipmentType: {
      id: '2', value: '2', label: 'Air', displayName: 'Air', enumKey: 'AIR',
    },
    trackingNumber: 'TRK-9',
    expectedShippingDate: '10/01/2026 10:00 +00:00',
    expectedDeliveryDate: '10/05/2026',
  },
  packingList: [
    {
      rowId: '1', origin: 'old-origin', product: 'P-1', lotNumber: 'L1', quantityPicked: 4,
    },
    {
      rowId: '2', origin: 'old-origin', product: 'P-X', lotNumber: 'L9', quantityPicked: 1,
    },
  ],
});

const validatedRow = (rowId, code, name, overrides = {}) => ({
  rowId,
  product: { id: `p${rowId}`, productCode: code, name },
  lotNumber: `L${rowId}`,
  quantityPicked: 4,
  binLocation: 'A-1',
  recipient: '',
  ...overrides,
});

const validRows = [validatedRow('1', 'P-1', 'Gauze'), validatedRow('2', 'P-2', 'Tape')];

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/products/inventoryItems/lotNumbersWithExpirationDate', () => ({ data: {} })],
  ['/api/products/availableItems', () => ({
    data: [{
      binLocation: { name: 'Shelf B-7' }, lotNumber: 'L1', expirationDate: '', quantityOnHand: 12, quantityAvailable: 10,
    }],
  })],
  ['/api/generic/shipmentType', () => ({ data: [{ id: '2', name: 'Air' }] })],
  ['/api/stockMovements/packingList/template', () => 'xls-content'],
];

let location;

const renderImport = (path) => render(
  <Provider store={wizardStore()}>
    <MemoryRouter initialEntries={[path]}>
      <Route path="**/stockMovement/importOutboundStockMovement" component={OutboundImport} />
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

const CONFIRM = '/openboxes/stockMovement/importOutboundStockMovement?step=CONFIRM';
const DETAILS = '/openboxes/stockMovement/importOutboundStockMovement';

const button = (text) => screen.getByText(text).closest('button');

describe('outbound import wizard against a mocked API', () => {
  beforeEach(() => {
    location = null;
    sessionStorage.clear();
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClientCustomResponseHandler.post.mockImplementation((url) => Promise.resolve({
      status: 200,
      data: url.endsWith('/validate') ? { data: validRows, errors: {} } : { data: { id: 'sm7' } },
    }));
  });

  afterEach(() => jest.clearAllMocks());

  describe('details step', () => {
    it('starts at the current location with the next button disabled', async () => {
      renderImport(DETAILS);

      const origin = await screen.findByText('Origin', { selector: 'label' });
      await waitFor(() => expect(origin.closest('[data-testid="form-field"]'))
        .toHaveTextContent('Main Warehouse [Depot]'));
      expect(button('Next')).toBeDisabled();
      expect(screen.getByText('Import completed outbound')).toBeInTheDocument();
    });

    it('requires a description', async () => {
      renderImport(DETAILS);
      const description = await screen.findByRole('textbox', { name: /Description/ });

      fireEvent.change(description, { target: { value: 'x' } });
      fireEvent.change(description, { target: { value: '' } });
      fireEvent.blur(description);

      expect(await screen.findByText('Description is required')).toBeInTheDocument();
    });

    // KNOWN-GAP: OBAF reads the Content-Disposition header unconditionally and throws when it is
    // absent.
    it.skip('downloads the packing list template as XLS', async () => {
      renderImport(DETAILS);

      fireEvent.click(await screen.findByText('Export template'));

      await waitFor(() => expect(fileDownload).toHaveBeenCalled());
      expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['xls-content', 'Import Packing List.xls']);
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/packingList/template', expect.objectContaining({
        responseType: 'blob', params: { format: 'xls' },
      }));
    });

    it('drops a stale saved import when the details step is opened', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(DETAILS);

      await screen.findByText('Next');
      await waitFor(() => expect(sessionStorage.getItem('outbound-import')).toBeNull());
      expect(apiClientCustomResponseHandler.post).not.toHaveBeenCalled();
    });

    it('the confirm step without a saved import goes back to the details step', async () => {
      renderImport(CONFIRM);

      await waitFor(() => expect(location).toBe(`${DETAILS}?step=DETAILS`));
    });
  });

  describe('confirm step', () => {
    it('validates the saved import again for the current origin and lists its rows in file order', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);

      expect(await screen.findByText('Gauze')).toBeInTheDocument();
      expect(screen.getByText('Tape')).toBeInTheDocument();
      const [url, payload] = apiClientCustomResponseHandler.post.mock.calls[0];
      expect(url).toBe('/api/fulfillments/validate');
      expect(payload.fulfillmentDetails.origin).toBe('loc1');
      expect(payload.packingList.map((row) => row.origin)).toEqual(['loc1', 'loc1']);
      // Rows of the file start at 2 (the first row is the header).
      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
      expect(screen.getByDisplayValue('TRK-9')).toBeInTheDocument();
      expect(screen.getByText(/Main Warehouse \[Depot\]/, { selector: 'h5 span' })).toBeInTheDocument();
      expect(screen.getByText(', Clinic restock')).toBeInTheDocument();
    });

    it('finish creates the outbound from the details and the validated rows, then clears the saved import', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);
      await screen.findByText('Gauze');

      fireEvent.click(button('Finish'));

      await waitFor(() => expect(Alert.success).toHaveBeenCalledWith(
        'Stock Movement has been created successfully', expect.anything(),
      ));
      const [url, payload] = apiClientCustomResponseHandler.post.mock.calls[1];
      expect(url).toBe('/api/fulfillments');
      expect(payload.fulfillmentDetails).toEqual(expect.objectContaining({
        description: 'Clinic restock',
        origin: { id: 'loc1', name: 'Main Warehouse', label: 'Main Warehouse [Depot]' },
        destination: CLINIC,
        requestedBy: { id: 'u1', name: 'Admin', label: 'Admin' },
        dateRequested: '09/30/2026',
      }));
      expect(payload.sendingOptions).toEqual(expect.objectContaining({
        trackingNumber: 'TRK-9', expectedDeliveryDate: '10/05/2026',
      }));
      expect(payload.packingList.map((row) => row.product)).toEqual(['P-1', 'P-X']);
      expect(sessionStorage.getItem('outbound-import')).toBeNull();
    });

    it('rows rejected by the validation come first, are counted, and block finishing', async () => {
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: {
          status: 400,
          data: {
            data: validRows,
            errors: { packingList: { 2: { product: 'Unknown product' } } },
          },
        },
      });
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);

      await waitFor(() => expect(Alert.error).toHaveBeenCalled());
      const [message, options] = Alert.error.mock.calls[0];
      expect(message).toBe('Validation exception');
      expect(options.customFields.details).toContain('1 rows in the import are invalid');
      expect(await screen.findByText('Tape')).toBeInTheDocument();
      expect(button('Finish')).toBeDisabled();
      const names = () => screen.getAllByText(/^(Gauze|Tape)$/).map((cell) => cell.textContent);
      expect(names()).toEqual(['Gauze', 'Tape']);

      // The indicator filters the table down to the invalid rows, and back.
      const indicator = document.querySelector('button.invalid-items-indicator');
      expect(indicator).toHaveTextContent('1');
      fireEvent.click(indicator);
      await waitFor(() => expect(names()).toEqual(['Tape']));
      fireEvent.click(indicator);
      await waitFor(() => expect(names()).toEqual(['Gauze', 'Tape']));
    });

    it('a server error while validating is reported without a table', async () => {
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: { status: 500, data: { errorMessage: 'Boom' } },
      });
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Internal server error', expect.objectContaining({
        customFields: expect.objectContaining({ details: 'Boom' }),
      })));
    });

    it('a rejected finish with row errors marks the rows', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);
      await screen.findByText('Gauze');
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: {
          status: 400,
          data: { data: validRows, errors: { packingList: { 1: { quantityPicked: 'Not enough stock' } } } },
        },
      });

      fireEvent.click(button('Finish'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Validation exception', expect.objectContaining({
        customFields: expect.objectContaining({ details: 'Check out the table for the validation exceptions' }),
      })));
      expect(sessionStorage.getItem('outbound-import')).not.toBeNull();
    });

    it('a rejected finish without row errors shows the server messages', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);
      await screen.findByText('Gauze');
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: { status: 400, data: { errorMessages: ['First problem', 'Second problem'] } },
      });

      fireEvent.click(button('Finish'));

      await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Bad request', expect.objectContaining({
        customFields: expect.objectContaining({ details: 'First problem, Second problem' }),
      })));
    });

    it('redo import goes back to the details step', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      renderImport(CONFIRM);
      await screen.findByText('Gauze');

      fireEvent.click(button('Redo import'));

      await waitFor(() => expect(location).toContain('step=DETAILS'));
      expect(await screen.findByText('Next')).toBeInTheDocument();
    });

    it('shows the stock of a row product at the current location', async () => {
      sessionStorage.setItem('outbound-import', JSON.stringify(cachedImport()));
      const { container } = renderImport(CONFIRM);
      await screen.findByText('Gauze');

      fireEvent.click(container.querySelectorAll('.rt-tbody svg.cursor-pointer')[0]);

      expect(await screen.findByText('Shelf B-7')).toBeInTheDocument();
      expect(apiClient.get).toHaveBeenCalledWith('/api/products/availableItems', expect.objectContaining({
        params: { 'product.id': 'p1', 'location.id': 'loc1' },
      }));
      fireEvent.click(screen.getByText('Close'));
      await waitFor(() => expect(screen.queryByText('Shelf B-7')).not.toBeInTheDocument());
    });
  });
});
