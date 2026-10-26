// OBAF-modified: the outbound edit step shows a Backordered column, red when anything is
// backordered. Set-up adapted from regression-tests/stock-movement-wizard/outboundSteps.test.jsx.
import React from 'react';

import {
  configure, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => {
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
jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

/* eslint-disable import/first,import/order */
import StockMovement from 'components/stock-movement-wizard/StockMovement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

installNavigationSpy();

const current = {
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode: 'REQUESTED',
  origin: { ...HERE, locationType: DEPOT },
  destination: { id: 'loc2', name: 'Clinic', locationType: DEPOT },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'ADHOC' },
  dateRequested: '10/01/2026',
  hasManageInventory: true,
  lineItems: [],
  lastUpdated: '2026-10-01T10:00:00Z',
};

const editItem = (id, overrides = {}) => ({
  requisitionItemId: `ri${id}`,
  productCode: `P-${id}`,
  productName: `Product ${id}`,
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Product ${id}` },
  quantityRequested: 10,
  quantityOnHand: 50,
  quantityAvailable: 40,
  quantityDemandFulfilling: 3,
  substitutionStatus: 'NO',
  statusCode: 'APPROVED',
  ...overrides,
});

const editItems = [
  editItem('1', { quantityBackordered: 1234 }),
  editItem('2', { quantityBackordered: 0 }),
];

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems?stepNumber=3', () => ({ data: editItems, totalCount: editItems.length })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: editItems.length })],
  ['/api/reasonCodes', () => ({ data: [{ id: 'STOCKOUT', name: 'Stock out' }] })],
];

const renderWizard = () => render(
  <Provider store={wizardStore()}>
    <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
      <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
    </MemoryRouter>
  </Provider>,
);

const backorderedCells = () => screen.getAllByRole('cell', { name: 'Backordered' });

describe('outbound edit step Backordered column (OBAF)', () => {
  beforeEach(() => {
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
  });

  afterEach(() => jest.clearAllMocks());

  it('shows the backordered quantity, red when above zero', async () => {
    renderWizard();
    await waitFor(() => expect(backorderedCells()).toHaveLength(2));

    expect(screen.getAllByText('Backordered').length).toBeGreaterThan(0);
    const [first, second] = backorderedCells();
    expect(first).toHaveTextContent('1,234');
    expect(first.querySelector('.text-danger')).not.toBeNull();
    expect(second).toHaveTextContent('0');
    expect(second.querySelector('.text-danger')).toBeNull();
  });
});
