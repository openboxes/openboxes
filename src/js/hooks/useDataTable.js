import { useState } from 'react';

import { getCoreRowModel, getPaginationRowModel, useReactTable } from '@tanstack/react-table';

// Hook handling logic for DataTable component. It handles pagination, changing pages,
// changing page size and some default table settings
const useDataTable = ({ pageSize, columns, data }) => {
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: pageSize || 5,
  });

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    state: {
      pagination,
    },
    onPaginationChange: setPagination,
  });

  const onPageChange = (page) => {
    setPagination((prev) => ({
      ...prev,
      pageIndex: page,
    }));
  };

  const onPageSizeChange = (selectedPageSize) => {
    setPagination((prev) => ({
      ...prev,
      pageSize: selectedPageSize,
    }));
  };

  const pages = Math.ceil(data.length / pagination.pageSize);
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
