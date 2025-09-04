import { act, renderHook } from '@testing-library/react-hooks';
import moment from 'moment/moment';

import cycleCountApi from 'api/services/CycleCountApi';
import notification from 'components/Layout/notifications/notification';
import countIndex from 'consts/countIndex';
import cycleCountColumn from 'consts/cycleCountColumn';
import NotificationType from 'consts/notificationTypes';
import useAssignCycleCountModal from 'hooks/cycleCount/useAssignCycleCountModal';

import { baseUser } from '../__mocks__/users';

jest.mock('react-redux', () => ({
  useSelector: jest.fn().mockImplementation(() => ({
    currentLocation: { id: 'loc-123' },
    cycleCountIds: ['req-1'],
  })),
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (key) => key));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('utils/dateUtils', () => jest.fn());
jest.mock('api/services/CycleCountApi', () => ({
  updateCycleCountRequests: jest.fn().mockResolvedValue({ status: 200 }),
}));
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => jest.fn()));

describe('useAssignCycleCountModal', () => {
  const selectedCycleCounts = {
    current: [
      {
        cycleCountRequestId: 'req-1',
        product: { id: 'prod-1', productCode: '1234', name: 'Product 1' },
        assignee: null,
        deadline: null,
        inventoryItemsCount: 5,
      },
    ],
  };
  const defaultProps = {
    selectedCycleCounts,
    isRecount: false,
    refetchData: jest.fn(),
    closeModal: jest.fn(),
    assignDataDirectly: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should initialize with correct columns', () => {
    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));
    const { columns } = result.current;

    expect(columns).toHaveLength(4);
    expect(columns[0].id).toBe(cycleCountColumn.PRODUCT);
    expect(columns[1].id).toBe(cycleCountColumn.ASSIGNEE);
    expect(columns[2].id).toBe(cycleCountColumn.DEADLINE);
    expect(columns[3].id).toBe(cycleCountColumn.INVENTORY_ITEMS_COUNT);
  });

  it('should set document.body.style.overflowY to hidden on mount and reset on unmount', () => {
    const { unmount } = renderHook(() => useAssignCycleCountModal(defaultProps));

    expect(document.body.style.overflowY).toBe('hidden');

    unmount();

    expect(document.body.style.overflowY).toBe('auto');
  });

  it('should update assignee for a specific cycle count', () => {
    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));

    act(() => {
      result.current.handleUpdateAssignees(
        ['req-1'],
        cycleCountColumn.ASSIGNEE,
        baseUser,
      );
    });

    expect(selectedCycleCounts.current[0].assignee).toEqual(baseUser);
  });

  it('should update deadline for a specific cycle count', () => {
    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));
    const newDate = moment('2025-08-26T10:00:00Z');

    act(() => {
      result.current.handleUpdateAssignees(
        ['req-1'],
        cycleCountColumn.DEADLINE,
        newDate,
      );
    });

    expect(selectedCycleCounts.current[0].deadline).toEqual(newDate);
  });

  it('should update multiple cycle counts at once', () => {
    selectedCycleCounts.current.push({
      cycleCountRequestId: 'req-2',
      product: { id: 'prod-2', productCode: '342', name: 'Product 2' },
      assignee: null,
      deadline: null,
      inventoryItemsCount: 2,
    });

    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));

    act(() => {
      result.current.handleUpdateAssignees(
        ['req-1', 'req-2'],
        cycleCountColumn.ASSIGNEE,
        baseUser,
      );
    });

    expect(selectedCycleCounts.current[0].assignee).toEqual(baseUser);
    expect(selectedCycleCounts.current[1].assignee).toEqual(baseUser);
  });

  it('should execute handleAssign and call closeModal & refetchData with notification', async () => {
    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));
    await result.current.handleAssign();

    expect(defaultProps.closeModal).toHaveBeenCalled();
    expect(defaultProps.refetchData).toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
  });

  it('should use RECOUNT_INDEX when isRecount = true', async () => {
    const { result } = renderHook(() => useAssignCycleCountModal({
      ...defaultProps,
      isRecount: true,
    }));
    await result.current.handleAssign();

    const [, { commands }] = cycleCountApi.updateCycleCountRequests.mock.calls[0];
    expect(commands[0].assignments).toHaveProperty(countIndex.RECOUNT_INDEX);
  });

  it('should show and hide spinner when assigning successfully', async () => {
    const { result } = renderHook(() => useAssignCycleCountModal(defaultProps));
    await result.current.handleAssign();

    expect(mockSpinner.show).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });
});
