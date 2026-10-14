import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

import '@testing-library/jest-dom';

// The API: every call answers from answers() below (or fails when the test says so). The helpers
// the actions use come from the real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
  };
});

/* eslint-disable import/first,import/order */
import LoadDemoDataPage from 'components/load-demo-data/LoadDemoDataPage';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const SUMMARY = {
  title: 'The demo data contains',
  description: '<ul><li>Two depots</li><li>Forty products</li></ul>',
};
const LINKS = { knowledgeBase: 'https://help.example.org', discussionForum: 'https://forum.example.org' };

let demoLoad;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return Promise.resolve({ data: { messages: {}, currentLocale: 'en' } });
  }
  if (url === '/api/loadData/listOfDemoData') {
    return Promise.resolve({ data: { data: SUMMARY } });
  }
  if (url === '/api/supportLinks') {
    return Promise.resolve({ data: { data: LINKS } });
  }
  if (url === '/api/config/data/demo') {
    return demoLoad();
  }
  return Promise.resolve({ data: { data: [] } });
};

const renderPage = () => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/loadData']}>
        <Route path="/openboxes/loadData" component={LoadDemoDataPage} />
        <Route path="*" render={({ location }) => <div data-testid="path">{location.pathname}</div>} />
      </MemoryRouter>
    </Provider>,
  );
  return store;
};

const changeLocale = (store, activeLanguage) => store.dispatch({
  type: 'CHANGE_CURRENT_LOCALE',
  payload: { data: { data: { activeLanguage, activeLanguageTag: activeLanguage } } },
});

const path = () => screen.getByTestId('path').textContent;

describe('load demo data page', () => {
  beforeEach(() => {
    demoLoad = () => Promise.resolve({ data: {} });
    apiClient.get.mockImplementation(answers);
  });

  afterEach(() => jest.clearAllMocks());

  it('fetches the load data translations and the demo data summary on mount', async () => {
    renderPage();

    expect(screen.getByText('Welcome to OpenBoxes!')).toBeInTheDocument();
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/loadData/listOfDemoData'));
    expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=&prefix=react.loadData');
  });

  it('keeps Next disabled until an option is chosen', () => {
    renderPage();

    const next = screen.getByRole('button', { name: 'Next' });
    expect(next).toBeDisabled();
    fireEvent.click(screen.getByLabelText('Load Demo Data'));
    expect(next).toBeEnabled();
    expect(screen.getByLabelText('Load Demo Data')).toBeChecked();
  });

  it('shows the demo data summary, goes back to the welcome step, and forward again', async () => {
    renderPage();
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/loadData/listOfDemoData'));

    fireEvent.click(screen.getByLabelText('Load Demo Data'));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    expect(await screen.findByText('Two depots')).toBeInTheDocument();
    expect(screen.getByText('Forty products')).toBeInTheDocument();
    expect(screen.getByText(/The demo data contains/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Back' }));
    expect(screen.getByText('Welcome to OpenBoxes!')).toBeInTheDocument();
  });

  it('loads the demo data and offers the dashboard when the load succeeds', async () => {
    let finish;
    demoLoad = () => new Promise((resolve) => { finish = resolve; });
    renderPage();

    fireEvent.click(screen.getByLabelText('Load Demo Data'));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Proceed' }));

    // While the load runs, the progress screen asks the user to wait.
    expect(screen.getByText('Importing demo files...')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/config/data/demo');
    expect(apiClient.get).toHaveBeenCalledWith('/api/supportLinks');

    finish({ data: {} });
    expect(await screen.findByText('Data Successfully Loaded')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'helpscout knowledge base.' })).toHaveAttribute('href', LINKS.knowledgeBase);

    fireEvent.click(screen.getByRole('button', { name: 'Explore dashboard' }));
    expect(path()).toBe('/openboxes/dashboard');
  });

  it('shows the error message with the forum link when the load fails', async () => {
    demoLoad = () => Promise.reject(new Error('load failed'));
    renderPage();

    fireEvent.click(screen.getByLabelText('Load Demo Data'));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Proceed' }));

    expect(await screen.findByText('Error occurred while loading demo data')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole('link', { name: 'discussion forum' }))
      .toHaveAttribute('href', LINKS.discussionForum));

    fireEvent.click(screen.getByRole('button', { name: 'Back to dashboard' }));
    expect(path()).toBe('/openboxes/dashboard');
  });

  it('sends the user to the location wizard when they choose to create their first location', () => {
    renderPage();

    fireEvent.click(screen.getByLabelText('Create your First Location'));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    expect(path()).toBe('/openboxes/locationsConfiguration/create');
  });

  it('refetches the translations when the locale changes, not when it is first set', () => {
    const store = renderPage();
    const translationCalls = () => apiClient.get.mock.calls
      .filter(([url]) => url.startsWith('/api/localizations')).map(([url]) => url);

    changeLocale(store, 'en');
    expect(translationCalls()).toEqual(['/api/localizations?languageCode=&prefix=react.loadData']);

    changeLocale(store, 'fr');
    expect(translationCalls()).toEqual([
      '/api/localizations?languageCode=&prefix=react.loadData',
      '/api/localizations?languageCode=fr&prefix=react.loadData',
    ]);
  });

  it.each([
    ['the close button', () => document.querySelector('.fa-close').closest('button')],
    ['the skip link', () => screen.getByText('Skip for now')],
  ])('leaves for the dashboard through %s', (name, target) => {
    renderPage();

    fireEvent.click(target());

    expect(path()).toBe('/openboxes/dashboard');
  });
});
