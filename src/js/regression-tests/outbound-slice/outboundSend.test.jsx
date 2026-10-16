// Not covered on OBAF (it has no /api/stockMovements/{id}/documents upload and no rollback
// without a page load): uploading the dropped files through the API in one call, and rolling a
// sent movement back in place.
// OBAF's own upload path is covered by stock-movement-wizard/outboundSendStep.test.jsx.
import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import moment from 'moment';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import rootReducer from 'reducers';

import '@testing-library/jest-dom';

// The API: every call answers from answers() below. The pick, pack and send steps use the client
// with the custom response handler; it is the same mock here. The helpers the actions use come
// from the real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: { response: { use: jest.fn() } },
  };
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

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const HERE = { id: 'loc1', name: 'Main Warehouse' };
// The clock is fixed at noon local time, so 'tomorrow' is the same calendar day in every zone.
const NOW = new Date(2026, 9, 1, 12, 0, 0);
const TOMORROW = moment(NOW).add(1, 'day').format('MM/DD/YYYY');
const DOCUMENT_URI = 'http://api.example.org:8080/openboxes/stockMovement/exportCsv/sm1';

const movement = ({ statusCode, shipped = false }) => ({
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode,
  shipped,
  received: false,
  origin: { ...HERE, locationType: { locationTypeCode: 'DEPOT', description: 'Depot' } },
  destination: { id: 'loc2', name: 'Clinic', locationType: { locationTypeCode: 'DEPOT', description: 'Depot' } },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'STOCK' },
  dateRequested: '10/01/2026',
  dateShipped: TOMORROW,
  expectedDeliveryDate: TOMORROW,
  shipmentType: { id: '2', name: 'Air' },
  hasManageInventory: true,
  lineItems: [],
  associations: {
    documents: [{
      name: 'Export Packing List', documentType: 'EXPORT', stepNumber: 5, uri: DOCUMENT_URI,
    }],
  },
});

let current;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url.startsWith('/api/stockMovements/sm1/stockMovementItems')) {
    return { data: [], totalCount: 0 };
  }
  if (url.startsWith('/api/stockMovements/sm1')) {
    return { data: current, totalCount: 0 };
  }
  if (url === '/api/generic/shipmentType') {
    return { data: [{ id: '1', name: 'Default' }, { id: '2', name: 'Air' }] };
  }
  return { data: [] };
};

const store = ({ hasPackingSupport = false } = {}) => {
  const created = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  created.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  created.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            ...HERE,
            hasPackingSupport,
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
  return created;
};

const renderWizard = (options) => render(
  <Provider store={store(options)}>
    <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
      <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
    </MemoryRouter>
  </Provider>,
);

const postedUrls = () => apiClient.post.mock.calls.map(([url]) => url);

describe('outbound stock movement send step against a mocked API', () => {
  // Only Date is faked, starting at NOW and moving with real time; the timers stay real.
  beforeAll(() => {
    jest.useFakeTimers({
      now: NOW,
      advanceTimers: true,
      doNotFake: [
        'hrtime', 'nextTick', 'performance', 'queueMicrotask', 'requestAnimationFrame',
        'cancelAnimationFrame', 'requestIdleCallback', 'cancelIdleCallback', 'setImmediate',
        'clearImmediate', 'setInterval', 'clearInterval', 'setTimeout', 'clearTimeout',
      ],
    });
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  beforeEach(() => {
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    apiClient.post.mockImplementation(() => Promise.resolve({
      data: { data: current }, headers: {},
    }));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('save and exit on the pick step opens the movement page through followLink', async () => {
    current = movement({ statusCode: 'PICKING' });
    renderWizard();

    fireEvent.click(await screen.findByText('Save and exit'));

    await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
  });

  it('save and exit on the pack step opens the movement page through followLink', async () => {
    current = movement({ statusCode: 'PACKING' });
    renderWizard({ hasPackingSupport: true });

    fireEvent.click(await screen.findByText('Save and exit'));

    await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
  });

  it('opens a document of the step in a tab opened during the click, after saving', async () => {
    // The navigation spy's window.open hands out the tab and reports where it is sent.
    const open = jest.spyOn(window, 'open');
    current = movement({ statusCode: 'PICKED' });
    renderWizard();

    fireEvent.click(await screen.findByText('Export Packing List'));

    expect(open).toHaveBeenCalledWith('', '_blank');
    const tab = open.mock.results[0].value;
    await waitFor(() => expect(followLink)
      .toHaveBeenCalledWith(DOCUMENT_URI, { targetWindow: tab }));
    expect(postedUrls()).toEqual(['/api/stockMovements/sm1/updateShipment']);
    open.mockRestore();
  });
});
