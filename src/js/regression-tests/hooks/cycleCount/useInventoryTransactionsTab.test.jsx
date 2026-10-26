import { screen } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { findColumn, renderCell, renderHeader } from 'regression-tests/hooks/cycleCount/tabTestUtils';

import inventoryTransactionReportColumn from 'consts/inventoryTransactionReportColumn';
import useInventoryTransactionsTab from 'hooks/cycleCount/useInventoryTransactionsTab';
import useTableDataV2 from 'hooks/useTableDataV2';

import '@testing-library/jest-dom';

let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
}));
jest.mock('selectors', () => ({
  getCurrentLocale: (state) => state.locale,
  getCurrentLocationId: (state) => state.locationId,
  getCycleCountReasonCodes: (state) => state.reasonCodes,
  getDefaultTranslationsFetched: () => true,
}));
jest.mock('components/DataTable', () => ({
  // eslint-disable-next-line global-require
  TableCell: require('regression-tests/hooks/cycleCount/tabTestUtils').MockTableCell,
}));
jest.mock(
  'components/DataTable/TableHeaderCell',
  // eslint-disable-next-line global-require
  () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockTableHeaderCell,
);
jest.mock(
  'components/DataTable/v2/ValueIndicator',
  // eslint-disable-next-line global-require
  () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockValueIndicator,
);
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => `${id}|${defaultMessage}`));

const columnIds = inventoryTransactionReportColumn;

const row = {
  varianceTypeCode: 'LESS',
  product: { id: 'prod-1', productCode: 'P1', name: 'Gloves' },
  transactionAction: 'Cycle count',
  dateRecorded: new Date('2026-03-04T12:00:00').toISOString(),
  recordedBy: { name: 'Ann Smith' },
  transaction: { transactionNumber: 'TX-1' },
  quantityBefore: 10,
  quantityAfter: 8,
  quantityDifference: -2,
  rootCauses: ['DAMAGED', 'UNKNOWN'],
  comments: ['torn box', 'recounted'],
};

