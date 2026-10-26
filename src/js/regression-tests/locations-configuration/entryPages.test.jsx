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

// The shared client serves the translations; the import page builds its own client with
// axios.create({}), which
// hands back one shared stand-in client here (importClient below). The response interceptor the
// page registers on it is kept, so a test can run an error through it as axios would.
jest.mock('axios', () => {
  const actual = jest.requireActual('axios');
  const importClient = {
    get: jest.fn(),
    post: jest.fn(),
    interceptors: { request: { use: jest.fn() }, response: { use: jest.fn() } },
  };
  const create = () => importClient;
  return {
    ...actual, __esModule: true, default: { ...actual, create }, create,
  };
});

jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleSuccess: (response) => response,
    handleError: jest.fn((error) => Promise.reject(error)),
  };
});

jest.mock('js-file-download', () => jest.fn());
jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));

/* eslint-disable import/first,import/order */
import axios from 'axios';
import fileDownload from 'js-file-download';
import Alert from 'react-s-alert';

import { LOCATION_IMPORT, LOCATION_TEMPLATE, SUPPORT_LINKS } from 'api/urls';
import ImportLocations from 'components/locations-configuration/ImportLocations';
import WelcomePage from 'components/locations-configuration/WelcomePage';
import apiClient, { handleError } from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// The mocked axios.create always returns the same client the import page builds.
const importClient = axios.create({});

// The import page addresses the backend by its context path (window.CONTEXT_PATH, set by the page
// that hosts the app), so the expected URLs come from api/urls.
const LINKS = { configureOrganizationsAndLocations: 'https://help.example.org/locations' };

const renderAt = (path, routePath, Page) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  const view = render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <Route path={routePath} component={Page} />
        <Route path="*" render={({ location }) => <div data-testid="path">{location.pathname}</div>} />
      </MemoryRouter>
    </Provider>,
  );
  return { ...view, store };
};

const path = () => screen.getByTestId('path').textContent;

beforeEach(() => {
  apiClient.get.mockResolvedValue({ data: { messages: {}, currentLocale: 'en' } });
});

afterEach(() => jest.clearAllMocks());

