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
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

jest.mock('js-file-download', () => jest.fn());
jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';
import Alert from 'react-s-alert';

import ProductsConfigurationWizard from 'components/products-configuration/ProductsConfigurationWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

jest.setTimeout(30000);

const CATEGORY_OPTIONS = {
  unspsc: { title: 'UNSPSC categories', description: '<p>The <b>standard</b> classification</p>' },
};
const PRODUCT_OPTIONS = {
  essentials: { title: 'Essential medicines', description: '<p>WHO list</p>' },
};

let categoriesCount;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  switch (url) {
    case '/api/productsConfiguration/categoryOptions':
      return { data: CATEGORY_OPTIONS };
    case '/api/productsConfiguration/categoriesCount':
      return { data: categoriesCount };
    case '/api/productsConfiguration/productOptions':
      return { data: PRODUCT_OPTIONS };
    case '/api/productsConfiguration/downloadCategories':
      return 'name,parent';
    case '/api/productsConfiguration/downloadCategoryTemplate':
      return 'category template';
    case '/batch/downloadCsvTemplate?template=products.csv':
      return 'product template';
    default:
      return { data: [] };
  }
};

const makeStore = () => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({
    languages: [{ name: 'English', code: 'en' }, { name: 'French', code: 'fr' }],
    options: {
      renderToStaticMarkup: false,
      onMissingTranslation: ({ translationId }) => translationId,
    },
  }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: { id: 'loc1', name: 'Main Warehouse', locationType: { locationTypeCode: 'DEPOT' } },
          supportedActivities: [],
          activeLanguage: 'en',
          menuItems: [],
          supportedLocales: [],
        },
      },
    },
  });
  return store;
};

const renderWizard = (store = makeStore()) => {
  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/productsConfiguration/index']}>
        <Route path="**/" render={(props) => <ProductsConfigurationWizard {...props} />} />
      </MemoryRouter>
    </Provider>,
  );
  return store;
};

const postedUrls = () => apiClient.post.mock.calls.map(([url]) => url);

