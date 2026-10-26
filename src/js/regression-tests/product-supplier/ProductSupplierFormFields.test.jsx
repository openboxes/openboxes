import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
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

jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => jest.fn()),
}));

/* eslint-disable import/first,import/order */
import ProductSupplierForm from 'components/productSupplier/create/ProductSupplierForm';
import apiClient from 'utils/apiClient';

import productSupplierStore from './productSupplierStore';
/* eslint-enable import/first,import/order */

// A whole form renders per test; give it time on a busy machine.
jest.setTimeout(30000);

const PRODUCT_SUPPLIER = {
  id: 'ps1',
  code: 'PS-0001',
  name: 'Ibuprofen from Acme',
  active: true,
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  supplier: { id: 'org1', displayName: 'Acme Supplies' },
  defaultProductPackage: { uom: { id: 'BX', name: 'Box' }, quantity: 10, productPrice: { price: 20 } },
  attributes: [],
  productSupplierPreferences: [
    {
      id: 'pref1',
      destinationParty: null,
      preferenceType: { id: 'PT1', name: 'Preferred' },
      comments: 'Bid 2026',
    },
    {
      id: 'pref2',
      destinationParty: { id: 'org9', displayName: 'Clinic Buyer' },
      preferenceType: { id: 'PT1', name: 'Preferred' },
      comments: 'Clinic bid',
    },
  ],
};

const ATTRIBUTES = [
  // Options plus "Other": a select whose menu offers to add a value.
  {
    id: 'attr1', name: 'Color', options: ['Blue', 'Red'], allowOther: true, required: false,
  },
  // No options, other values allowed: a text input.
  {
    id: 'attr2', name: 'Strength', options: [], allowOther: true, required: false,
  },
];

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/productSuppliers/ps1') {
    return { data: PRODUCT_SUPPLIER };
  }
  if (url === '/api/preferenceTypeOptions') {
    return { data: [{ id: 'PT1', value: 'PT1', label: 'Preferred' }] };
  }
  if (url === '/api/attributes') {
    return { data: ATTRIBUTES };
  }
  if (url === '/api/unitOfMeasures/options') {
    return { data: [{ id: 'EA', value: 'EA', label: 'Each' }, { id: 'BX', value: 'BX', label: 'Box' }] };
  }
  return { data: [] };
};

const renderForm = (path) => render(
  <Provider store={productSupplierStore()}>
    <MemoryRouter initialEntries={[path]}>
      <Route path="/productSupplier/create/:productSupplierId?" component={ProductSupplierForm} />
      <Route path="/openboxes/productSupplier/list" render={() => <p>Product source list page</p>} />
    </MemoryRouter>
  </Provider>,
);

// The field (label and input) whose title is the given text.
const field = (title) => Array.from(document.querySelectorAll('[data-testid="form-field"]'))
  .find((element) => element.querySelector('label')?.textContent === title) ?? null;
const input = (name) => document.getElementById(name);

// The open Other modal (react-modal hides it from the accessibility tree in tests).
const attributeModal = () => {
  const modal = document.querySelector('.ReactModal__Content');
  if (!modal) {
    throw new Error('no modal open');
  }
  return modal;
};

const openMenu = (element) => {
  const selectInput = element.querySelector('input');
  fireEvent.focus(selectInput);
  fireEvent.keyDown(selectInput, { key: 'ArrowDown', keyCode: 40 });
};

