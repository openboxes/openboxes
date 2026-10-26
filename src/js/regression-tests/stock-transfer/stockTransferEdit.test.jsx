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

const item = (id, name, quantityNotPicked, extra = {}) => ({
  id,
  product: { productCode: `C-${id}`, name },
  lotNumber: `LOT-${id}`,
  originZone: 'Zone A',
  originBinLocation: { id: `bin-${id}`, name: `A-${id}` },
  destinationBinLocation: { id: 'b1', name: 'B-1' },
  destinationZone: { id: 'z1', name: 'Z1' },
  quantityOnHand: quantityNotPicked,
  quantityNotPicked,
  quantity: quantityNotPicked,
  status: 'PENDING',
  splitItems: [],
  ...extra,
});

const transfer = (status, items) => ({
  id: 'st1',
  stockTransferNumber: 'ST-0001',
  status,
  stockTransferItems: items,
});

const BINS = [
  { id: 'b2', name: 'B-2', zoneName: 'Z1' },
  { id: 'b1', name: 'B-1', zoneName: 'Z1' },
  { id: 'b3', name: 'Loose bin' },
];

let current;

const answerTransfer = () => answer(apiClient.get, [
  ['/api/stockTransfers/st1', () => ({ data: current })],
  ['/api/internalLocations', { data: BINS }],
]);

const renderWizard = () => renderAt({
  component: StockTransferWizard,
  path: PATH,
  entry: '/openboxes/stockTransfer/create/st1',
});

const bodyRows = () => Array.from(document.querySelectorAll('.rt-tbody .rt-tr'));
const quantityInputs = () => Array.from(document.querySelectorAll('.rt-tbody input[type="number"]'));
const button = (label) => screen.getByText(label).closest('button');
const setQuantity = (index, value) =>
  fireEvent.change(quantityInputs()[index], { target: { value } });
const putPayload = (call = 0) => apiClient.put.mock.calls[call][1];

