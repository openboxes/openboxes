import { fireEvent, screen, waitFor } from '@testing-library/react';
import { answer, renderAt } from 'regression-tests/__mocks__/screenHarness';
import {
  candidate, CREATE_ENTRY, PATH, pickOption,
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

const CANDIDATES = [
  candidate({
    id: 'p1', name: 'Paracetamol', quantity: 1200, sm: 'sm1',
  }),
  candidate({
    id: 'p2', name: 'Bandage', quantity: 4, sm: 'sm1',
  }),
  candidate({
    id: 'p3', name: 'Gloves', quantity: 7, sm: 'sm2',
  }),
  candidate({
    id: 'p4', name: 'Syringe', quantity: 3, sm: 'sm3', status: 'PENDING',
  }),
];

const renderFirstPage = (routes = []) => {
  answer(apiClient.get, [
    ['/api/putaways?location.id=loc1', { data: CANDIDATES }],
    ...routes,
  ]);
  return renderAt({
    component: PutAwayMainPage, path: PATH, entry: CREATE_ENTRY,
  });
};

const startButtons = () => screen.getAllByTestId('start-putaway');

const rowCheckbox = (productName) => screen.getByText(productName).closest('.rt-tr')
  .querySelector('input[type="checkbox"]');

describe('put-away, first page (lines in receiving)', () => {
  afterEach(() => jest.clearAllMocks());

  it('lists the ready lines grouped by stock movement and hides pending ones', async () => {
    renderFirstPage();

    expect(await screen.findByText('SM-sm1 (2)')).toBeInTheDocument();
    expect(screen.getByText('SM-sm2 (1)')).toBeInTheDocument();
    expect(screen.queryByText(/SM-sm3/)).not.toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/putaways?location.id=loc1');

    fireEvent.click(screen.getByText('SM-sm1 (2)'));

    expect(await screen.findByText('Paracetamol')).toBeInTheDocument();
    expect(screen.getByText('P1')).toBeInTheDocument();
    expect(screen.getByText('LOT-p1')).toBeInTheDocument();
    expect(screen.getAllByText('01/15/2030').length).toBe(2);
    expect(screen.getByText('1,200')).toBeInTheDocument();
    startButtons().forEach((button) => expect(button).toBeDisabled());
  });

  it('starts a put-away for the lines of a checked stock movement and moves to step two', async () => {
    const { currentPath } = renderFirstPage([
      ['/api/putaways/pa1', { data: { id: 'pa1', putawayNumber: 'PA-1', putawayItems: [] } }],
    ]);
    apiClient.post.mockResolvedValue({
      data: { data: { id: 'pa1', putawayNumber: 'PA-1', putawayItems: [] } },
    });
    await screen.findByText('SM-sm1 (2)');

    const groupCheckbox = screen.getByText('SM-sm1 (2)').closest('.rt-tr')
      .querySelector('input[name="aggregationCheckbox"]');
    fireEvent.click(groupCheckbox);

    startButtons().forEach((button) => expect(button).toBeEnabled());
    fireEvent.click(startButtons()[0]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/putaways?location.id=loc1');
    expect(payload).toMatchObject({ putawayStatus: 'PENDING', putawayNumber: '' });
    expect(payload.putawayItems.map((item) => [item.product.id, item.putawayStatus]))
      .toEqual([['p1', 'PENDING'], ['p2', 'PENDING']]);
    expect(payload.putawayItems[0].putawayFacility).toEqual({ id: 'loc1' });

    await waitFor(() => expect(currentPath()).toBe('/openboxes/putaway/create/pa1'));
    expect(await screen.findByText('PA-1')).toBeInTheDocument();
    expect(screen.getByTestId('next-button')).toBeInTheDocument();
  });

  it('selects single lines in the product view and unselects them again', async () => {
    renderFirstPage();
    await screen.findByText('SM-sm1 (2)');

    fireEvent.click(screen.getByTestId('show-by-button'));
    expect(screen.getByTestId('show-by-button')).toHaveTextContent('Product');
    expect(screen.getByText('Gloves')).toBeInTheDocument();

    fireEvent.click(rowCheckbox('Gloves'));
    expect(rowCheckbox('Gloves')).toBeChecked();
    startButtons().forEach((button) => expect(button).toBeEnabled());

    fireEvent.click(rowCheckbox('Gloves'));
    expect(rowCheckbox('Gloves')).not.toBeChecked();
    startButtons().forEach((button) => expect(button).toBeDisabled());

    fireEvent.click(screen.getByTestId('show-by-button'));
    expect(screen.getByTestId('show-by-button')).toHaveTextContent('Stock Movement');
  });

  it('select all picks every ready line but never the pending ones', async () => {
    renderFirstPage();
    apiClient.post.mockResolvedValue({ data: { data: { id: 'pa2', putawayItems: [] } } });
    await screen.findByText('SM-sm1 (2)');

    pickOption(screen.getAllByTestId('custom-select-element')[0], 'Include');
    expect(await screen.findByText('SM-sm3 (1)')).toBeInTheDocument();

    fireEvent.click(screen.getByTestId('show-by-button'));
    expect(rowCheckbox('Syringe')).toBeDisabled();

    const selectAll = document.querySelector('.rt-thead input[type="checkbox"]');
    fireEvent.click(selectAll);

    expect(rowCheckbox('Paracetamol')).toBeChecked();
    expect(rowCheckbox('Gloves')).toBeChecked();
    expect(rowCheckbox('Syringe')).not.toBeChecked();

    fireEvent.click(startButtons()[1]);
    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(apiClient.post.mock.calls[0][1].putawayItems.map((item) => item.product.id).sort())
      .toEqual(['p1', 'p2', 'p3']);
  });

  it('switching back to excluding pending lines hides them again', async () => {
    renderFirstPage();
    await screen.findByText('SM-sm1 (2)');
    const select = screen.getAllByTestId('custom-select-element')[0];

    pickOption(select, 'Include');
    expect(await screen.findByText('SM-sm3 (1)')).toBeInTheDocument();

    pickOption(select, 'Exclude');
    await waitFor(() => expect(screen.queryByText('SM-sm3 (1)')).not.toBeInTheDocument());
  });

  it('filters the lines by product name and by expiry date', async () => {
    renderFirstPage();
    await screen.findByText('SM-sm1 (2)');
    fireEvent.click(screen.getByTestId('show-by-button'));

    const filters = screen.getAllByPlaceholderText('Search');
    // Columns: code, name, lot, expiry, recipient, quantity, stock movement.
    fireEvent.change(filters[1], { target: { value: 'band' } });
    expect(screen.getByText('Bandage')).toBeInTheDocument();
    expect(screen.queryByText('Gloves')).not.toBeInTheDocument();

    fireEvent.change(filters[1], { target: { value: '' } });
    fireEvent.change(filters[3], { target: { value: '01/15' } });
    expect(screen.getByText('Gloves')).toBeInTheDocument();
    fireEvent.change(filters[3], { target: { value: '1999' } });
    expect(screen.queryByText('Gloves')).not.toBeInTheDocument();
  });

  it('stays on the first page when starting the put-away fails', async () => {
    const { currentPath } = renderFirstPage();
    apiClient.post.mockRejectedValue(new Error('boom'));
    await screen.findByText('SM-sm2 (1)');

    fireEvent.click(screen.getByText('SM-sm2 (1)').closest('.rt-tr')
      .querySelector('input[name="aggregationCheckbox"]'));
    fireEvent.click(startButtons()[0]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(currentPath()).toBe(CREATE_ENTRY);
    expect(screen.getByTestId('show-by-button')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByTestId('next-button')).not.toBeInTheDocument());
  });

  it('shows an empty table when the lines cannot be loaded', async () => {
    answer(apiClient.get, [['/api/putaways?location.id=loc1', new Error('down')]]);
    renderAt({
      component: PutAwayMainPage, path: PATH, entry: CREATE_ENTRY,
    });

    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/putaways?location.id=loc1'));
    expect(screen.queryByText(/SM-sm1/)).not.toBeInTheDocument();
    startButtons().forEach((button) => expect(button).toBeDisabled());
  });
});
