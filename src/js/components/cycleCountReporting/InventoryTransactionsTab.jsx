import React, { useEffect } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import useInventoryTransactionsTab from 'hooks/cycleCount/useInventoryTransactionsTab';
import useInventoryTransactionsTabExport from 'hooks/cycleCount/useInventoryTransactionsTabExport';
import DropdownButton from 'utils/DropdownButton';

const InventoryTransactionsTab = ({
  filterParams,
  tablePaginationProps,
  shouldFetch,
  setShouldFetch,
  filtersInitialized,
}) => {
  const {
    paginationProps,
    setTotalCount,
    pageSize,
    offset,
    serializedParams,
  } = tablePaginationProps;
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
  } = useInventoryTransactionsTab({
    paginationProps,
    filterParams,
    pageSize,
    offset,
    shouldFetch,
    setShouldFetch,
    serializedParams,
    filtersInitialized,
  });

  const { actions } = useInventoryTransactionsTabExport(filterParams);

  useEffect(() => {
    setTotalCount(tableData.totalCount);
  }, [tableData.totalCount]);

  return (
    <div>
      <div className="w-100 d-flex justify-content-end pr-3 pb-3">
        <DropdownButton
          disabled={!filterParams.startDate || !filterParams.endDate}
          buttonLabel="react.default.button.export.label"
          buttonDefaultLabel="Export"
          actions={actions}
        />
      </div>
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

export default InventoryTransactionsTab;

InventoryTransactionsTab.propTypes = {
  filterParams: PropTypes.shape({
    startDate: PropTypes.string.isRequired,
    endDate: PropTypes.string.isRequired,
  }).isRequired,
  tablePaginationProps: PropTypes.shape({
    paginationProps: PropTypes.shape({}).isRequired,
    offset: PropTypes.number.isRequired,
    pageSize: PropTypes.number.isRequired,
    setTotalCount: PropTypes.func.isRequired,
    serializedParams: PropTypes.number.isRequired,
  }).isRequired,
  shouldFetch: PropTypes.bool.isRequired,
  setShouldFetch: PropTypes.func.isRequired,
  filtersInitialized: PropTypes.bool.isRequired,
};
