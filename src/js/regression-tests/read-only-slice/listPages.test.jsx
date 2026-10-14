import React from 'react';

import { render, screen } from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

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

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import ProductsList from 'components/products/ProductsList';
import PurchaseOrderList from 'components/purchaseOrder/PurchaseOrderList';
import StockMovementInboundList from 'components/stock-movement/inbound/StockMovementInboundList';
import StockMovementOutboundList from 'components/stock-movement/outbound/StockMovementOutboundList';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const movement = (identifier, direction) => ({
  id: identifier,
  identifier,
  name: `${direction} movement`,
  displayStatus: { name: 'PENDING', label: 'Pending', variant: 'primary' },
  lineItemCount: 2,
  origin: { id: 'o1', name: 'Supplier depot' },
  destination: { id: 'loc1', name: 'Main Warehouse' },
  requestedBy: { id: 'u1', name: 'Admin' },
  order: {},
});

const answers = (url, config) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/purchaseOrders') {
    return {
      data: [{
        id: 'po1', orderNumber: 'PO-0001', name: 'First order', status: 'PLACED',
      }],
      totalCount: 1,
      totalPrice: 120.5,
    };
  }
  if (url === '/api/products') {
    return {
      data: [{
        id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg', productCatalogs: [], active: true,
      }],
      totalCount: 1,
    };
  }
  if (url === '/api/stockMovements') {
    const direction = config?.params?.direction;
    return { data: [movement(`SM-${direction}`, direction)], totalCount: 1 };
  }
  return { data: [] };
};

const storeWithLocation = () => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            id: 'loc1',
            name: 'Main Warehouse',
            supportedActivities: ['MANAGE_INVENTORY', 'PLACE_ORDER', 'RECEIVE_STOCK', 'SEND_STOCK'],
            locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
          },
          supportedActivities: ['MANAGE_INVENTORY', 'PLACE_ORDER', 'RECEIVE_STOCK', 'SEND_STOCK'],
          currentLocationRoles: [],
          highestRole: 'Superuser',
          isSuperuser: true,
          isUserAdmin: true,
          isUserManager: true,
          pageSize: 10,
          isPaginated: true,
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

const PAGES = [
  ['product list', '/openboxes/product/list', ProductsList, '/api/products', 'P-0001'],
  ['purchase order list', '/openboxes/purchaseOrder/list', PurchaseOrderList, '/api/purchaseOrders', 'PO-0001'],
  ['inbound stock movement list', '/openboxes/stockMovement/list?direction=INBOUND', StockMovementInboundList,
    '/api/stockMovements', 'SM-INBOUND'],
  ['outbound stock movement list', '/openboxes/stockMovement/list?direction=OUTBOUND', StockMovementOutboundList,
    '/api/stockMovements', 'SM-OUTBOUND'],
];

describe('read-only slice: list pages against a mocked API', () => {
  beforeEach(() => {
    apiClient.get.mockImplementation((url, config) =>
      Promise.resolve({ data: answers(url, config), headers: {} }));
  });

  afterEach(() => jest.clearAllMocks());

  it.each(PAGES)('the %s lists what the API answers', async (name, path, Page, listUrl, shown) => {
    render(
      <Provider store={storeWithLocation()}>
        <MemoryRouter initialEntries={[path]}>
          <Route path="**/" component={Page} />
        </MemoryRouter>
      </Provider>,
    );

    expect(await screen.findByText(shown)).toBeInTheDocument();
    const listCalls = apiClient.get.mock.calls.filter(([url]) => url === listUrl);
    expect(listCalls.length).toBeGreaterThan(0);
    // Every call goes to the API, relative to it (the client adds the backend and the token).
    apiClient.get.mock.calls.forEach(([url]) => expect(url).toMatch(/^\/api\//));
  });
});
