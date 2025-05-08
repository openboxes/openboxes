import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';

import DataTableBody from 'components/DataTable/v2/DataTableBody';
import DataTableFooter from 'components/DataTable/v2/DataTableFooter';
import DataTableHeader from 'components/DataTable/v2/DataTableHeader';
import useDataTable from 'hooks/useDataTable';

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

  const [isScreenWiderThanTable, setIsScreenWiderThanTable] = useState(false);
  const totalSize = table.getTotalSize();
  useEffect(() => {
    const handleResize = () => {
      setIsScreenWiderThanTable(window.innerWidth > totalSize);
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [totalSize]);

  return (
    <div className="app-react-table-wrapper table-v2">
      <div className="ReactTable app-react-table">
        <div className="rt-table" role="grid">
          <DataTableHeader
            headerGroups={table.getHeaderGroups()}
            tableWithPinnedColumns={tableWithPinnedColumns}
            isScreenWiderThanTable={isScreenWiderThanTable}
          />
          <DataTableBody
            emptyTableMessage={emptyTableMessage}
            loadingMessage={loadingMessage}
            defaultLoadingTableMessage={defaultLoadingTableMessage}
            defaultEmptyTableMessage={defaultEmptyTableMessage}
            loading={loading}
            rowModel={table.getRowModel()}
            dataLength={data?.length}
            tableWithPinnedColumns={tableWithPinnedColumns}
            isScreenWiderThanTable={isScreenWiderThanTable}
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
  filterParams: PropTypes.shape({}).isRequired,
  disablePagination: PropTypes.bool,
  paginationProps: PropTypes.shape({}),
  tableWithPinnedColumns: PropTypes.bool,
};

DataTable.defaultProps = {
  footerComponent: null,
  loading: false,
  loadingMessage: null,
  emptyTableMessage: null,
  paginationProps: {},
  disablePagination: false,
  totalCount: 0,
  tableWithPinnedColumns: false,
};
