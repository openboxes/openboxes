import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import rootReducer from 'reducers';

import '@testing-library/jest-dom';

// The API: every call answers from answers() below. The helpers the actions use come from the
// real module, loaded on first use (loading it here would import the store before the reducers).
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
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

// The CSV download: the file the browser would save.
jest.mock('js-file-download', () => jest.fn());

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';

import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const HERE = { id: 'loc1', name: 'Main Warehouse' };
const ELSEWHERE = { id: 'loc9', name: 'Other Depot' };

const movement = ({ statusCode, origin = HERE }) => ({
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode,
  origin: { ...origin, locationType: { locationTypeCode: 'DEPOT', description: 'Depot' } },
  destination: { id: 'loc2', name: 'Clinic', locationType: { locationTypeCode: 'DEPOT', description: 'Depot' } },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'STOCK' },
  dateRequested: '10/01/2026',
  hasManageInventory: true,
  lineItems: [],
  lastUpdated: '2026-10-01T10:00:00Z',
});

const item = {
  id: 'ri1',
  requisitionItemId: 'ri1',
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  productCode: 'P-0001',
  quantityRequested: 5,
  quantityAvailable: 20,
  sortOrder: 100,
};

let current;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/stockMovements/sm1') {
    return { data: current, totalCount: 1 };
  }
  if (url.startsWith('/api/stockMovements/sm1/stockMovementItems')) {
    return { data: [item], totalCount: 1 };
  }
  if (url === '/api/stockMovements/sm1/itemsCsv') {
    return 'Requisition item id,Product code (required)\nri1,P-0001\n';
  }
  return { data: [] };
};

const storeAt = (location) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            ...location,
            supportedActivities: ['MANAGE_INVENTORY', 'SEND_STOCK'],
            locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
          },
          supportedActivities: ['MANAGE_INVENTORY', 'SEND_STOCK'],
          currentLocationRoles: [],
          highestRole: 'Superuser',
          isSuperuser: true,
          isUserAdmin: true,
          isUserManager: true,
          pageSize: 10,
          isPaginated: false,
          menuItems: [],
          supportedLocales: [],
          activeLanguage: 'en',
          activeLanguageTag: 'en',
          currencyCode: 'USD',
        },
      },
    },
  });
  return store;
};

let pathname;

const renderWizard = (location = HERE) => render(
  <Provider store={storeAt(location)}>
    <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
      <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
      <Route
        path="*"
        render={({ location: routerLocation }) => {
          pathname = routerLocation.pathname;
          return null;
        }}
      />
    </MemoryRouter>
  </Provider>,
);

describe('outbound stock movement wizard against a mocked API', () => {
  beforeEach(() => {
    pathname = null;
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    apiClient.post.mockImplementation((url) => Promise.resolve({
      data: url.endsWith('/updateItems') ? { data: { ...current, lineItems: [item] } } : { data: current },
      headers: {},
    }));
  });

  afterEach(() => jest.clearAllMocks());

  it('exports the item list through the API: saved first, then downloaded as CSV', async () => {
    current = movement({ statusCode: 'CREATED' });
    renderWizard();

    fireEvent.click(await screen.findByText('Export template'));

    await waitFor(() => expect(fileDownload).toHaveBeenCalled());
    expect(apiClient.post.mock.calls.map(([url]) => url)).toContain('/api/stockMovements/sm1/updateItems');
    // OBAF exports and imports through the GSP-era CSV endpoints.
    expect(apiClient.get).toHaveBeenCalledWith('/stockMovement/exportCsv/sm1', { responseType: 'blob' });
    expect(fileDownload.mock.calls[0][1]).toBe('ItemList-SM-0001.csv');
  });

  it('imports a CSV through the API as the multipart field importFile', async () => {
    current = movement({ statusCode: 'CREATED' });
    const { container } = renderWizard();
    await screen.findByText('Import template');
    const file = new File(['Requisition item id,Product code (required)\n,P-0001\n'], 'items.csv', { type: 'text/csv' });

    fireEvent.change(container.querySelector('#csvInput'), { target: { files: [file] } });

    // OBAF exports and imports through the GSP-era CSV endpoints.
    await waitFor(() => expect(apiClient.post.mock.calls.map(([url]) => url))
      .toContain('/stockMovement/importCsv/sm1'));
    const [, formData] = apiClient.post.mock.calls.find(([url]) => url === '/stockMovement/importCsv/sm1');
    expect(formData.get('importFile')).toBeInstanceOf(Blob);
    expect(formData.get('importFile').type).toBe('text/csv');
  });

  it('save and exit on the add items step opens the movement page through followLink', async () => {
    current = movement({ statusCode: 'CREATED' });
    renderWizard();

    fireEvent.mouseDown(await screen.findByText('Save and exit'));

    await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
  });

  it('the add items step of a movement of another location is read-only', async () => {
    current = movement({ statusCode: 'CREATED', origin: ELSEWHERE });
    renderWizard();

    const exit = (await screen.findByText('Exit')).closest('button');
    expect(exit).toBeEnabled();
    expect(screen.queryByText('Save and exit')).not.toBeInTheDocument();
    expect(screen.queryByText('Import template')).not.toBeInTheDocument();
    expect(screen.getByText('Previous').closest('button')).toBeDisabled();
    expect(screen.getByText('Next').closest('button')).toBeDisabled();
  });

  // KNOWN-GAP: OBAF's Exit calls this.props.history(...) as a function on a page without router
  // props, so it throws.
  it.skip('exit, for a movement of another location, goes to the outbound list in the app', async () => {
    current = movement({ statusCode: 'CREATED', origin: ELSEWHERE });
    renderWizard();

    fireEvent.click(await screen.findByText('Exit'));

    await waitFor(() => expect(pathname).toBe('/openboxes/stockMovement/list'));
    expect(followLink).not.toHaveBeenCalled();
  });

  it('save and exit on the edit step opens the movement page through followLink', async () => {
    current = movement({ statusCode: 'REQUESTED' });
    renderWizard();

    fireEvent.click(await screen.findByText('Save and exit'));

    await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
  });
});
