import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

import '@testing-library/jest-dom';

// The API: every GET answers from answers() below. The helpers the actions use come from the
// real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    // OBAF still imports flattenRequest here (upstream develop no longer has it).
    flattenRequest: actual('flattenRequest'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});

/* eslint-disable import/first,import/order */
import StocklistManagement from 'components/stock-list-management/StocklistManagement';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const MANAGER = {
  id: 'u2', name: 'Ward Manager', email: 'manager@example.org',
};

const item = (overrides) => ({
  id: 'sli1',
  stocklistId: 'st1',
  name: 'Ward A stocklist',
  location: { id: 'loc2', name: 'Ward A' },
  locationGroup: { id: 'lg1', name: 'North' },
  manager: MANAGER,
  replenishmentPeriod: 30,
  monthlyDemand: 12,
  maxQuantity: 40,
  uom: 'EA',
  ...overrides,
});

const AVAILABLE = {
  id: 'st9',
  name: 'Clinic stocklist',
  location: { id: 'loc9', name: 'Clinic' },
  locationGroup: { id: 'lg9', name: 'South' },
  manager: MANAGER,
  replenishmentPeriod: 14,
};

let items;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/stocklistItems/availableStocklists') {
    return { data: [AVAILABLE] };
  }
  if (url.startsWith('/api/stocklistItems')) {
    return { data: items };
  }
  if (url === '/api/products/p1/withCatalogs') {
    return {
      data: {
        id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg', catalogs: [{ id: 'c1', name: 'Essentials' }],
      },
    };
  }
  if (url === '/api/persons') {
    return { data: [MANAGER] };
  }
  return { data: [] };
};

const createTestStore = ({ isUserAdmin = true } = {}) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: { id: 'loc1', name: 'Main Warehouse', locationType: { locationTypeCode: 'DEPOT' } },
          supportedActivities: [],
          isUserAdmin,
          activeLanguage: 'en',
          activeLanguageTag: 'en',
          menuItems: [],
          supportedLocales: [],
        },
      },
    },
  });
  return store;
};

const renderPage = (options) => {
  const store = createTestStore(options);
  const view = render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/stocklistManagement/index/p1']}>
        <Route path="**/stocklistManagement/index/:productId" component={StocklistManagement} />
      </MemoryRouter>
    </Provider>,
  );
  return { store, ...view };
};

// The table pivots by location group and location ("North (1)"); open both levels to see the
// item rows.
const expandGroups = async (groupName, locationName) => {
  fireEvent.click(await screen.findByText(new RegExp(`^${groupName} `)));
  fireEvent.click(await screen.findByText(new RegExp(`^${locationName} `)));
};

const buttonsOf = (row) => within(row).getAllByRole('button');

