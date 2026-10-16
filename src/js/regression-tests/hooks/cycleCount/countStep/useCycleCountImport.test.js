import { act, renderHook } from '@testing-library/react-hooks';

import useCycleCountImport from 'hooks/cycleCount/countStep/useCycleCountImport';
import { importCycleCounts } from 'utils/cycleCountUtils';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useStore: () => ({ getState: () => mockState }),
}));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('utils/cycleCountUtils', () => ({
  importCycleCounts: jest.fn(),
}));

describe('useCycleCountImport', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      countWorkflow: {
        entities: { 'cc-1': { id: 'cc-1' }, 'cc-2': { id: 'cc-2' } },
      },
    };
  });

  it('imports the first file against the current entities and dispatches the result', async () => {
    const importAction = { type: 'IMPORT' };
    importCycleCounts.mockResolvedValue(importAction);
    const file = { name: 'counts.xlsx' };

    const { result } = renderHook(() => useCycleCountImport('loc-1', 'fr'));
    await result.current.importItems([file, { name: 'ignored.xlsx' }]);

    expect(importCycleCounts).toHaveBeenCalledWith({
      importFile: file,
      locationId: 'loc-1',
      currentCycleCountEntities: [{ id: 'cc-1' }, { id: 'cc-2' }],
      setImportErrors: result.current.setImportErrors,
      locale: 'fr',
    });
    expect(mockDispatch).toHaveBeenCalledWith(importAction);
    expect(mockSpinner.show).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('exposes the errors the import reports', async () => {
    importCycleCounts.mockImplementation(async ({ setImportErrors }) => {
      act(() => setImportErrors(['Row 2: bad quantity']));
      return { type: 'IMPORT' };
    });
    const { result } = renderHook(() => useCycleCountImport('loc-1', 'en'));
    expect(result.current.importErrors).toEqual([]);

    await result.current.importItems([{}]);

    expect(result.current.importErrors).toEqual(['Row 2: bad quantity']);
  });

  it('hides the spinner and dispatches nothing when the import fails', async () => {
    importCycleCounts.mockRejectedValue(new Error('invalid file'));
    const { result } = renderHook(() => useCycleCountImport('loc-1', 'en'));

    await expect(result.current.importItems([{}])).rejects.toThrow('invalid file');
    expect(mockDispatch).not.toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });
});
