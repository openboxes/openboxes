// Shared data and helpers for the legacy partial receiving screen tests.
// eslint-disable-next-line import/no-extraneous-dependencies
import { fireEvent, screen } from '@testing-library/react';
import {
  answer, HERE, renderAt, sessionStore,
} from 'regression-tests/__mocks__/screenHarness';

import ReceivingPage from 'components/receiving/ReceivingPage';
import apiClient from 'utils/apiClient';

export const ROUTE = '**/partialReceiving/create/:shipmentId';
export const ENTRY = '/openboxes/partialReceiving/create/s1';

export const item = (overrides = {}) => ({
  shipmentItemId: 'si1',
  product: {
    id: 'p1', productCode: 'P-1', name: 'Ibuprofen', displayNames: { default: 'Ibuprofen' },
  },
  lotNumber: 'L1',
  expirationDate: '12/31/2027',
  quantityShipped: 10,
  quantityReceived: 0,
  quantityRemaining: 10,
  binLocation: { id: null, name: null },
  ...overrides,
});

export const candidates = (overrides = {}) => ({
  shipmentId: 's1',
  shipment: { shipmentNumber: 'SH-1' },
  origin: { id: 'o1', name: 'Supplier A' },
  destination: { id: 'loc1', name: 'Main Warehouse' },
  dateShipped: '2026-09-28T10:00:00Z',
  dateDelivered: '09/30/2026 10:00 +0000',
  description: 'Inbound order',
  shipmentStatus: 'SHIPPED',
  recipient: { id: 'u1', name: 'Admin' },
  containers: [{
    container: { id: 'c1', name: 'Box 1' },
    parentContainer: null,
    shipmentItems: [item()],
  }],
  ...overrides,
});

/** A store at a location with the given receiving features switched on. */
export const storeWith = ({ bins = true, partial = true, session = {} } = {}) => sessionStore({
  location: { ...HERE, hasBinLocationSupport: bins, hasPartialReceivingSupport: partial },
  session,
});

export const field = (path) => document.getElementById(path);

export const type = (path, value) => fireEvent.change(field(path), { target: { value } });

/** Replaces window.location with a plain object the code under test can assign to. */
export const stubLocation = () => {
  const real = window.location;
  delete window.location;
  window.location = { reload: jest.fn(), href: '' };
  return () => {
    window.location = real;
  };
};

/** Body of the n-th call of a mocked method whose URL starts with `prefix`. */
export const bodyOf = (method, prefix, n = 0) =>
  method.mock.calls.filter(([url]) => url.startsWith(prefix))[n]?.[1];

export const RECEIVING = '/api/partialReceiving/s1';

const binList = [
  { id: 'b2', name: 'Bin B', zoneName: 'Zone 1' },
  { id: 'b1', name: 'Bin A', zoneName: 'Zone 1' },
  { id: 'b3', name: 'Dock' },
];

/**
 * Answers GETs with `data` for both receiving steps (or `step2` for the check step; an Error
 * rejects), and saves with what was posted.
 */
export const serve = (data = candidates(), { step2 = data } = {}) => {
  answer(apiClient.get, [
    [(url) => url.startsWith(`${RECEIVING}?stepNumber=2`), step2 instanceof Error ? step2 : { data: step2 }],
    [(url) => url.startsWith(`${RECEIVING}?stepNumber=1`), { data }],
    ['/api/internalLocations/receiving', { data: binList }],
  ]);
  answer(apiClient.post, [
    [(url) => url.startsWith('/api/partialReceiving/exportCsv'), 'code,lot\nP-1,L1'],
    [(url) => url.startsWith('/api/partialReceiving/importCsv'), {}],
    [(url) => url.startsWith(RECEIVING), (url, body) => ({ data: { ...data, ...body } })],
  ]);
};

export const open = async (options) => {
  const utils = renderAt({
    component: ReceivingPage, path: ROUTE, entry: ENTRY, store: storeWith(options),
  });
  await screen.findAllByText('Ibuprofen');
  return utils;
};

export const button = (name) => screen.getByText(name).closest('button');
