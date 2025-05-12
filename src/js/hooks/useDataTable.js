import { getCoreRowModel, useReactTable } from '@tanstack/react-table';

const useDataTable = ({
  columns, data, defaultColumn, meta,
}) => {
  const initialColumnPinning = {
    left: columns
      .filter((col) => col.meta?.pinned === 'left')
      .map((col) => col.accessorKey || col.id),
    right: columns
      .filter((col) => col.meta?.pinned === 'right')
      .map((col) => col.accessorKey || col.id),
  };

  const table = useReactTable({
    columns,
    data,
    defaultColumn,
    getCoreRowModel: getCoreRowModel(),
    manualFiltering: true,
    manualPagination: true,
    manualSorting: true,
    enableColumnPinning: true,
    state: {
      columnPinning: initialColumnPinning,
    },
    meta,
  });

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
    table,
  };
};

export default useDataTable;
