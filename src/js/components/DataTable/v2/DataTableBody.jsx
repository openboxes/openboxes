import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';
import useTableColumnMeta from 'hooks/useTableColumnMeta';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';

const DataTableBody = ({
  emptyTableMessage,
  loadingMessage,
  defaultLoadingTableMessage,
  defaultEmptyTableMessage,
  loading,
  rowModel,
  dataLength,
  tableWithFixedColumns,
  isScreenWider,
}) => (
  <div
    className="rt-tbody-v2"
    style={{ width: (!isScreenWider && tableWithFixedColumns) ? 'fit-content' : undefined }}
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
    {dataLength > 0 &&
        !loading &&
        rowModel.rows.map((row) => (
          <div key={row.id} className="rt-tr-group cell-wrapper" role="rowgroup">
            <TableRow key={row.id} className="rt-tr">
              {row.getVisibleCells().map((cell) => {
                const { hide, flexWidth, className } = useTableColumnMeta(cell.column);
                if (hide) {
                  return null;
                }
                return (
                  <div
                    className={`d-flex ${className}`}
                    style={{
                      background: tableWithFixedColumns && 'white',
                      ...getCommonPinningStyles(cell.column, flexWidth, isScreenWider),
                    }}
                    key={cell.id}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </div>
                );
              })}
            </TableRow>
          </div>
        ))}
  </div>
);

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
  tableWithFixedColumns: PropTypes.bool,
  isScreenWider: PropTypes.bool.isRequired,
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
  tableWithFixedColumns: false,
};
