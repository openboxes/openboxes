// OBAF-new: the Auto-Issuance Transactions tab hook (GET /api/reports/auto-issuance-transactions).
import { fireEvent, screen } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { findColumn, renderCell, renderHeader } from 'regression-tests/hooks/cycleCount/tabTestUtils';

import columnIds from 'consts/autoIssuanceTransactionReportColumn';
import { DateFormat } from 'consts/timeFormat';
import useAutoIssuanceTransactionsTab from 'hooks/cycleCount/useAutoIssuanceTransactionsTab';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';

import '@testing-library/jest-dom';

let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
}));
jest.mock('selectors', () => ({
  getCurrentLocationId: (state) => state.locationId,
  getFormatLocalizedDate: (state) => state.formatLocalizedDate,
}));
jest.mock('components/DataTable', () => ({
  // eslint-disable-next-line global-require
  TableCell: require('regression-tests/hooks/cycleCount/tabTestUtils').MockTableCell,
}));
jest.mock('components/DataTable/TableHeaderCell', () => {
  // A header stand-in that also exposes toggleSort, so the sort wiring can be clicked.
  // eslint-disable-next-line global-require
  const React = require('react');
  // eslint-disable-next-line react/prop-types
  return ({
    children, columnId, sortable, toggleSort,
  }) => (
    <button
      type="button"
      data-testid="header-cell"
      data-column={columnId}
      data-sortable={sortable ? 'true' : undefined}
      onClick={toggleSort ? toggleSort(columnId) : undefined}
    >
      {children}
    </button>
  );
});
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTableSorting', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage));

const ROW = {
  product: { id: 'prod-1', productCode: 'P1', name: 'Gloves' },
  dateLastCounted: '2026-03-02T12:00:00Z',
  binLocation: { name: 'A-01' },
  quantityOnHand: -4,
  transaction: { id: 'tx-1', transactionNumber: 'TX-1', transactionDate: '2026-03-05T12:00:00Z' },
  requisition: { id: 'req-1', requestNumber: 'REQ-1' },
  quantityReduced: 6,
};

