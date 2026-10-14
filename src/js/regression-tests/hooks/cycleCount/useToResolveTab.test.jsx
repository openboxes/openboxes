import { screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react-hooks';
import fileDownload from 'js-file-download';
import { useHistory } from 'react-router-dom';
import {
  findColumn, installRandomUUID, renderCell, renderHeader,
} from 'regression-tests/hooks/cycleCount/tabTestUtils';

import { startResolution } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import notification from 'components/Layout/notifications/notification';
import cycleCountColumn from 'consts/cycleCountColumn';
import useToResolveTab from 'hooks/cycleCount/useToResolveTab';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
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
  startResolution: jest.fn((ids, locationId) => ({ type: 'START_RESOLUTION', ids, locationId })),
}));
jest.mock('api/services/CycleCountApi', () => ({
  deleteRequests: jest.fn(),
  startRecount: jest.fn(),
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
jest.mock('hooks/useQueryParams', () => jest.fn(() => ({ tab: 'TO_RESOLVE' })));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTableCheckboxes', () => jest.fn());
jest.mock('hooks/useTableDataV2', () => jest.fn());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
jest.mock('utils/confirmationModalUtils', () => jest.fn());
jest.mock('utils/file-download-util', () => ({
  __esModule: true,
  default: jest.fn(),
  extractFilenameFromHeader: jest.fn(() => 'resolve-form.xls'),
}));
jest.mock('js-file-download', () => jest.fn());

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const buildRow = (id, overrides = {}) => ({
  status: 'COUNTED',
  product: { id: `prod-${id}`, productCode: `P${id}`, name: `Product ${id}` },
  category: { name: 'Supplies' },
  internalLocations: 'R1,R2,R3,R4,R5',
  quantityOnHand: 0,
  cycleCountRequest: {
    id: `req-${id}`,
    inventoryItemsCount: 4,
    verificationCount: { assignee: { id: 'u2', name: 'John Roe' }, deadline: '2026-02-01' },
  },
  testMeta: { isRowDisabled: false },
  ...overrides,
});

describe('useToResolveTab', () => {
  beforeAll(installRandomUUID);

  let history;
  let fetchData;
  let checkboxes;
  let tableData;

  const filterParams = {
    tab: 'TO_RESOLVE',
    searchTerm: '',
    dateLastCount: null,
    categories: [],
    internalLocations: [{ name: 'R1' }, { name: 'R2' }],
    tags: [],
    catalogs: [{ id: 'cat-9' }],
    negativeQuantity: true,
    recountAssignees: [{ id: 'u2' }],
    recountDeadline: '2026-02-01',
  };

  const renderToResolveTab = (overrides = {}) => renderHook(() => useToResolveTab({
    filterParams,
    offset: 20,
    pageSize: 5,
    serializedParams: 'serialized',
    ...overrides,
  }));

  const tableDataArgs = () => useTableDataV2.mock.calls[0][0];

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      locale: 'en',
      location: { id: 'loc-2', name: 'Depot' },
      maxSelected: 5,
      formatLocalizedDate: jest.fn((date, format) => `${date}|${format}`),
    };
    history = { push: jest.fn() };
    useHistory.mockReturnValue(history);
    fetchData = jest.fn();
    tableData = { data: [buildRow(1), buildRow(2)], totalCount: 2 };
    useTableDataV2.mockImplementation(() => ({ tableData, loading: true, fetchData }));
    checkboxes = {
      selectRow: jest.fn((id) => jest.fn(() => id)),
      isChecked: jest.fn(() => false),
      selectHeaderCheckbox: jest.fn(() => jest.fn()),
      selectedCheckboxesAmount: 2,
      headerCheckboxProps: { value: false },
      checkedCheckboxes: ['req-1', 'req-2'],
      resetCheckboxes: jest.fn(),
    };
    useTableCheckboxes.mockReturnValue(checkboxes);
  });

  it('fetches the pending requests with resolve statuses and recount filters', () => {
    const { result } = renderToResolveTab();
    expect(tableDataArgs()).toMatchObject({
      url: '/api/facilities/loc-2/cycle-counts/requests/pending',
      shouldFetch: true,
      pageSize: 5,
    });
    expect(result.current.loading).toBe(true);
    expect(result.current.selectedCheckboxesAmount).toBe(2);

    const params = tableDataArgs().getParams({ sortingParams: { sort: 'quantityOnHand', order: 'desc' } });
    expect(params).toEqual({
      tab: 'TO_RESOLVE',
      statuses: ['COUNTED', 'INVESTIGATING'],
      offset: '20',
      max: '5',
      sort: 'quantityOnHand',
      order: 'desc',
      facility: 'loc-2',
      internalLocations: ['R1', 'R2'],
      catalogs: ['cat-9'],
      recountAssignees: ['u2'],
      recountDeadline: '02/01/2026',
      negativeQuantity: true,
    });
  });

  it('does not fetch when another tab is active', () => {
    renderToResolveTab({ filterParams: { ...filterParams, tab: 'TO_COUNT' } });
    expect(tableDataArgs().shouldFetch).toBe(false);
  });

  it('adds availability meta to rows and passes empty data through', () => {
    const { result } = renderToResolveTab();
    expect(result.current.tableData.data.map((row) => row.meta)).toEqual([
      { isRowDisabled: false }, { isRowDisabled: false },
    ]);

    tableData = { totalCount: 0 };
    const { result: empty } = renderToResolveTab();
    expect(empty.current.tableData).toBe(tableData);
  });

  it('header checkbox selects every request on the page and row checkboxes select one request', () => {
    const { result } = renderToResolveTab();
    const column = findColumn(result.current.columns, cycleCountColumn.SELECTED);
    const header = renderHeader(column);
    expect(checkboxes.selectHeaderCheckbox.mock.calls[0][0]()).toEqual(['req-1', 'req-2']);
    header.unmount();

    renderCell(column, result.current.tableData.data[1]);
    expect(checkboxes.selectRow).toHaveBeenCalledWith('req-2');
    expect(checkboxes.isChecked).toHaveBeenCalledWith('req-2');
    expect(screen.getByTestId('checkbox')).not.toBeChecked();
  });

  it('renders the row cells', () => {
    const { result } = renderToResolveTab();
    const row = result.current.tableData.data[0];
    const { columns } = result.current;

    const expectations = [
      [cycleCountColumn.STATUS, 'To resolve'],
      [cycleCountColumn.PRODUCT, 'P1 Product 1'],
      [cycleCountColumn.CATEGORY_NAME, 'Supplies'],
      [cycleCountColumn.VERIFICATION_COUNT_ASSIGNEE, 'John Roe'],
      [cycleCountColumn.VERIFICATION_COUNT_DEADLINE, '2026-02-01|DD/MMM/yyyy'],
      [cycleCountColumn.INVENTORY_ITEMS, '4'],
      [cycleCountColumn.QUANTITY_ON_HAND, '0'],
    ];
    expectations.forEach(([id, text]) => {
      const { container, unmount } = renderCell(findColumn(columns, id), row);
      expect(container).toHaveTextContent(text);
      unmount();
    });

    const status = renderCell(findColumn(columns, cycleCountColumn.STATUS), row);
    expect(screen.getByTestId('status')).toHaveAttribute('data-variant', 'primary');
    status.unmount();
    const disabledStatus = renderCell(
      findColumn(columns, cycleCountColumn.STATUS),
      { ...row, meta: { isRowDisabled: true } },
    );
    expect(screen.getByTestId('status')).toHaveAttribute('data-variant', 'gray');
    disabledStatus.unmount();

    const bins = renderCell(findColumn(columns, cycleCountColumn.INTERNAL_LOCATIONS), row);
    expect(screen.getAllByText(/^R\d$/)).toHaveLength(4);
    expect(screen.getByText(/more/)).toHaveTextContent('+1 more');
    bins.unmount();

    const noBins = renderCell(findColumn(columns, cycleCountColumn.INTERNAL_LOCATIONS), { ...row, internalLocations: '' });
    expect(noBins.container).toBeEmptyDOMElement();
  });

  it('renders the headers with translated labels', () => {
    const { result } = renderToResolveTab();
    const labels = result.current.columns.slice(1).map((column) => {
      const { container, unmount } = renderHeader(column);
      const text = container.textContent;
      unmount();
      return text;
    });
    expect(labels).toEqual([
      'Status', 'Products', 'Category', 'Bin Location', 'Assignee', 'Deadline', '# Inventory Items', 'Quantity',
    ]);
  });

  it('exports the candidates of the current location', () => {
    const { result } = renderToResolveTab();
    result.current.exportTableData();
    const exportArgs = exportFileFromAPI.mock.calls[0][0];
    expect(exportArgs.url).toBe('/api/facilities/loc-2/cycle-counts/candidates');
    expect(exportArgs.params.statuses).toEqual(['COUNTED', 'INVESTIGATING']);
    expect(exportArgs.filename).toMatch(/^CycleCountReport-Depot-/);
    expect(mockSpinner.show).toHaveBeenCalled();
  });

  it('prints the recount form with count index 1 for each checked request', async () => {
    cycleCountApi.startRecount.mockResolvedValue({ data: 'xls', headers: { 'content-disposition': 'x' } });
    const { result } = renderToResolveTab();
    await result.current.printResolveForm('XLS');

    expect(cycleCountApi.startRecount).toHaveBeenCalledWith({
      payload: {
        requests: [
          { cycleCountRequest: 'req-1', countIndex: 1 },
          { cycleCountRequest: 'req-2', countIndex: 1 },
        ],
      },
      locationId: 'loc-2',
      format: 'XLS',
      config: { responseType: 'blob' },
    });
    expect(fileDownload).toHaveBeenCalledWith('xls', 'resolve-form.xls', 'application/vnd.ms-excel');
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('cancels the checked requests after confirmation', async () => {
    cycleCountApi.deleteRequests.mockResolvedValue({});
    const { result } = renderToResolveTab();
    result.current.openCancelCountsModal();

    const modal = confirmationModal.mock.calls[0][0];
    expect(modal.content.label).toBe('react.cycleCount.modal.cancelRecounts.content.label');
    const onClose = jest.fn();
    const [back, confirm] = modal.buttons(onClose);
    back.onClick();
    expect(cycleCountApi.deleteRequests).not.toHaveBeenCalled();
    await confirm.onClick();

    expect(onClose).toHaveBeenCalledTimes(2);
    expect(cycleCountApi.deleteRequests).toHaveBeenCalledWith('loc-2', ['req-1', 'req-2']);
    expect(fetchData).toHaveBeenCalled();
    expect(checkboxes.resetCheckboxes).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('starts the resolution and navigates to the resolve step', async () => {
    const { result } = renderToResolveTab();
    await result.current.moveToResolving();

    expect(startResolution).toHaveBeenCalledWith(['req-1', 'req-2'], 'loc-2');
    expect(mockDispatch).toHaveBeenCalledWith(expect.objectContaining({ type: 'START_RESOLUTION' }));
    expect(history.push).toHaveBeenCalledWith('/openboxes/inventory/cycleCount/resolve');
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('blocks the resolution when too many products are selected', async () => {
    mockState.maxSelected = 1;
    const { result } = renderToResolveTab();
    await result.current.moveToResolving();

    expect(startResolution).not.toHaveBeenCalled();
    expect(history.push).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith('error-outlined');
  });

  it('maps the verification count of the selected requests into the assign modal', async () => {
    cycleCountApi.getPendingRequests.mockResolvedValue({
      data: {
        data: [{
          cycleCountRequest: {
            id: 'req-1',
            product: { id: 'prod-1' },
            inventoryItemsCount: 4,
            verificationCount: { assignee: { id: 'u2', name: 'John Roe' }, deadline: '2026-02-01' },
          },
        }, {
          cycleCountRequest: {
            id: 'req-2',
            product: { id: 'prod-2' },
            inventoryItemsCount: 1,
            verificationCount: { assignee: null, deadline: null },
          },
        }],
      },
    });
    const { result } = renderToResolveTab();
    result.current.openAssignCountModal();
    await flushPromises();

    expect(cycleCountApi.getPendingRequests).toHaveBeenCalledWith({
      locationId: 'loc-2', requestIds: ['req-1', 'req-2'], max: 2,
    });
    expect(result.current.isAssignCountModalOpen).toBe(true);
    expect(result.current.assignCountModalData.current.map((item) => item.assignee)).toEqual([
      {
        id: 'u2', name: 'John Roe', value: 'u2', label: 'John Roe',
      },
      null,
    ]);
    expect(mockSpinner.hide).toHaveBeenCalled();

    act(() => result.current.closeAssignCountModal());
    expect(result.current.isAssignCountModalOpen).toBe(false);
    expect(result.current.assignCountModalData.current).toEqual([]);
  });
});