describe('locations configuration welcome page', () => {
  const renderWelcome = () => renderAt('/openboxes/locationsConfiguration/index', '/openboxes/locationsConfiguration/index', WelcomePage);

  it('fetches the locations configuration translations', () => {
    renderWelcome();

    expect(screen.getByText('Welcome to OpenBoxes!')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=&prefix=react.locationsConfiguration');
  });

  it.each([
    ['Create your first location', '/openboxes/locationsConfiguration/create'],
    ['Import Location List', '/openboxes/locationsConfiguration/upload'],
    ['Skip Configuration for now', '/openboxes/dashboard'],
  ])('"%s" leads to %s', (label, target) => {
    renderWelcome();

    fireEvent.click(screen.getByRole('button', { name: label }));

    expect(path()).toBe(target);
  });

  it('closes to the dashboard', () => {
    renderWelcome();

    fireEvent.click(document.querySelector('.fa-close').closest('button'));

    expect(path()).toBe('/openboxes/dashboard');
  });

  it('refetches the translations for a new locale only once a locale was set', () => {
    const { store } = renderWelcome();
    const changeLocale = (activeLanguage) => store.dispatch({
      type: 'CHANGE_CURRENT_LOCALE',
      payload: { data: { data: { activeLanguage, activeLanguageTag: activeLanguage } } },
    });

    changeLocale('en');
    changeLocale('es');

    const translationUrls = apiClient.get.mock.calls.map(([url]) => url);
    expect(translationUrls).toEqual([
      '/api/localizations?languageCode=&prefix=react.locationsConfiguration',
      '/api/localizations?languageCode=es&prefix=react.locationsConfiguration',
    ]);
  });
});

describe('import locations page', () => {
  const CSV = new File(['name,type\nDepot A,DEPOT\n'], 'locations.csv', { type: 'text/csv' });

  const renderImport = () => renderAt('/openboxes/locationsConfiguration/upload', '/openboxes/locationsConfiguration/upload', ImportLocations);

  const chooseFile = async (container, file = CSV) => {
    fireEvent.change(container.querySelector('input[type="file"]'), { target: { files: [file] } });
    await waitFor(() => expect(screen.getByRole('button', { name: 'Upload' })).toBeEnabled());
  };

  // The error handler the page registered on its client, as axios would call it.
  // (Picked by name: anything else that builds an axios client gets the same stand-in.)
  const interceptedError = () => importClient.interceptors.response.use.mock.calls
    .map(([, onError]) => onError)
    .find((onError) => onError.name === 'bound handleValidationErrors');

  beforeEach(() => {
    importClient.get.mockImplementation((url) => {
      if (url === SUPPORT_LINKS) {
        return Promise.resolve({ data: { data: LINKS } });
      }
      return Promise.resolve({ data: 'name,type\n' });
    });
  });

  it('links the location help article from the support links', async () => {
    renderImport();

    expect(screen.getByText('Import Locations')).toBeInTheDocument();
    await waitFor(() => expect(document.querySelector(`a[href="${LINKS.configureOrganizationsAndLocations}"]`))
      .not.toBeNull());
    expect(importClient.get).toHaveBeenCalledWith(SUPPORT_LINKS);
  });

  it('downloads the location template as a csv file', async () => {
    renderImport();

    fireEvent.click(screen.getAllByText('here')[0]);

    await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('name,type\n', 'Locations_template.csv', 'text/csv'));
    expect(importClient.get).toHaveBeenCalledWith(LOCATION_TEMPLATE);
  });

  it('enables the upload once a file is chosen and posts it as multipart form data', async () => {
    importClient.post.mockResolvedValue({ data: {} });
    const { container } = renderImport();
    expect(screen.getByRole('button', { name: 'Upload' })).toBeDisabled();

    await chooseFile(container);
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));

    expect(await screen.findByText('Locations Created Successfully')).toBeInTheDocument();
    const [url, body, config] = importClient.post.mock.calls[0];
    expect(url).toBe(LOCATION_IMPORT);
    expect(body).toBeInstanceOf(FormData);
    expect(body.get('importFile')).toBeInstanceOf(Blob);
    expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
    expect(Alert.success).toHaveBeenCalledWith('Locations Created Successfully');

    // The success screen offers the next steps.
    expect(screen.getByRole('link', { name: 'View Location List' })).toHaveAttribute('href', '/openboxes/location/list');
    fireEvent.click(screen.getByRole('link', { name: 'Product Creation Wizard' }));
    expect(path()).toBe('/openboxes/productsConfiguration/index');
  });

  it('shows the first error line and every listed error when the import is rejected as invalid', async () => {
    importClient.post.mockImplementation(() => interceptedError()({
      response: {
        status: 400,
        data: { errorMessage: 'Import failed\nsecond line', errorMessages: ['Row 1: name is missing', 'Row 2: bad type'] },
      },
    }));
    const { container } = renderImport();

    await chooseFile(container);
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));

    const alert = await screen.findByTestId('alert-message');
    expect(alert.textContent).toBe('Import failed \n Row 1: name is missing\nRow 2: bad type');
    expect(screen.queryByText('Locations Created Successfully')).not.toBeInTheDocument();
    expect(handleError).not.toHaveBeenCalled();
  });

  it('leaves other errors to the shared error handler', async () => {
    const error = { response: { status: 500, data: {} } };
    importClient.post.mockImplementation(() => interceptedError()(error));
    const { container } = renderImport();

    await chooseFile(container);
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));

    await waitFor(() => expect(handleError).toHaveBeenCalledWith(error));
    expect(screen.queryByTestId('alert-message')).not.toBeInTheDocument();
    expect(screen.getByText('Import Locations')).toBeInTheDocument();
  });
});
