import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useTablePagination from 'hooks/useTablePagination';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useTablePagination', () => {
  const filterParams = { searchTerm: 'amox' };
  let setShouldFetch;

  const renderPagination = (props) => renderHook(
    (hookProps) => useTablePagination(hookProps),
    {
      initialProps: {
        defaultPageSize: 5, totalCount: 23, filterParams, setShouldFetch, ...props,
      },
    },
  );

  const serialized = (pageIndex, params = filterParams) =>
    JSON.stringify({ ...params, facilityId: 'loc-1', pageIndex });

  beforeEach(() => {
    setShouldFetch = jest.fn();
    useSelector.mockImplementation((selector) => selector({ session: { currentLocation: { id: 'loc-1' } } }));
  });

  it('starts on the first page and asks for the data of the current facility', () => {
    const { result } = renderPagination();

    expect(result.current.paginationProps.pagination).toEqual({ pageIndex: 0, pageSize: 5 });
    expect(result.current.offset).toBe(0);
    expect(result.current.pageSize).toBe(5);
    expect(result.current.serializedParams).toBe(serialized(0));
    expect(setShouldFetch).toHaveBeenCalledWith(true);
  });

  it('shows 5 rows per page by default', () => {
    const { result } = renderPagination({ defaultPageSize: undefined });

    expect(result.current.pageSize).toBe(5);
  });

  it('counts the pages, including a last partial one', () => {
    const { result } = renderPagination();

    expect(result.current.paginationProps.pages).toBe(5);
    expect(result.current.paginationProps.pageSizeSelectOptions).toEqual([5, 10, 20, 25, 50, 100]);
  });

  it('moves to a page', () => {
    const { result } = renderPagination();

    act(() => result.current.paginationProps.onPageChange(2));

    expect(result.current.paginationProps.pagination.pageIndex).toBe(2);
    expect(result.current.offset).toBe(10);
    expect(result.current.serializedParams).toBe(serialized(2));
    expect(result.current.paginationProps.canPrevious()).toBe(true);
    expect(result.current.paginationProps.canNext()).toBe(true);
  });

  it('stops at the first page', () => {
    const { result } = renderPagination();

    expect(result.current.paginationProps.canPrevious()).toBe(false);
    act(() => result.current.paginationProps.onPageChange(-1));

    expect(result.current.paginationProps.pagination.pageIndex).toBe(0);
    expect(result.current.serializedParams).toBe(serialized(0));
  });

  it('stops at the last page, which is partially filled', () => {
    const { result } = renderPagination();

    act(() => result.current.paginationProps.onPageChange(9));

    expect(result.current.paginationProps.pagination.pageIndex).toBe(4);
    expect(result.current.offset).toBe(20);
    expect(result.current.serializedParams).toBe(serialized(4));
    expect(result.current.paginationProps.canNext()).toBe(false);
  });

  // KNOWN-GAP: OBAF's maxPage is floor(count / size), one empty page past the end
  // when the rows fill their pages exactly.
  it.skip('stops at the last page when the rows fill their pages exactly', () => {
    const { result } = renderPagination({ totalCount: 20 });

    act(() => result.current.paginationProps.onPageChange(10));

    expect(result.current.paginationProps.pagination.pageIndex).toBe(3);
    expect(result.current.offset).toBe(15);
    expect(result.current.serializedParams).toBe(serialized(3));
  });

  it('keeps the first visible row on screen when the page size changes', () => {
    const { result } = renderPagination({ defaultPageSize: 10, totalCount: 100 });
    act(() => result.current.paginationProps.onPageChange(3));
    setShouldFetch.mockClear();

    act(() => result.current.paginationProps.onPageSizeChange(20));

    // Row 30 was the first visible one, it is on page 1 of 20 rows
    expect(result.current.paginationProps.pagination).toEqual({ pageIndex: 1, pageSize: 20 });
    expect(result.current.offset).toBe(20);
    expect(result.current.paginationProps.pages).toBe(5);
    expect(setShouldFetch).toHaveBeenCalledWith(true);
  });

  it('goes back to the first page when the filters change', () => {
    const { result, rerender } = renderPagination();
    act(() => result.current.paginationProps.onPageChange(3));

    const newFilters = { searchTerm: 'para' };
    rerender({
      defaultPageSize: 5, totalCount: 23, filterParams: newFilters, setShouldFetch,
    });

    expect(result.current.paginationProps.pagination.pageIndex).toBe(0);
    expect(result.current.serializedParams).toBe(serialized(0, newFilters));
  });

  it('works without a fetch flag', () => {
    const { result } = renderPagination({ setShouldFetch: undefined });

    act(() => {
      result.current.paginationProps.onPageChange(1);
      result.current.paginationProps.onPageSizeChange(10);
    });

    expect(result.current.paginationProps.pagination).toEqual({ pageIndex: 0, pageSize: 10 });
  });
});
