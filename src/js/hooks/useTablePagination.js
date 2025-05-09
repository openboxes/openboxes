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
  // serializedParams triggers data fetching in useTableDataV2, preventing duplicate
  // API calls. Previously, useEffect dependencies [filterParams, offset] caused two requests,
  // as useTablePagination useEffect listened to filterParams and later updated offset
  const [serializedParams, setSerializedParams] = useState('');

  const maxPage = useMemo(
    () => Math.floor(totalCount / pagination.pageSize),
    [totalCount, pagination.pageSize],
  );

  const generateSerializedParams = (newPageIndex) => JSON.stringify({
    ...filterParams,
    pageIndex: newPageIndex,
  });

  const onPageChange = (page) => {
    if (page > maxPage) {
      setPagination((prev) => ({
        ...prev,
        pageIndex: maxPage,
      }));
      setSerializedParams(generateSerializedParams(maxPage));
      return;
    }
    if (page < 0) {
      setPagination((prev) => ({
        ...prev,
        pageIndex: 0,
      }));
      setSerializedParams(generateSerializedParams(0));
      return;
    }
    setPagination((prev) => ({
      ...prev,
      pageIndex: page,
    }));
    setSerializedParams(generateSerializedParams(page));
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
    serializedParams,
  };
};

export default useTablePagination;
