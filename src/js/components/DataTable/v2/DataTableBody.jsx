import React, { useRef } from 'react';

import { flexRender } from '@tanstack/react-table';
import { useVirtualizer } from '@tanstack/react-virtual';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';
import useTableColumnMeta from 'hooks/useTableColumnMeta';
import useTranslate from 'hooks/useTranslate';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';
import CustomTooltip from 'wrappers/CustomTooltip';

// To enable row virtualization pass the `virtualize` config object:
// virtualize: {
//    enabled: true/false - ability to dynamically turn on/off virtualization
//    estimateSize: number - this value is required even if the customRowsHeight is
//                   set to true. The value should be set to the average height of the
//                   row to ensure that any issues won't be seen before attaching the
//                   ResizeObserver to the browser.
//    overscan: number - the number of items to render above and below the visible area.
//                       Increasing this number will increase the amount of time it takes
//                       to render the virtualizer, but might decrease the likelihood of seeing
//                       slow-rendering blank items
// }
const DataTableBody = ({
  emptyTableMessage,
  loadingMessage,
  defaultLoadingTableMessage,
  defaultEmptyTableMessage,
  loading,
  rowModel,
  dataLength,
  tableWithPinnedColumns,
  isScreenWiderThanTable,
  virtualize,
}) => {
  const translate = useTranslate();
  const parentRef = useRef(null);
  const { enabled: isVirtualizationEnabled, estimateSize, overscan } = virtualize;

  const rowVirtualizer = useVirtualizer({
    count: rowModel?.rows?.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => estimateSize,
    overscan,
    enabled: isVirtualizationEnabled,
  });

  const dataToMap = isVirtualizationEnabled
    ? rowVirtualizer.getVirtualItems()
    : rowModel.rows;

  return (
    <div
      ref={parentRef}
      className="rt-tbody-v2"
      style={{
        width: (!isScreenWiderThanTable && tableWithPinnedColumns && dataLength && !loading) ? 'fit-content' : undefined,
      }}
    >
      <DataTableStatus
        label={emptyTableMessage?.id || defaultEmptyTableMessage.id}
        defaultMessage={emptyTableMessage?.defaultMessage
          || defaultEmptyTableMessage.defaultMessage}
        shouldDisplay={!dataLength && !loading}
      />
      <DataTableStatus
        label={loadingMessage?.id || defaultLoadingTableMessage.id}
        defaultMessage={loadingMessage?.defaultMessage || defaultLoadingTableMessage.defaultMessage}
        shouldDisplay={loading}
      />
      {dataLength > 0 && !loading && (
        <div
          style={{
            height: isVirtualizationEnabled ? `${rowVirtualizer.getTotalSize()}px` : 'auto',
            width: '100%',
            position: isVirtualizationEnabled ? 'relative' : 'static',
          }}
        >
          {dataToMap.map((row) => {
            const rowProps = isVirtualizationEnabled
              ? {
                'data-index': row.index,
                style: {
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  transform: `translateY(${row.start}px)`,
                  width: '100%',
                  height: `${row.size}px`,
                },
              } : {};
            const rowData = isVirtualizationEnabled
              ? rowModel.rows[row.index]
              : row;
            const { isRowDisabled, label, defaultMessage } = rowData.original?.meta || {
              isRowDisabled: false,
              label: '',
              defaultMessage: '',
            };
            return (
              <CustomTooltip
                content={isRowDisabled && translate(label, defaultMessage)}
                show={isRowDisabled}
              >
                <div
                  key={rowData.id}
                  className="rt-tr-group cell-wrapper"
                  role="rowgroup"
                  {...rowProps}
                >
                  <TableRow key={rowData.id} className={`rt-tr ${isRowDisabled && 'bg-light'}`}>
                    {rowData.getVisibleCells().map((cell) => {
                      const { hide, flexWidth, className } = useTableColumnMeta(cell.column);
                      if (hide) {
                        return null;
                      }
                      return (
                        <div
                          className={`d-flex ${className} ${isRowDisabled && 'text-muted'}`}
                          style={{
                            ...getCommonPinningStyles(
                              cell.column,
                              flexWidth,
                              isScreenWiderThanTable,
                              dataLength,
                              loading,
                            ),
                          }}
                          key={cell.id}
                        >
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </div>
                      );
                    })}
                  </TableRow>
                </div>
              </CustomTooltip>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default DataTableBody;

DataTableBody.propTypes = {
  emptyTableMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  loadingMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  defaultLoadingTableMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
  defaultEmptyTableMessage: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
  loading: PropTypes.bool,
  rowModel: PropTypes.shape({
    rows: PropTypes.arrayOf(
      PropTypes.shape({}),
    ).isRequired,
  }).isRequired,
  dataLength: PropTypes.number.isRequired,
  tableWithPinnedColumns: PropTypes.bool,
  isScreenWiderThanTable: PropTypes.bool.isRequired,
  virtualize: PropTypes.shape({
    enabled: PropTypes.bool,
    estimateSize: PropTypes.number,
    overscan: PropTypes.number,
  }),
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
  tableWithPinnedColumns: false,
  virtualize: {
    enabled: false,
    estimateSize: 50,
    overscan: 10,
  },
};