describe('products configuration wizard', () => {
  beforeEach(() => {
    categoriesCount = 12;
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    apiClient.post.mockResolvedValue({ data: {} });
  });

  afterEach(() => jest.clearAllMocks());

  it('imports a category tree, reviews it, then imports a product list', async () => {
    renderWizard();

    // The three steps, and the category options as tabs next to the Excel import.
    expect(screen.getByText('Configure Product Categories')).toBeInTheDocument();
    expect(screen.getByText('Review Categories')).toBeInTheDocument();
    expect(screen.getByText('Configure Products')).toBeInTheDocument();
    expect(await screen.findByText('UNSPSC categories', { selector: 'h3' })).toBeInTheDocument();
    expect(screen.getByText('standard')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=&prefix=react.productsConfiguration');

    fireEvent.click(screen.getByText('Import Categories'));
    await waitFor(() => expect(postedUrls())
      .toEqual(['/api/productsConfiguration/importCategories?categoryOption=unspsc']));
    expect(Alert.success).toHaveBeenCalledWith('Categories imported successfully');

    // Review: the imported categories can be downloaded.
    expect(await screen.findByText('Import Complete!')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/productsConfiguration/categoriesCount');
    expect(screen.getByText('View Category Page').closest('a')).toHaveAttribute('target', '_blank');
    fireEvent.click(screen.getByText('Download CSV file'));
    await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('name,parent', 'Categories.csv', 'text/csv'));

    fireEvent.click(screen.getByText('Next'));
    expect(await screen.findByText('Essential medicines', { selector: 'h3' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Import Products List' }));
    await waitFor(() => expect(postedUrls())
      .toContain('/api/productsConfiguration/importProducts?productOption=essentials'));
    expect(await screen.findByText('View Products List')).toBeInTheDocument();

    // With categories imported, Previous goes back to the review.
    fireEvent.click(screen.getByText('Previous'));
    expect(await screen.findByText('Download CSV file')).toBeInTheDocument();
  });

  it('skips the categories straight to products, and Previous from there returns to the categories', async () => {
    renderWizard();
    await screen.findByText('UNSPSC categories', { selector: 'h3' });

    fireEvent.click(screen.getByText('Skip this step'));
    expect(await screen.findByText('Essential medicines', { selector: 'h3' })).toBeInTheDocument();
    expect(apiClient.get).not.toHaveBeenCalledWith('/api/productsConfiguration/categoriesCount');

    fireEvent.click(screen.getByText('Previous'));
    expect(await screen.findByText('Skip this step')).toBeInTheDocument();
    expect(postedUrls()).toEqual([]);
  });

  it('tells the user when the review finds no categories', async () => {
    categoriesCount = 0;
    renderWizard();
    fireEvent.click(await screen.findByText('Import Categories'));

    expect(await screen.findByText('No categories found')).toBeInTheDocument();
    expect(screen.queryByText('Download CSV file')).not.toBeInTheDocument();

    fireEvent.click(screen.getByText('Previous'));
    expect(await screen.findByText('Skip this step')).toBeInTheDocument();
  });

  it('stays on the categories step when the import fails', async () => {
    apiClient.post.mockRejectedValue(new Error('import failed'));
    const store = renderWizard();
    fireEvent.click(await screen.findByText('Import Categories'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(Alert.success).not.toHaveBeenCalled();
    expect(screen.getByText('Skip this step')).toBeInTheDocument();
  });

  it('offers only the Excel import when the category options cannot be loaded', async () => {
    apiClient.get.mockImplementation((url) => (url === '/api/productsConfiguration/categoryOptions'
      ? Promise.reject(new Error('down'))
      : Promise.resolve({ data: answers(url), headers: {} })));
    const store = renderWizard();

    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.getByText('Import from Excel')).toBeInTheDocument();
    expect(screen.queryByText('UNSPSC categories')).not.toBeInTheDocument();
  });

  it('imports categories from an uploaded csv file and downloads the template', async () => {
    renderWizard();
    await screen.findByText('UNSPSC categories', { selector: 'h3' });

    fireEvent.click(screen.getByText('Import from Excel'));
    expect(await screen.findByText('Excel Import')).toBeInTheDocument();
    const importButton = screen.getByText('Import File').closest('button');
    expect(importButton).toBeDisabled();

    fireEvent.click(screen.getByText('.csv file template'));
    await waitFor(() => expect(fileDownload)
      .toHaveBeenCalledWith('category template', 'Category_template.csv', 'text/csv'));

    const file = new File(['name,parent\nMedicines,'], 'categories.csv', { type: 'text/csv' });
    fireEvent.change(document.querySelector('input[type="file"]'), { target: { files: [file] } });
    await waitFor(() => expect(importButton).not.toBeDisabled());
    fireEvent.click(importButton);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, body, config] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/productsConfiguration/importCategoryCsv');
    expect(body.get('importFile')).toBeInstanceOf(Blob);
    expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
    // On to the review.
    expect(await screen.findByText('Import Complete!')).toBeInTheDocument();
  });

  it('links to the product csv import and downloads its template', async () => {
    renderWizard();
    fireEvent.click(await screen.findByText('Skip this step'));
    await screen.findByText('Essential medicines', { selector: 'h3' });

    fireEvent.click(screen.getByText('Import from Excel'));
    expect(await screen.findByText('Create a Custom Product List')).toBeInTheDocument();
    expect(screen.getByText('Import Products List').closest('a').getAttribute('href')).toMatch(/importCSV|csv/i);

    fireEvent.click(screen.getByText('here'));
    await waitFor(() => expect(fileDownload)
      .toHaveBeenCalledWith('product template', 'Product_template.csv', 'text/csv'));
  });

  it('fetches the translations again when the language changes', async () => {
    const store = renderWizard();
    await screen.findByText('UNSPSC categories', { selector: 'h3' });

    store.dispatch({
      type: 'CHANGE_CURRENT_LOCALE',
      payload: { data: { data: { activeLanguage: 'fr', activeLanguageTag: 'fr' } } },
    });

    await waitFor(() => expect(apiClient.get)
      .toHaveBeenCalledWith('/api/localizations?languageCode=fr&prefix=react.productsConfiguration'));
  });
});
