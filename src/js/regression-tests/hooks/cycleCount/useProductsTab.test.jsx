import { screen } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { findColumn, renderCell, renderHeader } from 'regression-tests/hooks/cycleCount/tabTestUtils';

import cycleCountColumn from 'consts/cycleCountColumn';
import useLastCountedDate from 'hooks/cycleCount/useLastCountedDate';
import useProductsTab from 'hooks/cycleCount/useProductsTab';
import useTableDataV2 from 'hooks/useTableDataV2';

import '@testing-library/jest-dom';

let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
}));
jest.mock('selectors', () => ({
  getCurrentLocale: (state) => state.locale,
  getCurrentLocationId: (state) => state.locationId,
  getFormatLocalizedDate: (state) => state.formatLocalizedDate,
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
// eslint-disable-next-line global-require
jest.mock('utils/Badge', () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockBadge);
jest.mock(
  'components/spinner/Spinner',
  // eslint-disable-next-line global-require
  () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockSpinner,
);
jest.mock('hooks/cycleCount/useLastCountedDate', () => jest.fn());
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));

const row = {
  product: {
    id: 'prod-1',
    productCode: 'P1',
    name: 'Bandage',
    category: 'Dressings',
    tags: [{ id: 't1', name: 'Fragile' }],
    catalogs: [{ id: 'c1', name: 'Core' }],
    abcClass: 'B',
  },
  numberOfCounts: 3,
  numberOfAdjustments: 0,
  quantityAdjusted: -4,
  valueAdjusted: -12.5,
  monthsOfStockChange: 1.25,
  quantityOnHand: 12345,
  valueOnHand: 1000,
};

describe('useProductsTab', () => {
  let setTableData;
  let setShouldFetch;
  let lastCountedDateMap;

  const filterParams = {
    startDate: '2026-01-01',
    endDate: '2026-01-31',
    products: [{ id: 'prod-1' }, { id: 'prod-2' }],
  };

  const renderProductsTab = (overrides = {}) => renderHook((props) => useProductsTab(props), {
    initialProps: {
      filterParams,
      offset: 10,
      pageSize: 10,
      shouldFetch: true,
      setShouldFetch,
      serializedParams: 'serialized',
      filtersInitialized: true,
      ...overrides,
    },
  });

  const tableDataArgs = () => useTableDataV2.mock.calls[0][0];

  const cellOf = (columns, id, original = row) => renderCell(findColumn(columns, id), original);

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      locale: 'en',
      locationId: 'loc-1',
      formatLocalizedDate: jest.fn((date, format) => `${date}|${format}`),
    };
    setTableData = jest.fn();
    setShouldFetch = jest.fn();
    lastCountedDateMap = { 'prod-1': '2026-01-15' };
    useLastCountedDate.mockImplementation(() => ({ lastCountedDateMap }));
    useTableDataV2.mockImplementation(() => ({
      tableData: { data: [row], totalCount: 1 }, loading: false, setTableData,
    }));
  });

  it('fetches the audit summary only once both dates are selected', () => {
    renderProductsTab();
    expect(tableDataArgs()).toMatchObject({
      url: '/api/reports/inventory-audit-summary',
      shouldFetch: true,
      setShouldFetch,
      disableInitialLoading: true,
      filtersInitialized: true,
    });
    expect(tableDataArgs().getParams({ sortingParams: {} })).toEqual({
      offset: '10',
      max: '10',
      startDate: '01/01/2026',
      endDate: '01/31/2026',
      products: ['prod-1', 'prod-2'],
      facility: 'loc-1',
    });

    useTableDataV2.mockClear();
    renderProductsTab({ filterParams: { ...filterParams, endDate: null } });
    expect(tableDataArgs().shouldFetch).toBe(false);

    useTableDataV2.mockClear();
    renderProductsTab({ shouldFetch: false });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('clears the table when the location changes', () => {
    const { rerender } = renderProductsTab();
    expect(setTableData).toHaveBeenCalledTimes(1);
    expect(setTableData).toHaveBeenCalledWith({ data: [], totalCount: 0 });

    rerender();
    expect(setTableData).toHaveBeenCalledTimes(1);

    mockState.locationId = 'loc-2';
    rerender();
    expect(setTableData).toHaveBeenCalledTimes(2);
  });

  it('asks for a time range until dates are selected', () => {
    const { result } = renderProductsTab({ filterParams: {} });
    expect(result.current.emptyTableMessage.id).toBe('react.cycleCount.reporting.emptyTable.label');

    const { result: withDates } = renderProductsTab();
    expect(withDates.current.emptyTableMessage.id).toBe('react.cycleCount.table.noResultFound.label');
  });

  it('places the last counted column after the first five columns', () => {
    const { result } = renderProductsTab();
    expect(result.current.columns.map((column) => column.id ?? column.accessorKey)).toEqual([
      cycleCountColumn.PRODUCT,
      cycleCountColumn.PRODUCTS_TAB_CATEGORY,
      cycleCountColumn.PRODUCTS_TAB_TAGS,
      cycleCountColumn.PRODUCTS_TAB_CATALOGS,
      cycleCountColumn.PRODUCTS_TAB_ABC_CLASS,
      cycleCountColumn.LAST_COUNTED,
      cycleCountColumn.NUMBER_OF_COUNTS,
      cycleCountColumn.NUMBER_OF_ADJUSTMENTS,
      cycleCountColumn.TOTAL_OF_ADJUSTMENTS,
      cycleCountColumn.ADJUSTMENTS_VALUE,
      cycleCountColumn.MONTHS_OF_STOCK_CHANGE,
      cycleCountColumn.QUANTITY_ON_HAND,
      cycleCountColumn.VALUE_ON_HAND,
    ]);
    const labels = result.current.columns.map((column) => {
      const { container, unmount } = renderHeader(column);
      const text = container.textContent;
      unmount();
      return text;
    });
    expect(labels).toEqual([
      'Product', 'Category', 'Tag', 'Product Catalogue', 'ABC Class', 'Last Counted', 'Number Of Counts',
      'Number Of Adjustments', 'Total Of Adjustments', 'Adjustments Value', 'Months Of Stock Change',
      'Currently in Stock', 'Value in Stock',
    ]);
  });

  it('renders the product, category, tags, catalogs and counters', () => {
    const { result } = renderProductsTab();
    const { columns } = result.current;

    const product = cellOf(columns, cycleCountColumn.PRODUCT);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('P1 Bandage');
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/inventoryItem/showStockCard/prod-1');
    product.unmount();

    [
      [cycleCountColumn.PRODUCTS_TAB_CATEGORY, 'Dressings'],
      [cycleCountColumn.PRODUCTS_TAB_TAGS, 'Fragile'],
      [cycleCountColumn.PRODUCTS_TAB_CATALOGS, 'Core'],
      [cycleCountColumn.PRODUCTS_TAB_ABC_CLASS, 'B'],
      [cycleCountColumn.NUMBER_OF_COUNTS, '3'],
      [cycleCountColumn.NUMBER_OF_ADJUSTMENTS, '0'],
      [cycleCountColumn.QUANTITY_ON_HAND, '12,345'],
      [cycleCountColumn.VALUE_ON_HAND, '$1,000.00'],
    ].forEach(([id, text]) => {
      const { container, unmount } = cellOf(columns, id);
      expect(container).toHaveTextContent(text);
      unmount();
    });
  });

  it('shows the last counted date, or a spinner while it is loading', () => {
    const { result } = renderProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.LAST_COUNTED);

    const loaded = renderCell(column, row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('2026-01-15|DD/MMM/yyyy');
    loaded.unmount();

    renderCell(column, { ...row, product: { ...row.product, id: 'prod-unknown' } });
    expect(screen.getByTestId('spinner')).toBeInTheDocument();
  });

  it('colors the total of adjustments by sign', () => {
    const { result } = renderProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.TOTAL_OF_ADJUSTMENTS);

    const negative = renderCell(column, row);
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'NEGATIVE');
    expect(screen.getByTestId('value-indicator')).toHaveClass('value-indicator--less');
    expect(screen.getByTestId('value-indicator')).toHaveTextContent('-4');
    negative.unmount();

    const positive = renderCell(column, { ...row, quantityAdjusted: 2 });
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'POSITIVE');
    expect(screen.getByTestId('value-indicator')).toHaveClass('value-indicator--more');
    positive.unmount();

    renderCell(column, { ...row, quantityAdjusted: 0 });
    expect(screen.getByTestId('value-indicator')).toHaveAttribute('data-variant', 'EQUAL');
    expect(screen.getByTestId('value-indicator')).not.toHaveClass('value-indicator--less');
    expect(screen.getByTestId('value-indicator')).not.toHaveClass('value-indicator--more');
  });

  it('formats the adjustments value as signed currency', () => {
    const { result } = renderProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.ADJUSTMENTS_VALUE);

    const negative = renderCell(column, row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('-$12.50');
    expect(screen.getByTestId('table-cell')).toHaveClass('font-red-ob');
    negative.unmount();

    const positive = renderCell(column, { ...row, valueAdjusted: 5 });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('+$5.00');
    expect(screen.getByTestId('table-cell')).toHaveClass('font-green-ob');
    positive.unmount();

    renderCell(column, { ...row, valueAdjusted: 0 });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('$0.00');
    expect(screen.getByTestId('table-cell')).not.toHaveClass('font-red-ob');
    expect(screen.getByTestId('table-cell')).not.toHaveClass('font-green-ob');
  });

  it('formats the months of stock change with one decimal and a sign', () => {
    const { result } = renderProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.MONTHS_OF_STOCK_CHANGE);

    const positive = renderCell(column, row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('+1.3');
    expect(screen.getByTestId('table-cell')).toHaveClass('font-green-ob');
    positive.unmount();

    renderCell(column, { ...row, monthsOfStockChange: -0.5 });
    expect(screen.getByTestId('table-cell')).toHaveTextContent('-0.5');
    expect(screen.getByTestId('table-cell')).toHaveClass('font-red-ob');
  });
});
