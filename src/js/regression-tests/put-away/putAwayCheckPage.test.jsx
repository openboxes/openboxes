import { fireEvent, screen, waitFor } from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';
import {
  PATH, putawayItem,
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
jest.mock('react-s-alert', () => ({ __esModule: true, default: { success: jest.fn() } }));

/* eslint-disable import/first,import/order */
import Alert from 'react-s-alert';

import PutAwayMainPage from 'components/put-away/PutAwayMainPage';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const BIN_A = {
  id: 'b1', name: 'A-01', zoneId: 'z1', zoneName: 'Zone A',
};
const BIN_B = { id: 'b2', name: 'B-02' };

const SPLIT_LINE = putawayItem({
  id: 'p1',
  name: 'Paracetamol',
  quantity: 10,
  putawayLocation: BIN_A,
  preferredBin: BIN_A,
  splitItems: [
    {
      id: 's1', quantity: 6, putawayLocation: BIN_A, putawayFacility: { id: 'loc1' },
    },
    {
      id: 's2', quantity: 4, putawayLocation: BIN_B, putawayFacility: { id: 'loc1' },
    },
  ],
});
const PLAIN_LINE = putawayItem({
  id: 'p2', name: 'Bandage', quantity: 5, putawayLocation: BIN_B, sm: 'sm2',
});

/**
 * Opens a pending put-away and clicks "Next"; the save answers with `saved` lines, the way the
 * API returns them (bins always as objects).
 */
const renderCheckPage = async (lines, saved = lines) => {
  const putAway = {
    id: 'pa1', putawayNumber: 'PA-1', putawayStatus: 'PENDING', putawayItems: lines,
  };
  answer(apiClient.get, [
    ['/api/putaways/pa1', { data: putAway }],
    ['/api/internalLocations', { data: [BIN_A, BIN_B] }],
  ]);
  apiClient.post.mockResolvedValueOnce({ data: { data: { ...putAway, putawayItems: saved } } });
  const rendered = renderAt({
    component: PutAwayMainPage, path: PATH, entry: '/openboxes/putaway/create/pa1',
  });
  fireEvent.click(await screen.findByTestId('next-button'));
  await screen.findAllByTestId('complete-putaway-button');
  apiClient.post.mockResolvedValue({ data: { data: {} } });
  return rendered;
};

const complete = () => fireEvent.click(screen.getAllByTestId('complete-putaway-button')[0]);
const completionPayload = () => apiClient.post.mock.calls[1][1];

describe('put-away, confirmation page', () => {
  afterEach(() => jest.clearAllMocks());

  it('shows each split as its own line and completes the put-away', async () => {
    await renderCheckPage([SPLIT_LINE, PLAIN_LINE]);

    // Two rows for the split line plus one for the plain line.
    expect(screen.getAllByText('Paracetamol')).toHaveLength(2);
    expect(screen.getByText('6')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getAllByText('B-02')).toHaveLength(2);

    complete();

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(2));
    expect(apiClient.post.mock.calls[1][0]).toBe('/api/putaways?location.id=loc1');
    const payload = completionPayload();
    expect(payload.putawayStatus).toBe('COMPLETED');
    expect(payload.putawayItems.map((item) => item.putawayStatus)).toEqual(['CANCELLED', 'COMPLETED']);
    expect(payload.putawayItems[0].splitItems.map((item) => [item.id, item.putawayStatus]))
      .toEqual([['s1', 'COMPLETED'], ['s2', 'COMPLETED']]);
    await waitFor(() => expect(Alert.success).toHaveBeenCalledWith('Putaway was successfully completed!', { timeout: 3000 }));
  });

  it('asks before completing when stock stays in receiving', async () => {
    const partial = { ...PLAIN_LINE, quantity: 3, quantityAvailable: 5 };
    await renderCheckPage([partial]);

    complete();
    expect(await screen.findByText(/of item/)).toHaveTextContent('Qty2 of item Bandage is still in the receiving bin');
    fireEvent.click(screen.getByText('No'));
    expect(apiClient.post).toHaveBeenCalledTimes(1);

    complete();
    fireEvent.click(await screen.findByText('Yes'));
    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(2));
    expect(completionPayload().putawayItems[0]).toMatchObject({ quantity: 3, putawayStatus: 'COMPLETED' });
  });

  it('asks before completing lines without a bin', async () => {
    const unbinned = putawayItem({ id: 'p3', name: 'Gloves', quantity: 2 });
    await renderCheckPage([unbinned], [{ ...unbinned, putawayLocation: { id: null } }]);

    complete();
    expect(await screen.findByText(/There are some lines with empty bin locations/)).toBeInTheDocument();
    fireEvent.click(screen.getByText('No'));
    expect(apiClient.post).toHaveBeenCalledTimes(1);

    complete();
    fireEvent.click(await screen.findByText('Yes'));
    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(2));
  });

  it('goes back to the bins page with the edit button', async () => {
    await renderCheckPage([PLAIN_LINE]);

    fireEvent.click(screen.getAllByTestId('edit-button')[0]);

    expect(await screen.findByTestId('next-button')).toBeInTheDocument();
    expect(screen.queryByTestId('complete-putaway-button')).not.toBeInTheDocument();
  });

  it('toggles the grouping and filters the lines', async () => {
    await renderCheckPage([SPLIT_LINE, PLAIN_LINE]);

    expect(screen.getByText('SM-sm1 (2)')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('show-by-button'));
    expect(screen.getByTestId('show-by-button')).toHaveTextContent('Product');
    expect(screen.queryByText('SM-sm1 (2)')).not.toBeInTheDocument();

    const filters = screen.getAllByPlaceholderText('Search');
    fireEvent.change(filters[1], { target: { value: 'band' } });
    expect(screen.queryByText('Paracetamol')).not.toBeInTheDocument();
    expect(screen.getByText('Bandage')).toBeInTheDocument();

    fireEvent.change(filters[1], { target: { value: '' } });
    fireEvent.change(filters[3], { target: { value: '01/15/2030' } });
    expect(screen.getAllByText('Paracetamol')).toHaveLength(2);

    fireEvent.click(screen.getByTestId('show-by-button'));
    expect(screen.getByTestId('show-by-button')).toHaveTextContent('Stock Movement');
  });

  it('shows a completed put-away read-only and leads back to the list', async () => {
    answer(apiClient.get, [
      ['/api/putaways/pa1', {
        data: {
          id: 'pa1', putawayNumber: 'PA-1', putawayStatus: 'COMPLETED', putawayItems: [PLAIN_LINE],
        },
      }],
    ]);
    const { currentPath } = renderAt({
      component: PutAwayMainPage, path: PATH, entry: '/openboxes/putaway/create/pa1',
    });

    expect(await screen.findByTestId('back-button')).toBeInTheDocument();
    expect(screen.getByText('Bandage')).toBeInTheDocument();
    expect(screen.queryByTestId('complete-putaway-button')).not.toBeInTheDocument();

    fireEvent.click(screen.getByTestId('back-button'));

    await waitFor(() => expect(currentPath()).toBe('/openboxes/putaway/create'));
    expect(await screen.findAllByTestId('start-putaway')).toHaveLength(2);
    expect(apiClient.get).toHaveBeenCalledWith('/api/putaways?location.id=loc1');
  });
});