describe('stock transfer wizard, quantities step', () => {
  beforeEach(() => {
    current = transfer('PENDING', [item('i1', 'Aspirin', 80), item('i2', 'Bandage', 5)]);
    answerTransfer();
    apiClient.put.mockImplementation((url, payload) => {
      current = { ...payload, stockTransferItems: payload.stockTransferItems };
      return Promise.resolve({ data: { data: current } });
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('opens an existing pending transfer with blank quantities and loads the bins', async () => {
    renderWizard();

    expect(await screen.findByText('Aspirin')).toBeInTheDocument();
    expect(screen.getByText('ST-0001')).toBeInTheDocument();
    expect(quantityInputs().map((input) => input.value)).toEqual(['', '']);
    expect(button('Next')).toBeDisabled();

    const binCall = apiClient.get.mock.calls.find(([url]) => url === '/api/internalLocations');
    expect(binCall[1].params).toEqual({
      'location.id': 'loc1',
      locationTypeCode: ['BIN_LOCATION', 'INTERNAL'],
      ignoreActivityCodes: ['RECEIVE_STOCK'],
    });
    expect(binCall[1].paramsSerializer({ a: ['x', 'y'] })).toBe('a=x&a=y');
  });

  it('flags quantities that are missing, too high or negative', async () => {
    renderWizard();
    await screen.findByText('Aspirin');

    setQuantity(0, '81');
    expect(quantityInputs()[0].closest('div')).toHaveClass('has-error');
    setQuantity(0, '-1');
    expect(quantityInputs()[0].closest('div')).toHaveClass('has-error');
    setQuantity(0, '80');
    expect(quantityInputs()[0].closest('div')).not.toHaveClass('has-error');
    // The second line is still blank.
    expect(quantityInputs()[1].closest('div')).toHaveClass('has-error');
    expect(button('Next')).toBeDisabled();

    setQuantity(1, '5');
    expect(button('Next')).not.toBeDisabled();
  });

  it('autofills every line and saves it as approved before moving to the check step', async () => {
    renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(button('Autofill quantities'));
    expect(quantityInputs().map((input) => input.value)).toEqual(['80', '5']);

    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    expect(apiClient.put.mock.calls[0][0]).toBe('/api/stockTransfers/st1');
    const payload = putPayload();
    expect(payload.status).toBe('APPROVED');
    expect(payload.stockTransferItems
      .map(({ id, quantity, splitItems }) => ({ id, quantity, splitItems })))
      .toEqual([
        { id: 'i1', quantity: 80, splitItems: [] },
        { id: 'i2', quantity: 5, splitItems: [] },
      ]);
    expect(await screen.findByText('Complete Stock Transfer')).toBeInTheDocument();
  });

  it('saves without leaving the step and keeps the saved quantities', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    setQuantity(0, '30');

    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    // A blank quantity is sent as the quantity on hand.
    expect(putPayload().stockTransferItems.map(({ quantity }) => quantity)).toEqual(['30', 5]);
    await waitFor(() => expect(quantityInputs().map((input) => input.value)).toEqual(['30', '5']));
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
  });

  it('saves and opens the printable transfer', async () => {
    const open = jest.spyOn(window, 'open').mockImplementation(() => null);
    renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(button('Generate Stock Transfer'));

    await waitFor(() => expect(open).toHaveBeenCalledWith('/openboxes/stockTransfer/print/st1', '_blank'));
    expect(apiClient.put).toHaveBeenCalledTimes(1);
    open.mockRestore();
  });

  it('does not open the printable transfer when saving fails', async () => {
    const open = jest.spyOn(window, 'open').mockImplementation(() => null);
    apiClient.put.mockRejectedValue(new Error('conflict'));
    const { store } = renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(button('Generate Stock Transfer'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(open).not.toHaveBeenCalled();
    expect(screen.getByText('Autofill quantities')).toBeInTheDocument();
    open.mockRestore();
  });

  it('splits a saved line into two new lines and sends them as split items', async () => {
    renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(within(bodyRows()[0]).getByText('Split line'));

    expect(bodyRows()).toHaveLength(4);
    // The original line is canceled and locked; the new lines cannot be split again.
    expect(quantityInputs()[0]).toBeDisabled();
    expect(screen.getAllByText('Split line')).toHaveLength(2);

    setQuantity(1, '50');
    setQuantity(2, '40');
    setQuantity(3, '5');
    // 50 + 40 is more than the 80 left in the bin.
    expect(quantityInputs()[1].closest('div')).toHaveClass('has-error');
    expect(button('Next')).toBeDisabled();

    setQuantity(2, '30');
    expect(quantityInputs()[1].closest('div')).not.toHaveClass('has-error');
    expect(button('Next')).not.toBeDisabled();

    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    const [aspirin, bandage] = putPayload().stockTransferItems;
    expect(aspirin.status).toBe('CANCELED');
    expect(aspirin.splitItems.map(({ referenceId, quantity }) => ({ referenceId, quantity })))
      .toEqual([{ referenceId: 'i1', quantity: '50' }, { referenceId: 'i1', quantity: '30' }]);
    expect(bandage.splitItems).toEqual([]);
  });

  it('re-enables the original line once its unsaved split lines are deleted', async () => {
    renderWizard();
    await screen.findByText('Aspirin');
    fireEvent.click(within(bodyRows()[0]).getByText('Split line'));
    expect(bodyRows()).toHaveLength(4);

    fireEvent.click(within(bodyRows()[1]).getByText('Delete'));
    expect(bodyRows()).toHaveLength(3);
    expect(quantityInputs()[0]).toBeDisabled();

    fireEvent.click(within(bodyRows()[1]).getByText('Delete'));
    expect(bodyRows()).toHaveLength(2);
    expect(quantityInputs()[0]).not.toBeDisabled();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it('adds one more line when splitting a line that is already canceled', async () => {
    current = transfer('APPROVED', [item('i1', 'Aspirin', 80, {
      status: 'CANCELED',
      splitItems: [item('s1', 'Aspirin', 80, { quantity: 20, status: 'APPROVED' })],
    })]);
    renderWizard();
    await screen.findAllByText('Aspirin');
    expect(bodyRows()).toHaveLength(2);
    // Saved lines show their saved quantity; the split line takes the quantity on hand of its line.
    expect(quantityInputs().map((input) => input.value)).toEqual(['80', '20']);

    fireEvent.click(within(bodyRows()[0]).getByText('Split line'));

    expect(bodyRows()).toHaveLength(3);
    expect(quantityInputs()[1].value).toBe('80');
  });

  it('deletes a saved line through the API, together with its split lines', async () => {
    current = transfer('APPROVED', [
      item('i1', 'Aspirin', 80, {
        status: 'CANCELED',
        splitItems: [item('s1', 'Aspirin', 80, { quantity: 20 })],
      }),
      item('i2', 'Bandage', 5),
    ]);
    apiClient.delete.mockResolvedValue({ data: { data: transfer('APPROVED', []) } });
    renderWizard();
    await screen.findByText('Bandage');
    expect(bodyRows()).toHaveLength(3);

    fireEvent.click(within(bodyRows()[0]).getByText('Delete'));

    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransferItems/i1');
    expect(screen.queryByText('Aspirin')).not.toBeInTheDocument();
  });

  it('re-enables the original line after its last saved split line is deleted', async () => {
    current = transfer('APPROVED', [item('i1', 'Aspirin', 80, {
      status: 'CANCELED',
      splitItems: [item('s1', 'Aspirin', 80, { quantity: 20, referenceId: 'i1' })],
    })]);
    apiClient.delete.mockResolvedValue({ data: { data: transfer('APPROVED', []) } });
    renderWizard();
    await screen.findAllByText('Aspirin');
    expect(quantityInputs()[0]).toBeDisabled();

    fireEvent.click(within(bodyRows()[1]).getByText('Delete'));

    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransferItems/s1');
    expect(quantityInputs()[0]).not.toBeDisabled();
  });

  it('keeps the line when deleting it fails', async () => {
    apiClient.delete.mockRejectedValue(new Error('locked'));
    const { store } = renderWizard();
    await screen.findByText('Aspirin');

    fireEvent.click(within(bodyRows()[1]).getByText('Delete'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stockTransferItems/i2'));
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(bodyRows()).toHaveLength(2);
  });

  it('filters the lines by product and by destination bin', async () => {
    current = transfer('APPROVED', [
      item('i1', 'Aspirin', 80),
      item('i2', 'Bandage', 5, { destinationBinLocation: { id: 'b3', name: 'Loose bin' } }),
    ]);
    renderWizard();
    await screen.findByText('Aspirin');
    const filters = document.querySelectorAll('.rt-thead.-filters input');

    fireEvent.change(filters[1], { target: { value: 'band' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));

    fireEvent.change(filters[1], { target: { value: '' } });
    // OBAF adds a Reason for Shortage column before Qty to Transfer, so the destination bin filter
    // is one further right.
    fireEvent.change(filters[9], { target: { value: 'loose' } });
    await waitFor(() => expect(bodyRows()).toHaveLength(1));
    expect(screen.getByText('Bandage')).toBeInTheDocument();
  });

  it('shows the saved destination bins', async () => {
    current = transfer('APPROVED', [item('i1', 'Aspirin', 80)]);
    renderWizard();
    await screen.findByText('Aspirin');

    expect(within(bodyRows()[0]).getByText('B-1')).toBeInTheDocument();
  });

  it('sends the bin chosen as destination, offering bins grouped by zone', async () => {
    // The select menu renders into the page's #root element.
    const root = document.createElement('div');
    root.id = 'root';
    document.body.appendChild(root);
    renderWizard();
    await screen.findByText('Aspirin');

    // OBAF adds a Reason for Shortage select (also .select-xs) before the destination bin select.
    const select = bodyRows()[0].querySelectorAll('.select-xs')[1].querySelector('input');
    fireEvent.focus(select);
    fireEvent.keyDown(select, { key: 'ArrowDown', keyCode: 40 });
    const menu = await screen.findByTestId('custom-select-dropdown-menu');
    expect(within(menu).getByText('Z1')).toBeInTheDocument();
    const options = within(menu).getAllByText(/^(B-1|B-2|Loose bin)$/).map((node) => node.textContent);
    // Zoned bins come first, the bins without a zone after them.
    expect(options[2]).toBe('Loose bin');
    expect(options.slice(0, 2).sort()).toEqual(['B-1', 'B-2']);
    fireEvent.click(within(menu).getByText('B-2'));
    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    expect(putPayload().stockTransferItems[0].destinationBinLocation).toEqual(expect.objectContaining({ id: 'b2' }));
    root.remove();
  });

  it('stays on the step when saving fails', async () => {
    apiClient.put.mockRejectedValue(new Error('conflict'));
    const { store } = renderWizard();
    await screen.findByText('Aspirin');
    fireEvent.click(button('Autofill quantities'));

    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.put).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.queryByText('Complete Stock Transfer')).not.toBeInTheDocument();
  });

  it('hides the spinner when the transfer cannot be loaded', async () => {
    answer(apiClient.get, [['/api/stockTransfers/st1', new Error('gone')], ['/api/internalLocations', new Error('gone')]]);
    const { store } = renderWizard();

    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stockTransfers/st1'));
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(bodyRows()).toHaveLength(0);
  });
});
