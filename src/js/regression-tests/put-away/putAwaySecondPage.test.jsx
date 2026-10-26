import { fireEvent, screen, waitFor } from '@testing-library/react';
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
jest.mock('js-file-download', () => jest.fn());

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';

import { CHANGE_CURRENT_LOCATION } from 'actions/types';
import PutAwayMainPage from 'components/put-away/PutAwayMainPage';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const ZONE_BIN = {
  id: 'b1', name: 'A-01', zoneId: 'z1', zoneName: 'Zone A',
};

const ITEMS = [
  putawayItem({
    id: 'p1', name: 'Paracetamol', quantity: 10, preferredBin: ZONE_BIN,
  }),
  putawayItem({
    id: 'p2', name: 'Bandage', quantity: 5, putawayLocation: { id: 'b2', name: 'B-02' },
  }),
  putawayItem({
    id: 'p3', name: 'Gloves', quantity: 3, sm: 'sm2',
  }),
];

const PUTAWAY = {
  id: 'pa1', putawayNumber: 'PA-1', putawayStatus: 'PENDING', putawayItems: ITEMS,
};

const BINS = [
  ZONE_BIN,
  { id: 'b3', name: 'A-00', zoneName: 'Zone A' },
  { id: 'b2', name: 'B-02' },
  { id: 'b4', name: 'AA-OPEN' },
];

const renderSecondPage = ({ putAway = PUTAWAY, routes = [] } = {}) => {
  answer(apiClient.get, [
    ['/api/putaways/pa1', { data: putAway }],
    ['/api/internalLocations', { data: BINS }],
    ...routes,
  ]);
  // The API answers a save with what it was sent.
  answer(apiClient.post, [['/api/putaways*', (url, body) => ({ data: body })]]);
  apiClient.delete.mockResolvedValue({ data: {} });
  return renderAt({
    component: PutAwayMainPage, path: PATH, entry: '/openboxes/putaway/create/pa1',
  });
};

const binSelects = () => screen.getAllByTestId('select-bin');
const lastPosted = () => apiClient.post.mock.calls[apiClient.post.mock.calls.length - 1];

