import { useEffect, useMemo, useState } from 'react';

const useTablePagination = ({
  defaultPageSize,
  totalCount,
  filterParams,
}) => {
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: defaultPageSize || 5,
  });

  const maxPage = useMemo(
    () => Math.floor(totalCount / pagination.pageSize),
    [totalCount, pagination.pageSize],
  );

  const onPageChange = (page) => {
    if (page > maxPage) {
      setPagination((prev) => ({
        ...prev,
        pageIndex: maxPage,
      }));
      return;
    }
    if (page < 0) {
      setPagination((prev) => ({
        ...prev,
        pageIndex: 0,
      }));
      return;
    }
    setPagination((prev) => ({
      ...prev,
      pageIndex: page,
    }));
  };

  const onPageSizeChange = (selectedPageSize) => {
    const newPageIndex = Math.floor(
      (pagination.pageIndex * pagination.pageSize) / selectedPageSize,
    );
    setPagination({
      pageIndex: newPageIndex,
      pageSize: selectedPageSize,
    });
  };

  const pages = Math.ceil(totalCount / pagination.pageSize);
  const pageSizeSelectOptions = [5, 10, 20, 25, 50, 100];

  const canNext = () => pagination.pageIndex + 1 < pages;
  const canPrevious = () => pagination.pageIndex > 0;

  useEffect(() => {
    onPageChange(0);
  }, [filterParams]);

  return {
    paginationProps: {
      onPageChange,
      onPageSizeChange,
      pages,
      pageSizeSelectOptions,
      canNext,
      canPrevious,
      pagination,
    },
    offset: pagination.pageIndex * pagination.pageSize,
    pageSize: pagination?.pageSize,
  };
};

export default useTablePagination;
