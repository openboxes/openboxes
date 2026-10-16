import { act, renderHook } from '@testing-library/react-hooks';

import useTableSorting from 'hooks/useTableSorting';

// Not covered on OBAF (OBAF lacks the code): 'resets the sorting' (no resetSort).

const toggle = (result, columnId) => act(() => {
  result.current.sortableProps.toggleSort(columnId)();
});

describe('useTableSorting', () => {
  it('starts unsorted, with no column marked', () => {
    const { result } = renderHook(() => useTableSorting());

    expect(result.current.sort).toBeNull();
    expect(result.current.order).toBeNull();
    expect(result.current.sortableProps.dynamicClassName('name')).toBeNull();
  });

  it('sorts a clicked column ascending and marks only that column', () => {
    const { result } = renderHook(() => useTableSorting());

    toggle(result, 'name');

    expect(result.current.sort).toBe('name');
    expect(result.current.order).toBe('asc');
    expect(result.current.sortableProps.dynamicClassName('name')).toBe('-sort-asc');
    expect(result.current.sortableProps.dynamicClassName('code')).toBeNull();
  });

  it('flips the order on every further click of the same column', () => {
    const { result } = renderHook(() => useTableSorting());

    toggle(result, 'name');
    toggle(result, 'name');
    expect(result.current.order).toBe('desc');
    expect(result.current.sortableProps.dynamicClassName('name')).toBe('-sort-desc');

    toggle(result, 'name');
    expect(result.current.order).toBe('asc');
  });

  it('starts ascending again on another column', () => {
    const { result } = renderHook(() => useTableSorting());

    toggle(result, 'name');
    toggle(result, 'name');
    toggle(result, 'code');

    expect(result.current.sort).toBe('code');
    expect(result.current.order).toBe('asc');
  });

  it('lets the caller set the sorting directly', () => {
    const { result } = renderHook(() => useTableSorting());

    act(() => {
      result.current.setSort('date');
      result.current.setOrder('desc');
    });

    expect(result.current.sortableProps.dynamicClassName('date')).toBe('-sort-desc');
  });
});
