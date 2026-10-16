import React from 'react';

import {
  act, configure, fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';
import {
  DEPOT, HERE, routeAnswers, wizardStore,
} from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

// The API: GETs answer from the routes below. The helpers the actions use come from the real
// module, loaded on first use.
jest.mock('utils/apiClient', () => {
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

// Confirmation dialogs: the test runs the button handlers or renders the custom dialog itself.
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

const ELSEWHERE = { id: 'loc9', name: 'Other Depot' };

const movement = (overrides = {}) => ({
  id: 'sm1',
  identifier: 'SM-0001',
  name: 'Outbound to clinic',
  description: 'Weekly restock',
  statusCode: 'PICKED',
  shipped: false,
  received: false,
  origin: { ...HERE, locationType: DEPOT },
  destination: { id: 'loc2', name: 'Clinic', locationType: DEPOT },
  requestedBy: { id: 'u1', name: 'Admin' },
  requestType: { name: 'STOCK' },
  dateRequested: '10/01/2026',
  dateShipped: '10/05/2026',
  expectedDeliveryDate: '10/09/2026',
  shipmentType: { id: '2', name: 'Air' },
  trackingNumber: 'TRK-1',
  hasManageInventory: true,
  lineItems: [],
  associations: {
    documents: [
      {
        name: 'Packing List', documentType: 'PACKING', stepNumber: 5, uri: '/doc/packing',
      },
      {
        name: 'Invoice',
        documentType: 'INVOICE',
        stepNumber: 5,
        downloadOptions: [{ name: 'Invoice PDF', uri: '/doc/invoice.pdf' }, { name: 'Invoice XLS', uri: '/doc/invoice.xls' }],
      },
      {
        name: 'Hidden', stepNumber: 5, hidden: true, uri: '/doc/hidden',
      },
      { name: 'Earlier step', stepNumber: 2, uri: '/doc/earlier' },
    ],
  },
  ...overrides,
});

const shipmentItem = (id) => ({
  shipmentItemId: `si${id}`,
  productCode: `P-${id}`,
  product: { id: `p${id}`, productCode: `P-${id}`, name: `Product ${id}` },
  lotNumber: `LOT-${id}`,
  quantitySelected: 4,
  recalled: id === '2',
});

let current;

const routes = () => [
  ['/api/localizations', () => ({ messages: {}, currentLocale: 'en' })],
  ['/api/stockMovements/sm1/stockMovementItems', () => ({ data: [shipmentItem('1'), shipmentItem('2')], totalCount: 2 })],
  ['/api/stockMovements/sm1', () => ({ data: current, totalCount: 2 })],
  ['/api/generic/shipmentType', () => ({ data: [{ id: '1', name: 'Default' }, { id: '2', name: 'Air|fr:Avion' }] })],
];

let store;

const renderWizard = (session) => {
  store = wizardStore(session);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/stockMovement/createOutbound/sm1']}>
        <Route path="**/stockMovement/createOutbound/:stockMovementId?" component={StockMovement} />
      </MemoryRouter>
    </Provider>,
  );
};

const confirmButton = (label) => {
  const { buttons } = confirmAlert.mock.calls[confirmAlert.mock.calls.length - 1][0];
  return buttons.find((button) => button.label === label);
};

const posted = (url) => apiClient.post.mock.calls
  .filter(([called]) => called === url)
  .map(([, body]) => body);

const button = (text) => screen.getByText(text).closest('button');

const ready = () => screen.findByDisplayValue('TRK-1');

// OBAF's send step renders the Dropzone without its <input> (upstream added it in
// #6031), so files are dropped on the dropzone root instead of set on the input. The drop is
// handled asynchronously; the callers wait for the file name to show.
const dropFiles = (container, files) => fireEvent.drop(container.querySelector('.dropzone [tabindex]'), {
  dataTransfer: {
    files,
    items: files.map((file) => ({ kind: 'file', type: file.type, getAsFile: () => file })),
    types: ['Files'],
  },
});

describe('outbound stock movement send step against a mocked API', () => {
  beforeEach(() => {
    current = movement();
    apiClient.get.mockImplementation((url) => routeAnswers(routes())(url));
    apiClient.post.mockImplementation(
      () => Promise.resolve({ data: { data: current }, headers: {} }),
    );
  });

  afterEach(() => jest.clearAllMocks());

  it('lists the picked items, flags recalled lots and saves the shipment details', async () => {
    renderWizard();
    await ready();
    expect(await screen.findByText('LOT-1')).toBeInTheDocument();
    expect(screen.getByText('LOT-2').closest('.table-row')).toHaveClass('recalled');

    fireEvent.change(screen.getByDisplayValue('TRK-1'), { target: { value: 'TRK-2' } });
    fireEvent.click(button('Save'));

    await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Changes saved successfully', { timeout: 3000 }));
    expect(posted('/api/stockMovements/sm1/updateShipment')).toEqual([{
      destination: { id: 'loc2' },
      dateShipped: '10/05/2026',
      shipmentType: { id: '2' },
      trackingNumber: 'TRK-2',
      driverName: '',
      comments: '',
      expectedDeliveryDate: '10/09/2026',
    }]);
  });

  it('lists the documents of the step, each download option on its own', async () => {
    renderWizard();
    await ready();

    fireEvent.click(button('Download'));

    expect(screen.getByText('Packing List')).toBeInTheDocument();
    expect(screen.getByText('Invoice PDF')).toBeInTheDocument();
    expect(screen.getByText('Invoice XLS')).toBeInTheDocument();
    expect(screen.queryByText('Hidden')).not.toBeInTheDocument();
    expect(screen.queryByText('Earlier step')).not.toBeInTheDocument();
  });

  it('does not send while the shipment type is still the default one', async () => {
    current = movement({ shipmentType: { id: '1', name: 'Default' } });
    renderWizard();
    await ready();

    fireEvent.click(button('Send shipment'));

    await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Please populate shipment type before continuing'));
    expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
  });

  it('reports a failed upload and does not send', async () => {
    const { container } = renderWizard();
    await ready();
    apiClient.post.mockRejectedValue(new Error('upload failed'));

    dropFiles(container, [new File(['%PDF-1.4'], 'note.pdf', { type: 'application/pdf' })]);
    await screen.findByText('note.pdf');
    fireEvent.click(button('Send shipment'));

    await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Error occured during file upload!'));
    expect(posted('/api/stockMovements/sm1/status')).toEqual([]);
    expect(screen.getByText('note.pdf')).toBeInTheDocument();
  });

  it('reports a failed upload of several files', async () => {
    const { container } = renderWizard();
    await ready();
    apiClient.post.mockRejectedValue(new Error('upload failed'));

    dropFiles(container, [
      new File(['%PDF-1.4'], 'a.pdf', { type: 'application/pdf' }),
      new File(['%PDF-1.4'], 'b.pdf', { type: 'application/pdf' }),
    ]);
    await screen.findByText('b.pdf');
    fireEvent.click(button('Send shipment'));

    await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('Error occured during files upload!'));
  });

  it('removes a dropped file before sending', async () => {
    const { container } = renderWizard();
    await ready();
    dropFiles(container, [new File(['%PDF-1.4'], 'note.pdf', { type: 'application/pdf' })]);
    await screen.findByText('note.pdf');

    fireEvent.click(container.querySelector('.remove-button'));

    await waitFor(() => expect(screen.queryByText('note.pdf')).not.toBeInTheDocument());
  });

  it('a past ship date asks how to proceed; updating it to today sends with today as both dates', async () => {
    jest.useFakeTimers({ doNotFake: ['nextTick', 'queueMicrotask'] });
    jest.setSystemTime(new Date('2026-10-10T09:00:00'));
    try {
      renderWizard();
      await ready();

      fireEvent.click(button('Send shipment'));

      await waitFor(() => expect(confirmAlert).toHaveBeenCalled());
      const onClose = jest.fn();
      render(
        <Provider store={store}>
          {confirmAlert.mock.calls[0][0].customUI({ onClose })}
        </Provider>,
      );
      expect(screen.getByText(/shipped on 05\/Oct\/2026, which is 5 days ago/)).toBeInTheDocument();
      fireEvent.click(screen.getByText(/Update the ship date to today/).closest('label').querySelector('input'));
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toEqual([{ status: 'DISPATCHED' }]));
      const [shipment] = posted('/api/stockMovements/sm1/updateShipment');
      expect(shipment.dateShipped).toMatch(/^10\/10\/2026 09:00/);
      expect(shipment.expectedDeliveryDate).toBe(shipment.dateShipped);
      expect(onClose).toHaveBeenCalled();
      await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
    } finally {
      jest.useRealTimers();
    }
  });

  it('a past ship date can also be kept as entered', async () => {
    jest.useFakeTimers({ doNotFake: ['nextTick', 'queueMicrotask'] });
    jest.setSystemTime(new Date('2026-10-10T09:00:00'));
    try {
      renderWizard();
      await ready();

      fireEvent.click(button('Send shipment'));
      await waitFor(() => expect(confirmAlert).toHaveBeenCalled());
      render(
        <Provider store={store}>
          {confirmAlert.mock.calls[0][0].customUI({ onClose: jest.fn() })}
        </Provider>,
      );
      fireEvent.click(screen.getByText('Next'));

      await waitFor(() => expect(posted('/api/stockMovements/sm1/status')).toHaveLength(1));
      expect(posted('/api/stockMovements/sm1/updateShipment')[0]).toEqual(expect.objectContaining({
        dateShipped: '10/05/2026', expectedDeliveryDate: '10/09/2026',
      }));
    } finally {
      jest.useRealTimers();
    }
  });

  it('without inventory management only the destination may roll a shipment back', async () => {
    current = movement({ statusCode: 'DISPATCHED', shipped: true, hasManageInventory: false });
    renderWizard();

    fireEvent.click(await screen.findByText('Rollback'));

    await waitFor(() => expect(Alert.error).toHaveBeenCalledWith('You are not able to rollback shipment from your location.'));
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('a movement of another location is read only: exit goes back to the outbound list', async () => {
    current = movement({ origin: { ...ELSEWHERE, locationType: DEPOT } });
    renderWizard();
    await ready();

    expect(screen.queryByText('Save')).not.toBeInTheDocument();
    expect(button('Send shipment')).toBeDisabled();
    expect(screen.getByText('Exit')).toBeInTheDocument();
  });

  it('save and exit with a missing delivery date asks before leaving without saving', async () => {
    current = movement({ expectedDeliveryDate: null });
    renderWizard();
    await ready();

    fireEvent.click(button('Save and exit'));

    expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Confirm save' }));
    act(() => confirmButton('Yes').onClick());
    expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1');
    expect(posted('/api/stockMovements/sm1/updateShipment')).toEqual([]);
  });

  it('save and exit saves the shipment and opens the movement page', async () => {
    renderWizard();
    await ready();

    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(followLink).toHaveBeenCalledWith('/openboxes/stockMovement/show/sm1'));
    expect(posted('/api/stockMovements/sm1/updateShipment')).toHaveLength(1);
  });

  it('previous with a missing delivery date asks before leaving without saving', async () => {
    current = movement({ expectedDeliveryDate: null });
    renderWizard();
    await ready();

    fireEvent.click(button('Previous'));

    expect(confirmAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Validation error' }));
    expect(posted('/api/stockMovements/sm1/updateShipment')).toEqual([]);
  });
});
