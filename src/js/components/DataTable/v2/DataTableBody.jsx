import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';

const DataTableBody = ({
  emptyTableMessage,
  loadingMessage,
  defaultLoadingTableMessage,
  defaultEmptyTableMessage,
  loading,
  rowModel,
  dataLength,
}) => (
  <div className="rt-tbody">
    <DataTableStatus
      label={emptyTableMessage?.id || defaultEmptyTableMessage.id}
      defaultMessage={
          emptyTableMessage?.defaultMessage || defaultEmptyTableMessage.defaultMessage
        }
      shouldDisplay={!dataLength && !loading}
    />
    <DataTableStatus
      label={loadingMessage?.id || defaultLoadingTableMessage.id}
      defaultMessage={
          loadingMessage?.defaultMessage || defaultLoadingTableMessage.defaultMessage
        }
      shouldDisplay={loading}
    />
    {(dataLength > 0 && !loading) && rowModel
      .rows
      .map((row) => (
        <div key={row.id} className="rt-tr-group cell-wrapper" role="rowgroup">
          <TableRow key={row.id} className="rt-tr">
            {row.getVisibleCells()
              .map((cell) => {
                if (cell.column.columnDef?.meta?.hide) {
                  return null;
                }
                const className = cell.column.columnDef?.meta?.getCellContext?.()?.className;
                const flexWidth = cell.column.columnDef.meta?.flexWidth || 1;
                return (
                  <div className={`d-flex ${className}`} style={{ flex: flexWidth }} key={cell.id}>
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
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
};
