import { getCoreRowModel, getExpandedRowModel, useReactTable } from '@tanstack/react-table';

const useDataTable = ({
  columns, data, defaultColumn, meta, getSubRows, defaultExpandedSubRows,
}) => {
  const initialColumnPinning = {
    left: columns
      .filter((col) => col.meta?.pinned === 'left')
      .map((col) => (col.accessorKey || col.id).replace(/\./g, '_')),
    right: columns
      .filter((col) => col.meta?.pinned === 'right')
      .map((col) => (col.accessorKey || col.id).replace(/\./g, '_')),
  };

  const table = useReactTable({
    columns,
    data,
    defaultColumn,
    getCoreRowModel: getCoreRowModel(),
    getExpandedRowModel: getExpandedRowModel(),
    getSubRows,
    // `expanded: true` means all expandable rows start expanded (they can still be collapsed)
    initialState: defaultExpandedSubRows ? { expanded: true } : {},
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
