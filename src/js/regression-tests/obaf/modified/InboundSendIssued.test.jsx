// OBAF-modified: the inbound (v2) send step treats an ISSUED shipment as dispatched (send
// disabled, rollback available), like DISPATCHED. Set-up adapted from
// regression-tests/stock-movement-wizard/inboundV2Wizard.test.jsx.
import React from 'react';

import {
  configure, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes below; the location search calls the client directly.
jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  });
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
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));
jest.mock('js-file-download', () => jest.fn());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The wizard renders large forms; give the queries room on a busy machine.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import Inbound from 'components/stock-movement-wizard/inboundV2/Inbound';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const SUPPLIER = { locationTypeCode: 'SUPPLIER', description: 'Supplier' };

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'IN-0001',
  name: 'Inbound',
  description: 'Vendor delivery',
  statusCode: 'CREATED',
  displayStatus: { name: 'Pending' },
  origin: { id: 'sup1', name: 'Acme Supplies', locationType: SUPPLIER },
  destination: { ...HERE, locationType: DEPOT },
  requestedBy: { id: 'u1', name: 'Admin' },
  dateRequested: '09/15/2026',
  dateShipped: '2026-09-20T10:00:00',
  expectedDeliveryDate: '2026-09-25T00:00:00',
  shipmentType: { id: '2', name: 'Air', displayName: 'Air' },
  trackingNumber: 'TRK-1',
  driverName: '',
  comments: '',
  hasManageInventory: false,
  shipped: false,
  associations: { documents: [] },
  ...overrides,
});

const item = (id, overrides = {}) => ({
  id,
  statusCode: 'PENDING',
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Bandage ${id}` },
  quantityRequested: 5,
  lotNumber: `L${id}`,
  expirationDate: null,
  palletName: '',
  boxName: '',
  recipient: null,
  inventoryItem: null,
  sortOrder: 100,
  ...overrides,
});

let current;
let items;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: items })],
  ['/api/stockMovements/sm1', () => ({ data: current })],
  ['/api/stocklists', () => ({ data: [{ id: 'sl1', name: 'Vendor list' }] })],
  ['/api/generic/shipmentType', () => ({ data: [{ id: '1', name: 'Default' }, { id: '2', name: 'Air' }] })],
  ['/openboxes/stockMovement/exportCsv/sm1', () => 'csv-content'],
];

const renderWizard = (path) => render(
  <Provider store={wizardStore()}>
    <MemoryRouter initialEntries={[path]}>
      <Route path="**/stockMovement/createInbound/:stockMovementId?" component={Inbound} />
    </MemoryRouter>
  </Provider>,
);

const button = (text) => screen.getByText(text).closest('button');

const SEND = '/openboxes/stockMovement/createInbound/sm1?step=SEND_SHIPMENT';

describe('inbound send step, ISSUED as dispatched (OBAF)', () => {
  beforeEach(() => {
    items = [item('1')];
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
  });

  afterEach(() => jest.clearAllMocks());

  it.each(['ISSUED', 'DISPATCHED'])('a %s shipment cannot be sent again but can be rolled back', async (statusCode) => {
    current = movement({ statusCode, shipped: true, displayStatus: { name: 'Shipped' } });
    renderWizard(SEND);

    await screen.findByDisplayValue('TRK-1');
    expect(button('Send shipment')).toBeDisabled();
    await waitFor(() => expect(button('Rollback')).not.toBeDisabled());
  });

  it('a pending shipment can be sent and offers no rollback', async () => {
    current = movement({ statusCode: 'CHECKING' });
    renderWizard(SEND);

    await screen.findByDisplayValue('TRK-1');
    await waitFor(() => expect(button('Send shipment')).not.toBeDisabled());
    expect(screen.queryByText('Rollback')).toBeNull();
  });
});
