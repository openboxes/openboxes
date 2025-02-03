import { getCoreRowModel, useReactTable } from '@tanstack/react-table';

// Hook handling logic for DataTable component.
const useDataTable = ({
  columns,
  data,
  defaultColumn,
  meta,
}) => {
  const table = useReactTable({
    columns,
    data,
    defaultColumn,
    getCoreRowModel: getCoreRowModel(),
    manualFiltering: true,
    manualPagination: true,
    manualSorting: true,
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
