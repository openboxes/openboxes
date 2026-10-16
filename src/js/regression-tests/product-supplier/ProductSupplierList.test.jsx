import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';

import '@testing-library/jest-dom';
// Node 14 has no String.prototype.replaceAll, which OBAF's downloader uses (browsers have it).
import 'regression-tests/utils/replaceAllPolyfill';

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

const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => mockShowNotification),
}));

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';

import ProductSupplierList from 'components/productSupplier/ProductSupplierList';
import apiClient from 'utils/apiClient';

import productSupplierStore from './productSupplierStore';
/* eslint-enable import/first,import/order */

// A whole list page renders per test; give it time on a busy machine.
jest.setTimeout(30000);

const source = (overrides) => ({
  id: 'ps1',
  code: 'PS-0001',
  name: 'Ibuprofen from Acme',
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  supplier: { id: 'org1', displayName: 'Acme Supplies' },
  supplierCode: 'ACME-IBU',
  packageSize: 'BX/10',
  packagePrice: 20,
  unitPrice: 2,
  dateCreated: '2026-01-05T10:00:00Z',
  createdBy: { name: 'Admin' },
  updatedBy: { name: 'Buyer' },
  active: true,
  productSupplierPreferences: [],
  ...overrides,
});

const SOURCES = [
  source({}),
  source({
    id: 'ps2',
    code: 'PS-0002',
    active: false,
    productSupplierPreferences: [{ preferenceType: { id: 'PT1', name: 'Preferred' } }],
  }),
  source({
    id: 'ps3',
    code: 'PS-0003',
    productSupplierPreferences: [
      { preferenceType: { id: 'PT1', name: 'Preferred' } },
      { destinationParty: { id: 'd2', displayName: 'Zeta Clinic' }, preferenceType: { id: 'PT2', name: 'Backup' } },
      { destinationParty: { id: 'd1', displayName: 'Alpha Clinic' }, preferenceType: { id: 'PT1', name: 'Preferred' } },
    ],
  }),
];

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/productSuppliers') {
    return { data: SOURCES, totalCount: 3 };
  }
  if (url === '/api/preferenceTypeOptions') {
    return { data: [{ id: 'PT1', value: 'PT1', label: 'Preferred' }] };
  }
  return { data: [] };
};

const renderList = (search, store = productSupplierStore()) => render(
  <Provider store={store}>
    <MemoryRouter initialEntries={[`/openboxes/productSupplier/list${search}`]}>
      <Route path="/openboxes/productSupplier/list" component={ProductSupplierList} />
    </MemoryRouter>
  </Provider>,
);

const listCalls = () => apiClient.get.mock.calls.filter(([url]) => url === '/api/productSuppliers');

