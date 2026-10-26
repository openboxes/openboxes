import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
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

const candidate = (code, name, quantityNotPicked, extra = {}) => ({
  product: { productCode: code, name },
  lotNumber: `LOT-${code}`,
  originZone: 'Zone A',
  originBinLocation: { id: `bin-${code}`, name: `A-${code}` },
  quantityOnHand: quantityNotPicked + 10,
  quantityNotPicked,
  ...extra,
});

const CANDIDATES = [
  candidate('P1', 'Aspirin', 1200),
  candidate('P2', 'Bandage', 5, { product: { productCode: 'P2', name: 'Bandage', displayName: 'Gauze bandage' } }),
  candidate('P3', 'Catheter', 0, { expirationDate: new Date('2027-01-02T12:00:00').toISOString() }),
];

const EXPIRED = [candidate('P9', 'Expired saline', 3)];

const renderWizard = () => renderAt({
  component: StockTransferWizard,
  path: PATH,
  entry: '/openboxes/stockTransfer/create',
});

const candidateCalls = () => apiClient.get.mock.calls
  .map(([url]) => url)
  .filter((url) => url.startsWith('/api/stockTransfers/candidates'));

const bodyRows = () => Array.from(document.querySelectorAll('.rt-tbody .rt-tr'));

const selectedCount = () => document.querySelector('.count-selected');

const rowOf = (text) => screen.getByText(text).closest('.rt-tr');

