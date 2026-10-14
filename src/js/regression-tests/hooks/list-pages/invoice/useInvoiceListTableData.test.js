import { renderHook } from '@testing-library/react-hooks';
import { useDispatch } from 'react-redux';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useInvoiceListTableData from 'hooks/list-pages/invoice/useInvoiceListTableData';
import useTableData from 'hooks/list-pages/useTableData';
import exportFileFromAPI from 'utils/file-download-util';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useDispatch: jest.fn(),
}));
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn() }));
jest.mock('hooks/list-pages/useTableData', () => ({ __esModule: true, default: jest.fn() }));

describe('useInvoiceListTableData', () => {
  let dispatch;
  let tableArgs;
  const tableData = { data: [], currentParams: { offset: '0', max: '10', status: 'POSTED' } };

  const renderTable = (filterParams = {}) => renderHook(
    () => useInvoiceListTableData(filterParams),
  );

  beforeEach(() => {
    jest.clearAllMocks();
    dispatch = jest.fn();
    useDispatch.mockReturnValue(dispatch);
    useTableData.mockImplementation((args) => {
      tableArgs = args;
      return {
        tableRef: { current: null }, loading: false, tableData, onFetchHandler: jest.fn(),
      };
    });
  });

  it('sorts by invoice date by default', () => {
    renderTable();

    expect(tableArgs).toMatchObject({
      url: '/api/invoices',
      defaultSorting: { sort: 'dateInvoiced', order: 'desc' },
      errorMessageId: 'react.invoice.error.fetching.label',
    });
  });

  it('maps the selected options to ids and drops empty values', () => {
    renderTable({
      status: { value: 'POSTED', id: 'ignored' },
      invoiceTypeCode: { id: 'INVOICE' },
      vendor: { id: 'sup-1' },
      createdBy: { id: 'u1' },
      buyerOrganization: { id: 'org-1' },
      dateInvoiced: '',
    });

    expect(tableArgs.getParams({
      offset: 10, state: { pageSize: 10 }, sortingParams: { sort: 'dateInvoiced', order: 'desc' },
    })).toEqual({
      offset: '10',
      max: '10',
      sort: 'dateInvoiced',
      order: 'desc',
      status: 'POSTED',
      invoiceTypeCode: 'INVOICE',
      vendor: 'sup-1',
      createdBy: 'u1',
      buyerOrganization: 'org-1',
    });
  });

  it('downloads the invoices without paging params behind a spinner', async () => {
    const { result } = renderTable();

    await result.current.downloadInvoices(true);
    await result.current.downloadInvoices();

    expect(exportFileFromAPI.mock.calls).toEqual([
      [{ url: '/api/invoices', params: { status: 'POSTED', invoiceItems: true } }],
      [{ url: '/api/invoices', params: { status: 'POSTED', invoiceItems: false } }],
    ]);
    expect(dispatch.mock.calls.map(([a]) => a.type))
      .toEqual([SHOW_SPINNER, HIDE_SPINNER, SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('hides the spinner when the download fails', async () => {
    exportFileFromAPI.mockRejectedValueOnce(new Error('timeout'));
    const { result } = renderTable();

    await expect(result.current.downloadInvoices()).rejects.toThrow('timeout');
    expect(dispatch.mock.calls.map(([a]) => a.type)).toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });
});
