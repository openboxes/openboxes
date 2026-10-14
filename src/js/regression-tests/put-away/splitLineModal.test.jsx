import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';
import {
  PATH, pickOption, putawayItem,
} from 'regression-tests/put-away/putAwayFixtures';

import '@testing-library/jest-dom';

// OBAF still exports flattenRequest (an identity function in OBAF); the shared mock predates
// it.
jest.mock('utils/apiClient', () => ({
  // eslint-disable-next-line global-require
  ...require('regression-tests/__mocks__/screenApiClient').default(),
  flattenRequest: (...args) => jest.requireActual('utils/apiClient').flattenRequest(...args),
}));
// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));
// SplitLineModal imports the ES-module build of connect, which jest does not transform.
jest.mock('react-redux/es/connect/connect', () => jest.requireActual('react-redux/lib/connect/connect'));

/* eslint-disable import/first,import/order */
import PutAwayMainPage from 'components/put-away/PutAwayMainPage';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const BIN_A = {
  id: 'b1', name: 'A-01', zoneId: 'z1', zoneName: 'Zone A',
};
const BIN_B = { id: 'b2', name: 'B-02' };

const renderWithLine = (line) => {
  answer(apiClient.get, [
    ['/api/putaways/pa1', {
      data: {
        id: 'pa1', putawayNumber: 'PA-1', putawayStatus: 'PENDING', putawayItems: [line],
      },
    }],
    ['/api/internalLocations', { data: [BIN_A, BIN_B] }],
  ]);
  answer(apiClient.post, [['/api/putaways*', (url, body) => ({ data: body })]]);
  return renderAt({
    component: PutAwayMainPage, path: PATH, entry: '/openboxes/putaway/create/pa1',
  });
};

const openModal = async () => {
  fireEvent.click(await screen.findByTestId('open-modal'));
  return within(screen.getByTestId('split-line-modal'));
};

const LINE = putawayItem({
  id: 'p1', name: 'Paracetamol', quantity: 10, putawayLocation: BIN_A,
});