describe('stock transfer wizard, create step', () => {
  beforeEach(() => {
    answer(apiClient.get, [
      ['/api/stockTransfers/candidates*', (url) => ({
        data: url.includes('showExpiredItemsOnly=true') ? EXPIRED : CANDIDATES,
      })],
    ]);
  });

  afterEach(() => jest.clearAllMocks());

  it('lists the candidates of the current location and shows the info bar', async () => {
    const { store } = renderWizard();

    expect(await screen.findByText('Aspirin')).toBeInTheDocument();
    expect(candidateCalls()).toEqual([
      '/api/stockTransfers/candidates?location.id=loc1&showExpiredItemsOnly=false',
    ]);
    expect(screen.getByText('Create Stock Transfer')).toBeInTheDocument();
    // The product cell shows the display name when there is one.
    expect(screen.getByText('Gauze bandage')).toBeInTheDocument();
    // Quantities are formatted with thousands separators.
    expect(within(rowOf('Aspirin')).getByText('1,200')).toBeInTheDocument();
    expect(within(rowOf('Aspirin')).getByText('1,210')).toBeInTheDocument();
    expect(bodyRows()).toHaveLength(3);
    expect(store.getState().infoBar.bars.STOCK_TRANSFER_DESCRIPTION).toBeDefined();
    expect(store.getState().spinner.show).toBe(false);
  });

  it('keeps start disabled until a line is selected and cannot select an empty line', async () => {
    renderWizard();
    await screen.findByText('Aspirin');

    const startButtons = screen.getAllByText('Start Stock Transfer', { selector: 'button *, button' })
      .map((node) => node.closest('button'));
    startButtons.forEach((button) => expect(button).toBeDisabled());
    expect(within(rowOf('Catheter')).getByRole('checkbox')).toBeDisabled();

    fireEvent.click(within(rowOf('Aspirin')).getByRole('checkbox'));

    expect(selectedCount()).toHaveTextContent('1 Selected');
    startButtons.forEach((button) => expect(button).not.toBeDisabled());

    // Clicking again deselects.
    fireEvent.click(within(rowOf('Aspirin')).getByRole('checkbox'));
    expect(selectedCount()).toHaveTextContent('0 Selected');
  });

  it('selects every line with quantity left when the header box is checked, and clears them again', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    const selectAll = document.querySelector('.rt-thead .rt-th input[type="checkbox"]');

    fireEvent.click(selectAll);
    expect(selectedCount()).toHaveTextContent('2 Selected');
    expect(within(rowOf('Aspirin')).getByRole('checkbox')).toBeChecked();
    expect(within(rowOf('Catheter')).getByRole('checkbox')).not.toBeChecked();

    fireEvent.click(selectAll);
    expect(selectedCount()).toHaveTextContent('0 Selected');
  });

  it('filters the lines by product name and by code', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    const filters = document.querySelectorAll('.rt-thead.-filters input');

    // Second filter: product (matches the name or the display name).
    fireEvent.change(filters[1], { target: { value: 'gauze' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(screen.getByText('Gauze bandage')).toBeInTheDocument();

    fireEvent.change(filters[1], { target: { value: '' } });
    fireEvent.change(filters[0], { target: { value: 'p3' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(screen.getByText('Catheter')).toBeInTheDocument();
  });

  it('shows expiry dates in the configured format and filters on that text', async () => {
    renderWizard();
    await screen.findByText('Aspirin');

    expect(await within(rowOf('Catheter')).findByText('01/02/2027')).toBeInTheDocument();

    fireEvent.change(document.querySelectorAll('.rt-thead.-filters input')[3], { target: { value: '01/02' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(screen.getByText('Catheter')).toBeInTheDocument();
  });

  it('selects all only the filtered lines', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    const filters = document.querySelectorAll('.rt-thead.-filters input');
    fireEvent.change(filters[1], { target: { value: 'aspirin' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));

    fireEvent.click(document.querySelector('.rt-thead .rt-th input[type="checkbox"]'));

    expect(selectedCount()).toHaveTextContent('1 Selected');
  });

  it('switches to expired items and back', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    const toggle = screen.getByText('Expired Items').closest('button');
    expect(toggle).toHaveClass('btn-primary');

    fireEvent.click(toggle);

    expect(await screen.findByText('Expired saline')).toBeInTheDocument();
    expect(screen.queryByText('Aspirin')).not.toBeInTheDocument();
    expect(toggle).toHaveClass('btn-outline-primary');
    // The toggle refetches without a location: the backend falls back to the session location.
    expect(candidateCalls()[1]).toBe('/api/stockTransfers/candidates?showExpiredItemsOnly=true');

    fireEvent.click(toggle);
    expect(await screen.findByText('Aspirin')).toBeInTheDocument();
  });

  it('creates the transfer from the selected lines and moves to the second step', async () => {
    const created = {
      id: 'st1',
      stockTransferNumber: 'ST-0001',
      status: 'PENDING',
      stockTransferItems: [],
    };
    apiClient.post.mockResolvedValue({ data: { data: created } });
    const { currentPath } = renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(within(rowOf('Aspirin')).getByRole('checkbox'));
    fireEvent.click(within(rowOf('Gauze bandage')).getByRole('checkbox'));
    fireEvent.click(screen.getAllByText('Start Stock Transfer', { selector: 'button *, button' })[1].closest('button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/stockTransfers/');
    expect(payload.stockTransferItems.map((item) => item.product.productCode)).toEqual(['P1', 'P2']);
    await waitFor(() => expect(currentPath()).toBe('/openboxes/stockTransfer/create/st1'));
    expect(await screen.findByText('ST-0001')).toBeInTheDocument();
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
  });

  it('stays on the step when creating fails', async () => {
    apiClient.post.mockRejectedValue(new Error('boom'));
    const { currentPath, store } = renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(within(rowOf('Aspirin')).getByRole('checkbox'));
    fireEvent.click(screen.getAllByText('Start Stock Transfer', { selector: 'button *, button' })[1].closest('button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(currentPath()).toBe('/openboxes/stockTransfer/create');
    expect(screen.getByText('Aspirin')).toBeInTheDocument();
  });

  it('hides the spinner when the candidates cannot be loaded', async () => {
    answer(apiClient.get, [['/api/stockTransfers/candidates*', new Error('down')]]);
    const { store } = renderWizard();

    await waitFor(() => expect(candidateCalls()).toHaveLength(1));
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(bodyRows()).toHaveLength(0);
  });
});
