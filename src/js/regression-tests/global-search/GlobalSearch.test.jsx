import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
}));

/* eslint-disable import/first,import/order */
import GlobalSearch from 'components/GlobalSearch/GlobalSearch';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location directly; the spy reports it to followLink.
installNavigationSpy();

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

const store = configureStore([])({
  localize,
  session: { searchConfig: { debounceTime: 0, minSearchLength: 3 } },
});

const renderSearch = (props) => {
  const utils = render(
    <Provider store={store}>
      <GlobalSearch {...props} />
    </Provider>,
  );
  const input = () => utils.container.querySelector('input');
  return { ...utils, input };
};

const type = (input, value) => fireEvent.change(input, { target: { value } });

describe('GlobalSearch', () => {
  beforeEach(() => {
    apiClient.get.mockResolvedValue({
      data: [
        {
          url: '/openboxes/product/show/p1', label: 'Ibuprofen 200mg', color: 'red', value: 'Ibuprofen', displayName: 'Ibu',
        },
        { url: '/openboxes/product/show/p2', label: 'Paracetamol', color: 'blue' },
      ],
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('searches the typed term and highlights the matching part of each result', async () => {
    const { input, container } = renderSearch({ visible: true });
    expect(screen.getByText('Search...')).toBeInTheDocument();

    type(input(), 'pro');
    await screen.findByText('Paracetamol');
    // OBAF searches through the GSP-era /json endpoint.
    expect(apiClient.get).toHaveBeenCalledWith('/json/globalSearch?term=pro');
    const option = container.querySelector('strong.font-weight-bold').parentElement;
    expect(option).toHaveTextContent('Ibuprofen 200mg');
    expect(option.querySelectorAll('span')[0]).toHaveTextContent('Ibu');
    expect(option.querySelector('strong')).toHaveTextContent('pro');
    expect(option.querySelectorAll('span')[1]).toHaveTextContent('fen 200mg');
    // A result without the term is shown as it is.
    expect(screen.getByText('Paracetamol')).toBeInTheDocument();
  });

  it('opens the selected result', async () => {
    const { input } = renderSearch({ visible: true });
    type(input(), 'Para');
    fireEvent.click(await screen.findByText('cetamol'));
    expect(followLink).toHaveBeenCalledWith('/openboxes/product/show/p2');
  });

  it('does not search terms shorter than the minimum length', async () => {
    const { input } = renderSearch({ visible: true });
    type(input(), 'ib');
    expect(await screen.findByText('No options')).toBeInTheDocument();
    expect(apiClient.get).not.toHaveBeenCalledWith('/json/globalSearch?term=ib');
  });

  it('shows no results when the search fails', async () => {
    apiClient.get.mockRejectedValue(new Error('down'));
    const { input } = renderSearch({ visible: true });
    type(input(), 'ibu');
    expect(await screen.findByText('No options')).toBeInTheDocument();
  });

  it('clears a selected value with the close button', async () => {
    const { input, container } = renderSearch({ visible: true });
    type(input(), 'Para');
    fireEvent.click(await screen.findByText('cetamol'));
    expect(container.querySelector('.app-global-search__single-value')).toHaveTextContent('Paracetamol');

    fireEvent.click(container.querySelector('.app-global-search__clear-btn'));
    expect(container.querySelector('.app-global-search__single-value')).not.toBeInTheDocument();
    // Clearing does not navigate anywhere.
    expect(followLink).toHaveBeenCalledTimes(1);
  });

  describe('behind a button', () => {
    const renderButton = jest.fn(({ isVisible, showSearchbar }) => (
      <button type="button" onClick={showSearchbar}>{isVisible ? 'open' : 'closed'}</button>
    ));

    it('shows the search bar from the button and hides it on blur', async () => {
      const { input } = renderSearch({ renderButton });
      expect(input()).toBeNull();
      expect(screen.getByText('closed')).toBeInTheDocument();

      fireEvent.click(screen.getByText('closed'));
      expect(screen.getByText('open')).toBeInTheDocument();
      expect(input()).toBeInTheDocument();

      fireEvent.blur(input());
      await waitFor(() => expect(input()).toBeNull());
    });

    it('hides the empty search bar with the close button', () => {
      const { input, container } = renderSearch({ renderButton, visible: true });
      expect(container.querySelector('.app-global-search')).toHaveClass('position-absolute');
      fireEvent.click(container.querySelector('.app-global-search__clear-btn'));
      expect(input()).toBeNull();
    });
  });

  it('stays open on blur and on close without a button', () => {
    const { input, container } = renderSearch({ visible: true });
    fireEvent.blur(input());
    fireEvent.click(container.querySelector('.app-global-search__clear-btn'));
    expect(input()).toBeInTheDocument();
  });
});