describe('useAutoIssuanceTransactionsTab (OBAF)', () => {
  let setTableData;
  let innerToggleSort;
  const toggleSortForColumn = jest.fn();

  const filterParams = {
    startDate: '2026-03-01T00:00:00',
    endDate: '2026-03-31T00:00:00',
    products: [{ id: 'prod-1' }, { id: 'prod-2' }],
    binLocations: [{ id: 'bin-1' }],
    negativeOnly: false,
    includeFoo: true,
  };

  const renderTab = (overrides = {}) => renderHook((p) => useAutoIssuanceTransactionsTab(p), {
    initialProps: {
      filterParams,
      offset: 20,
      pageSize: 10,
      shouldFetch: true,
      setShouldFetch: jest.fn(),
      serializedParams: 'serialized',
      filtersInitialized: true,
      ...overrides,
    },
  });

  const tableDataArgs = () => useTableDataV2.mock.calls[useTableDataV2.mock.calls.length - 1][0];
  const column = (result, id) => findColumn(result.current.columns, id);

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      locationId: 'loc-1',
      formatLocalizedDate: (date, format) => `${date.slice(0, 10)}@${format}`,
    };
    setTableData = jest.fn();
    innerToggleSort = jest.fn();
    toggleSortForColumn.mockImplementation(() => innerToggleSort);
    useTableSorting.mockReturnValue({
      sortableProps: { toggleSort: toggleSortForColumn, sort: 'product', order: 'asc' },
      sort: 'product',
      order: 'asc',
    });
    useTableDataV2.mockImplementation(() => ({
      tableData: { data: [ROW], totalCount: 1 }, loading: false, setTableData,
    }));
  });

  it('configures the fetch against the auto-issuance report', () => {
    renderTab();

    expect(tableDataArgs()).toMatchObject({
      url: '/api/reports/auto-issuance-transactions',
      errorMessageId: 'react.cycleCount.table.autoIssuance.errorMessage.label',
      defaultErrorMessage: 'Unable to fetch auto-issuance transactions',
      shouldFetch: true,
      disableInitialLoading: true,
      pageSize: 10,
      offset: 20,
      sort: 'product',
      order: 'asc',
      searchTerm: null,
      serializedParams: 'serialized',
      filtersInitialized: true,
    });
  });

  it('only fetches when asked and both dates are set', () => {
    renderTab({ shouldFetch: false });
    expect(tableDataArgs().shouldFetch).toBe(false);

    renderTab({ filterParams: { ...filterParams, endDate: undefined } });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('builds params with ids, unzoned dates and the facility, dropping empty and false values', () => {
    renderTab();

    expect(tableDataArgs().getParams({ sortingParams: { sort: 'product', order: 'asc' } })).toEqual({
      offset: '20',
      max: '10',
      sort: 'product',
      order: 'asc',
      startDate: '03/01/2026',
      endDate: '03/31/2026',
      products: ['prod-1', 'prod-2'],
      binLocations: ['bin-1'],
      includeFoo: true,
      facility: 'loc-1',
    });
  });

  it('omits the product and bin filters when none are chosen', () => {
    renderTab({
      filterParams: { startDate: filterParams.startDate, endDate: filterParams.endDate },
    });

    const params = tableDataArgs().getParams({ sortingParams: {} });
    expect(params).not.toHaveProperty('products');
    expect(params).not.toHaveProperty('binLocations');
    expect(params.facility).toBe('loc-1');
  });

  it('clears the table when the facility changes', () => {
    const { rerender } = renderTab();
    expect(setTableData).toHaveBeenCalledTimes(1);

    mockState = { ...mockState, locationId: 'loc-2' };
    rerender();
    expect(setTableData).toHaveBeenCalledTimes(2);
    expect(setTableData).toHaveBeenLastCalledWith({ data: [], totalCount: 0 });
  });

  it('defines the eight columns in order, sortable only on product and bin', () => {
    const { result } = renderTab();

    expect(result.current.columns.map((c) => c.accessorKey ?? c.id)).toEqual([
      columnIds.PRODUCT,
      columnIds.LAST_COUNTED,
      columnIds.BIN_LOCATION,
      columnIds.QUANTITY_ON_HAND,
      columnIds.TRANSACTION_NUMBER,
      columnIds.REQUISITION_NUMBER,
      columnIds.QUANTITY_REDUCED,
      columnIds.TRANSACTION_DATE,
    ]);
    const headers = result.current.columns.map((c) => {
      const { container, unmount } = renderHeader(c);
      const header = container.querySelector('[data-testid="header-cell"]');
      const out = [header.textContent, header.getAttribute('data-sortable')];
      unmount();
      return out;
    });
    expect(headers).toEqual([
      ['Product', 'true'],
      ['Last Counted', null],
      ['Bin Location', 'true'],
      ['Quantity On Hand', null],
      ['Transaction ID', null],
      ['Requisition Number', null],
      ['Quantity Reduced', null],
      ['Transaction Date', null],
    ]);
  });

  it('re-arms the fetch when a sortable header is toggled', () => {
    const setShouldFetch = jest.fn();
    const { result } = renderTab({ setShouldFetch });

    renderHeader(column(result, columnIds.BIN_LOCATION));
    fireEvent.click(screen.getByText('Bin Location'));

    expect(toggleSortForColumn).toHaveBeenCalledWith(columnIds.BIN_LOCATION);
    expect(innerToggleSort).toHaveBeenCalled();
    expect(setShouldFetch).toHaveBeenCalledWith(true);
  });

  it('renders the product cell as a stock card link', () => {
    const { result } = renderTab();
    renderCell(column(result, columnIds.PRODUCT), ROW);

    const cell = screen.getByTestId('table-cell');
    expect(cell).toHaveAttribute('data-link', '/openboxes/inventoryItem/showStockCard/prod-1');
    expect(cell).toHaveAttribute('data-tooltip', 'P1 Gloves');
    expect(cell).toHaveTextContent('P1 Gloves');
  });

  it('formats the last counted date', () => {
    const { result } = renderTab();

    renderCell(column(result, columnIds.LAST_COUNTED), ROW);
    expect(screen.getByTestId('table-cell')).toHaveTextContent(`2026-03-02@${DateFormat.DD_MMM_YYYY}`);
  });

  it('formats the transaction date, blank when absent', () => {
    const { result } = renderTab();
    const { unmount } = renderCell(column(result, columnIds.TRANSACTION_DATE), ROW);
    expect(screen.getByTestId('table-cell')).toHaveTextContent(`2026-03-05@${DateFormat.DD_MMM_YYYY}`);
    unmount();

    renderCell(column(result, columnIds.LAST_COUNTED), { ...ROW, dateLastCounted: null });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('');
  });

  it('shows the bin name, or a Default label when there is no bin', () => {
    const { result } = renderTab();
    const { unmount } = renderCell(column(result, columnIds.BIN_LOCATION), ROW);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('A-01');
    unmount();

    renderCell(column(result, columnIds.BIN_LOCATION), { ...ROW, binLocation: null });
    expect(screen.getByText('Default')).toHaveClass('bin-location-default-label');
  });

  it('highlights a negative quantity on hand and defaults a missing one to 0', () => {
    const { result } = renderTab();
    const { unmount } = renderCell(column(result, columnIds.QUANTITY_ON_HAND), ROW);
    expect(screen.getByText('-4')).toHaveClass('negative-quantity');
    unmount();

    renderCell(column(result, columnIds.QUANTITY_ON_HAND), { ...ROW, quantityOnHand: null });
    expect(screen.getByText('0')).not.toHaveClass('negative-quantity');
  });

  it('links the transaction and the requisition, without links when ids are missing', () => {
    const { result } = renderTab();
    let r = renderCell(column(result, columnIds.TRANSACTION_NUMBER), ROW);
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/inventory/showTransaction/tx-1');
    expect(screen.getByTestId('table-cell')).toHaveTextContent('TX-1');
    r.unmount();

    r = renderCell(column(result, columnIds.REQUISITION_NUMBER), ROW);
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/stockMovement/show/req-1');
    expect(screen.getByTestId('table-cell')).toHaveTextContent('REQ-1');
    r.unmount();

    r = renderCell(column(result, columnIds.TRANSACTION_NUMBER), { ...ROW, transaction: null });
    expect(screen.getByTestId('table-cell')).not.toHaveAttribute('data-link');
    r.unmount();

    renderCell(column(result, columnIds.REQUISITION_NUMBER), { ...ROW, requisition: null });
    expect(screen.getByTestId('table-cell')).not.toHaveAttribute('data-link');
  });

  it('shows the quantity reduced, 0 when absent', () => {
    const { result } = renderTab();
    const { unmount } = renderCell(column(result, columnIds.QUANTITY_REDUCED), ROW);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('6');
    unmount();

    renderCell(column(result, columnIds.QUANTITY_REDUCED), { ...ROW, quantityReduced: undefined });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('0');
  });

  it('asks for a time range until both dates are chosen', () => {
    const { result } = renderTab({ filterParams: { startDate: filterParams.startDate } });
    expect(result.current.emptyTableMessage).toEqual({
      id: 'react.cycleCount.reporting.emptyTable.label',
      defaultMessage: 'Select a time range from above filters to load the table.',
    });

    const { result: withDates } = renderTab();
    expect(withDates.current.emptyTableMessage.id).toBe('react.cycleCount.table.noResultFound.label');
    expect(withDates.current.tableData).toEqual({ data: [ROW], totalCount: 1 });
    expect(withDates.current.loading).toBe(false);
  });
});
