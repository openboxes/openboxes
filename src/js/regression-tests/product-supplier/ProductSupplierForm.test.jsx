import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';

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

// Notifications only record what they were asked to show.
const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => mockShowNotification),
}));

/* eslint-disable import/first,import/order */
import notification from 'components/Layout/notifications/notification';
import ProductSupplierForm from 'components/productSupplier/create/ProductSupplierForm';
import NotificationType from 'consts/notificationTypes';
import apiClient from 'utils/apiClient';

import productSupplierStore from './productSupplierStore';
/* eslint-enable import/first,import/order */

// A whole form renders per test; give it time on a busy machine.
jest.setTimeout(30000);

const PRODUCT_SUPPLIER = {
  id: 'ps1',
  code: 'PS-0001',
  name: 'Ibuprofen from Acme',
  productCode: 'LEG-1',
  supplierCode: 'ACME-IBU',
  active: true,
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  supplier: { id: 'org1', displayName: 'Acme Supplies' },
  manufacturer: { id: 'org2', displayName: 'Pharma Co' },
  ratingTypeCode: 'GOOD',
  manufacturerCode: 'MAN-1',
  brandName: 'Ibu',
  dateCreated: new Date('2026-01-05T12:00:00').toISOString(),
  lastUpdated: new Date('2026-02-05T12:00:00').toISOString(),
  createdBy: { id: 'u1', name: 'Admin' },
  updatedBy: { id: 'u2', name: 'Buyer' },
  minOrderQuantity: 10,
  eachPrice: 2,
  tieredPricing: false,
  contractPrice: null,
  defaultProductPackage: {
    uom: { id: 'BX', name: 'Box' },
    quantity: 10,
    productPrice: { price: 20 },
  },
  attributes: [{ attribute: { id: 'attr1' }, value: 'Blue' }],
  productSupplierPreferences: [
    {
      id: 'pref1',
      destinationParty: null,
      preferenceType: { id: 'PT1', name: 'Preferred' },
      validityStartDate: null,
      validityEndDate: null,
      comments: 'Bid 2026',
    },
  ],
};

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/productSuppliers/ps1') {
    return { data: PRODUCT_SUPPLIER };
  }
  if (url === '/api/generic/product/p1') {
    return { data: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' } };
  }
  if (url === '/api/preferenceTypeOptions') {
    return { data: [{ id: 'PT1', value: 'PT1', label: 'Preferred' }] };
  }
  if (url === '/api/ratingTypeCodeOptions') {
    return { data: [{ id: 'GOOD', value: 'GOOD', label: 'GOOD' }] };
  }
  if (url === '/api/attributes') {
    return {
      data: [{
        id: 'attr1', name: 'Color', options: ['Blue', 'Red'], allowOther: false, required: false,
      }],
    };
  }
  if (url === '/api/unitOfMeasures/options') {
    return { data: [{ id: 'EA', value: 'EA', label: 'Each' }, { id: 'BX', value: 'BX', label: 'Box' }] };
  }
  return { data: [] };
};

const renderForm = (path, store = productSupplierStore()) => render(
  <Provider store={store}>
    <MemoryRouter initialEntries={[path]}>
      <Route path="/productSupplier/create/:productSupplierId?" component={ProductSupplierForm} />
      <Route path="/openboxes/productSupplier/list" render={() => <p>Product source list page</p>} />
    </MemoryRouter>
  </Provider>,
);

