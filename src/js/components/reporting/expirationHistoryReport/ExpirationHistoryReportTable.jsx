import React from 'react';

import DataTable from 'components/DataTable/v2/DataTable';
import useExpirationHistoryReport from 'hooks/reporting/useExpirationHistoryReport';
import useTablePagination from 'hooks/useTablePagination';

const ExpirationHistoryReportTable = ({
  filterParams,
}) => {
  const {
    paginationProps,
  } = useTablePagination({
    totalCount: 0,
    filterParams,
    setShouldFetch: () => {},
    disableAutoUpdateFilterParams: false,
  });

  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
  } = useExpirationHistoryReport();

  return (
    <div>
      <DataTable
        columns={columns}
        data={tableData.data}
        emptyTableMessage={emptyTableMessage}
        loading={loading}
        totalCount={tableData.totalCount}
        filterParams={filterParams}
        paginationProps={paginationProps}
        tableWithPinnedColumns
      />
    </div>
  );
};

export default ExpirationHistoryReportTable;
