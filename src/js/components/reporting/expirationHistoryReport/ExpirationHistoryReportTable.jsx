import React from 'react';

import PropTypes from 'prop-types';

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

ExpirationHistoryReportTable.propTypes = {
  filterParams: PropTypes.shape({
    startDate: PropTypes.string,
    endDate: PropTypes.string,
    location: PropTypes.shape({
      id: PropTypes.string,
      value: PropTypes.string,
      name: PropTypes.string,
      label: PropTypes.string,
    }),
    search: PropTypes.string,
  }).isRequired,
  tableData: PropTypes.shape({
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    totalCount: PropTypes.number,
  }).isRequired,
  loading: PropTypes.bool.isRequired,
  paginationProps: PropTypes.shape({}).isRequired,
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  emptyTableMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
};