describe('product source form', () => {
  beforeEach(() => {
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    apiClient.post.mockImplementation(() => Promise.resolve({ data: { data: {} } }));
    apiClient.put.mockImplementation(() =>
      Promise.resolve({ data: { data: { id: 'ps1', code: 'PS-0001' } } }));
  });

  afterEach(() => jest.clearAllMocks());

  it('loads an existing source into the form and saves every part of it', async () => {
    renderForm('/productSupplier/create/ps1');

    // The title shows the source being edited, with its code.
    expect(await screen.findByText('Ibuprofen from Acme')).toBeInTheDocument();
    expect(screen.getByText('(PS-0001)')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByDisplayValue('ACME-IBU')).toBeInTheDocument());
    expect(screen.getByDisplayValue('LEG-1')).toBeInTheDocument();
    // Created / updated combine the date and the user.
    expect(screen.getByDisplayValue('Jan 05, 2026, by Admin')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Feb 05, 2026, by Buyer')).toBeInTheDocument();
    // An existing source can be deleted.
    expect(screen.getByText('Delete Product Source')).toBeInTheDocument();

    // The form loaded the source and the options the fields need.
    const requested = apiClient.get.mock.calls.map(([url]) => url);
    expect(requested).toEqual(expect.arrayContaining([
      '/api/productSuppliers/ps1', '/api/preferenceTypeOptions', '/api/ratingTypeCodeOptions',
      '/api/attributes', '/api/unitOfMeasures/options',
    ]));
    const attributesCall = apiClient.get.mock.calls.find(([url]) => url === '/api/attributes');
    expect(attributesCall[1].params).toEqual({ entityType: 'PRODUCT_SUPPLIER' });

    const save = screen.getByText('Save').closest('button');
    await waitFor(() => expect(save).not.toBeDisabled());
    fireEvent.click(save);

    await waitFor(() => expect(apiClient.put).toHaveBeenCalledTimes(1));
    const [detailsUrl, details] = apiClient.put.mock.calls[0];
    expect(detailsUrl).toBe('/api/productSuppliers/ps1');
    expect(details).toEqual(expect.objectContaining({
      code: 'PS-0001',
      name: 'Ibuprofen from Acme',
      supplierCode: 'ACME-IBU',
      productCode: 'LEG-1',
      active: true,
      product: 'p1',
      supplier: 'org1',
      manufacturer: 'org2',
      ratingTypeCode: 'GOOD',
      manufacturerCode: 'MAN-1',
      brandName: 'Ibu',
      tieredPricing: false,
    }));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(3));
    const posts = Object.fromEntries(apiClient.post.mock.calls);
    expect(posts['/api/productPackages']).toEqual(expect.objectContaining({
      uom: 'BX',
      productPackageQuantity: 10,
      productPackagePrice: 20,
      minOrderQuantity: 10,
      contractPricePrice: null,
      contractPriceValidUntil: null,
      productSupplier: 'ps1',
    }));
    expect(posts['/api/productSupplierPreferences/batch']).toEqual({
      productSupplierPreferences: [expect.objectContaining({
        id: 'pref1',
        preferenceType: 'PT1',
        comments: 'Bid 2026',
        validityStartDate: null,
        validityEndDate: null,
        productSupplier: 'ps1',
      })],
    });
    expect(posts['/api/productSupplierAttributes/batch']).toEqual({
      productAttributes: [{ attribute: 'attr1', productSupplier: 'ps1', value: 'Blue' }],
    });

    // A success message, then back to the list.
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockShowNotification).toHaveBeenCalledTimes(1);
    expect(await screen.findByText('Product source list page')).toBeInTheDocument();
  });

  it('starts a new source for the product given in the URL, with Save disabled until it is complete', async () => {
    renderForm('/productSupplier/create?productId=p1');

    expect(await screen.findByText('Create Product Source', { selector: 'span' })).toBeInTheDocument();
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/generic/product/p1'));
    expect(await screen.findByText('P-0001 - Ibuprofen 200mg')).toBeInTheDocument();
    // Nothing to delete yet, and the required fields are empty.
    expect(screen.queryByText('Delete Product Source')).not.toBeInTheDocument();
    expect(screen.getByText('Save').closest('button')).toBeDisabled();
    expect(apiClient.get).not.toHaveBeenCalledWith('/api/productSuppliers/ps1');
  });

  it('goes back to the list straight away when nothing was changed', async () => {
    renderForm('/productSupplier/create');

    fireEvent.click(await screen.findByText('Back to Product Source List'));

    expect(await screen.findByText('Product source list page')).toBeInTheDocument();
  });

  it('cancel leaves the form for the list', async () => {
    renderForm('/productSupplier/create');

    fireEvent.click(await screen.findByText('Cancel'));

    expect(await screen.findByText('Product source list page')).toBeInTheDocument();
  });

  it('asks before deleting an existing source and deletes it on confirmation', async () => {
    apiClient.delete.mockResolvedValue({});
    renderForm('/productSupplier/create/ps1');

    fireEvent.click(await screen.findByText('Delete Product Source'));
    expect(await screen.findByText('Are you sure you want to delete this Product Source?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Delete'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/productSuppliers/ps1'));
    expect(await screen.findByText('Product source list page')).toBeInTheDocument();
  });
});