describe('product source list', () => {
  const { location } = window;
  const assign = jest.fn();

  beforeEach(() => {
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    // Edit and create leave the page through window.location; record where they go instead.
    delete window.location;
    window.location = { ...location, href: location.href, assign };
  });

  afterEach(() => {
    window.location = location;
    jest.clearAllMocks();
  });

  it('lists the sources with their preference type and status, newest first', async () => {
    renderList('?tab=details');

    expect(await screen.findByText('PS-0003')).toBeInTheDocument();
    expect(screen.getByText(/List Product Sources/).textContent).toContain('3');
    const [, config] = listCalls()[0];
    expect(config.params).toEqual(expect.objectContaining({
      offset: '0', max: '10', sort: 'dateCreated', order: 'desc', tab: 'details',
    }));
    // The preference type options are fetched with "None" included for the filter.
    const preferenceCall = apiClient.get.mock.calls.find(([url]) => url === '/api/preferenceTypeOptions');
    expect(preferenceCall[1].params).toEqual({ includeNone: true });

    // None, the single type by name, Multiple for several.
    expect(screen.getByText('None')).toBeInTheDocument();
    expect(screen.getByText('Preferred')).toBeInTheDocument();
    expect(screen.getByText('Multiple')).toBeInTheDocument();
    expect(screen.getAllByText('Active')).toHaveLength(2);
    expect(screen.getByText('Inactive')).toBeInTheDocument();
    // Source codes link to the edit page for product managers.
    expect(screen.getByText('PS-0001').closest('a')).toHaveAttribute('href', '/openboxes/productSupplier/create/ps1');
  });

  it('shows the preference types per destination for a source with several, and edits from there', async () => {
    renderList('?tab=details');

    fireEvent.click(await screen.findByText('Multiple'));

    const modal = await waitFor(() => {
      const element = document.querySelector('.ReactModal__Content');
      expect(element).not.toBeNull();
      return element;
    });
    expect(within(modal).getByText('Default:').nextSibling).toHaveTextContent('Preferred');
    // Destinations are sorted by name.
    const rows = Array.from(modal.querySelectorAll('.preference-type-modal-list-element'))
      .map((row) => row.textContent);
    expect(rows).toEqual(['Alpha Clinic: Preferred', 'Zeta Clinic: Backup']);
    expect(document.body.style.overflowY).toBe('hidden');

    fireEvent.click(within(modal).getByText('Edit'));
    expect(window.location).toBe('/openboxes/productSupplier/create/ps3');
  });

  it('does not open the preference modal for a single preference type', async () => {
    renderList('?tab=details');

    fireEvent.click(await screen.findByText('Preferred'));

    expect(document.querySelector('.ReactModal__Content')).toBeNull();
  });

  it('exports all sources, or only the filtered results', async () => {
    apiClient.get.mockImplementation((url) => Promise.resolve({
      data: url === '/api/productSuppliers/export' ? 'xls' : answers(url),
      headers: { 'content-disposition': 'attachment; filename="sources.xls"' },
    }));
    renderList('?tab=details&searchTerm=ibu');
    await screen.findByText('PS-0003');

    fireEvent.click(screen.getByText('Export All'));
    await waitFor(() => expect(fileDownload).toHaveBeenCalledTimes(1));
    expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['xls', 'sources.xls']);
    const exportCalls = () => apiClient.get.mock.calls.filter(([url]) => url === '/api/productSuppliers/export');
    expect(exportCalls()[0][1].params).toEqual({
      format: 'xls', includeInactive: true, disableMaxLimit: true,
    });

    fireEvent.click(screen.getByText('Export Results'));
    await waitFor(() => expect(exportCalls()).toHaveLength(2));
    // The results export takes the list's filters, including its (unchecked) inactive filter.
    expect(exportCalls()[1][1].params).toEqual(expect.objectContaining({
      format: 'xls', includeInactive: '', disableMaxLimit: true, searchTerm: 'ibu', defaultPreferenceTypes: [],
    }));
  });

  it('deletes a source from its row menu after confirmation and reloads the list', async () => {
    apiClient.delete.mockResolvedValue({});
    renderList('?tab=details');
    await screen.findByText('PS-0003');
    const callsBefore = listCalls().length;

    fireEvent.click(screen.getAllByTestId('dropdown-toggle')[0]);
    fireEvent.click(await screen.findByText('Delete Product Source'));
    expect(await screen.findByText('Are you sure you want to delete this Product Source?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Delete'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/productSuppliers/ps1'));
    expect(mockShowNotification).toHaveBeenCalledTimes(1);
    await waitFor(() => expect(listCalls().length).toBeGreaterThan(callsBefore));
  });

  it('edits a source from its row menu', async () => {
    renderList('?tab=details');
    await screen.findByText('PS-0003');

    fireEvent.click(screen.getAllByTestId('dropdown-toggle')[1]);
    fireEvent.click(await screen.findByText('Edit'));

    expect(window.location).toBe('/openboxes/productSupplier/create/ps2');
  });

  it('opens the create page from the header for product managers', async () => {
    renderList('?tab=details');

    fireEvent.click(await screen.findByText('Create Product Source'));

    expect(window.location).toBe('/openboxes/productSupplier/create');
  });

  it('offers no create, edit or delete to users who cannot manage products', async () => {
    renderList('?tab=details', productSupplierStore({ canManageProducts: false }));
    await screen.findByText('PS-0003');

    expect(screen.queryByText('Create Product Source')).not.toBeInTheDocument();
    expect(screen.getByText('PS-0001').closest('a')).toBeNull();
    fireEvent.click(screen.getAllByTestId('dropdown-toggle')[0]);
    expect(screen.queryByText('Delete Product Source')).not.toBeInTheDocument();
  });

  it('hides the table on the preference types tab and shows it again on details', async () => {
    renderList('?tab=details');
    expect(await screen.findByText('PS-0003')).toBeInTheDocument();

    fireEvent.click(screen.getByText('Preference Types'));
    await waitFor(() => expect(screen.queryByText(/List Product Sources/)).not.toBeInTheDocument());

    fireEvent.click(screen.getByText('Details'));
    expect(await screen.findByText('PS-0003')).toBeInTheDocument();
  });

  it('restores the filters from the URL and sends them with the list request', async () => {
    renderList('?tab=details&searchTerm=ibu&createdFrom=01/01/2026&createdTo=02/01/2026&includeInactive=true');

    await screen.findByText('PS-0003');
    await waitFor(() => expect(listCalls().some(([, config]) => config.params.searchTerm === 'ibu')).toBe(true));
    const [, config] = listCalls().find(([, c]) => c.params.searchTerm === 'ibu');
    expect(config.params).toEqual(expect.objectContaining({
      searchTerm: 'ibu', createdFrom: '01/01/2026', createdTo: '02/01/2026',
    }));
  });
});
