import { renderHook } from '@testing-library/react-hooks';

import useCycleCountFetchData from 'hooks/cycleCount/countStep/useCycleCountFetchData';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useSelector: (selector) => selector(mockState),
}));
jest.mock('actions', () => ({
  clearErrorsData: 'CLEAR_ERRORS_DATA',
  fetchUsers: jest.fn(() => ({ type: 'FETCH_USERS' })),
  fetchBinLocations: jest.fn((...args) => ({ type: 'FETCH_BIN_LOCATIONS', args })),
  fetchCycleCounts: jest.fn((...args) => ({ type: 'FETCH_CYCLE_COUNTS', args })),
  fetchLotNumbersByProductIds: jest.fn((ids) => ({ type: 'FETCH_LOT_NUMBERS', ids })),
}));

// getCurrentLocation caches locations by id, so every test uses its own location id.
const buildState = (locationId, supportedActivities, entities = {}) => ({
  session: { currentLocation: { id: locationId, supportedActivities } },
  countWorkflow: { entities },
});

const BIN_ACTIVITIES = ['PUTAWAY_STOCK', 'PICK_STOCK'];

describe('useCycleCountFetchData', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('fetches users, bins, cycle counts and lot numbers for a bin-enabled location', () => {
    mockState = buildState('loc-1', BIN_ACTIVITIES, {
      'cc-1': { cycleCountItems: [{ product: { id: 'p-1' } }, { product: { id: 'p-2' } }] },
      'cc-2': { cycleCountItems: [{ product: { id: 'p-1' } }, { product: null }] },
    });

    const { result } = renderHook(() => useCycleCountFetchData('loc-1', ['cc-1', 'cc-2'], true, true));

    expect(result.current.uniqueProductIds).toEqual(['p-1', 'p-2']);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_USERS' });
    expect(mockDispatch).toHaveBeenCalledWith({
      type: 'FETCH_BIN_LOCATIONS', args: ['loc-1', [], 'sortOrder,locationType,name'],
    });
    expect(mockDispatch).toHaveBeenCalledWith({
      type: 'FETCH_CYCLE_COUNTS', args: [['cc-1', 'cc-2'], 'loc-1', true, true],
    });
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_LOT_NUMBERS', ids: ['p-1', 'p-2'] });
  });

  it('skips bins without putaway and picking, cycle counts without ids, and lots when read-only', () => {
    mockState = buildState('loc-2', ['PUTAWAY_STOCK'], {
      'cc-1': { cycleCountItems: [{ product: { id: 'p-1' } }] },
    });

    renderHook(() => useCycleCountFetchData('loc-2', [], false, false));

    const types = mockDispatch.mock.calls.map(([action]) => action.type);
    expect(types).toEqual(['FETCH_USERS']);
  });

  it('passes the bin flag as false to the cycle count fetch when bins are unsupported', () => {
    mockState = buildState('loc-3', []);
    renderHook(() => useCycleCountFetchData('loc-3', ['cc-1'], false, true));

    expect(mockDispatch).toHaveBeenCalledWith({
      type: 'FETCH_CYCLE_COUNTS', args: [['cc-1'], 'loc-3', false, false],
    });
    expect(mockDispatch).not.toHaveBeenCalledWith(expect.objectContaining({ type: 'FETCH_LOT_NUMBERS' }));
  });

  it('clears the error data on unmount', () => {
    mockState = buildState('loc-4', []);
    const { unmount } = renderHook(() => useCycleCountFetchData('loc-4', ['cc-1'], false, true));
    expect(mockDispatch).not.toHaveBeenCalledWith('CLEAR_ERRORS_DATA');

    unmount();

    expect(mockDispatch).toHaveBeenCalledWith('CLEAR_ERRORS_DATA');
  });
});