describe('stocklist management page against a mocked API', () => {
  beforeEach(() => {
    items = [item()];
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
  });

  afterEach(() => jest.clearAllMocks());

  it('loads the product, its stocklist items, the available stocklists and the users once translations arrive', async () => {
    renderPage();

    expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    expect(screen.getByText('P-0001')).toBeInTheDocument();
    expect(screen.getByText('Essentials')).toBeInTheDocument();
    await expandGroups('North', 'Ward A');
    expect(screen.getByText('Ward A stocklist').closest('a')).toHaveAttribute('href', '/openboxes/requisitionTemplate/show/st1');
    expect(screen.getByText('Print PDF')).toHaveAttribute('href', '/openboxes/stocklist/renderPdf/st1');
    expect(screen.getByText('Print XLS')).toHaveAttribute('href', '/openboxes/stocklist/generateCsv/st1');

    const urls = apiClient.get.mock.calls.map(([url]) => url);
    expect(urls).toEqual(expect.arrayContaining([
      '/api/localizations?languageCode=&prefix=react.stockListManagement',
      '/api/stocklistItems?product.id=p1',
      '/api/stocklistItems/availableStocklists',
      '/api/products/p1/withCatalogs',
      '/api/persons',
    ]));
    expect(apiClient.get).toHaveBeenCalledWith('/api/persons', { params: { status: true } });
  });

  it('groups items without a location group under "No location group"', async () => {
    items = [item({ locationGroup: null })];
    renderPage();

    expect(await screen.findByText(/^No location group /)).toBeInTheDocument();
  });

  it('edits the replenishment quantity of an item and saves it to the item URL', async () => {
    apiClient.post.mockResolvedValue({ data: { data: item({ maxQuantity: 55 }) } });
    const { container } = renderPage();
    await expandGroups('North', 'Ward A');
    const row = screen.getByText('Ward A stocklist').closest('.rt-tr');
    const [edit, save] = buttonsOf(row);
    expect(save).toBeDisabled();

    fireEvent.click(edit);
    const input = container.querySelector('.rt-tr input');
    fireEvent.change(input, { target: { value: '55' } });
    fireEvent.click(buttonsOf(screen.getByText('Ward A stocklist').closest('.rt-tr'))[1]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, body] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/stocklistItems/sli1');
    expect(body).toEqual(expect.objectContaining({ id: 'sli1', edit: true, maxQuantity: '55' }));
    // The saved item comes back without the edit flag, so the quantity is plain text again.
    expect(await screen.findByText('55')).toBeInTheDocument();
    expect(container.querySelector('.rt-tr input')).toBeNull();
  });

  it('marks an emptied quantity as an error and keeps save disabled', async () => {
    const { container } = renderPage();
    await expandGroups('North', 'Ward A');
    fireEvent.click(buttonsOf(screen.getByText('Ward A stocklist').closest('.rt-tr'))[0]);

    fireEvent.change(container.querySelector('.rt-tr input'), { target: { value: '' } });

    expect(container.querySelector('.has-error')).not.toBeNull();
    expect(buttonsOf(screen.getByText('Ward A stocklist').closest('.rt-tr'))[1]).toBeDisabled();
  });

  it('deletes a saved item through the API and removes its row', async () => {
    apiClient.delete.mockResolvedValue({});
    renderPage();
    await expandGroups('North', 'Ward A');

    fireEvent.click(within(screen.getByText('Ward A stocklist').closest('.rt-tr')).getByText('Delete'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/stocklistItems/sli1'));
    await waitFor(() => expect(screen.queryByText('Ward A stocklist')).not.toBeInTheDocument());
  });

  it('adds an available stocklist as a new row and saves it to the product URL', async () => {
    apiClient.post.mockResolvedValue({
      data: {
        data: item({
          id: 'sli2', stocklistId: 'st9', name: 'Clinic stocklist', location: AVAILABLE.location, locationGroup: AVAILABLE.locationGroup, maxQuantity: 8,
        }),
      },
    });
    const { container } = renderPage();
    const add = await screen.findByText('Add stocklist');
    expect(add.closest('button')).toBeDisabled();
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stocklistItems/availableStocklists'));

    const select = container.querySelector('.stocklist-select input');
    fireEvent.focus(select);
    fireEvent.keyDown(select, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(await screen.findByText('Clinic stocklist'));
    fireEvent.click(add);
    await expandGroups('South', 'Clinic');

    const row = () => screen.getByText('Clinic stocklist').closest('.rt-tr');
    // A new row has no quantity yet, so it cannot be saved.
    expect(buttonsOf(row())[1]).toBeDisabled();
    fireEvent.change(row().querySelector('input'), { target: { value: '8' } });
    fireEvent.click(buttonsOf(row())[1]);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    const [url, body] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/stocklistItems?product.id=p1');
    expect(body).toEqual(expect.objectContaining({
      stocklistId: 'st9',
      name: 'Clinic stocklist',
      location: { id: 'loc9', name: 'Clinic' },
      locationGroup: { id: 'lg9', name: 'South' },
      manager: { id: 'u2', name: 'Ward Manager' },
      replenishmentPeriod: 14,
      maxQuantity: '8',
      new: true,
    }));
  });

  it('removes a new row without calling the API', async () => {
    const { container } = renderPage();
    const add = await screen.findByText('Add stocklist');
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/stocklistItems/availableStocklists'));
    const select = container.querySelector('.stocklist-select input');
    fireEvent.focus(select);
    fireEvent.keyDown(select, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(await screen.findByText('Clinic stocklist'));
    fireEvent.click(add);
    await expandGroups('South', 'Clinic');

    fireEvent.click(within(screen.getByText('Clinic stocklist').closest('.rt-tr')).getByText('Delete'));

    expect(screen.queryByText('Clinic stocklist')).not.toBeInTheDocument();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it('shows no edit actions and no stocklist picker to a user who is not an admin', async () => {
    renderPage({ isUserAdmin: false });
    await expandGroups('North', 'Ward A');

    expect(screen.queryByText('Edit')).not.toBeInTheDocument();
    expect(screen.queryByText('Delete')).not.toBeInTheDocument();
    expect(screen.queryByText('Add stocklist')).not.toBeInTheDocument();
    expect(screen.getByText('Print PDF')).toBeInTheDocument();
  });

  it('opens the stock card from the return button', async () => {
    const { location } = window;
    delete window.location;
    window.location = {};
    try {
      renderPage();
      fireEvent.click(await screen.findByText('Return to stock card'));
      expect(window.location).toBe('/openboxes/inventoryItem/showStockCard/p1');
    } finally {
      window.location = location;
    }
  });

  // KNOWN-GAP: OBAF reads original.manager.name without a null check, so the table crashes.
  it.skip('shows a stocklist that has no manager', async () => {
    items = [item({ manager: null })];
    renderPage();
    await expandGroups('North', 'Ward A');

    expect(screen.getByText('Ward A stocklist')).toBeInTheDocument();
  });

  // KNOWN-GAP: OBAF calls hideSpinner() immediately in .catch(...), so the spinner is gone before
  // the delete settles.
  it.skip('keeps the spinner while an item is deleted and hides it when the delete fails', async () => {
    let rejectDelete;
    apiClient.delete.mockImplementation(() => new Promise((resolve, reject) => {
      rejectDelete = reject;
    }));
    const { store } = renderPage();
    await expandGroups('North', 'Ward A');
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));

    fireEvent.click(within(screen.getByText('Ward A stocklist').closest('.rt-tr')).getByText('Delete'));

    expect(store.getState().spinner.show).toBe(true);
    rejectDelete(new Error('delete failed'));
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(screen.getByText('Ward A stocklist')).toBeInTheDocument();
  });

  it('hides the spinner after a successful delete', async () => {
    apiClient.delete.mockResolvedValue({});
    const { store } = renderPage();
    await expandGroups('North', 'Ward A');

    fireEvent.click(within(screen.getByText('Ward A stocklist').closest('.rt-tr')).getByText('Delete'));

    await waitFor(() => expect(screen.queryByText('Ward A stocklist')).not.toBeInTheDocument());
    expect(store.getState().spinner.show).toBe(false);
  });
});