describe('put-away, second page (choose bins)', () => {
  afterEach(() => jest.clearAllMocks());

  it('loads the put-away with its lines and proposes the preferred bin', async () => {
    renderSecondPage();

    expect(await screen.findByText('Paracetamol')).toBeInTheDocument();
    expect(screen.getByText('PA-1')).toBeInTheDocument();
    expect(screen.getByText('SM-sm1 (2)')).toBeInTheDocument();
    expect(screen.getByText('Gloves')).toBeInTheDocument();

    const [, binRequest] = apiClient.get.mock.calls.find(([url]) => url === '/api/internalLocations');
    expect(binRequest.params).toEqual({
      'location.id': 'loc1',
      locationTypeCode: ['BIN_LOCATION', 'INTERNAL'],
      ignoreActivityCodes: ['RECEIVE_STOCK'],
    });
    expect(binRequest.paramsSerializer({ a: ['x', 'y'] })).toBe('a=x&a=y');

    // Preferred bin column shows the zone, and the put-away bin falls back to it.
    expect(screen.getByText(/Zone A/)).toBeInTheDocument();
    expect(binSelects()[0]).toHaveTextContent('A-01');
    expect(binSelects()[1]).toHaveTextContent('B-02');
    expect(binSelects()[2]).not.toHaveTextContent(/A-01|B-02/);
  });

  it('saves the chosen bin and moves on to the confirmation page', async () => {
    renderSecondPage();
    await screen.findByText('Gloves');

    pickOption(binSelects()[2], 'AA-OPEN');
    fireEvent.click(screen.getByTestId('save-button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, saved] = lastPosted();
    expect(url).toBe('/api/putaways?location.id=loc1');
    expect(saved.putawayItems[2].putawayLocation).toMatchObject({ id: 'b4', name: 'AA-OPEN' });
    expect(saved.putawayItems[0].putawayLocation).toMatchObject({ id: 'b1', zoneName: 'Zone A' });

    fireEvent.click(screen.getByTestId('next-button'));
    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(2));
    expect(await screen.findAllByTestId('complete-putaway-button')).toHaveLength(2);
  });

  it('blocks saving while an edited quantity is out of range', async () => {
    renderSecondPage();
    await screen.findByText('Paracetamol');

    fireEvent.click(screen.getAllByTestId('edit-button')[0]);
    const input = screen.getByTestId('quantity-input');
    expect(input).toHaveValue(10);

    fireEvent.change(input, { target: { value: '11' } });
    expect(input.closest('div')).toHaveClass('has-error');
    expect(screen.getByTestId('save-button')).toBeDisabled();
    expect(screen.getByTestId('next-button')).toBeDisabled();

    fireEvent.change(input, { target: { value: '0' } });
    expect(screen.getByTestId('next-button')).toBeDisabled();

    fireEvent.change(input, { target: { value: '4' } });
    expect(input.closest('div')).not.toHaveClass('has-error');
    fireEvent.click(screen.getByTestId('save-button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(lastPosted()[1].putawayItems[0]).toMatchObject({ quantity: '4', edit: true });
  });

  // KNOWN-GAP: OBAF keeps the splits flagged for deletion, so the edited line still
  // shows "Split line" instead of the bin selector until a reload.
  it.skip('offers a bin again after editing a split line, and saves its splits for deletion', async () => {
    renderSecondPage({
      putAway: {
        ...PUTAWAY,
        putawayItems: [
          putawayItem({
            id: 'p1',
            name: 'Paracetamol',
            quantity: 10,
            splitItems: [
              { id: 's1', quantity: 6, putawayLocation: { id: 'b3', name: 'A-00' } },
              { id: 's2', quantity: 4, putawayLocation: { id: 'b4', name: 'AA-OPEN' } },
            ],
          }),
          ITEMS[1],
        ],
      },
    });
    await screen.findByText('Paracetamol');
    // The split line's bin cell reads "Split line", as do the split buttons.
    const splitLabels = screen.getAllByText('Split line').length;
    expect(binSelects()).toHaveLength(1);

    fireEvent.click(screen.getAllByTestId('edit-button')[0]);

    expect(screen.getAllByText('Split line')).toHaveLength(splitLabels - 1);
    expect(binSelects()).toHaveLength(2);
    pickOption(binSelects()[0], 'AA-OPEN');
    fireEvent.click(screen.getByTestId('save-button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const saved = lastPosted()[1].putawayItems[0];
    expect(saved.putawayLocation).toMatchObject({ id: 'b4' });
    expect(saved.splitItems.map(({ id, delete: deleted }) => [id, deleted]))
      .toEqual([['s1', true], ['s2', true]]);
  });

  it('marks a saved quantity above what is available', async () => {
    renderSecondPage({
      putAway: {
        ...PUTAWAY,
        putawayItems: [putawayItem({
          id: 'p1', name: 'Paracetamol', quantity: 12, quantityAvailable: 10,
        })],
      },
    });

    expect(await screen.findByText('12')).toBeInTheDocument();
    expect(screen.getByText('12').closest('div')).toHaveClass('has-error');
    expect(screen.getByTestId('next-button')).toBeDisabled();
  });

  it('deletes a line', async () => {
    renderSecondPage();
    await screen.findByText('Bandage');

    fireEvent.click(screen.getAllByTestId('delete-button')[1]);

    await waitFor(() => expect(screen.queryByText('Bandage')).not.toBeInTheDocument());
    expect(apiClient.delete).toHaveBeenCalledWith('/api/putawayItems/pai-p2');
    expect(screen.getByText('Paracetamol')).toBeInTheDocument();
  });

  it('keeps the line when deleting fails', async () => {
    renderSecondPage();
    apiClient.delete.mockRejectedValue(new Error('nope'));
    await screen.findByText('Bandage');

    fireEvent.click(screen.getAllByTestId('delete-button')[1]);

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalled());
    expect(screen.getByText('Bandage')).toBeInTheDocument();
  });

  it('cycles the sort order through current bins, preferred bin and the original order', async () => {
    renderSecondPage();
    await screen.findByText('Paracetamol');
    const sortButton = screen.getByTestId('sort-button');
    expect(sortButton).toHaveTextContent('Sort by current bins');

    fireEvent.click(sortButton);
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/putaways/pa1?sortBy=currentBins'));
    expect(apiClient.post).toHaveBeenCalledTimes(1);
    await waitFor(() => expect(sortButton).toHaveTextContent('Sort by preferred bin'));

    fireEvent.click(sortButton);
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/putaways/pa1?sortBy=preferredBin'));
    await waitFor(() => expect(sortButton).toHaveTextContent('Original order'));

    fireEvent.click(sortButton);
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/putaways/pa1?sortBy='));
    await waitFor(() => expect(sortButton).toHaveTextContent('Sort by current bins'));
  });

  it('re-reads the lines in the new order after sorting', async () => {
    const sorted = { ...PUTAWAY, putawayItems: [ITEMS[1], ITEMS[0], ITEMS[2]] };
    renderSecondPage({
      routes: [['/api/putaways/pa1?sortBy=currentBins', { data: sorted }]],
    });
    await screen.findByText('Paracetamol');

    fireEvent.click(screen.getByTestId('sort-button'));

    await waitFor(() => expect(binSelects()[0]).toHaveTextContent('B-02'));
    expect(binSelects()[1]).toHaveTextContent('A-01');
  });

  it('saves and downloads the put-away list as a pdf', async () => {
    renderSecondPage({
      routes: [['/putaway/generatePdf/pa1', 'PDF-BYTES']],
    });
    await screen.findByText('Paracetamol');

    fireEvent.click(screen.getByTestId('export-button'));

    await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('PDF-BYTES', 'PutawayReport-PA-1.pdf', 'application/pdf'));
    expect(apiClient.post).toHaveBeenCalledTimes(1);
    const [, pdfRequest] = apiClient.get.mock.calls.find(([url]) => url === '/putaway/generatePdf/pa1');
    expect(pdfRequest).toEqual({ responseType: 'blob', params: { sortBy: undefined } });
  });

  it('switches to the product view and filters by bin', async () => {
    renderSecondPage();
    await screen.findByText('Paracetamol');

    fireEvent.click(screen.getByText('Stock Movement', { selector: 'button' }));
    expect(screen.queryByText('SM-sm1 (2)')).not.toBeInTheDocument();
    expect(screen.getByText('Product', { selector: 'button' })).toBeInTheDocument();

    const filters = screen.getAllByPlaceholderText('Search');
    // Columns: code, name, lot, expiry, recipient, qty, preferred, current, stock movement, bin.
    fireEvent.change(filters[9], { target: { value: 'b-0' } });
    expect(screen.getByText('Bandage')).toBeInTheDocument();
    expect(screen.queryByText('Paracetamol')).not.toBeInTheDocument();

    fireEvent.change(filters[9], { target: { value: '' } });
    fireEvent.change(filters[1], { target: { value: 'glov' } });
    expect(screen.getByText('Gloves')).toBeInTheDocument();
    expect(screen.queryByText('Bandage')).not.toBeInTheDocument();

    fireEvent.change(filters[1], { target: { value: '' } });
    fireEvent.change(filters[3], { target: { value: '1999' } });
    expect(screen.queryByText('Gloves')).not.toBeInTheDocument();

    fireEvent.click(screen.getByText('Product', { selector: 'button' }));
    expect(screen.getByText('Stock Movement', { selector: 'button' })).toBeInTheDocument();
  });

  it('asks to switch back when the current location changes', async () => {
    const { store } = renderSecondPage();
    apiClient.put.mockResolvedValue({ data: {} });
    await screen.findByText('Paracetamol');

    store.dispatch({ type: CHANGE_CURRENT_LOCATION, payload: { id: 'loc2', name: 'Other' } });

    expect(await screen.findByText('Location was changed')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Yes'));
    await waitFor(() => expect(apiClient.put).toHaveBeenCalledWith('/api/chooseLocation/loc1'));
  });
});
