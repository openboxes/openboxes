import { act, renderHook } from '@testing-library/react-hooks';

import useResolveStepImport from 'hooks/cycleCount/useResolveStepImport';
import { importCycleCountsRecount } from 'utils/cycleCountUtils';

const mockShow = jest.fn();
const mockHide = jest.fn();

jest.mock('hooks/useSpinner', () => () => ({ show: mockShow, hide: mockHide }));
jest.mock('utils/cycleCountUtils', () => ({
  importCycleCountsRecount: jest.fn(),
}));

describe('useResolveStepImport', () => {
  const props = {
    currentLocationId: 'loc-1',
    locale: 'en',
    tableData: { current: [] },
    recountedBy: { current: {} },
    defaultRecountedBy: { current: {} },
    dateRecounted: { current: {} },
    reasonCodes: [{ id: 'DAMAGED' }],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('starts with no import errors', () => {
    const { result } = renderHook(() => useResolveStepImport(props));

    expect(result.current.importErrors).toEqual([]);
  });

  it('imports the first file with the hook context and toggles the spinner', async () => {
    importCycleCountsRecount.mockResolvedValue();
    const { result } = renderHook(() => useResolveStepImport(props));
    const file = new File(['x'], 'recount.xls');

    await result.current.importItems([file, new File(['y'], 'other.xls')]);

    expect(importCycleCountsRecount).toHaveBeenCalledWith(expect.objectContaining({
      importFile: file,
      locationId: 'loc-1',
      tableData: props.tableData,
      recountedBy: props.recountedBy,
      dateRecounted: props.dateRecounted,
      defaultRecountedBy: props.defaultRecountedBy,
      locale: 'en',
      reasonCodes: props.reasonCodes,
    }));
    expect(mockShow).toHaveBeenCalledTimes(1);
    expect(mockHide).toHaveBeenCalledTimes(1);
  });

  it('exposes errors reported by the import through setImportErrors', async () => {
    importCycleCountsRecount.mockImplementation(async ({ setImportErrors }) => {
      setImportErrors(['Row 2: invalid quantity']);
    });
    const { result } = renderHook(() => useResolveStepImport(props));

    // The mocked import reports errors synchronously, so a sync act is enough
    // (async act hangs on React 16.8)
    let importPromise;
    act(() => {
      importPromise = result.current.importItems([new File(['x'], 'recount.xls')]);
    });
    await importPromise;

    expect(result.current.importErrors).toEqual(['Row 2: invalid quantity']);
  });

  it('hides the spinner even when the import fails', async () => {
    importCycleCountsRecount.mockRejectedValue(new Error('boom'));
    const { result } = renderHook(() => useResolveStepImport(props));

    await expect(result.current.importItems([new File(['x'], 'recount.xls')])).rejects.toThrow('boom');
    expect(mockHide).toHaveBeenCalledTimes(1);
  });
});
