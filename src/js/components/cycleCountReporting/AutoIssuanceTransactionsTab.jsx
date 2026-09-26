import React, { useEffect } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import useAutoIssuanceTransactionsTab from 'hooks/cycleCount/useAutoIssuanceTransactionsTab';
import useAutoIssuanceTransactionsTabExport from 'hooks/cycleCount/useAutoIssuanceTransactionsTabExport';
import DropdownButton from 'utils/DropdownButton';

import 'components/cycleCountReporting/cycleCountReporting.scss';

const AutoIssuanceTransactionsTab = ({
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
  } = useAutoIssuanceTransactionsTab({
    paginationProps,
    filterParams,
    pageSize,
    offset,
    shouldFetch,
    setShouldFetch,
    serializedParams,
    filtersInitialized,
  });

  const { actions } = useAutoIssuanceTransactionsTabExport(filterParams);

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

export default AutoIssuanceTransactionsTab;

AutoIssuanceTransactionsTab.propTypes = {
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
