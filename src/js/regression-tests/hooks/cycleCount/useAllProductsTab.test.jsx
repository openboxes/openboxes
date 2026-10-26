import { screen } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import {
  findColumn, installRandomUUID, renderCell, renderHeader,
} from 'regression-tests/hooks/cycleCount/tabTestUtils';

import cycleCountApi from 'api/services/CycleCountApi';
import notification from 'components/Layout/notifications/notification';
import cycleCountColumn from 'consts/cycleCountColumn';
import useAllProductsTab from 'hooks/cycleCount/useAllProductsTab';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTableDataV2 from 'hooks/useTableDataV2';
import exportFileFromAPI from 'utils/file-download-util';

import '@testing-library/jest-dom';

let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
}));
jest.mock('selectors', () => ({
  getCurrentLocation: (state) => state.location,
  getCycleCountMaxSelectedProducts: (state) => state.maxSelected,
  getFormatLocalizedDate: (state) => state.formatLocalizedDate,
}));
jest.mock('api/services/CycleCountApi', () => ({
  createRequest: jest.fn(),
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
  'components/form-elements/v2/Checkbox',
  // eslint-disable-next-line global-require
  () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockCheckbox,
);
// eslint-disable-next-line global-require
jest.mock('utils/Badge', () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockBadge);
jest.mock('components/Layout/notifications/notification', () => {
  const mockNotify = jest.fn();
  return jest.fn(() => mockNotify);
});
jest.mock('utils/Translate', () => jest.fn(({ defaultMessage }) => defaultMessage));
jest.mock('hooks/cycleCount/useCycleCountProductAvailability', () => jest.fn((row) => row.testMeta));
jest.mock('hooks/useQueryParams', () => jest.fn(() => ({ tab: 'ALL_PRODUCTS' })));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTableCheckboxes', () => jest.fn());
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn() }));

const buildRow = (id, overrides = {}) => ({
  dateLastCount: '2025-12-01',
  product: { id: `prod-${id}`, productCode: `P${id}`, name: `Product ${id}` },
  category: { name: 'Lab' },
  internalLocations: 'S1',
  tags: [{ id: 't1', tag: 'Cold chain' }],
  productCatalogs: [{ id: 'c1', name: 'Essentials' }],
  abcClass: 'A',
  quantityOnHand: 7,
  testMeta: { isRowDisabled: false, isFromOtherTab: false },
  ...overrides,
});

