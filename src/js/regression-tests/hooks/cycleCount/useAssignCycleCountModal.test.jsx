import React from 'react';

import { fireEvent, screen } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { findColumn, renderCell, renderHeader } from 'regression-tests/hooks/cycleCount/tabTestUtils';

import { clearCountWorkflow } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import notification from 'components/Layout/notifications/notification';
import cycleCountColumn from 'consts/cycleCountColumn';
import useAssignCycleCountModal from 'hooks/cycleCount/useAssignCycleCountModal';
import { debouncePeopleFetch } from 'utils/option-utils';

import '@testing-library/jest-dom';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
}));
jest.mock('selectors', () => ({
  getCurrentLocation: (state) => state.location,
  getCycleCountIds: (state) => state.cycleCountIds,
  getDebounceTime: () => 300,
  getMinSearchLength: () => 3,
}));
jest.mock('actions', () => ({ clearCountWorkflow: jest.fn() }));
jest.mock('api/services/CycleCountApi', () => ({
  updateCycleCountRequests: jest.fn(),
  getCycleCounts: jest.fn(),
  updateCycleCountItemsBatch: jest.fn(),
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
// Field stand-ins expose the props the hook passes and a button that fires onChange.
jest.mock('components/form-elements/v2/SelectField', () => ({
  placeholder, onChange, defaultValue, loadOptions,
}) => (
  <div data-testid="select-field" data-default={defaultValue?.label} data-async-loader={loadOptions ? 'yes' : 'no'}>
    {placeholder}
    <button type="button" onClick={() => onChange({ id: 'u9', label: 'Picked User' })}>pick</button>
  </div>
));
jest.mock('components/form-elements/v2/DateField', () => ({ placeholder, onChange, value }) => (
  <div data-testid="date-field" data-value={value || undefined}>
    {placeholder}
    <button type="button" onClick={() => onChange('2026-05-01')}>pick</button>
  </div>
));
jest.mock('components/Layout/notifications/notification', () => {
  const mockNotify = jest.fn();
  return jest.fn(() => mockNotify);
});
const mockForceRerender = jest.fn();
jest.mock('hooks/useForceRender', () => jest.fn(() => ({ forceRerender: mockForceRerender })));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
jest.mock('utils/option-utils', () => ({
  debouncePeopleFetch: jest.fn(() => jest.fn()),
}));

const buildSelected = () => ({
  current: [
    {
      cycleCountRequestId: 'req-1',
      product: { id: 'prod-1', productCode: 'P1', name: 'Mask' },
      assignee: { id: 'u1', label: 'Jane' },
      deadline: '2026-04-10',
      inventoryItemsCount: 2,
    },
    {
      cycleCountRequestId: 'req-2',
      product: { id: 'prod-2', productCode: 'P2', name: 'Gown' },
      assignee: null,
      deadline: null,
      inventoryItemsCount: 1,
    },
  ],
});

describe('useAssignCycleCountModal (assignment flows and cells)', () => {
  let selectedCycleCounts;
  let closeModal;
  let refetchData;

  const renderModal = (overrides = {}) => renderHook(() => useAssignCycleCountModal({
    selectedCycleCounts,
    isRecount: false,
    refetchData,
    closeModal,
    assignDataDirectly: false,
    ...overrides,
  }));

  beforeEach(() => {
    jest.clearAllMocks();
    mockState = { location: { id: 'loc-1' }, cycleCountIds: ['cc-1', 'cc-2'] };
    selectedCycleCounts = buildSelected();
    closeModal = jest.fn();
    refetchData = jest.fn();
    cycleCountApi.updateCycleCountRequests.mockResolvedValue({ status: 200 });
  });

  it('creates the people loader from the debounce settings', () => {
    renderModal();
    expect(debouncePeopleFetch).toHaveBeenCalledWith(300, 3);
  });

  it('sends one count command per selected request', async () => {
    const { result } = renderModal();
    await result.current.handleAssign();

    expect(cycleCountApi.updateCycleCountRequests).toHaveBeenCalledWith('loc-1', {
      commands: [
        { cycleCountRequest: 'req-1', assignments: { 0: { assignee: 'u1', deadline: '04/10/2026' } } },
        { cycleCountRequest: 'req-2', assignments: { 0: { assignee: undefined, deadline: null } } },
      ],
    });
    expect(cycleCountApi.getCycleCounts).not.toHaveBeenCalled();
    expect(mockDispatch).toHaveBeenCalledWith(clearCountWorkflow);
    expect(closeModal).toHaveBeenCalled();
    expect(refetchData).toHaveBeenCalled();
  });

  it('assigns the new assignee directly to the items of the latest count', async () => {
    cycleCountApi.getCycleCounts.mockResolvedValue({
      data: {
        data: [
          {
            requestId: 'req-1',
            maxCountIndex: 1,
            cycleCountItems: [
              { id: 'item-a', countIndex: 0 },
              { id: 'item-b', countIndex: 1 },
              { id: 'item-c', countIndex: 1 },
            ],
          },
          {
            requestId: 'req-2',
            maxCountIndex: 1,
            cycleCountItems: [{ id: 'item-d', countIndex: 1 }],
          },
        ],
      },
    });
    const { result } = renderModal({ isRecount: true, assignDataDirectly: true });
    await result.current.handleAssign();

    expect(cycleCountApi.getCycleCounts).toHaveBeenCalledWith('loc-1', ['cc-1', 'cc-2']);
    // req-2 has no assignee, so its items are left alone.
    expect(cycleCountApi.updateCycleCountItemsBatch).toHaveBeenCalledWith(
      {
        itemsToUpdate: [
          { id: 'item-b', recount: true, assignee: 'u1' },
          { id: 'item-c', recount: true, assignee: 'u1' },
        ],
      },
      'loc-1',
    );
  });

  it('skips the batch update when no item gets an assignee', async () => {
    selectedCycleCounts.current = [selectedCycleCounts.current[1]];
    cycleCountApi.getCycleCounts.mockResolvedValue({
      data: { data: [{ requestId: 'req-2', maxCountIndex: 0, cycleCountItems: [{ id: 'item-d', countIndex: 0 }] }] },
    });
    const { result } = renderModal({ assignDataDirectly: true });
    await result.current.handleAssign();

    expect(cycleCountApi.getCycleCounts).toHaveBeenCalled();
    expect(cycleCountApi.updateCycleCountItemsBatch).not.toHaveBeenCalled();
  });

  it('does not notify success for a non-200 response and tolerates a missing refetch', async () => {
    cycleCountApi.updateCycleCountRequests.mockResolvedValue({ status: 207 });
    const { result } = renderModal({ refetchData: undefined });
    await result.current.handleAssign();

    expect(notification).not.toHaveBeenCalled();
    expect(closeModal).toHaveBeenCalled();
  });

  it('still closes the modal and clears the workflow when the update fails', async () => {
    cycleCountApi.updateCycleCountRequests.mockRejectedValue(new Error('fail'));
    const { result } = renderModal();
    await expect(result.current.handleAssign()).rejects.toThrow('fail');

    expect(refetchData).not.toHaveBeenCalled();
    expect(mockDispatch).toHaveBeenCalledWith(clearCountWorkflow);
    expect(closeModal).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('renders the product cell with a stock card link', () => {
    const { result } = renderModal();
    renderCell(
      findColumn(result.current.columns, cycleCountColumn.PRODUCT),
      selectedCycleCounts.current[0],
    );
    expect(screen.getByTestId('table-cell')).toHaveTextContent('P1 Mask');
    expect(screen.getByTestId('table-cell')).toHaveAttribute('data-link', '/openboxes/inventoryItem/showStockCard/prod-1');
  });

  it('assignee header applies the picked person to every row', () => {
    const { result } = renderModal();
    renderHeader(findColumn(result.current.columns, cycleCountColumn.ASSIGNEE));
    expect(screen.getByTestId('select-field')).toHaveTextContent('Assignee');
    fireEvent.click(screen.getByText('pick'));

    expect(selectedCycleCounts.current.map((item) => item.assignee)).toEqual([
      { id: 'u9', label: 'Picked User' }, { id: 'u9', label: 'Picked User' },
    ]);
    expect(mockForceRerender).toHaveBeenCalled();
  });

  it('assignee cell shows the current value and changes only its row', () => {
    const { result } = renderModal();
    renderCell(
      findColumn(result.current.columns, cycleCountColumn.ASSIGNEE),
      selectedCycleCounts.current[1],
    );
    expect(screen.getByTestId('select-field')).toHaveAttribute('data-async-loader', 'yes');
    fireEvent.click(screen.getByText('pick'));

    expect(selectedCycleCounts.current[0].assignee).toEqual({ id: 'u1', label: 'Jane' });
    expect(selectedCycleCounts.current[1].assignee).toEqual({ id: 'u9', label: 'Picked User' });
    expect(mockForceRerender).toHaveBeenCalled();
  });

  it('deadline header and cell update all rows or a single row', () => {
    const { result } = renderModal();
    const column = findColumn(result.current.columns, cycleCountColumn.DEADLINE);

    const cell = renderCell(column, selectedCycleCounts.current[0]);
    expect(screen.getByTestId('date-field')).toHaveAttribute('data-value', '2026-04-10');
    fireEvent.click(screen.getByText('pick'));
    expect(selectedCycleCounts.current.map((item) => item.deadline)).toEqual(['2026-05-01', null]);
    cell.unmount();

    renderHeader(column);
    fireEvent.click(screen.getByText('pick'));
    expect(selectedCycleCounts.current.map((item) => item.deadline)).toEqual(['2026-05-01', '2026-05-01']);
    expect(mockForceRerender).toHaveBeenCalledTimes(2);
  });

  it('renders the inventory items count', () => {
    const { result } = renderModal();
    const column = findColumn(result.current.columns, cycleCountColumn.INVENTORY_ITEMS_COUNT);
    const header = renderHeader(column);
    expect(header.container).toHaveTextContent('# Inventory Items');
    header.unmount();

    renderCell(column, selectedCycleCounts.current[0]);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('2');
  });
});
