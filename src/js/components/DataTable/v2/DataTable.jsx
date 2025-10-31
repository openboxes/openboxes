import React from 'react';

import { useWindowVirtualizer } from '@tanstack/react-virtual';
import PropTypes from 'prop-types';

import DataTableBody from 'components/DataTable/v2/DataTableBody';
import DataTableFooter from 'components/DataTable/v2/DataTableFooter';
import DataTableHeader from 'components/DataTable/v2/DataTableHeader';
import useDataTable from 'hooks/useDataTable';
import useWindowWidthCheck from 'hooks/useWindowWidthCheck';

import 'components/DataTable/DataTable.scss';

// To enable virtualization of the table the "virtualize" object should be passed.
// virtualize: {
//    enabled: true/false - ability to dynamically turn on/off virtualization
//    customRowsHeight: true/false - if true, the height of rows will be recalculated
//                      while scrolling, it has worse performance than hardcoded
//                      row height
//    estimatedSize: number - this value is required even if the customRowsHeight is
//                   set to true. The value should be set to the average height of the
//                   row to ensure that any issues won't be seen before attaching the
//                   ResizeObserver to the browser.
//    overscan: number - the number of items to render above and below the visible area.
//                       Increasing this number will increase the amount of time it takes
//                       to render the virtualizer, but might decrease the likelihood of seeing
//                       slow-rendering blank items
// }
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

  const tableVirtualizer = virtualize.enabled
    ? useWindowVirtualizer({
      count: data?.length || 0,
      estimateSize: () => virtualize.estimatedSize,
      overscan: virtualize.overscan,
    })
    : {};

  const shouldDisplayPagination = Boolean(data?.length && !loading) && !disablePagination;

  const isScreenWiderThanTable = useWindowWidthCheck(table.getTotalSize());

  return (
    <div className="app-react-table-wrapper table-v2">
      <div className="ReactTable app-react-table">
        <div className="rt-table" role="grid">
          <DataTableHeader
            headerGroups={table.getHeaderGroups()}
            tableWithPinnedColumns={tableWithPinnedColumns}
            isScreenWiderThanTable={isScreenWiderThanTable}
            emptyTableMessage={emptyTableMessage}
          />
          <DataTableBody
            tableVirtualizer={tableVirtualizer}
            isVirtualizationEnabled={virtualize.enabled}
            isCustomRowsHeightEnabled={virtualize.customRowsHeight}
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
  filterParams: PropTypes.shape({}),
  disablePagination: PropTypes.bool,
  paginationProps: PropTypes.shape({}),
  tableWithPinnedColumns: PropTypes.bool,
  virtualize: PropTypes.shape({
    enabled: PropTypes.bool,
    customRowsHeight: PropTypes.bool,
    estimatedSize: PropTypes.number,
    overscan: PropTypes.number,
  }),
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
    customRowsHeight: false,
    estimatedSize: 50,
    overscan: 10,
  },
};