describe('put-away, split line modal', () => {
  afterEach(() => jest.clearAllMocks());

  it('splits a line over two bins and saves the split', async () => {
    renderWithLine(LINE);
    const modal = await openModal();

    expect(modal.getByText('P1')).toBeInTheDocument();
    expect(modal.getByTestId('expiration-date')).toHaveTextContent('01/15/2030');
    expect(modal.getByTestId('total-qty')).toHaveTextContent('10');
    expect(modal.getByTestId('putaway-qty')).toHaveTextContent('10');
    // The proposed split is the whole line into its current put-away bin.
    expect(modal.getByTestId('bin-select')).toHaveTextContent('A-01');
    expect(modal.getByTestId('quantity-input')).toHaveValue(10);

    fireEvent.click(modal.getByTestId('add-line-button'));
    expect(modal.getAllByTestId('quantity-input')).toHaveLength(2);
    // The new line has no bin and no quantity yet.
    expect(modal.getByTestId('save-button')).toBeDisabled();

    pickOption(modal.getAllByTestId('bin-select')[1], 'B-02');
    fireEvent.change(modal.getAllByTestId('quantity-input')[0], { target: { value: '6' } });
    fireEvent.change(modal.getAllByTestId('quantity-input')[1], { target: { value: '4' } });
    expect(modal.getByTestId('putaway-qty')).toHaveTextContent('10');
    expect(modal.getByTestId('save-button')).toBeEnabled();

    fireEvent.click(modal.getByTestId('save-button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const { splitItems } = apiClient.post.mock.calls[0][1].putawayItems[0];
    expect(splitItems.map((item) => [item.putawayLocation.id, item.quantity]))
      .toEqual([['b1', '6'], ['b2', '4']]);
    expect(splitItems[1]).toMatchObject({
      product: { id: 'p1' }, inventoryItem: { id: 'ii-p1' }, currentLocation: { id: 'recv' },
    });
    expect(screen.queryByTestId('split-line-modal')).not.toBeInTheDocument();
    // The bin column now reads "Split line" instead of offering a bin.
    await waitFor(() => expect(screen.queryByTestId('select-bin')).not.toBeInTheDocument());
  });

  it('rejects a split above the line quantity or below one', async () => {
    renderWithLine(LINE);
    const modal = await openModal();

    fireEvent.change(modal.getByTestId('quantity-input'), { target: { value: '11' } });
    expect(modal.getByTestId('save-button')).toBeDisabled();
    expect(modal.getByTestId('quantity-input').closest('.has-error')).not.toBeNull();

    fireEvent.change(modal.getByTestId('quantity-input'), { target: { value: '-1' } });
    expect(modal.getByTestId('save-button')).toBeDisabled();

    fireEvent.change(modal.getByTestId('quantity-input'), { target: { value: '10' } });
    expect(modal.getByTestId('save-button')).toBeEnabled();
    expect(modal.getByTestId('quantity-input').closest('.has-error')).toBeNull();
  });

  it('asks before leaving stock in receiving and saves only on "No"', async () => {
    renderWithLine(LINE);
    const modal = await openModal();

    fireEvent.change(modal.getByTestId('quantity-input'), { target: { value: '7' } });
    fireEvent.click(modal.getByTestId('save-button'));

    expect(await screen.findByText('Confirm split line')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Yes'));
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(screen.getByTestId('split-line-modal')).toBeInTheDocument();

    fireEvent.click(modal.getByTestId('save-button'));
    fireEvent.click(await screen.findByText('No'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    expect(apiClient.post.mock.calls[0][1].putawayItems[0].splitItems)
      .toEqual([expect.objectContaining({ quantity: '7' })]);
  });

  it('drops unsaved new lines and flags saved ones for deletion', async () => {
    const saved = {
      id: 'split1', quantity: 4, putawayLocation: BIN_B, product: { id: 'p1' }, inventoryItem: { id: 'ii-p1' },
    };
    const other = {
      id: 'split2', quantity: 6, putawayLocation: BIN_A, product: { id: 'p1' }, inventoryItem: { id: 'ii-p1' },
    };
    renderWithLine({ ...LINE, splitItems: [saved, other] });
    const modal = await openModal();
    expect(modal.getAllByTestId('quantity-input').map((input) => input.value)).toEqual(['4', '6']);

    fireEvent.click(modal.getByTestId('add-line-button'));
    expect(modal.getAllByTestId('quantity-input')).toHaveLength(3);
    fireEvent.click(modal.getAllByTestId('delete-button')[2]);
    expect(modal.getAllByTestId('quantity-input')).toHaveLength(2);

    fireEvent.click(modal.getAllByTestId('delete-button')[0]);
    expect(modal.getAllByTestId('quantity-input').map((input) => input.value)).toEqual(['6']);
    expect(modal.getByTestId('putaway-qty')).toHaveTextContent('6');

    fireEvent.click(modal.getByTestId('save-button'));
    fireEvent.click(await screen.findByText('No'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(apiClient.post.mock.calls[0][1].putawayItems[0].splitItems).toEqual([
      expect.objectContaining({ id: 'split1', delete: true }),
      expect.objectContaining({ id: 'split2', quantity: 6 }),
    ]);
  });

  it('closes without saving on cancel', async () => {
    renderWithLine(LINE);
    const modal = await openModal();

    fireEvent.click(modal.getByTestId('add-line-button'));
    fireEvent.click(modal.getByTestId('cancel-button'));

    expect(screen.queryByTestId('split-line-modal')).not.toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('editing a split line makes its quantity editable and marks the old split for deletion', async () => {
    const split = {
      id: 'split1', quantity: 10, putawayLocation: BIN_B, product: { id: 'p1' }, inventoryItem: { id: 'ii-p1' },
    };
    renderWithLine({ ...LINE, splitItems: [split] });
    await screen.findByText('Paracetamol');
    expect(screen.queryByTestId('select-bin')).not.toBeInTheDocument();

    fireEvent.click(screen.getAllByTestId('edit-button')[0]);
    fireEvent.change(await screen.findByTestId('quantity-input'), { target: { value: '8' } });
    fireEvent.click(screen.getByTestId('save-button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(apiClient.post.mock.calls[0][1].putawayItems[0]).toMatchObject({
      quantity: '8',
      splitItems: [{ id: 'split1', delete: true }],
    });
  });
});
