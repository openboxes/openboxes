import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';
import useTableColumnMeta from 'hooks/useTableColumnMeta';
import useTranslate from 'hooks/useTranslate';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';
import CustomTooltip from 'wrappers/CustomTooltip';

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
  isVirtualizationEnabled,
  isCustomRowsHeightEnabled,
  tableVirtualizer,
  overflowVisible,
}) => {
  const translate = useTranslate();

  const dataToMap = isVirtualizationEnabled
    ? tableVirtualizer.getVirtualItems()
    : rowModel.rows;

  return (
    <div
      className={`rt-tbody-v2 ${overflowVisible && 'overflow-visible'}`}
      style={{
        width: (!isScreenWiderThanTable && tableWithPinnedColumns && dataLength && !loading) ? 'fit-content' : undefined,
        height: isVirtualizationEnabled ? `${tableVirtualizer.getTotalSize()}px` : 'auto',
        position: isVirtualizationEnabled ? 'relative' : 'static',
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
      {dataLength > 0
        && !loading
        && dataToMap.map((row) => {
          const rowProps = isVirtualizationEnabled
            ? {
              'data-index': row.index,
              ref: isCustomRowsHeightEnabled
                ? tableVirtualizer.measureElement
                : null,
              style: {
                position: 'absolute',
                top: 0,
                transform: `translateY(${row.start}px)`,
                width: '100%',
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
  isVirtualizationEnabled: PropTypes.bool,
  isCustomRowsHeightEnabled: PropTypes.bool,
  tableVirtualizer: PropTypes.shape({}),
  overflowVisible: PropTypes.bool,
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
  tableWithPinnedColumns: false,
  isVirtualizationEnabled: false,
  isCustomRowsHeightEnabled: false,
  tableVirtualizer: {},
  // it allows tooltips to overflow outside the table
  overflowVisible: false,
};
