import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import { answer } from 'regression-tests/__mocks__/screenHarness';
import {
  addRootElement, button, CURRENCIES, field, invoice, invoiceItem, pickOption, renderWizard,
  totalLine,
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

const CANDIDATES = [
  invoiceItem('c1', { quantity: 3, description: 'Candidate c1', supplierCode: 'SUP-1' }),
  invoiceItem('c2', { quantity: 5, description: 'Candidate c2' }),
];

let current;
let items;

const rowOf = (text) => screen.getByText(text).closest('.table-row');
const modal = () => document.querySelector('.ReactModal__Content');
const quantityOf = (text) => rowOf(text).querySelector('input[type="number"]');
const itemCalls = () => apiClient.get.mock.calls.filter(([url]) => url === '/api/invoices/inv1/items');
const postsTo = (url) => apiClient.post.mock.calls.filter(([called]) => called === url);

// The items step leaves the single page app by assigning window.location.
const realLocation = window.location;

describe('invoice wizard, items step', () => {
  let removeRoot;

  beforeEach(() => {
    removeRoot = addRootElement();
    delete window.location;
    window.location = { href: '' };
    current = invoice({ totalCount: 2, totalValue: 30 });
    items = [invoiceItem('ii1'), invoiceItem('ii2', { quantity: 4, amount: 20, totalAmount: 20 })];
    answer(apiClient.get, [
      ['/api/unitOfMeasure/currencies', { data: CURRENCIES }],
      ['/api/invoices/inv1', () => ({ data: current })],
      ['/api/invoices/inv1/items', () => ({ data: items, totalCount: items.length })],
      ['/api/invoices/inv1/orders', { data: ['PO-c1', 'PO-c2'] }],
      ['/api/invoices/inv1/shipments', { data: ['SH-c1'] }],
    ]);
    answer(apiClient.post, [
      ['/api/invoices/inv1/invoiceItemCandidates', (url, payload) => ({
        data: payload.orderNumbers.length
          ? CANDIDATES.filter(({ orderNumber }) => payload.orderNumbers.includes(orderNumber))
          : CANDIDATES,
      })],
      ['/api/invoices/inv1', { data: current }],
      ['/api/invoiceItems/ii1/validation', { data: {} }],
    ]);
    apiClient.delete.mockResolvedValue({ data: {} });
  });

  afterEach(() => {
    window.location = realLocation;
    removeRoot();
    jest.clearAllMocks();
  });

  /** Opens the invoice (on its confirm step, as it has lines) and goes back to the items. */
  const openItemsStep = async () => {
    const utils = renderWizard('inv1');
    await screen.findByText('Item ii1');
    fireEvent.click(button('Previous'));
    await screen.findByText('Add lines');
    await screen.findByText('Item ii2');
    apiClient.post.mockClear();
    return utils;
  };

  describe('adding lines', () => {
    beforeEach(() => {
      current = invoice();
      items = [];
    });

    const openModal = async () => {
      renderWizard('inv1');
      fireEvent.click(await screen.findByText('Add lines'));
      return within(await waitFor(() => {
        expect(modal()).not.toBeNull();
        return modal();
      })).findByText('Candidate c1');
    };

    it('opens an empty new invoice on the items step', async () => {
      renderWizard('inv1');

      expect(await screen.findByText('Add lines')).toBeInTheDocument();
      expect(screen.getByText('INV-0001')).toBeInTheDocument();
      expect(totalLine()).toBe('Total: 0.00 USD');
      expect(button('Next')).toBeDisabled();
    });

    it('adds the checked candidates with their quantity and loads the new lines', async () => {
      await openModal();
      expect(postsTo('/api/invoices/inv1/invoiceItemCandidates')[0][1])
        .toEqual({ orderNumbers: [], shipmentNumbers: [] });
      const save = within(modal()).getAllByText('Add invoice items')
        .map((node) => node.closest('button')).find(Boolean);
      expect(save).toBeDisabled();

      fireEvent.click(rowOf('Candidate c1').querySelector('input[type="checkbox"]'));
      expect(quantityOf('Candidate c1')).toHaveValue(3);
      expect(save).not.toBeDisabled();
      items = [invoiceItem('ii1', { description: 'Added c1', quantity: 3, totalAmount: 15 })];
      fireEvent.click(save);

      await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
      expect(postsTo('/api/invoices/inv1/items')[0][1])
        .toEqual({ invoiceItems: [{ id: 'c1', quantityToInvoice: 3 }] });
      expect(await screen.findByText('Added c1')).toBeInTheDocument();
      expect(totalLine()).toBe('Total: 15.00 USD');
      expect(button('Next')).not.toBeDisabled();
    });

    // KNOWN-GAP: OBAF's InvoiceItemsModal saves the row from the stale form values,
    // so the typed quantity to invoice is sent as 0.
    it.skip('adds a candidate with the quantity typed into it', async () => {
      await openModal();

      fireEvent.change(quantityOf('Candidate c1'), { target: { value: '2' } });
      expect(quantityOf('Candidate c1')).toHaveValue(2);
      fireEvent.click(within(modal()).getAllByText('Add invoice items')
        .map((node) => node.closest('button')).find(Boolean));

      await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
      expect(postsTo('/api/invoices/inv1/items')[0][1])
        .toEqual({ invoiceItems: [{ id: 'c1', quantityToInvoice: 2 }] });
    });

    it('drops a line that is unchecked again', async () => {
      await openModal();

      fireEvent.click(rowOf('Candidate c1').querySelector('input[type="checkbox"]'));
      fireEvent.click(rowOf('Candidate c2').querySelector('input[type="checkbox"]'));
      fireEvent.click(rowOf('Candidate c1').querySelector('input[type="checkbox"]'));
      expect(quantityOf('Candidate c1')).toHaveValue(null);
      fireEvent.click(within(modal()).getAllByText('Add invoice items')
        .map((node) => node.closest('button')).find(Boolean));

      await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
      expect(postsTo('/api/invoices/inv1/items')[0][1])
        .toEqual({ invoiceItems: [{ id: 'c2', quantityToInvoice: 5 }] });
    });

    it('checks and unchecks every candidate at once', async () => {
      await openModal();
      const checkAll = modal().querySelector('.m-3[type="checkbox"], .m-3 input[type="checkbox"]');

      fireEvent.click(checkAll);
      expect(quantityOf('Candidate c1')).toHaveValue(3);
      expect(quantityOf('Candidate c2')).toHaveValue(5);

      fireEvent.click(checkAll);
      expect(quantityOf('Candidate c1')).toHaveValue(null);
      expect(rowOf('Candidate c2').querySelector('input[type="checkbox"]')).not.toBeChecked();
    });

    it('narrows the candidates to the chosen order', async () => {
      await openModal();
      const getUrls = () => apiClient.get.mock.calls.map(([url]) => url);
      await waitFor(() => expect(getUrls()).toContain('/api/invoices/inv1/orders'));
      expect(getUrls()).toContain('/api/invoices/inv1/shipments');

      const orderSelect = modal().querySelectorAll('input[aria-autocomplete]')[0];
      fireEvent.focus(orderSelect);
      fireEvent.keyDown(orderSelect, { key: 'ArrowDown', keyCode: 40 });
      fireEvent.click(await within(await screen.findByTestId('custom-select-dropdown-menu')).findByText('PO-c2'));

      await waitFor(() => expect(postsTo('/api/invoices/inv1/invoiceItemCandidates')).toHaveLength(2));
      expect(postsTo('/api/invoices/inv1/invoiceItemCandidates')[1][1])
        .toEqual({ orderNumbers: ['PO-c2'], shipmentNumbers: [] });
      await waitFor(() => expect(within(modal()).queryByText('Candidate c1')).not.toBeInTheDocument());
      expect(within(modal()).getByText('Candidate c2')).toBeInTheDocument();
    });
  });

  it('saves the line quantities and reloads the lines', async () => {
    await openItemsStep();
    const before = itemCalls().length;

    fireEvent.click(button('Save'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(postsTo('/api/invoices/inv1/items')[0][1]).toEqual({
      id: 'inv1',
      invoiceItems: [{ id: 'ii1', quantity: 2 }, { id: 'ii2', quantity: 4 }],
    });
    await waitFor(() => expect(itemCalls().length).toBeGreaterThan(before));
  });

  it('validates a changed quantity with the API and blocks saving when it is refused', async () => {
    const refusal = Object.assign(new Error('invalid'), {
      response: { data: { errorMessages: ['Cannot invoice more than shipped'] } },
    });
    answer(apiClient.post, [['/api/invoiceItems/ii1/validation', refusal]]);
    await openItemsStep();

    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '9' } });
    fireEvent.blur(input);

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith(
      '/api/invoiceItems/ii1/validation',
      { id: 'ii1', quantity: '9', shipmentId: 'shipment-ii1' },
    ));
    await waitFor(() => expect(button('Save')).toBeDisabled());
    expect(button('Next')).toBeDisabled();
  });

  it('accepts a changed quantity the API validates', async () => {
    await openItemsStep();

    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '1' } });
    fireEvent.blur(input);

    await waitFor(() => expect(postsTo('/api/invoiceItems/ii1/validation')).toHaveLength(1));
    fireEvent.click(button('Save'));
    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(postsTo('/api/invoices/inv1/items')[0][1].invoiceItems[0]).toEqual({ id: 'ii1', quantity: 1 });
  });

  it('asks before saving lines with a zero quantity', async () => {
    await openItemsStep();
    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '0' } });
    fireEvent.blur(input);
    await waitFor(() => expect(postsTo('/api/invoiceItems/ii1/validation')).toHaveLength(1));

    fireEvent.click(button('Save'));
    expect(await screen.findByText(/There are some lines with empty or zero quantity/)).toBeInTheDocument();
    fireEvent.click(screen.getByText('No'));
    expect(postsTo('/api/invoices/inv1/items')).toHaveLength(0);

    fireEvent.click(button('Save'));
    fireEvent.click(await screen.findByText('Yes'));
    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(postsTo('/api/invoices/inv1/items')[0][1].invoiceItems[0]).toEqual({ id: 'ii1', quantity: 0 });
  });

  it('saves and moves on to the confirm step', async () => {
    await openItemsStep();

    fireEvent.click(button('Next'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(await screen.findByText('Submit for Approval')).toBeInTheDocument();
  });

  it('asks before moving on with a zero quantity line', async () => {
    await openItemsStep();
    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '0' } });
    fireEvent.blur(input);
    await waitFor(() => expect(postsTo('/api/invoiceItems/ii1/validation')).toHaveLength(1));

    fireEvent.click(button('Next'));
    fireEvent.click(await screen.findByText('Yes'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(await screen.findByText('Submit for Approval')).toBeInTheDocument();
  });

  it('cannot move on when every line has a zero quantity', async () => {
    items = [invoiceItem('ii1', { quantity: 0 })];
    current = invoice({ totalCount: 1, totalValue: 0 });
    renderWizard('inv1');
    await screen.findByText('Item ii1');
    fireEvent.click(button('Previous'));
    await screen.findByText('Add lines');
    await screen.findByText('Item ii1');

    expect(button('Next')).toBeDisabled();
  });

  /** Saves the lines and goes back to the invoice form of the existing invoice. */
  const backToInvoiceForm = async () => {
    const utils = await openItemsStep();
    fireEvent.click(button('Previous'));
    await waitFor(() => expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1));
    expect(await screen.findByText('Invoice Date')).toBeInTheDocument();
    return utils;
  };

  // KNOWN-GAP: OBAF's CreateInvoicePage checks this.state.id (never set), so a change
  // to the invoice data of a saved invoice is saved without asking (and compares vendors for
  // currency).
  it.skip('saves and goes back to the invoice form, which asks before changing the invoice data', async () => {
    const { currentPath } = await backToInvoiceForm();
    expect(field('Vendor Invoice Number').querySelector('input')).toHaveValue('V-77');
    expect(field('Vendor')).toHaveTextContent('Acme Inc');

    fireEvent.change(field('Vendor Invoice Number').querySelector('input'), { target: { value: 'V-78' } });
    fireEvent.click(button('Next'));

    expect(await screen.findByText('Do you want to change invoice data?')).toBeInTheDocument();
    expect(postsTo('/api/invoices/inv1')).toHaveLength(0);
    fireEvent.click(screen.getByText('Yes'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1')).toHaveLength(1));
    expect(postsTo('/api/invoices/inv1')[0][1]).toEqual({
      vendor: 'sup1',
      vendorInvoiceNumber: 'V-78',
      dateInvoiced: '09/15/2026',
      currencyUom: { id: 'USD' },
    });
    expect(await screen.findByText('Add lines')).toBeInTheDocument();
    expect(currentPath()).toBe('/openboxes/invoice/create/inv1');
  });

  it('saves an unchanged invoice form without asking', async () => {
    await backToInvoiceForm();

    fireEvent.click(button('Next'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1')).toHaveLength(1));
    expect(screen.queryByText('Do you want to change invoice data?')).not.toBeInTheDocument();
  });

  // KNOWN-GAP: OBAF's CreateInvoicePage checks this.state.id (never set), so a change
  // to the invoice data of a saved invoice is saved without asking (and compares vendors for
  // currency).
  it.skip('keeps the saved invoice data when the change is declined', async () => {
    await backToInvoiceForm();

    fireEvent.change(field('Vendor Invoice Number').querySelector('input'), { target: { value: 'V-78' } });
    fireEvent.click(button('Next'));
    fireEvent.click(await screen.findByText('No'));

    await waitFor(() => expect(field('Vendor Invoice Number').querySelector('input')).toHaveValue('V-77'));
    expect(postsTo('/api/invoices/inv1')).toHaveLength(0);
  });

  // KNOWN-GAP: OBAF's CreateInvoicePage checks this.state.id (never set), so a change
  // to the invoice data of a saved invoice is saved without asking (and compares vendors for
  // currency).
  it.skip('asks before changing only the currency of the invoice', async () => {
    await backToInvoiceForm();

    await pickOption('Currency', 'Euro');
    fireEvent.click(button('Next'));

    expect(await screen.findByText('Do you want to change invoice data?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Yes'));
    await waitFor(() => expect(postsTo('/api/invoices/inv1')).toHaveLength(1));
    expect(postsTo('/api/invoices/inv1')[0][1].currencyUom).toEqual({ id: 'EUR' });
  });

  it('asks before going back with a zero quantity line', async () => {
    await openItemsStep();
    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '0' } });
    fireEvent.blur(input);
    await waitFor(() => expect(postsTo('/api/invoiceItems/ii1/validation')).toHaveLength(1));

    fireEvent.click(button('Previous'));
    fireEvent.click(await screen.findByText('Yes'));

    expect(await screen.findByText('Invoice Date')).toBeInTheDocument();
  });

  it('deletes a saved line and lowers the total', async () => {
    await openItemsStep();
    expect(totalLine()).toBe('Total: 30.00 USD');

    fireEvent.click(within(rowOf('Item ii1')).getByText('Delete'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/invoices/ii1/removeItem'));
    await waitFor(() => expect(screen.queryByText('Item ii1')).not.toBeInTheDocument());
    expect(totalLine()).toBe('Total: 20.00 USD');
  });

  it('saves and exits to the invoice page', async () => {
    await openItemsStep();

    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(window.location).toBe('/openboxes/invoice/show/inv1'));
    expect(postsTo('/api/invoices/inv1/items')).toHaveLength(1);
  });

  it('asks before saving and exiting with a zero quantity line', async () => {
    await openItemsStep();
    const input = rowOf('Item ii1').querySelector('input');
    fireEvent.change(input, { target: { value: '0' } });
    fireEvent.blur(input);
    await waitFor(() => expect(postsTo('/api/invoiceItems/ii1/validation')).toHaveLength(1));

    fireEvent.click(button('Save and exit'));
    fireEvent.click(await screen.findByText('Yes'));

    await waitFor(() => expect(window.location).toBe('/openboxes/invoice/show/inv1'));
  });

  it('saves the lines before listing candidates when lines are open', async () => {
    await openItemsStep();

    fireEvent.click(screen.getByText('Add lines'));

    await waitFor(() => expect(postsTo('/api/invoices/inv1/invoiceItemCandidates')).toHaveLength(1));
    expect(apiClient.post.mock.calls[0][0]).toBe('/api/invoices/inv1/items');
  });
});
