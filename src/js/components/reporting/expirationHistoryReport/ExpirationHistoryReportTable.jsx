import React from 'react';

import DataTable from 'components/DataTable/v2/DataTable';

const ExpirationHistoryReportTable = ({
  filterParams,
  tableData,
  loading,
  paginationProps,
  columns,
  emptyTableMessage,
}) => (
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

export default ExpirationHistoryReportTable;
