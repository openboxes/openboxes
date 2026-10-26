// eslint-disable-next-line import/no-extraneous-dependencies
import { fireEvent, screen, within } from '@testing-library/react';
import { renderAt, sessionStore } from 'regression-tests/__mocks__/screenHarness';

import InvoiceWizard from 'components/invoice/create/InvoiceWizard';

export const PATH = '**/invoice/create/:invoiceId?';

export const CURRENCIES = [
  { id: 'USD', name: 'US Dollar', code: 'USD' },
  { id: 'EUR', name: 'Euro', code: 'EUR' },
];

export const invoice = (extra = {}) => ({
  id: 'inv1',
  invoiceNumber: 'INV-0001',
  vendor: 'sup1',
  vendorName: 'Acme Inc',
  vendorInvoiceNumber: 'V-77',
  dateInvoiced: '09/15/2026',
  currencyUom: { id: 'USD', code: 'USD', name: 'US Dollar' },
  totalCount: 0,
  totalValue: 0,
  status: 'PENDING',
  documents: [],
  ...extra,
});

export const invoiceItem = (id, extra = {}) => ({
  id,
  orderNumber: `PO-${id}`,
  orderId: `order-${id}`,
  shipmentNumber: `SH-${id}`,
  shipmentId: `shipment-${id}`,
  budgetCode: 'BUD',
  glCode: 'GL-1',
  productCode: `P-${id}`,
  description: `Item ${id}`,
  quantity: 2,
  uom: 'EA',
  unitPrice: 5,
  amount: 10,
  totalAmount: 10,
  ...extra,
});

/** The select menus and the date picker render into the page's #root element. */
export const addRootElement = () => {
  const root = document.createElement('div');
  root.id = 'root';
  document.body.appendChild(root);
  return () => root.remove();
};

// The invoice lines are only loaded page by page, which needs pagination on.
export const renderWizard = (id, session = {}) => renderAt({
  component: InvoiceWizard,
  path: PATH,
  entry: `/openboxes/invoice/create${id ? `/${id}` : ''}`,
  store: sessionStore({ session: { isPaginated: true, ...session } }),
});

export const field = (label) => screen.getByLabelText(label, { selector: '[data-testid="form-field"]' });

/** Opens the select of field `label` (typing `search` first when given) and picks `option`. */
export const pickOption = async (label, option, search) => {
  const input = field(label).querySelector('input[aria-autocomplete]');
  fireEvent.focus(input);
  if (search) {
    fireEvent.change(input, { target: { value: search } });
  } else {
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
  }
  const menu = await screen.findByTestId('custom-select-dropdown-menu');
  fireEvent.click(await within(menu).findByText(option));
};

export const button = (label) => screen.getByText(label).closest('button');

/** The total under the items table, with the non-breaking space made plain. */
export const totalLine = () => document.querySelector('.font-weight-bold.float-right')
  .textContent.replace(/\u00a0/g, ' ').trim();
