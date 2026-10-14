import {
  fireEvent, screen, waitFor,
} from '@testing-library/react';
import { answer } from 'regression-tests/__mocks__/screenHarness';
import {
  addRootElement, button, CURRENCIES, field, invoice, pickOption, renderWizard,
} from 'regression-tests/invoice/invoiceWizardHelpers';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// AutoSizer measures its parent, which is always 0 wide in jsdom.
jest.mock('react-virtualized', () => ({
  ...jest.requireActual('react-virtualized'),
  AutoSizer: ({ children }) => children({ width: 1200, height: 600 }),
}));

/* eslint-disable import/first,import/order */
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const REQUIRED = 'react.default.error.requiredField.label';

describe('invoice wizard, create step', () => {
  let removeRoot;

  beforeEach(() => {
    removeRoot = addRootElement();
    answer(apiClient.get, [
      ['/api/unitOfMeasure/currencies', { data: CURRENCIES }],
      ['/api/organizations?q=*', { data: [{ id: 'sup1', name: 'Acme', displayName: 'Acme Inc' }] }],
      ['/api/invoices/inv1/items', { data: [], totalCount: 0 }],
    ]);
    apiClient.post.mockResolvedValue({ data: { data: invoice() } });
  });

  afterEach(() => {
    removeRoot();
    jest.clearAllMocks();
  });

  it('shows the three steps and an empty invoice form', async () => {
    renderWizard();

    expect(await screen.findByText('Vendor Invoice Number')).toBeInTheDocument();
    ['Create', 'Add items', 'Confirm'].forEach((step) => expect(screen.getByText(step)).toBeInTheDocument());
    expect(field('Invoice number').querySelector('input')).toBeDisabled();
    expect(apiClient.get).toHaveBeenCalledWith('/api/unitOfMeasure/currencies');
  });

  it('requires vendor, vendor invoice number, date and currency', async () => {
    renderWizard();
    await screen.findByText('Vendor Invoice Number');

    fireEvent.click(button('Next'));

    await waitFor(() => expect(screen.getAllByText(REQUIRED)).toHaveLength(4));
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('rejects an invoice date that is not a date', async () => {
    renderWizard();
    await screen.findByText('Vendor Invoice Number');

    fireEvent.change(field('Invoice Date').querySelector('input'), { target: { value: '2026-13-45' } });
    fireEvent.click(button('Next'));

    expect(await screen.findByText('react.default.error.invalid.date')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('creates the invoice and moves on to adding items', async () => {
    const { currentPath } = renderWizard();
    await screen.findByText('Vendor Invoice Number');

    await pickOption('Vendor', 'Acme Inc', 'Acm');
    expect(apiClient.get).toHaveBeenCalledWith('/api/organizations?q=Acm&roleType=ROLE_SUPPLIER&active=false');
    fireEvent.change(field('Vendor Invoice Number').querySelector('input'), { target: { value: 'V-77' } });
    fireEvent.change(field('Invoice Date').querySelector('input'), { target: { value: '09/15/2026' } });
    await pickOption('Currency', 'US Dollar');
    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledTimes(1));
    expect(apiClient.post).toHaveBeenCalledWith('/api/invoices/', {
      vendor: 'sup1',
      vendorInvoiceNumber: 'V-77',
      dateInvoiced: '09/15/2026',
      currencyUom: { id: 'USD' },
    });
    await waitFor(() => expect(currentPath()).toBe('/openboxes/invoice/create/inv1'));
    expect(await screen.findByText('Add lines')).toBeInTheDocument();
    expect(screen.getByText('INV-0001')).toBeInTheDocument();
  });

  it('stays on the form when the invoice cannot be created', async () => {
    apiClient.post.mockImplementation(() => Promise.reject(new Error('rejected')));
    const { currentPath, store } = renderWizard();
    await screen.findByText('Vendor Invoice Number');

    await pickOption('Vendor', 'Acme Inc', 'Acm');
    fireEvent.change(field('Vendor Invoice Number').querySelector('input'), { target: { value: 'V-77' } });
    fireEvent.change(field('Invoice Date').querySelector('input'), { target: { value: '09/15/2026' } });
    await pickOption('Currency', 'US Dollar');
    fireEvent.click(button('Next'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    await waitFor(() => expect(store.getState().spinner.show).toBe(false));
    expect(currentPath()).toBe('/openboxes/invoice/create');
    expect(screen.queryByText('Add lines')).not.toBeInTheDocument();
  });
});