describe('useInventoryTransactionsTab', () => {
  let setTableData;
  // String.prototype.replaceAll is missing from the Node 14 runtime the tests use.
  // eslint-disable-next-line no-extend-native
  const nativeReplaceAll = String.prototype.replaceAll;

  beforeAll(() => {
    if (!nativeReplaceAll) {
      // eslint-disable-next-line no-extend-native
      String.prototype.replaceAll = function replaceAll(search, replacement) {
        return this.split(search).join(replacement);
      };
    }
  });

  afterAll(() => {
    if (!nativeReplaceAll) {
      delete String.prototype.replaceAll;
    }
  });

  const filterParams = {
    startDate: '2026-03-01',
    endDate: '2026-03-31',
    products: [{ id: 'prod-1' }],
  };

  const renderTab = (overrides = {}) => renderHook((props) => useInventoryTransactionsTab(props), {
    initialProps: {
      filterParams,
      offset: 0,
      pageSize: 50,
      shouldFetch: true,
      setShouldFetch: jest.fn(),
      serializedParams: 'serialized',
      filtersInitialized: true,
      ...overrides,
    },
  });

  const tableDataArgs = () => useTableDataV2.mock.calls[0][0];
  const column = (result, id) => findColumn(result.current.columns, id);

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      locale: 'en',
      locationId: 'loc-1',
      reasonCodes: [{ id: 'DAMAGED', label: 'Damaged' }, { id: 'EXPIRED', label: 'Expired' }],
    };
    setTableData = jest.fn();
    useTableDataV2.mockImplementation(() => ({
      tableData: { data: [row], totalCount: 1 }, loading: false, setTableData,
    }));
  });

  it('fetches the transactions summary once both dates are set', () => {
    renderTab();
    expect(tableDataArgs()).toMatchObject({
      url: '/api/reports/inventory-transactions-summary',
      shouldFetch: true,
      disableInitialLoading: true,
    });
    expect(tableDataArgs().getParams({ sortingParams: { sort: 'dateRecorded' } })).toEqual({
      offset: '0',
      max: '50',
      sort: 'dateRecorded',
      startDate: '03/01/2026',
      endDate: '03/31/2026',
      products: ['prod-1'],
      facility: 'loc-1',
    });

    useTableDataV2.mockClear();
    renderTab({ filterParams: { startDate: '2026-03-01' } });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('clears the table on mount and when the location changes', () => {
    const { rerender } = renderTab();
    expect(setTableData).toHaveBeenCalledWith({ data: [], totalCount: 0 });
    rerender();
    expect(setTableData).toHaveBeenCalledTimes(1);
    mockState.locationId = 'loc-2';
    rerender();
    expect(setTableData).toHaveBeenCalledTimes(2);
  });

  it('returns the empty message matching the selected time range', () => {
    expect(renderTab({ filterParams: {} }).result.current.emptyTableMessage.defaultMessage)
      .toBe('Select a time range from above filters to load the table.');
    expect(renderTab().result.current.emptyTableMessage.defaultMessage).toBe('No result found.');
  });

  it('renders a translated header for each column', () => {
    const { result } = renderTab();
    const labels = result.current.columns.map((col) => {
      const { container, unmount } = renderHeader(col);
      const text = container.textContent;
      unmount();
      return text;
    });
    expect(labels).toEqual([
      'react.cycleCount.table.alignment.label|Alignment',
      'react.cycleCount.table.product.label|Product',
      'react.cycleCount.table.type.label|Type',
      'react.cycleCount.table.recorded.label|Recorded',
      'react.cycleCount.table.transactionId.label|Transaction ID',
      'react.cycleCount.table.qtyBefore.label|Qty Before',
      'react.cycleCount.table.qtyAfter.label|Qty After',
      'react.cycleCount.table.difference.label|Difference',
      'react.cycleCount.table.rootCauses.label|Root Causes',
      'react.cycleCount.table.comments.label|Comments',
    ]);
  });

  it('renders alignment, product, recorded, transaction id and quantities', () => {
    const { result } = renderTab();

    const alignment = renderCell(column(result, columnIds.ALIGNMENT), row);
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'LESS');
    alignment.unmount();

    const product = renderCell(column(result, columnIds.PRODUCT), row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('P1 Gloves');
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-tooltip', 'P1 Gloves');
    product.unmount();

    const recorded = renderCell(column(result, columnIds.RECORDED), row);
    expect(recorded.container).toHaveTextContent('Ann Smith');
    expect(recorded.container).toHaveTextContent('04/Mar/2026');
    recorded.unmount();

    [
      [columnIds.TRANSACTION_ID, 'TX-1'],
      [columnIds.QTY_BEFORE, '10'],
      [columnIds.QTY_AFTER, '8'],
    ].forEach(([id, text]) => {
      const { container, unmount } = renderCell(column(result, id), row);
      expect(container).toHaveTextContent(text);
      unmount();
    });

    renderCell(column(result, columnIds.QTY_BEFORE), { ...row, quantityBefore: null });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('0');
  });

  it('translates the transaction type from its enum key', () => {
    const { result } = renderTab();
    renderCell(column(result, columnIds.TRANSACTION_TYPE), row);
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'TRANSACTION');
    expect(screen.getByTestId('value-indicator'))
      .toHaveTextContent('react.default.enum.TransactionAction.CYCLE_COUNT|CYCLE_COUNT');
  });

  it('shows the difference with its percentage of the quantity before', () => {
    const { result } = renderTab();
    const difference = column(result, columnIds.QTY_DIFFERENCE);

    const less = renderCell(difference, row);
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'NEGATIVE');
    expect(screen.getByTestId('value-indicator')).toHaveClass('value-indicator--less');
    expect(less.container.querySelector('p')).toHaveTextContent('(20 %)');
    less.unmount();

    const more = renderCell(difference, {
      ...row, quantityBefore: 3, quantityAfter: 4, quantityDifference: 1,
    });
    expect(screen.getByTestId('value-indicator')).toHaveClass('value-indicator--more');
    expect(more.container.querySelector('p')).toHaveTextContent('(33 %)');
    more.unmount();

    const fromZero = renderCell(difference, {
      ...row, quantityBefore: 0, quantityAfter: 5, quantityDifference: 5,
    });
    expect(fromZero.container.querySelector('p')).toHaveTextContent('(100 %)');
    fromZero.unmount();

    const fromNegative = renderCell(difference, {
      ...row, quantityBefore: -2, quantityAfter: 0, quantityDifference: 2,
    });
    expect(fromNegative.container.querySelector('p')).toHaveTextContent('(0 %)');
    fromNegative.unmount();

    const equal = renderCell(difference, {
      ...row, quantityBefore: 5, quantityAfter: 5, quantityDifference: 0,
    });
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'EQUAL');
    expect(equal.container.querySelector('p')).toBeNull();
  });

  it('maps root cause ids to reason code labels and joins comments', () => {
    const { result } = renderTab();

    const causes = renderCell(column(result, columnIds.ROOT_CAUSES), row);
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-tooltip', 'Damaged, ');
    causes.unmount();

    const noCauses = renderCell(
      column(result, columnIds.ROOT_CAUSES),
      { ...row, rootCauses: null },
    );
    expect(screen.getByTestId('table-cell')).toHaveTextContent('');
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-tooltip', '');
    noCauses.unmount();

    const comments = renderCell(column(result, columnIds.COMMENTS), row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('torn box, recounted');
    comments.unmount();

    renderCell(column(result, columnIds.COMMENTS), { ...row, comments: undefined });
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-tooltip', '');
  });
});
