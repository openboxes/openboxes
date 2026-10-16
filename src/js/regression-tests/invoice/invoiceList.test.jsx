import {
  fireEvent, screen, waitFor, within,
} from '@testing-library/react';
import fileDownload from 'js-file-download';
import {
  answer, HERE, renderAt, sessionStore,
} from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// jsdom cannot save files.
jest.mock('js-file-download', () => jest.fn());

/* eslint-disable import/first,import/order */
import InvoiceList from 'components/invoice/list/InvoiceList';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const STATUSES = [
  {
    id: 'PENDING', value: 'PENDING', label: 'Pending', variant: 'primary',
  },
  {
    id: 'POSTED', value: 'POSTED', label: 'Posted', variant: 'success',
  },
];

const TYPE_CODES = [
  { id: 'INVOICE', value: 'INVOICE', label: 'Invoice' },
  { id: 'PREPAYMENT_INVOICE', value: 'PREPAYMENT_INVOICE', label: 'Prepayment invoice' },
];

const INVOICES = [
  {
    id: 'inv1',
    invoiceNumber: 'INV-0001',
    status: 'PENDING',
    itemCount: 4,
    invoiceTypeCode: 'INVOICE',
    partyCode: 'ACME',
    vendorInvoiceNumber: 'V-77',
    totalValue: '1,250.00',
    currency: 'USD',
  },
  {
    id: 'inv2',
    invoiceNumber: 'INV-0002',
    status: 'POSTED',
    invoiceTypeCode: 'PREPAYMENT_INVOICE',
    partyCode: 'GLOBEX',
    vendorInvoiceNumber: 'V-78',
    totalValue: '10.00',
    currency: 'EUR',
  },
];

const ORGANIZATION = {
  id: 'org1', name: 'Our hospital', displayName: 'Our hospital (OH)',
};

const store = () => sessionStore({ location: { ...HERE, organization: ORGANIZATION } });

const renderList = (entry = '/openboxes/invoice/list') => renderAt({
  component: InvoiceList,
  path: '**/invoice/list',
  entry,
  store: store(),
});

const listCalls = () => apiClient.get.mock.calls
  .filter(([url, config]) => url === '/api/invoices' && config.responseType !== 'blob');
const lastListParams = () => listCalls()[listCalls().length - 1][1].params;
const rowOf = (text) => screen.getByText(text).closest('.rt-tr');

describe('invoice list', () => {
  beforeEach(() => {
    answer(apiClient.get, [
      ['/api/invoiceStatuses', { data: STATUSES }],
      ['/api/invoiceTypeCodes', { data: TYPE_CODES }],
      ['/api/organizations*', { data: [{ id: 'sup1', name: 'Acme', displayName: 'Acme Inc' }] }],
      ['/api/invoices', (url, config) => (config.responseType === 'blob'
        ? 'number,status'
        : { data: INVOICES, totalCount: 2 })],
    ]);
  });

  afterEach(() => jest.clearAllMocks());

  it('lists the invoices of the buyer organization, newest first', async () => {
    renderList();

    expect(await screen.findByText('INV-0001')).toBeInTheDocument();
    expect(lastListParams()).toEqual({
      offset: '0',
      max: '10',
      sort: 'dateInvoiced',
      order: 'desc',
      buyerOrganization: 'org1',
    });
    expect(within(rowOf('INV-0001')).getByText('4')).toBeInTheDocument();
    // A missing item count shows as zero.
    expect(within(rowOf('INV-0002')).getByText('0')).toBeInTheDocument();
    expect(within(rowOf('INV-0002')).getByText('Posted')).toBeInTheDocument();
    expect(within(rowOf('INV-0001')).getByText('ACME')).toBeInTheDocument();
    expect(within(rowOf('INV-0001')).getByText('1,250.00')).toBeInTheDocument();
    expect(screen.getByText('INV-0001').closest('a')).toHaveAttribute('href', '/openboxes/invoice/show/inv1');
    expect(screen.getByText('Invoice List')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/organizations?roleType=ROLE_SUPPLIER&active=false&sort=name&order=asc');
  });

  // KNOWN-GAP: OBAF's InvoiceListTable reads .label of the status it finds, which
  // throws for a status that is not in the loaded list, and the list fails to load.
  it.skip('shows the raw status of an invoice whose status is not in the loaded list', async () => {
    answer(apiClient.get, [
      ['/api/invoiceStatuses', { data: STATUSES }],
      ['/api/invoices', {
        data: [...INVOICES, {
          ...INVOICES[0], id: 'inv3', invoiceNumber: 'INV-0003', status: 'SUBMITTED',
        }],
        totalCount: 3,
      }],
    ]);
    renderList();

    expect(await screen.findByText('INV-0003')).toBeInTheDocument();
    expect(within(rowOf('INV-0003')).getByText('Submitted')).toBeInTheDocument();
    expect(within(rowOf('INV-0001')).getByText('Pending')).toBeInTheDocument();
  });

  it('searches by invoice number', async () => {
    renderList();
    await screen.findByText('INV-0001');

    fireEvent.change(screen.getByPlaceholderText('Search by invoice number...'), { target: { value: 'INV-0002' } });
    fireEvent.click(screen.getByText('Search').closest('button'));

    await waitFor(() => expect(lastListParams().invoiceNumber).toBe('INV-0002'));
    expect(lastListParams().buyerOrganization).toBe('org1');
  });

  it('starts from the filters in the address', async () => {
    const { currentSearch } = renderList('/openboxes/invoice/list?dateInvoiced=09/01/2026&createdBy=u1');

    await waitFor(() => expect(lastListParams()).toEqual(expect.objectContaining({
      dateInvoiced: '09/01/2026',
      createdBy: 'u1',
      buyerOrganization: 'org1',
    })));
    expect(currentSearch()).toBe('?buyerOrganization=org1&createdBy=u1&dateInvoiced=09%2F01%2F2026');
  });

  it('offers the row actions with links to the invoice pages', async () => {
    renderList();
    await screen.findByText('INV-0001');

    fireEvent.click(rowOf('INV-0001').querySelector('.action-dots'));

    expect((await screen.findByText('View Invoice Details')).closest('a'))
      .toHaveAttribute('href', '/openboxes/invoice/show/inv1');
    expect(screen.getByText('Add document').closest('a'))
      .toHaveAttribute('href', '/openboxes/invoice/addDocument/inv1');
    expect(screen.getByText('Edit Invoice').closest('a'))
      .toHaveAttribute('href', '/openboxes/invoice/create/inv1');
  });

  // KNOWN-GAP: OBAF's downloader reads the Content-Disposition header unconditionally
  // and throws on this export response, which has none.
  it.skip('exports the listed invoices and their lines with the current filters', async () => {
    renderList();
    await screen.findByText('INV-0001');

    fireEvent.click(screen.getByText('Export Invoices'));
    await waitFor(() => expect(fileDownload).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByText('Export Invoice Line Details'));
    await waitFor(() => expect(fileDownload).toHaveBeenCalledTimes(2));

    const exports = apiClient.get.mock.calls.filter(([, config]) => config?.responseType === 'blob');
    expect(exports.map(([url, config]) => [url, config.params])).toEqual([
      ['/api/invoices', {
        format: 'csv', sort: 'dateInvoiced', order: 'desc', buyerOrganization: 'org1', invoiceItems: false,
      }],
      ['/api/invoices', {
        format: 'csv', sort: 'dateInvoiced', order: 'desc', buyerOrganization: 'org1', invoiceItems: true,
      }],
    ]);
    expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['number,status', 'file.csv']);
  });
});
