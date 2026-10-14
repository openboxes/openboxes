import { act, renderHook } from '@testing-library/react-hooks';

import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import useCountStep from 'hooks/cycleCount/countStep/useCountStep';
import useCountStepValidation from 'hooks/cycleCount/countStep/useCountStepValidation';
import useCycleCountFetchData from 'hooks/cycleCount/countStep/useCycleCountFetchData';
import useCycleCountModal from 'hooks/cycleCount/countStep/useCycleCountModal';
import useCycleCountPersistence from 'hooks/cycleCount/countStep/useCycleCountPersistence';
import useResolveDiscrepancies from 'hooks/cycleCount/countStep/useResolveDiscrepancies';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  shallowEqual: jest.requireActual('react-redux').shallowEqual,
  useDispatch: () => mockDispatch,
  useSelector: (selector) => selector(mockState),
}));
const mockHistory = { push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
}));
jest.mock('actions', () => ({
  clearCountWorkflow: 'CLEAR_COUNT_WORKFLOW',
  submitForm: 'SUBMIT_FORM',
  fetchLotNumbersByProductIds: jest.fn((ids) => ({ type: 'FETCH_LOT_NUMBERS', ids })),
}));

const mockSave = jest.fn(() => Promise.resolve());
const mockValidateExistence = jest.fn((callback) => callback());
const mockValidateCountStep = jest.fn();
const mockOpenModal = jest.fn();
const mockResolve = jest.fn();
const mockSetIsSaveDisabled = jest.fn();
jest.mock('hooks/cycleCount/countStep/useCycleCountFetchData', () => jest.fn(() => ({ uniqueProductIds: ['p-1'] })));
jest.mock('hooks/cycleCount/countStep/useCycleCountImport', () => jest.fn(() => ({ importErrors: [], importItems: jest.fn() })));
jest.mock('hooks/cycleCount/countStep/useCycleCountPersistence', () => jest.fn(() => ({
  save: mockSave,
  printCountForm: jest.fn(),
  validateExistenceOfCycleCounts: mockValidateExistence,
  isSaveDisabled: false,
  setIsSaveDisabled: mockSetIsSaveDisabled,
})));
jest.mock('hooks/cycleCount/countStep/useCycleCountModal', () => jest.fn(() => ({
  isAssignCountModalOpen: false,
  assignCountModalData: { current: [] },
  openAssignCountModal: mockOpenModal,
  closeAssignCountModal: jest.fn(),
})));
jest.mock('hooks/cycleCount/countStep/useResolveDiscrepancies', () => jest.fn(() => ({ resolveDiscrepancies: mockResolve })));
jest.mock('hooks/cycleCount/countStep/useCountStepValidation', () => jest.fn(() => ({ validateCountStep: mockValidateCountStep })));

const buildState = ({ entities, requests = ['req-a', 'req-b'] } = {}) => ({
  session: {
    currentLocation: { id: 'loc-1' },
    user: { id: 'user-1' },
    activeLanguage: 'es',
  },
  cycleCount: { requests: { 'loc-1': requests } },
  countWorkflow: { entities },
});

describe('useCountStep', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = buildState();
  });

  it('falls back to the request ids before the count workflow is loaded', () => {
    const { result } = renderHook(() => useCountStep());

    expect(result.current.cycleCountIds).toEqual(['req-a', 'req-b']);
    expect(result.current.currentLocationId).toBe('loc-1');
    expect(result.current.isStepEditable).toBe(true);
    expect(result.current.sortByProductName).toBe(false);
    expect(useCycleCountFetchData).toHaveBeenCalledWith('loc-1', ['req-a', 'req-b'], false, true);
    expect(useCycleCountPersistence).toHaveBeenCalledWith('loc-1', ['req-a', 'req-b'], false, 'user-1', 'es');
  });

  it('prefers the count workflow ids, which follow the sorting', () => {
    mockState = buildState({ entities: { 'cc-2': {}, 'cc-1': {} } });
    const { result } = renderHook(() => useCountStep());

    expect(result.current.cycleCountIds).toEqual(['cc-2', 'cc-1']);
  });

  it('shares one discrepancy ref between the modal and the resolve hooks', () => {
    renderHook(() => useCountStep());

    const ref = useCycleCountModal.mock.calls[0][0];
    expect(ref).toEqual({ current: [] });
    expect(useResolveDiscrepancies).toHaveBeenCalledWith({
      currentLocationId: 'loc-1',
      openAssignCountModal: mockOpenModal,
      setIsSaveDisabled: mockSetIsSaveDisabled,
      requestIdsWithDiscrepanciesRef: ref,
    });
    expect(useCountStepValidation).toHaveBeenCalled();
  });

  it('next saves with the default assignee and locks the step when validation passes; back unlocks it', async () => {
    mockValidateCountStep.mockReturnValue(true);
    const { result } = renderHook(() => useCountStep());

    const pending = result.current.next();
    expect(mockDispatch).toHaveBeenCalledWith('SUBMIT_FORM');
    expect(mockSave).toHaveBeenCalledWith(true);
    await pending;
    expect(result.current.isStepEditable).toBe(false);
    expect(useCycleCountFetchData).toHaveBeenLastCalledWith('loc-1', ['req-a', 'req-b'], false, false);

    act(() => result.current.back());
    expect(result.current.isStepEditable).toBe(true);
  });

  it('next only marks the form submitted when validation fails', async () => {
    mockValidateCountStep.mockReturnValue(false);
    const { result } = renderHook(() => useCountStep());

    await result.current.next();

    expect(mockDispatch).toHaveBeenCalledWith('SUBMIT_FORM');
    expect(mockSave).not.toHaveBeenCalled();
    expect(result.current.isStepEditable).toBe(true);
  });

  it('header save checks the counts still exist, saves and refreshes the lot numbers', async () => {
    const { result } = renderHook(() => useCountStep());

    await result.current.handleCountStepHeaderSave();

    expect(mockValidateExistence).toHaveBeenCalledWith(mockSave);
    expect(mockSave).toHaveBeenCalled();
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_LOT_NUMBERS', ids: ['p-1'] });
  });

  it('back to list clears the workflow and opens the count tab', () => {
    const { result } = renderHook(() => useCountStep());

    result.current.handleBackToList();

    expect(mockDispatch).toHaveBeenCalledWith('CLEAR_COUNT_WORKFLOW');
    expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_COUNT_TAB));
  });

  it('toggling the sort passes the new value to the data hooks', () => {
    const { result } = renderHook(() => useCountStep());

    act(() => result.current.setSortByProductName(true));

    expect(result.current.sortByProductName).toBe(true);
    expect(useCycleCountFetchData).toHaveBeenLastCalledWith('loc-1', ['req-a', 'req-b'], true, true);
    expect(result.current.resolveDiscrepancies).toBe(mockResolve);
  });
});
