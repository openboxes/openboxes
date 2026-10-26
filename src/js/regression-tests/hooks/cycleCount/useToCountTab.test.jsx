import { fireEvent, screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react-hooks';
import fileDownload from 'js-file-download';
import { useHistory } from 'react-router-dom';
import {
  findColumn, installRandomUUID, renderCell, renderHeader,
} from 'regression-tests/hooks/cycleCount/tabTestUtils';

import { startCount } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import notification from 'components/Layout/notifications/notification';
import cycleCountColumn from 'consts/cycleCountColumn';
import useToCountTab from 'hooks/cycleCount/useToCountTab';
import useTableDataV2 from 'hooks/useTableDataV2';
import confirmationModal from 'utils/confirmationModalUtils';
import exportFileFromAPI from 'utils/file-download-util';

import '@testing-library/jest-dom';

let mockState;
const mockDispatch = jest.fn((action) => Promise.resolve(action));
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
}));
jest.mock('selectors', () => ({
  getCurrentLocale: (state) => state.locale,
  getCurrentLocation: (state) => state.location,
  getCycleCountMaxSelectedProducts: (state) => state.maxSelected,
  getCycleCountTranslations: () => true,
  getFormatLocalizedDate: (state) => state.formatLocalizedDate,
}));
jest.mock('react-router-dom', () => ({ useHistory: jest.fn() }));
jest.mock('actions', () => ({
  startCount: jest.fn((payload, locationId) => ({ type: 'START_COUNT', payload, locationId })),
}));
jest.mock('api/services/CycleCountApi', () => ({
  deleteRequests: jest.fn(),
  startCount: jest.fn(),
  getPendingRequests: jest.fn(),
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
jest.mock(
  'utils/StatusIndicator',
  // eslint-disable-next-line global-require
  () => require('regression-tests/hooks/cycleCount/tabTestUtils').MockStatusIndicator,
);
jest.mock('components/Layout/notifications/notification', () => {
  const mockNotify = jest.fn();
  return jest.fn(() => mockNotify);
});
jest.mock('utils/Translate', () => jest.fn(({ defaultMessage }) => defaultMessage));
jest.mock('hooks/cycleCount/useCycleCountProductAvailability', () => jest.fn((row) => row.testMeta));
jest.mock('hooks/useQueryParams', () => jest.fn(() => ({ tab: 'TO_COUNT' })));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
jest.mock('utils/confirmationModalUtils', () => jest.fn());
jest.mock('utils/file-download-util', () => ({
  __esModule: true,
  default: jest.fn(),
  extractFilenameFromHeader: jest.fn(() => 'count-form.pdf'),
}));
jest.mock('js-file-download', () => jest.fn());

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const buildRow = (id, overrides = {}) => ({
  status: 'REQUESTED',
  product: { id: `prod-${id}`, productCode: `P${id}`, name: `Product ${id}` },
  category: { name: 'Medicines' },
  internalLocations: 'A1,A2',
  quantityOnHand: 12,
  cycleCountRequest: {
    id: `req-${id}`,
    inventoryItemsCount: 3,
    initialCount: { assignee: { id: 'u1', name: 'Jane Doe' }, deadline: '2026-01-10' },
  },
  testMeta: { isRowDisabled: false, showCancelCheckbox: false },
  ...overrides,
});

describe('useToCountTab', () => {
  beforeAll(installRandomUUID);

  let history;
  let fetchData;
  let checkboxes;
  let tableData;

  const filterParams = {
    tab: 'TO_COUNT',
    searchTerm: 'para',
    dateLastCount: '2026-01-05',
    categories: [{ id: 'cat-1' }],
    internalLocations: [{ name: 'A1' }],
    tags: [{ id: 'tag-1' }],
    catalogs: [],
    negativeQuantity: false,
    countAssignees: [{ id: 'u1' }],
    countDeadline: null,
  };

  const renderToCountTab = (overrides = {}) => renderHook(() => useToCountTab({
    filterParams,
    offset: 0,
    pageSize: 10,
    toCountTabCheckboxes: checkboxes,
    serializedParams: 'serialized',
    ...overrides,
  }));

  const tableDataArgs = () => useTableDataV2.mock.calls[0][0];

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      locale: 'en',
      location: { id: 'loc-1', name: 'Main' },
      maxSelected: 2,
      formatLocalizedDate: jest.fn((date, format) => `${date}|${format}`),
    };
    history = { push: jest.fn() };
    useHistory.mockReturnValue(history);
    fetchData = jest.fn();
    tableData = { data: [buildRow(1), buildRow(2)], totalCount: 2 };
    useTableDataV2.mockImplementation(() => ({ tableData, loading: false, fetchData }));
    checkboxes = {
      selectRow: jest.fn((id) => jest.fn(() => id)),
      isChecked: jest.fn((id) => id === 'req-1'),
      selectHeaderCheckbox: jest.fn(() => jest.fn()),
      headerCheckboxProps: { value: false },
      checkedCheckboxes: ['req-1', 'req-2'],
      resetCheckboxes: jest.fn(),
    };
  });

  it('fetches pending requests of the current location only when the tab is active', () => {
    renderToCountTab();
    expect(tableDataArgs()).toMatchObject({
      url: '/api/facilities/loc-1/cycle-counts/requests/pending',
      shouldFetch: true,
      pageSize: 10,
      serializedParams: 'serialized',
    });

    useTableDataV2.mockClear();
    renderToCountTab({ filterParams: { ...filterParams, tab: 'ALL_PRODUCTS' } });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('builds request params from the filters and drops empty and false values', () => {
    renderToCountTab();
    const params = tableDataArgs().getParams({ sortingParams: { sort: 'product', order: 'asc' } });

    expect(params).toEqual({
      tab: 'TO_COUNT',
      statuses: ['CREATED', 'REQUESTED', 'COUNTING'],
      offset: '0',
      max: '10',
      sort: 'product',
      order: 'asc',
      searchTerm: 'para',
      facility: 'loc-1',
      dateLastCount: '01/05/2026',
      categories: ['cat-1'],
      internalLocations: ['A1'],
      tags: ['tag-1'],
      countAssignees: ['u1'],
    });
  });

  it('keeps negativeQuantity when it is true', () => {
    renderToCountTab({ filterParams: { ...filterParams, negativeQuantity: true } });
    expect(tableDataArgs().getParams({}).negativeQuantity).toBe(true);
  });

  it('extends every row with the product availability meta', () => {
    const { result } = renderToCountTab();
    expect(result.current.tableData.data[0].meta).toEqual(tableData.data[0].testMeta);
    expect(result.current.tableData.totalCount).toBe(2);
  });

  it('returns the table data untouched when there is no data', () => {
    tableData = { totalCount: 0 };
    const { result } = renderToCountTab();
    expect(result.current.tableData).toBe(tableData);
  });

  it('exposes the checkbox column followed by the data columns', () => {
    const { result } = renderToCountTab();
    expect(result.current.columns.map((column) => column.id ?? column.accessorKey)).toEqual([
      cycleCountColumn.SELECTED,
      cycleCountColumn.STATUS,
      cycleCountColumn.PRODUCT,
      cycleCountColumn.CATEGORY_NAME,
      cycleCountColumn.INTERNAL_LOCATIONS,
      cycleCountColumn.INITIAL_COUNT_ASSIGNEE,
      cycleCountColumn.INITIAL_COUNT_DEADLINE,
      cycleCountColumn.INVENTORY_ITEMS,
      cycleCountColumn.QUANTITY_ON_HAND,
    ]);
    expect(result.current.emptyTableMessage).toEqual({
      id: 'react.cycleCount.table.emptyTable.label',
      defaultMessage: 'No products match the given criteria',
    });
  });

  it('header checkbox selects only the requests that are not cancellable', () => {
    tableData = {
      data: [buildRow(1), buildRow(2, { testMeta: { showCancelCheckbox: true } })],
      totalCount: 2,
    };
    const { result } = renderToCountTab();
    renderHeader(findColumn(result.current.columns, cycleCountColumn.SELECTED));

    const getIds = checkboxes.selectHeaderCheckbox.mock.calls[0][0];
    expect(getIds()).toEqual(['req-1']);
  });

  it('row checkbox selects the request, or opens the cancel modal for a cancellable row', () => {
    const { result } = renderToCountTab();
    const column = findColumn(result.current.columns, cycleCountColumn.SELECTED);

    const { unmount } = renderCell(column, result.current.tableData.data[0]);
    expect(checkboxes.selectRow).toHaveBeenCalledWith('req-1');
    expect(screen.getByTestId('checkbox')).toBeChecked();
    fireEvent.click(screen.getByTestId('checkbox'));
    expect(confirmationModal).not.toHaveBeenCalled();
    unmount();

    checkboxes.selectRow.mockClear();
    renderCell(column, buildRow(3, { meta: { showCancelCheckbox: true } }));
    expect(checkboxes.selectRow).not.toHaveBeenCalled();
    expect(screen.getByTestId('checkbox')).toHaveClass('cancel-icon');
    fireEvent.click(screen.getByTestId('checkbox'));
    expect(confirmationModal).toHaveBeenCalledWith(expect.objectContaining({
      title: { label: 'react.cycleCount.modal.cancelCounts.title.label', default: 'Cancel Counts?' },
    }));
  });

  it('confirming the cancel modal deletes the single request and refreshes the table', async () => {
    cycleCountApi.deleteRequests.mockResolvedValue({});
    const { result } = renderToCountTab();
    result.current.openCancelCountsModal({ id: 'req-9' });

    const onClose = jest.fn();
    const [back, confirm] = confirmationModal.mock.calls[0][0].buttons(onClose);
    back.onClick();
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(cycleCountApi.deleteRequests).not.toHaveBeenCalled();

    await confirm.onClick();
    expect(onClose).toHaveBeenCalledTimes(2);
    expect(cycleCountApi.deleteRequests).toHaveBeenCalledWith('loc-1', 'req-9');
    expect(mockSpinner.show).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
    expect(fetchData).toHaveBeenCalled();
    expect(checkboxes.resetCheckboxes).toHaveBeenCalled();
  });

  it('cancelling without an id deletes all checked requests and still refreshes on failure', async () => {
    cycleCountApi.deleteRequests.mockRejectedValue(new Error('boom'));
    const { result } = renderToCountTab();
    result.current.openCancelCountsModal({});

    const [, confirm] = confirmationModal.mock.calls[0][0].buttons();
    await expect(confirm.onClick()).rejects.toThrow('boom');
    expect(cycleCountApi.deleteRequests).toHaveBeenCalledWith('loc-1', ['req-1', 'req-2']);
    expect(mockSpinner.hide).toHaveBeenCalled();
    expect(fetchData).toHaveBeenCalled();
    expect(checkboxes.resetCheckboxes).toHaveBeenCalled();
  });

  it('renders the status, product, category and quantity cells', () => {
    const { result } = renderToCountTab();
    const row = result.current.tableData.data[0];

    const status = renderCell(findColumn(result.current.columns, cycleCountColumn.STATUS), row);
    expect(screen.getByTestId('status')).toHaveAttribute('data-variant', 'danger');
    expect(screen.getByTestId('status')).toHaveTextContent('To count');
    status.unmount();

    const disabled = renderCell(
      findColumn(result.current.columns, cycleCountColumn.STATUS),
      { ...row, meta: { isRowDisabled: true } },
    );
    expect(screen.getByTestId('status')).toHaveAttribute('data-variant', 'gray');
    disabled.unmount();

    const product = renderCell(findColumn(result.current.columns, cycleCountColumn.PRODUCT), row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('P1 Product 1');
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/inventoryItem/showStockCard/prod-1');
    product.unmount();

    const category = renderCell(
      findColumn(result.current.columns, cycleCountColumn.CATEGORY_NAME),
      row,
    );
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Medicines');
    category.unmount();

    renderCell(findColumn(result.current.columns, cycleCountColumn.QUANTITY_ON_HAND), row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('12');
  });

  it('renders up to four bin locations and a counter of the hidden ones', () => {
    const { result } = renderToCountTab();
    const column = findColumn(result.current.columns, cycleCountColumn.INTERNAL_LOCATIONS);

    const few = renderCell(column, buildRow(1));
    expect(screen.getByText('A1')).toBeInTheDocument();
    expect(screen.getByText('A2')).toBeInTheDocument();
    expect(screen.queryByText(/more/)).not.toBeInTheDocument();
    few.unmount();

    const many = renderCell(column, buildRow(1, { internalLocations: 'B1,B2,B3,B4,B5,B6' }));
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-tooltip', 'B1,B2,B3,B4,B5,B6 (6)');
    expect(screen.queryByText('B5')).not.toBeInTheDocument();
    expect(screen.getByText(/more/)).toHaveTextContent('+2 more');
    many.unmount();

    const { container } = renderCell(column, buildRow(1, { internalLocations: null }));
    expect(container).toBeEmptyDOMElement();
  });

  it('renders the assignee, the localized deadline and the inventory items count', () => {
    const { result } = renderToCountTab();
    const row = buildRow(1);

    const assignee = renderCell(
      findColumn(result.current.columns, cycleCountColumn.INITIAL_COUNT_ASSIGNEE),
      row,
    );
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Jane Doe');
    assignee.unmount();

    const deadline = renderCell(
      findColumn(result.current.columns, cycleCountColumn.INITIAL_COUNT_DEADLINE),
      row,
    );
    expect(screen.getByTestId('table-cell')).toHaveTextContent('2026-01-10|DD/MMM/yyyy');
    deadline.unmount();

    renderCell(findColumn(result.current.columns, cycleCountColumn.INVENTORY_ITEMS), row);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('3');
  });

  it('marks product, category and quantity headers as sortable', () => {
    const { result } = renderToCountTab();
    const sortable = result.current.columns.slice(1).map((column) => {
      const { unmount } = renderHeader(column);
      const isSortable = screen.getByTestId('header-cell').getAttribute('data-sortable') === 'true';
      unmount();
      return isSortable;
    });
    expect(sortable).toEqual([false, true, true, false, false, false, false, true]);
  });

  it('exports the candidates with the current filters', () => {
    const { result } = renderToCountTab();
    result.current.exportTableData();

    expect(mockSpinner.show).toHaveBeenCalled();
    const exportArgs = exportFileFromAPI.mock.calls[0][0];
    expect(exportArgs.url).toBe('/api/facilities/loc-1/cycle-counts/candidates');
    expect(exportArgs.params).toMatchObject({ facility: 'loc-1', searchTerm: 'para', max: '10' });
    expect(exportArgs.filename).toMatch(/^CycleCountReport-Main-/);
    expect(exportArgs.afterExporting).toBe(mockSpinner.hide);
  });

  it('prints the count form for the checked requests', async () => {
    cycleCountApi.startCount.mockResolvedValue({
      data: 'blob',
      headers: { 'content-disposition': 'attachment; filename=count-form.pdf' },
    });
    const { result } = renderToCountTab();
    await result.current.printCountForm('PDF');

    expect(cycleCountApi.startCount).toHaveBeenCalledWith({
      payload: { requests: [{ cycleCountRequest: 'req-1' }, { cycleCountRequest: 'req-2' }] },
      locationId: 'loc-1',
      format: 'PDF',
      config: { responseType: 'blob' },
    });
    expect(fileDownload).toHaveBeenCalledWith('blob', 'count-form.pdf', 'application/pdf');
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('starts the count and navigates to the count step', async () => {
    const { result } = renderToCountTab();
    await result.current.moveToCounting();

    expect(startCount).toHaveBeenCalledWith(
      { requests: [{ cycleCountRequest: 'req-1' }, { cycleCountRequest: 'req-2' }] },
      'loc-1',
    );
    expect(mockDispatch).toHaveBeenCalledWith(expect.objectContaining({ type: 'START_COUNT' }));
    expect(history.push).toHaveBeenCalledWith('/openboxes/inventory/cycleCount/count');
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('does not navigate when starting the count fails', async () => {
    mockDispatch.mockRejectedValueOnce(new Error('failed'));
    const { result } = renderToCountTab();
    await expect(result.current.moveToCounting()).rejects.toThrow('failed');
    expect(history.push).not.toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('refuses to start counting more products than allowed', async () => {
    mockState.maxSelected = 1;
    const { result } = renderToCountTab();
    await result.current.moveToCounting();

    expect(startCount).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith('error-outlined');
  });

  it('loads the selected requests into the assign modal and clears them on close', async () => {
    cycleCountApi.getPendingRequests.mockResolvedValue({
      data: {
        data: [
          {
            cycleCountRequest: {
              id: 'req-1',
              product: { id: 'prod-1' },
              inventoryItemsCount: 2,
              initialCount: { assignee: { id: 'u1', name: 'Jane Doe' }, deadline: '2026-01-10' },
            },
          },
          {
            cycleCountRequest: {
              id: 'req-2',
              product: { id: 'prod-2' },
              inventoryItemsCount: 1,
              initialCount: { assignee: null, deadline: null },
            },
          },
        ],
      },
    });
    const { result } = renderToCountTab();
    expect(result.current.isAssignCountModalOpen).toBe(false);

    result.current.openAssignCountModal();
    await flushPromises();

    expect(cycleCountApi.getPendingRequests).toHaveBeenCalledWith({
      locationId: 'loc-1',
      requestIds: ['req-1', 'req-2'],
      max: 2,
    });
    expect(result.current.isAssignCountModalOpen).toBe(true);
    expect(result.current.assignCountModalData.current).toEqual([
      {
        cycleCountRequestId: 'req-1',
        product: { id: 'prod-1' },
        assignee: {
          id: 'u1', name: 'Jane Doe', value: 'u1', label: 'Jane Doe',
        },
        deadline: '2026-01-10',
        inventoryItemsCount: 2,
      },
      {
        cycleCountRequestId: 'req-2',
        product: { id: 'prod-2' },
        assignee: null,
        deadline: null,
        inventoryItemsCount: 1,
      },
    ]);

    act(() => result.current.closeAssignCountModal());
    expect(result.current.isAssignCountModalOpen).toBe(false);
    expect(result.current.assignCountModalData.current).toEqual([]);
  });
});
