import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import Alert from 'react-s-alert';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// OBAF still imports flattenRequest from utils/apiClient (upstream develop no longer has it).
jest.mock('utils/apiClient', () => ({
  // eslint-disable-next-line global-require
  ...require('regression-tests/__mocks__/screenApiClient').default(),
  flattenRequest: (...args) => jest.requireActual('utils/apiClient').flattenRequest(...args),
}));

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import StockTransferWizard from 'components/stock-transfer/StockTransferWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const PATH = '**/stockTransfer/create/:stockTransferId?';

const item = (id, name, quantity, extra = {}) => ({
  id,
  product: { productCode: `C-${id}`, name },
  lotNumber: `LOT-${id}`,
  originZone: 'Zone A',
  originBinLocation: { id: `bin-${id}`, name: `A-${id}` },
  destinationBinLocation: { id: `dest-${id}`, name: `Dest-${id}` },
  quantityOnHand: 100,
  quantityNotPicked: 100,
  quantity,
  status: 'APPROVED',
  splitItems: [],
  ...extra,
});

const ITEMS = [
  item('i1', 'Aspirin', 100, {
    status: 'CANCELED',
    // As the API sends a split line: its available quantity is its own quantity.
    splitItems: [item('s1', 'Aspirin', 60, {
      quantityNotPicked: 60, destinationBinLocation: { id: 'b9', name: 'Split bin' },
    })],
  }),
  item('i2', 'Bandage', 100),
];

const transfer = (status) => ({
  id: 'st1',
  stockTransferNumber: 'ST-0001',
  status,
  stockTransferItems: ITEMS,
});

let current;

const renderWizard = () => renderAt({
  component: StockTransferWizard,
  path: PATH,
  entry: '/openboxes/stockTransfer/create/st1',
});

const bodyRows = () => Array.from(document.querySelectorAll('.rt-tbody .rt-tr'));
const button = (label) => screen.getByText(label).closest('button');

// The check step leaves the single page app by assigning window.location.
const realLocation = window.location;

describe('stock transfer wizard, check step', () => {
  beforeEach(() => {
    delete window.location;
    window.location = { href: '' };
    current = transfer('APPROVED');
    answer(apiClient.get, [['/api/stockTransfers/st1', () => ({ data: current })]]);
    apiClient.put.mockResolvedValue({ data: { data: current } });
    jest.spyOn(Alert, 'success').mockImplementation(() => null);
  });

  afterEach(() => {
    window.location = realLocation;
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  const goToCheckStep = async () => {
    const utils = renderWizard();
    await screen.findByText('Bandage');
    fireEvent.click(button('Next'));
    await screen.findByText('Complete Stock Transfer');
    await screen.findByText('Bandage');
    apiClient.put.mockClear();
    return utils;
  };

  it('opens a completed transfer read-only on the check step', async () => {
    current = transfer('COMPLETED');
    renderWizard();

    // The wizard opens on the quantities step and moves on once it sees the status.
    expect(await screen.findByText('Exit')).toBeInTheDocument();
    await waitFor(() => expect(bodyRows()).toHaveLength(2));
    expect(screen.getByText('Split bin')).toBeInTheDocument();
    // The canceled line is left out; its split line is shown.
    expect(screen.queryByText('Dest-i1')).not.toBeInTheDocument();
    expect(button('Previous')).toBeDisabled();
    expect(button('Complete Stock Transfer')).toBeDisabled();

    fireEvent.click(button('Exit'));
    expect(window.location).toBe('/openboxes/stockTransfer/show/st1');
  });

  it('completes the transfer with the lines as the API sent them', async () => {
    await goToCheckStep();
    expect(screen.queryByText('Exit')).not.toBeInTheDocument();

    fireEvent.click(button('Complete Stock Transfer'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    const [url, payload] = apiClient.put.mock.calls[0];
    expect(url).toBe('/api/stockTransfers/st1');
    expect(payload.status).toBe('COMPLETED');
    expect(payload.stockTransferItems.map(({ id, splitItems }) => [id, splitItems.length]))
      .toEqual([['i1', 1], ['i2', 0]]);
    await waitFor(() => expect(window.location).toBe('/openboxes/stockTransfer/show/st1'));
    expect(Alert.success).toHaveBeenCalledWith('Stock transfer was successfully completed!', { timeout: 3000 });
  });

  // KNOWN-GAP: OBAF compares item.transferQty (absent from the API item), so it never warns.
  it.skip('warns about stock left in the receiving bin before completing', async () => {
    current = { ...transfer('APPROVED'), stockTransferItems: [ITEMS[0], item('i2', 'Bandage', 40)] };
    await goToCheckStep();

    fireEvent.click(button('Complete Stock Transfer'));

    const warnings = await screen.findAllByText(/is still in the receiving bin/);
    expect(warnings).toHaveLength(1);
    const [warning] = warnings;
    expect(warning).toHaveTextContent('60');
    expect(warning).toHaveTextContent('Bandage');
    expect(apiClient.put).not.toHaveBeenCalled();
    fireEvent.click(screen.getByText('Yes'));
    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    expect(apiClient.put.mock.calls[0][1].status).toBe('COMPLETED');
  });

  it('stays on the check step when completing fails', async () => {
    const { store } = await goToCheckStep();
    apiClient.put.mockRejectedValue(new Error('conflict'));

    fireEvent.click(button('Complete Stock Transfer'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(Alert.success).not.toHaveBeenCalled();
    expect(window.location).toEqual({ href: '' });
  });

  it('saves without completing and goes back to the quantities step', async () => {
    await goToCheckStep();

    fireEvent.click(button('Previous'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    const payload = apiClient.put.mock.calls[0][1];
    expect(payload.status).toBe('APPROVED');
    expect(payload.stockTransferItems.map(({ id }) => id)).toEqual(['i1', 'i2']);
    expect(await screen.findByText('Autofill quantities')).toBeInTheDocument();
  });

  it('stays on the check step when going back fails', async () => {
    const { store } = await goToCheckStep();
    apiClient.put.mockRejectedValue(new Error('conflict'));

    fireEvent.click(button('Previous'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.getByText('Complete Stock Transfer')).toBeInTheDocument();
  });

  it('filters the lines by product', async () => {
    await goToCheckStep();

    fireEvent.change(document.querySelectorAll('.rt-thead.-filters input')[1], { target: { value: 'aspi' } });

    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(within(bodyRows()[0]).getByText('Split bin')).toBeInTheDocument();
  });

  it('hides the spinner when the transfer cannot be loaded', async () => {
    current = transfer('COMPLETED');
    // The quantities step and the wizard load it first; the check step's own load fails.
    answer(apiClient.get, [['/api/stockTransfers/st1', (url) => (apiClient.get.mock.calls
      .filter(([called]) => called === url).length > 2 ? new Error('gone') : { data: current })]]);
    const { store } = renderWizard();

    await waitFor(() => expect(apiClient.get.mock.calls
      .filter(([url]) => url === '/api/stockTransfers/st1')).toHaveLength(3));
    await screen.findByText('Exit');
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    // The step keeps showing the lines the wizard loaded.
    expect(within(bodyRows()[1]).getByText('Bandage')).toBeInTheDocument();
  });
});
