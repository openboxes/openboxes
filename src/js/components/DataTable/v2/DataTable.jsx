import React from 'react';

import PropTypes from 'prop-types';

import DataTableBody from 'components/DataTable/v2/DataTableBody';
import DataTableFooter from 'components/DataTable/v2/DataTableFooter';
import DataTableHeader from 'components/DataTable/v2/DataTableHeader';
import useDataTable from 'hooks/useDataTable';
import useWindowWidthCheck from 'hooks/useWindowWidthCheck';

import 'react-table/react-table.css';
import 'components/DataTable/DataTable.scss';

const DataTable = ({
  columns,
  data,
  footerComponent,
  loading,
  loadingMessage,
  emptyTableMessage,
  totalCount,
  filterParams,
  paginationProps,
  disablePagination,
  defaultColumn,
  meta,
  tableWithPinnedColumns,
  virtualize,
  overflowVisible,
}) => {
  const {
    defaultEmptyTableMessage,
    defaultLoadingTableMessage,
    table,
  } = useDataTable({
    defaultColumn,
    meta,
    columns,
    data,
    totalCount,
    filterParams,
  });

  const shouldDisplayPagination = Boolean(data?.length && !loading) && !disablePagination;

  const isScreenWiderThanTable = useWindowWidthCheck(table.getTotalSize());

  return (
    <div className="app-react-table-wrapper table-v2">
      <div className="ReactTable app-react-table">
        <div className={`rt-table ${overflowVisible ? 'overflow-visible' : ''}`} role="grid">
          <DataTableHeader
            headerGroups={table.getHeaderGroups()}
            tableWithPinnedColumns={tableWithPinnedColumns}
            isScreenWiderThanTable={isScreenWiderThanTable}
            emptyTableMessage={emptyTableMessage}
          />
          <DataTableBody
            virtualize={virtualize}
            emptyTableMessage={emptyTableMessage}
            loadingMessage={loadingMessage}
            defaultLoadingTableMessage={defaultLoadingTableMessage}
            defaultEmptyTableMessage={defaultEmptyTableMessage}
            loading={loading}
            rowModel={table.getRowModel()}
            dataLength={totalCount}
            tableWithPinnedColumns={tableWithPinnedColumns}
            isScreenWiderThanTable={isScreenWiderThanTable}
            overflowVisible={overflowVisible}
          />
        </div>
        {shouldDisplayPagination && (
          <DataTableFooter
            footerComponent={footerComponent}
            totalData={totalCount}
            {...paginationProps}
          />
        )}
      </div>
    </div>
  );
};

export default DataTable;

DataTable.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  data: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  footerComponent: PropTypes.func,
  loading: PropTypes.bool,
  loadingMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  emptyTableMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  totalCount: PropTypes.number,
  filterParams: PropTypes.shape({}),
  disablePagination: PropTypes.bool,
  paginationProps: PropTypes.shape({}),
  tableWithPinnedColumns: PropTypes.bool,
  virtualize: PropTypes.shape({
    enabled: PropTypes.bool,
    estimateSize: PropTypes.number,
    overscan: PropTypes.number,
  }),
  overflowVisible: PropTypes.bool,
};

DataTable.defaultProps = {
  footerComponent: null,
  loading: false,
  loadingMessage: null,
  emptyTableMessage: null,
  paginationProps: {},
  filterParams: {},
  disablePagination: false,
  totalCount: 0,
  tableWithPinnedColumns: false,
  virtualize: {
    enabled: false,
    estimateSize: 50,
    overscan: 10,
  },
  // it allows tooltips to overflow outside the table
  overflowVisible: false,
};
