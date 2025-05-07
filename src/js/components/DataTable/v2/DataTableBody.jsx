import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';
import useTableColumnMeta from 'hooks/useTableColumnMeta';
import useTableTotalWidth from 'hooks/useTableTotalWidth';

const DataTableBody = ({
  emptyTableMessage,
  loadingMessage,
  defaultLoadingTableMessage,
  defaultEmptyTableMessage,
  loading,
  rowModel,
  dataLength,
}) => {
  const totalWidth = useTableTotalWidth(
    dataLength > 0 && rowModel.rows.length > 0 ? rowModel.rows[0].getVisibleCells() : [],
  );
  return (
    <div
      className="rt-tbody-v2"
      style={{ width: totalWidth ? 'fit-content' : 'auto' }}
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
        rowModel.rows.map((row) => {
          let fixedOffset = 0;
          return (
            <div key={row.id} className="rt-tr-group cell-wrapper" role="rowgroup">
              <TableRow key={row.id} className="rt-tr">
                {row.getVisibleCells().map((cell) => {
                  const {
                    hide, width, flexWidth, fixed, className,
                  } = useTableColumnMeta(cell.column);
                  if (hide) {
                    return null;
                  }

                  const leftPosition = fixed ? fixedOffset : undefined;
                  if (fixed) {
                    fixedOffset += width || 0;
                  }
                  return (
                    <div
                      className={`d-flex ${className}`}
                      style={{
                        flex: !width && flexWidth,
                        width: width && `${width}px`,
                        flexShrink: width && 0,
                        position: fixed && 'sticky',
                        left: leftPosition !== undefined && `${leftPosition}px`,
                        zIndex: fixed && 1,
                        background: width && 'white',
                      }}
                      key={cell.id}
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </div>
                  );
                })}
              </TableRow>
            </div>
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
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
};
