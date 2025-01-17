import { useEffect, useState } from 'react';

import { getCoreRowModel, useReactTable } from '@tanstack/react-table';

// Hook handling logic for DataTable component. It handles pagination, changing pages,
// changing page size and some default table settings
const useDataTable = ({
  pageSize,
  columns,
  data,
  setOffset,
  setPageSize,
  totalCount,
  filterParams,
}) => {
  // Managing pagination on the client side, no need for server side handling
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: pageSize || 5,
  });

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel(),
    manualFiltering: true,
    manualPagination: true,
    manualSorting: true,
  });

  const onPageChange = (page) => {
    // Function call for managing pagination outside the table (server side)
    setOffset?.(page * pagination.pageSize);
    setPagination((prev) => ({
      ...prev,
      pageIndex: page,
    }));
  };

  const onPageSizeChange = (selectedPageSize) => {
    // Function call for managing pagination outside the table (server side)
    setPageSize?.(selectedPageSize);
    setPagination((prev) => ({
      ...prev,
      pageSize: selectedPageSize,
    }));
  };

  const pages = Math.ceil(totalCount / pagination.pageSize);
  const pageSizeSelectOptions = [5, 10, 20, 25, 50, 100];

  const canNext = () => pagination.pageIndex + 1 < pages;
  const canPrevious = () => pagination.pageIndex > 0;

  const defaultEmptyTableMessage = {
    id: 'react.default.table.emptyTable.label',
    defaultMessage: 'No rows match the given criteria',
  };

  const defaultLoadingTableMessage = {
    id: 'react.default.loading.label',
    defaultMessage: 'Loading...',
  };

  useEffect(() => {
    onPageChange(0);
  }, [filterParams]);


  return {
    defaultEmptyTableMessage,
    defaultLoadingTableMessage,
    canPrevious,
    canNext,
    pageSizeSelectOptions,
    onPageSizeChange,
    onPageChange,
    table,
    pages,
    pagination,
  };
};

export default useDataTable;
