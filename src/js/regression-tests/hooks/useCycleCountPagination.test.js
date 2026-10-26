import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useCycleCountPagination from 'hooks/useCycleCountPagination';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useCycleCountPagination', () => {
  const filterParams = { tab: 'TO_COUNT' };

  beforeEach(() => {
    useSelector.mockImplementation((selector) => selector({ session: { currentLocation: { id: 'loc-1' } } }));
  });

  it('paginates by 5 rows until the total count is known', () => {
    const setShouldFetch = jest.fn();
    const { result } = renderHook(() => useCycleCountPagination({ filterParams, setShouldFetch }));

    expect(result.current.pageSize).toBe(5);
    expect(result.current.offset).toBe(0);
    expect(result.current.paginationProps.pages).toBe(0);
    expect(result.current.serializedParams)
      .toBe(JSON.stringify({ tab: 'TO_COUNT', facilityId: 'loc-1', pageIndex: 0 }));
    expect(setShouldFetch).toHaveBeenCalledWith(true);
  });

  it('counts the pages from the total count set after a fetch', () => {
    const { result } = renderHook(() => useCycleCountPagination({
      filterParams, setShouldFetch: jest.fn(), defaultPageSize: 10,
    }));

    act(() => result.current.setTotalCount(42));
    act(() => result.current.paginationProps.onPageChange(4));

    expect(result.current.paginationProps.pages).toBe(5);
    expect(result.current.offset).toBe(40);
    expect(result.current.paginationProps.canNext()).toBe(false);
  });

  it('lets the caller set the serialized parameters', () => {
    const { result } = renderHook(() => useCycleCountPagination({ filterParams }));

    act(() => result.current.setSerializedParams('{"refetch":true}'));

    expect(result.current.serializedParams).toBe('{"refetch":true}');
  });
});