describe('useAllProductsTab', () => {
  beforeAll(installRandomUUID);

  let checkboxes;
  let tableData;
  let switchTab;
  let setToCountCheckedCheckboxes;
  const resetForm = jest.fn();

  const filterParams = {
    tab: 'ALL_PRODUCTS',
    searchTerm: 'gauze',
    dateLastCount: null,
    categories: [{ id: 'cat-1' }, { id: 'cat-2' }],
    internalLocations: [],
    tags: [],
    catalogs: [],
    abcClasses: [{ id: 'A' }],
    negativeQuantity: false,
  };

  const renderAllProductsTab = (overrides = {}) => renderHook(() => useAllProductsTab({
    filterParams,
    switchTab,
    offset: 0,
    pageSize: 25,
    resetForm,
    setToCountCheckedCheckboxes,
    serializedParams: 'serialized',
    ...overrides,
  }));

  const tableDataArgs = () => useTableDataV2.mock.calls[0][0];

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      location: { id: 'loc-3', name: 'Ward' },
      maxSelected: 3,
      formatLocalizedDate: jest.fn((date, format) => `${date}|${format}`),
    };
    switchTab = jest.fn();
    setToCountCheckedCheckboxes = jest.fn();
    tableData = {
      data: [
        buildRow(1),
        buildRow(2, { testMeta: { isRowDisabled: true, isFromOtherTab: true } }),
      ],
      totalCount: 2,
    };
    useTableDataV2.mockImplementation(() => ({ tableData, loading: false }));
    checkboxes = {
      selectRow: jest.fn((id) => jest.fn(() => id)),
      isChecked: jest.fn(() => false),
      selectHeaderCheckbox: jest.fn(() => jest.fn()),
      selectedCheckboxesAmount: 1,
      checkedCheckboxes: ['prod-1'],
      headerCheckboxProps: {},
      resetCheckboxes: jest.fn(),
    };
    useTableCheckboxes.mockReturnValue(checkboxes);
  });

  it('fetches candidates in every in-progress status with the filter ids', () => {
    renderAllProductsTab();
    expect(tableDataArgs()).toMatchObject({
      url: '/api/facilities/loc-3/cycle-counts/candidates',
      shouldFetch: true,
      pageSize: 25,
    });
    expect(tableDataArgs().getParams({ sortingParams: { sort: 'abcClass', order: 'asc' } })).toEqual({
      tab: 'ALL_PRODUCTS',
      statuses: ['CREATED', 'REQUESTED', 'COUNTING', 'COUNTED', 'INVESTIGATING'],
      showCycleCountsInProgress: true,
      offset: '0',
      max: '25',
      sort: 'abcClass',
      order: 'asc',
      searchTerm: 'gauze',
      categories: ['cat-1', 'cat-2'],
      abcClasses: ['A'],
    });
  });

  it('maps bin, tag and catalog filters and keeps a true negativeQuantity', () => {
    renderAllProductsTab({
      filterParams: {
        ...filterParams,
        dateLastCount: '2026-02-01',
        internalLocations: [{ name: 'S1' }],
        tags: [{ id: 'tag-1' }],
        catalogs: [{ id: 'cat-x' }],
        negativeQuantity: true,
      },
    });
    expect(tableDataArgs().getParams({})).toMatchObject({
      dateLastCount: '02/01/2026',
      internalLocations: ['S1'],
      tags: ['tag-1'],
      catalogs: ['cat-x'],
      negativeQuantity: true,
    });
  });

  it('does not fetch while another tab is shown', () => {
    renderAllProductsTab({ filterParams: { ...filterParams, tab: 'TO_COUNT' } });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('header checkbox selects only products that are not disabled', () => {
    const { result } = renderAllProductsTab();
    renderHeader(findColumn(result.current.columns, cycleCountColumn.SELECTED));
    expect(checkboxes.selectHeaderCheckbox).toHaveBeenCalledWith(['prod-1']);
  });

  it('shows a disabled, checked checkbox for products already in another tab', () => {
    const { result } = renderAllProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.SELECTED);
    const [available, inOtherTab] = result.current.tableData.data;

    const first = renderCell(column, available);
    expect(screen.getByTestId('checkbox')).not.toBeChecked();
    expect(screen.getByTestId('checkbox')).toBeEnabled();
    expect(checkboxes.selectRow).toHaveBeenCalledWith('prod-1');
    first.unmount();

    renderCell(column, inOtherTab);
    expect(screen.getByTestId('checkbox')).toBeChecked();
    expect(screen.getByTestId('checkbox')).toBeDisabled();
  });

  it('links the product only when it is not in another tab', () => {
    const { result } = renderAllProductsTab();
    const column = findColumn(result.current.columns, cycleCountColumn.PRODUCT);
    const [available, inOtherTab] = result.current.tableData.data;

    const first = renderCell(column, available);
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/inventoryItem/showStockCard/prod-1');
    expect(screen.getByTestId('table-cell')).toHaveTextContent('P1 Product 1');
    first.unmount();

    renderCell(column, inOtherTab);
    expect(screen.getByTestId('table-cell')).not.toHaveAttribute('data-link');
  });

  it('renders the date, category, bins, tags, catalogs, abc class and quantity cells', () => {
    const { result } = renderAllProductsTab();
    const { columns } = result.current;
    const [row, disabledRow] = result.current.tableData.data;

    const expectations = [
      [cycleCountColumn.DATE_LAST_COUNTED, '2025-12-01|DD/MMM/yyyy'],
      [cycleCountColumn.CATEGORY_NAME, 'Lab'],
      [cycleCountColumn.INTERNAL_LOCATIONS, 'S1'],
      [cycleCountColumn.TAGS, 'Cold chain'],
      [cycleCountColumn.PRODUCT_CATALOGS, 'Essentials'],
      [cycleCountColumn.ABC_CLASS, 'A'],
      [cycleCountColumn.QUANTITY_ON_HAND, '7'],
    ];
    expectations.forEach(([id, text]) => {
      const { container, unmount } = renderCell(findColumn(columns, id), row);
      expect(container).toHaveTextContent(text);
      unmount();
    });

    const tags = renderCell(findColumn(columns, cycleCountColumn.TAGS), disabledRow);
    expect(screen.getByTestId('badge')).toHaveAttribute('data-variant', 'badge--purple');
    expect(tags.container.querySelector('.badge-container')).toHaveClass('disabled');
    tags.unmount();

    const catalogs = renderCell(findColumn(columns, cycleCountColumn.PRODUCT_CATALOGS), row);
    expect(screen.getByTestId('badge')).toHaveAttribute('data-variant', 'badge--blue');
    expect(catalogs.container.querySelector('.badge-container')).not.toHaveClass('disabled');
    catalogs.unmount();

    const bins = renderCell(findColumn(columns, cycleCountColumn.INTERNAL_LOCATIONS), { ...row, internalLocations: 'S1,S2,S3,S4,S5,S6,S7' });
    expect(screen.getByText(/more/)).toHaveTextContent('+3 more');
    bins.unmount();

    const noBins = renderCell(
      findColumn(columns, cycleCountColumn.INTERNAL_LOCATIONS),
      { ...row, internalLocations: null },
    );
    expect(noBins.container).toBeEmptyDOMElement();
  });

  it('orders the columns with checkbox and last counted first and labels them', () => {
    const { result } = renderAllProductsTab();
    const labels = result.current.columns.slice(1).map((column) => {
      const { container, unmount } = renderHeader(column);
      const text = container.textContent;
      unmount();
      return text;
    });
    expect(labels).toEqual([
      'Last Counted', 'Products', 'Category', 'Bin Location', 'Tag', 'Product Catalogue', 'ABC Class', 'Quantity',
    ]);
    expect(result.current.selectedCheckboxesAmount).toBe(1);
  });

  it('exports the candidates with the current filter params', () => {
    const { result } = renderAllProductsTab();
    result.current.exportTableData();
    const exportArgs = exportFileFromAPI.mock.calls[0][0];
    expect(exportArgs.url).toBe('/api/facilities/loc-3/cycle-counts/candidates');
    expect(exportArgs.params).toMatchObject({ searchTerm: 'gauze', categories: ['cat-1', 'cat-2'] });
    expect(exportArgs.filename).toMatch(/^CycleCountReport-Ward-/);
    expect(exportArgs.afterExporting).toBe(mockSpinner.hide);
  });

  it('creates blind count requests and switches to the To Count tab', async () => {
    cycleCountApi.createRequest.mockResolvedValue({ data: { data: [{ id: 'req-10' }] } });
    const { result } = renderAllProductsTab();
    await result.current.countSelected();

    expect(cycleCountApi.createRequest).toHaveBeenCalledWith(
      { requests: [{ product: 'prod-1', blindCount: true }] },
      'loc-3',
    );
    const updater = setToCountCheckedCheckboxes.mock.calls[0][0];
    expect(updater(['req-1'])).toEqual(['req-1', 'req-10']);
    expect(switchTab).toHaveBeenCalledWith('TO_COUNT', resetForm);
    expect(checkboxes.resetCheckboxes).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('resets the selection and keeps the tab when creating requests fails', async () => {
    cycleCountApi.createRequest.mockRejectedValue(new Error('nope'));
    const { result } = renderAllProductsTab();
    await expect(result.current.countSelected()).rejects.toThrow('nope');

    expect(switchTab).not.toHaveBeenCalled();
    expect(checkboxes.resetCheckboxes).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('notifies instead of counting when more products than allowed are selected', async () => {
    checkboxes.checkedCheckboxes = ['p1', 'p2', 'p3', 'p4'];
    const { result } = renderAllProductsTab();
    await result.current.countSelected();

    expect(cycleCountApi.createRequest).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith('error-outlined');
  });
});
