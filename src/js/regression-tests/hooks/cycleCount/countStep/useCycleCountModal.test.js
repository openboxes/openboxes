import { act, renderHook } from '@testing-library/react-hooks';

import { CYCLE_COUNT } from 'consts/applicationUrls';
import useCycleCountModal from 'hooks/cycleCount/countStep/useCycleCountModal';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useStore: () => ({ getState: () => mockState }),
}));
const mockHistory = { push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
}));
jest.mock('actions', () => ({
  clearCountWorkflow: 'CLEAR_COUNT_WORKFLOW',
}));

describe('useCycleCountModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      countWorkflow: {
        entities: {
          'cc-1': {
            requestId: 'req-1',
            cycleCountItems: [{ product: { id: 'prod-1' } }, { product: { id: 'prod-1' } }],
            verificationCount: { assignee: { id: 'user-1' }, deadline: '2026-10-10' },
          },
          'cc-2': {
            requestId: 'req-2',
            cycleCountItems: [{ product: { id: 'prod-2' } }],
          },
          'cc-3': { requestId: 'req-3' },
        },
      },
    };
  });

  it('starts closed with no modal data', () => {
    const { result } = renderHook(() => useCycleCountModal({ current: [] }));

    expect(result.current.isAssignCountModalOpen).toBe(false);
    expect(result.current.assignCountModalData.current).toEqual([]);
  });

  it('opens with modal rows for the requests that have discrepancies only', () => {
    const ref = { current: ['req-1', 'req-3'] };
    const { result } = renderHook(() => useCycleCountModal(ref));

    act(() => result.current.openAssignCountModal());

    expect(result.current.isAssignCountModalOpen).toBe(true);
    expect(result.current.assignCountModalData.current).toEqual([
      {
        product: { id: 'prod-1' },
        cycleCountRequestId: 'req-1',
        inventoryItemsCount: 2,
        assignee: { id: 'user-1' },
        deadline: '2026-10-10',
      },
      {
        product: undefined,
        cycleCountRequestId: 'req-3',
        inventoryItemsCount: 0,
        assignee: undefined,
        deadline: undefined,
      },
    ]);
  });

  it('closing clears the data and the workflow and goes to the resolve step', () => {
    const { result } = renderHook(() => useCycleCountModal({ current: ['req-2'] }));
    act(() => result.current.openAssignCountModal());
    expect(result.current.assignCountModalData.current).toHaveLength(1);

    act(() => result.current.closeAssignCountModal());

    expect(result.current.isAssignCountModalOpen).toBe(false);
    expect(result.current.assignCountModalData.current).toEqual([]);
    expect(mockDispatch).toHaveBeenCalledWith('CLEAR_COUNT_WORKFLOW');
    expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.resolveStep());
  });
});
