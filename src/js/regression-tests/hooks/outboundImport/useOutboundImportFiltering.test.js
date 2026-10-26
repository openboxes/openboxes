import { act, renderHook } from '@testing-library/react-hooks';

import useOutboundImportFiltering from 'hooks/outboundImport/useOutboundImportFiltering';

describe('useOutboundImportFiltering', () => {
  const withErrors = [{ rowId: 'b' }];
  const inOrder = [{ rowId: 'a' }, { rowId: 'b' }, { rowId: 'c' }];

  it('shows all rows until filtering is toggled on', () => {
    const { result } = renderHook(() => useOutboundImportFiltering());

    expect(result.current.isFiltered).toBe(false);
    expect(result.current.getFilteredTableData(withErrors, inOrder)).toBe(inOrder);
    expect(result.current.getTablePageSize(withErrors, inOrder)).toBe(3);
  });

  it('shows only rows with errors when filtered and toggles back', () => {
    const { result } = renderHook(() => useOutboundImportFiltering());

    act(() => result.current.toggleFiltering());

    expect(result.current.isFiltered).toBe(true);
    expect(result.current.getFilteredTableData(withErrors, inOrder)).toBe(withErrors);
    expect(result.current.getTablePageSize(withErrors, inOrder)).toBe(1);

    act(() => result.current.toggleFiltering());

    expect(result.current.isFiltered).toBe(false);
  });

  it('falls back to empty lists for missing data', () => {
    const { result } = renderHook(() => useOutboundImportFiltering());

    expect(result.current.getFilteredTableData(undefined, undefined)).toEqual([]);
    act(() => result.current.setIsFiltered(true));
    expect(result.current.getFilteredTableData(undefined, inOrder)).toEqual([]);
    expect(result.current.getTablePageSize(undefined, inOrder)).toBeUndefined();
  });
});
