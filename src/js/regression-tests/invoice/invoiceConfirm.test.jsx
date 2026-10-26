import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import { answer } from 'regression-tests/__mocks__/screenHarness';
import {
  addRootElement, button, field, invoice, invoiceItem, renderWizard, totalLine,
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

const ITEMS = [
  invoiceItem('ii1'),
  invoiceItem('ii2', {
    quantity: 4, amount: 20, totalAmount: 20, displayNames: { default: 'Gauze (synonym)' }, productName: 'Gauze',
  }),
];

let current;
let items;

const answerInvoice = () => answer(apiClient.get, [
  ['/api/invoices/inv1', () => ({ data: current })],
  ['/api/invoices/inv1/items', () => ({ data: items, totalCount: items.length })],
]);

const rowOf = (text) => screen.getByText(text).closest('.table-row');

// The confirm step leaves the single page app by assigning window.location.
const realLocation = window.location;

describe('invoice wizard, confirm step', () => {
  let removeRoot;

  beforeEach(() => {
    removeRoot = addRootElement();
    delete window.location;
    window.location = { href: '' };
    current = invoice({ totalCount: 2, totalValue: 30 });
    items = ITEMS;
    answerInvoice();
    apiClient.post.mockResolvedValue({ data: {} });
    apiClient.delete.mockResolvedValue({ data: {} });
  });

  afterEach(() => {
    window.location = realLocation;
    removeRoot();
    jest.clearAllMocks();
  });

  const openConfirmStep = async (session) => {
    const utils = renderWizard('inv1', session);
    await screen.findByText('Item ii1');
    return utils;
  };

  it('opens an invoice with lines on the confirm step with its details and lines', async () => {
    await openConfirmStep();

    expect(screen.getByText('Invoice options')).toBeInTheDocument();
    expect(field('Invoice number').querySelector('input')).toHaveValue('INV-0001');
    expect(field('Vendor').querySelector('input')).toHaveValue('Acme Inc');
    expect(field('Currency').querySelector('input')).toHaveValue('USD');
    expect(field('Total').querySelector('input')).toHaveValue('30.00');
    expect(field('Total')).not.toHaveClass('negative-value');
    expect(apiClient.get).toHaveBeenCalledWith('/api/invoices/inv1/items', { params: { offset: 0, max: 2 } });
    // Lines link to their order and shipment and show the synonym of the product.
    expect(within(rowOf('Item ii1')).getByText('PO-ii1').closest('a'))
      .toHaveAttribute('href', '/openboxes/order/show/order-ii1');
    expect(within(rowOf('Item ii1')).getByText('SH-ii1').closest('a'))
      .toHaveAttribute('href', '/openboxes/stockMovement/show/shipment-ii1');
    expect(screen.getByText('Gauze (synonym)')).toBeInTheDocument();
    expect(within(rowOf('Item ii1')).getByText('5.00')).toBeInTheDocument();
    expect(within(rowOf('Item ii1')).getByText('10.00')).toBeInTheDocument();
    await waitFor(() => expect(button('Submit for Approval')).not.toBeDisabled());
    expect(button('Post Invoice')).not.toBeDisabled();
  });

  it('marks a negative total', async () => {
    current = invoice({ totalCount: 2, totalValue: -5 });
    await openConfirmStep();

    await waitFor(() => expect(field('Total').querySelector('input')).toHaveValue('(5.00)'));
    expect(field('Total')).toHaveClass('negative-value');
  });

  it('lists the visible invoice documents under download', async () => {
    current = invoice({
      totalCount: 2,
      totalValue: 30,
      documents: [
        { name: 'Invoice report', link: '/openboxes/invoice/print/inv1' },
        { name: 'Internal sheet', link: '/x', hidden: true },
      ],
    });
    await openConfirmStep();

    const dropdown = document.querySelector('.dropdown-content');
    expect(dropdown).not.toHaveClass('visible');
    fireEvent.click(button('Download'));

    expect(dropdown).toHaveClass('visible');
    expect(within(dropdown).getByText('Invoice report').closest('a'))
      .toHaveAttribute('href', '/openboxes/invoice/print/inv1');
    expect(within(dropdown).queryByText('Internal sheet')).not.toBeInTheDocument();
  });

  it('submits the invoice for approval and opens it', async () => {
    await openConfirmStep();
    await waitFor(() => expect(button('Submit for Approval')).not.toBeDisabled());

    fireEvent.click(button('Submit for Approval'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/invoices/inv1/submit'));
    await waitFor(() => expect(window.location).toBe('/openboxes/invoice/show/inv1'));
  });

  it('posts the invoice and opens it', async () => {
    await openConfirmStep();
    await waitFor(() => expect(button('Post Invoice')).not.toBeDisabled());

    fireEvent.click(button('Post Invoice'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/invoices/inv1/post'));
    await waitFor(() => expect(window.location).toBe('/openboxes/invoice/show/inv1'));
  });

  it('offers posting to superusers only', async () => {
    await openConfirmStep({ isSuperuser: false });

    expect(screen.queryByText('Post Invoice')).not.toBeInTheDocument();
    expect(screen.getByText('Submit for Approval')).toBeInTheDocument();
  });

  it('exits to the invoice page without saving anything', async () => {
    await openConfirmStep();

    fireEvent.click(button('Save and exit'));

    expect(window.location).toBe('/openboxes/invoice/show/inv1');
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(screen.queryByText('Save')).not.toBeInTheDocument();
  });

  it('locks a posted invoice', async () => {
    current = invoice({
      totalCount: 2, totalValue: 30, datePosted: '09/20/2026', dateSubmitted: '09/18/2026',
    });
    await openConfirmStep();

    expect(button('Previous')).toBeDisabled();
    expect(button('Submit for Approval')).toBeDisabled();
    expect(button('Post Invoice')).toBeDisabled();
  });

  it('goes back to the items step with the lines editable', async () => {
    await openConfirmStep();

    fireEvent.click(button('Previous'));

    expect(await screen.findByText('Add lines')).toBeInTheDocument();
    expect(await screen.findByText('Gauze (synonym)')).toBeInTheDocument();
    expect(screen.getByText('Item ii1')).toBeInTheDocument();
    await waitFor(() => expect(totalLine()).toBe('Total: 30.00 USD'));
  });
});

describe('invoice wizard, confirm step of an invoice with a prepayment', () => {
  let removeRoot;

  beforeEach(() => {
    removeRoot = addRootElement();
    delete window.location;
    window.location = { href: '' };
    current = invoice({
      totalCount: 3, totalValue: 30, hasPrepaymentInvoice: true, invoiceType: 'INVOICE',
    });
    items = [
      invoiceItem('ii1', { quantityAvailableToInvoice: 2 }),
      invoiceItem('ii2', {
        description: 'Prepayment of ii1', isPrepaymentItem: true, inverse: true, quantity: 2, amount: -10,
      }),
      invoiceItem('adj1', {
        description: 'Freight', orderAdjustment: true, quantity: 1, unitPrice: 30, amount: 30, unitPriceAvailableToInvoice: 0,
      }),
    ];
    answerInvoice();
    apiClient.post.mockResolvedValue({ data: {} });
    apiClient.delete.mockResolvedValue({ data: {} });
  });

  afterEach(() => {
    window.location = realLocation;
    removeRoot();
    jest.clearAllMocks();
  });

  const openActions = async (rowText) => {
    fireEvent.click(rowOf(rowText).querySelector('.action-dots'));
  };

  const quantityInput = (rowText) => rowOf(rowText).querySelector('input[type="number"]');

  it('marks prepayment lines, negates their quantity and hides their actions', async () => {
    renderWizard('inv1');
    await screen.findByText('Prepayment of ii1');

    const prepaymentRow = rowOf('Prepayment of ii1');
    expect(within(prepaymentRow).getByText('\u24C5')).toBeInTheDocument();
    expect(within(prepaymentRow).getByText('-2')).toBeInTheDocument();
    expect(prepaymentRow.querySelector('.action-dots')).toBeNull();
    expect(prepaymentRow.closest('.negative-row-value')).not.toBeNull();
    expect(rowOf('Item ii1').querySelector('.action-dots')).not.toBeNull();
    // The prepaid invoice cannot go back to the items step.
    expect(button('Previous')).toBeDisabled();
    expect(screen.getByText('Save')).toBeInTheDocument();
  });

  it('edits a line quantity, flags a quantity above what is left to invoice, and saves the change', async () => {
    renderWizard('inv1');
    await screen.findByText('Item ii1');

    await openActions('Item ii1');
    fireEvent.click(await screen.findByText('Edit'));
    const input = quantityInput('Item ii1');
    expect(input).toHaveValue(2);

    // The message itself is in the hover tooltip; the field gets a red border.
    fireEvent.change(input, { target: { value: '3' } });
    await waitFor(() => expect(quantityInput('Item ii1')).toHaveClass('has-errors'));
    await waitFor(() => expect(button('Save')).toBeDisabled());
    expect(button('Submit for Approval')).toBeDisabled();

    fireEvent.change(quantityInput('Item ii1'), { target: { value: '1' } });
    await waitFor(() => expect(button('Save')).not.toBeDisabled());
    fireEvent.click(button('Save'));

    await waitFor(() => expect(apiClient.post)
      .toHaveBeenCalledWith('/api/prepaymentInvoices/inv1/invoiceItems', [{ id: 'ii1', quantity: 1 }]));
    // The lines are reloaded and the line is no longer in edit mode.
    await waitFor(() => expect(quantityInput('Item ii1')).toBeNull());
  });

  it('edits the amount of an order adjustment and rejects one above what is left', async () => {
    renderWizard('inv1');
    await screen.findByText('Freight');

    await openActions('Freight');
    fireEvent.click(await screen.findByText('Edit'));
    const input = quantityInput('Freight');
    expect(input).toHaveValue(30);

    fireEvent.change(input, { target: { value: '31' } });
    await waitFor(() => expect(quantityInput('Freight')).toHaveClass('has-errors'));
    expect(button('Save')).toBeDisabled();
    // An adjustment cannot change sign.
    fireEvent.change(quantityInput('Freight'), { target: { value: '-5' } });
    await waitFor(() => expect(quantityInput('Freight')).toHaveValue(-5));
    expect(quantityInput('Freight')).toHaveClass('has-errors');
    fireEvent.change(quantityInput('Freight'), { target: { value: '20' } });
    await waitFor(() => expect(quantityInput('Freight')).not.toHaveClass('has-errors'));
    expect(button('Save')).not.toBeDisabled();
  });

  it('saves the edited lines before submitting', async () => {
    renderWizard('inv1');
    await screen.findByText('Item ii1');
    await openActions('Item ii1');
    fireEvent.click(await screen.findByText('Edit'));
    fireEvent.change(quantityInput('Item ii1'), { target: { value: '1' } });
    await waitFor(() => expect(button('Submit for Approval')).not.toBeDisabled());

    fireEvent.click(button('Submit for Approval'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/invoices/inv1/submit'));
    expect(apiClient.post.mock.calls.map(([url]) => url)).toEqual([
      '/api/prepaymentInvoices/inv1/invoiceItems',
      '/api/invoices/inv1/submit',
    ]);
  });

  it('saves the edited lines before posting and before exiting', async () => {
    renderWizard('inv1');
    await screen.findByText('Item ii1');
    await openActions('Item ii1');
    fireEvent.click(await screen.findByText('Edit'));
    fireEvent.change(quantityInput('Item ii1'), { target: { value: '1' } });
    await waitFor(() => expect(button('Post Invoice')).not.toBeDisabled());

    fireEvent.click(button('Post Invoice'));
    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/invoices/inv1/post'));

    fireEvent.click(button('Save and exit'));
    await waitFor(() => expect(apiClient.post.mock.calls
      .filter(([url]) => url === '/api/prepaymentInvoices/inv1/invoiceItems')).toHaveLength(2));
    expect(window.location).toBe('/openboxes/invoice/show/inv1');
  });

  it('exits without a request when no line was edited', async () => {
    renderWizard('inv1');
    await screen.findByText('Item ii1');

    fireEvent.click(button('Save and exit'));

    await waitFor(() => expect(window.location).toBe('/openboxes/invoice/show/inv1'));
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('removes a line and reloads the lines', async () => {
    renderWizard('inv1');
    await screen.findByText('Item ii1');
    const itemCalls = () => apiClient.get.mock.calls.filter(([url]) => url === '/api/invoices/inv1/items');
    const before = itemCalls().length;
    items = items.filter(({ id }) => id !== 'ii1' && id !== 'ii2');

    await openActions('Item ii1');
    fireEvent.click(await screen.findByText('Remove'));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith('/api/prepaymentInvoiceItems/ii1'));
    await waitFor(() => expect(itemCalls().length).toBeGreaterThan(before));
    await waitFor(() => expect(screen.queryByText('Item ii1')).not.toBeInTheDocument());
  });

  it('offers only removal for a canceled line', async () => {
    items = [invoiceItem('ii1', { isCanceled: true })];
    current = invoice({ totalCount: 1, totalValue: 10, hasPrepaymentInvoice: true });
    renderWizard('inv1');
    await screen.findByText('Item ii1');

    await openActions('Item ii1');

    expect(await screen.findByText('Remove')).toBeInTheDocument();
    expect(screen.queryByText('Edit')).not.toBeInTheDocument();
  });

  it('shows a prepayment invoice read-only', async () => {
    current = invoice({
      totalCount: 1, totalValue: 10, isPrepaymentInvoice: true, invoiceType: 'PREPAYMENT_INVOICE',
    });
    items = [invoiceItem('ii1')];
    renderWizard('inv1');
    await screen.findByText('Item ii1');

    expect(within(rowOf('Item ii1')).getByText('\u24C5')).toBeInTheDocument();
    expect(rowOf('Item ii1').querySelector('.action-dots')).toBeNull();
    expect(button('Previous')).toBeDisabled();
    expect(screen.queryByText('Save')).not.toBeInTheDocument();
  });
});
