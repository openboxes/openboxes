import React from 'react';

import {
  act, render, screen, waitFor,
} from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import configureStore from 'redux-mock-store';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
}));

jest.mock('actions', () => ({
  fetchTranslations: jest.fn((...args) => ({ type: 'fetchTranslations', args })),
}));

/* eslint-disable import/first,import/order */
import StockRequestDashboard from 'components/dashboard/StockRequestDashboard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

let activeLanguage;

// The state is read on every access, so a test can switch the language and notify the page.
const createStore = () => configureStore([])(() => ({
  localize,
  session: { activeLanguage, currentLocation: { id: 'loc1', name: 'Clinic' } },
}));

const page = (store) => (
  <Provider store={store}>
    <MemoryRouter>
      <StockRequestDashboard />
    </MemoryRouter>
  </Provider>
);

describe('StockRequestDashboard', () => {
  beforeEach(() => {
    activeLanguage = 'en';
    apiClient.get.mockResolvedValue({
      data: {
        totalCount: 25,
        data: [{
          id: 'sm1',
          identifier: 'REQ-1',
          description: 'Monthly request',
          statusCode: 'PENDING',
          displayStatus: { label: 'Pending' },
          currentEvent: { comment: 'Waiting for approval', eventType: { eventCode: 'PENDING' } },
          origin: { name: 'Central depot' },
          requestedBy: { name: 'Nurse' },
          dateRequested: '2026-09-15T12:00:00',
        }],
      },
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('lists the inbound requests of the current location', async () => {
    const store = createStore();
    render(page(store));

    expect(store.getActions()).toEqual([{ type: 'fetchTranslations', args: ['', 'stockMovement'] }]);
    expect(await screen.findByText('REQ-1')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements', {
      params: {
        destination: 'loc1', direction: 'INBOUND', offset: 0, max: 10, sort: 'dateRequested',
      },
    });
    expect(screen.getByText('REQ-1').closest('a')).toHaveAttribute('href', '/openboxes/stockMovement/show/sm1');
    expect(screen.getByText('Monthly request')).toBeInTheDocument();
    expect(screen.getByText('Pending')).toBeInTheDocument();
    expect(screen.getByText('Central depot')).toBeInTheDocument();
    expect(screen.getByText('Nurse')).toBeInTheDocument();
    expect(screen.getByText('15/Sep/2026')).toBeInTheDocument();
    expect(screen.getByText('Create New Request').closest('a'))
      .toHaveAttribute('href', '/openboxes/stockMovement/createRequest');
    // 25 requests at 10 per page
    await waitFor(() => expect(document.querySelector('.-totalPages')).toHaveTextContent('3'));
  });

  it('fetches the translations again when the language changes', () => {
    const store = createStore();
    render(page(store));
    activeLanguage = 'fr';
    act(() => {
      store.dispatch({ type: 'LANGUAGE_CHANGED' });
    });
    expect(store.getActions()).toContainEqual({ type: 'fetchTranslations', args: ['fr', 'stockMovement'] });
  });
});