describe('product source form fields', () => {
  beforeEach(() => {
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url), headers: {} }));
    apiClient.delete.mockResolvedValue({});
  });

  afterEach(() => jest.clearAllMocks());

  it('reports a cleared source name when the field loses focus', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('basicDetails.name')).toHaveValue('Ibuprofen from Acme'));

    fireEvent.change(input('basicDetails.name'), { target: { value: '' } });
    fireEvent.blur(input('basicDetails.name'));

    expect(await screen.findByText('(Source) Name is required')).toBeInTheDocument();
    expect(screen.getByText('Save').closest('button')).toBeDisabled();
  });

  it('fixes the package size at 1 for Each and computes the each price from the package price', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('packageSpecification.productPackageQuantity')).toHaveValue(10));
    // 20 per box of 10.
    await waitFor(() => expect(input('packageSpecification.eachPrice')).toHaveValue(2));

    openMenu(field('Default Source Package'));
    fireEvent.click(await screen.findByText('Each'));

    await waitFor(() => expect(input('packageSpecification.productPackageQuantity')).toHaveValue(1));
    expect(input('packageSpecification.productPackageQuantity')).toBeDisabled();
    await waitFor(() => expect(input('packageSpecification.eachPrice')).toHaveValue(20));

    // Any other unit clears the size again and lets the user type it.
    openMenu(field('Default Source Package'));
    fireEvent.click(await screen.findByText('Box', { selector: '[role="listitem"] *, [role="listitem"]' }));
    await waitFor(() => expect(input('packageSpecification.productPackageQuantity')).toHaveValue(null));
    expect(input('packageSpecification.productPackageQuantity')).not.toBeDisabled();
  });

  it('adds an attribute value that is not in the list through the Other modal', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(field('Color')).not.toBeNull());
    // An attribute without options is typed in.
    expect(field('Strength').querySelector('input[type="text"], input:not([type])')).not.toBeNull();

    openMenu(field('Color'));
    expect(await screen.findByText('Red')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Other'));

    const modal = await waitFor(() => attributeModal());
    expect(within(modal).getByText('Color')).toBeInTheDocument();
    const confirm = within(modal).getByText('Confirm').closest('button');
    expect(confirm).toBeDisabled();

    const otherInput = modal.querySelector('input');
    fireEvent.change(otherInput, { target: { value: 'Green' } });
    expect(confirm).not.toBeDisabled();
    // Emptying it again shows why it cannot be confirmed.
    fireEvent.change(otherInput, { target: { value: '' } });
    expect(within(modal).getByText('Other is required')).toBeInTheDocument();
    fireEvent.change(otherInput, { target: { value: 'Green' } });
    fireEvent.click(confirm);

    await waitFor(() => expect(document.querySelector('.ReactModal__Content')).not.toBeInTheDocument());
    expect(await within(field('Color')).findByText('Green')).toBeInTheDocument();
  });

  it('closes the Other modal without setting a value on cancel', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(field('Color')).not.toBeNull());

    openMenu(field('Color'));
    fireEvent.click(await screen.findByText('Other'));
    const modal = await waitFor(() => attributeModal());
    fireEvent.change(modal.querySelector('input'), { target: { value: 'Green' } });
    fireEvent.click(within(modal).getByText('Cancel'));

    await waitFor(() => expect(document.querySelector('.ReactModal__Content')).not.toBeInTheDocument());
    expect(screen.queryByText('Green')).not.toBeInTheDocument();
  });

  it('deletes the default preference after confirmation and empties its fields', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('defaultPreferenceType.comments')).toHaveValue('Bid 2026'));

    fireEvent.click(document.querySelector('.preference-type-bin.active'));
    expect(await screen.findByText('Are you sure you want to delete the default preference for this source?'))
      .toBeInTheDocument();
    fireEvent.click(screen.getByText('Confirm'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/productSupplierPreferences/pref1'));
    await waitFor(() => expect(input('defaultPreferenceType.comments')).toHaveValue(''));
    expect(document.querySelector('.preference-type-bin.disabled')).not.toBeNull();
  });

  it('lists the preference variations, adds a new one and deletes a saved one after confirmation', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('defaultPreferenceType.comments')).toHaveValue('Bid 2026'));

    fireEvent.click(screen.getByText('Preference Type Variations'));
    expect(await screen.findByDisplayValue('Clinic bid')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/organizations?roleType=ROLE_BUYER&active=false');

    const bins = () => document.querySelectorAll('.preference-type-variations-subsection .preference-type-bin');
    expect(bins()).toHaveLength(1);
    fireEvent.click(screen.getByText('Add new'));
    await waitFor(() => expect(bins()).toHaveLength(2));
    // A blank new row is not counted as invalid.
    expect(document.querySelector('.invalid-items-indicator').textContent).toContain('0');

    // The new row is first; the saved one is second.
    fireEvent.click(bins()[1]);
    expect(await screen.findByText('Are you sure you want to delete this preference variation?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Confirm'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/productSupplierPreferences/pref2'));
    await waitFor(() => expect(screen.queryByDisplayValue('Clinic bid')).not.toBeInTheDocument());
    expect(bins()).toHaveLength(1);
  });

  it('requires a default preference type once its bid name is filled in', async () => {
    renderForm('/productSupplier/create');
    await waitFor(() => expect(input('defaultPreferenceType.comments')).not.toBeNull());

    fireEvent.change(input('defaultPreferenceType.comments'), { target: { value: 'Bid 2027' } });
    fireEvent.blur(input('defaultPreferenceType.comments'));

    expect(await screen.findByText('Default preference type must also be selected')).toBeInTheDocument();
  });

  it('counts a started but incomplete variation as invalid and filters the table down to it', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('defaultPreferenceType.comments')).toHaveValue('Bid 2026'));
    fireEvent.click(screen.getByText('Preference Type Variations'));
    await screen.findByDisplayValue('Clinic bid');

    fireEvent.click(screen.getByText('Add new'));
    const newRowBidName = await waitFor(() => {
      const element = input('productSupplierPreferences.0.comments');
      expect(element).not.toBeNull();
      return element;
    });
    fireEvent.change(newRowBidName, { target: { value: 'Half done' } });
    fireEvent.blur(newRowBidName);

    const indicator = () => document.querySelector('.invalid-items-indicator');
    await waitFor(() => expect(indicator().textContent).toContain('1'));
    fireEvent.click(indicator());

    // Only the invalid new row is left in the table.
    await waitFor(() => expect(screen.queryByDisplayValue('Clinic bid')).not.toBeInTheDocument());
    expect(screen.getByDisplayValue('Half done')).toBeInTheDocument();
  });

  it('asks before leaving with unsaved changes and leaves on confirmation', async () => {
    renderForm('/productSupplier/create/ps1');
    await waitFor(() => expect(input('basicDetails.name')).toHaveValue('Ibuprofen from Acme'));

    fireEvent.change(input('basicDetails.supplierCode'), { target: { value: 'NEW-CODE' } });
    fireEvent.click(screen.getByText('Back to Product Source List'));

    expect(await screen.findByText('Are you sure you want to go back without saving?')).toBeInTheDocument();
    expect(screen.queryByText('Product source list page')).not.toBeInTheDocument();
    fireEvent.click(screen.getByText('Confirm'));

    expect(await screen.findByText('Product source list page')).toBeInTheDocument();
  });
});
