import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import TableRow from 'components/DataTable/TableRow';
import DataTableStatus from 'components/DataTable/v2/DataTableStatus';
import useCycleCountProductAvailability from 'hooks/cycleCount/useCycleCountProductAvailability';
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
  data,
  showDisabledProducts,
}) => {
  const translate = useTranslate();

  return (
    <div
      className="rt-tbody-v2"
      style={{ width: (!isScreenWiderThanTable && tableWithPinnedColumns && dataLength && !loading) ? 'fit-content' : undefined }}
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
        rowModel.rows.map((row, index) => {
          const { isProductDisabled, label, defaultMessage } = showDisabledProducts
            ? useCycleCountProductAvailability(data[index].status)
            : { isProductDisabled: false, label: '', defaultMessage: '' };

          return (
            <CustomTooltip
              content={isProductDisabled && translate(label, defaultMessage)}
              show={isProductDisabled && showDisabledProducts}
            >
              <div key={row.id} className="rt-tr-group cell-wrapper" role="rowgroup">
                <TableRow key={row.id} className={`rt-tr ${isProductDisabled && showDisabledProducts && 'bg-light'}`}>
                  {row.getVisibleCells().map((cell) => {
                    const { hide, flexWidth, className } = useTableColumnMeta(cell.column);
                    if (hide) {
                      return null;
                    }
                    return (
                      <div
                        className={`d-flex ${className} ${isProductDisabled && 'text-muted'}`}
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
  data: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  showDisabledProducts: PropTypes.bool,
};

DataTableBody.defaultProps = {
  emptyTableMessage: null,
  loadingMessage: null,
  loading: false,
  tableWithPinnedColumns: false,
  showDisabledProducts: false,
};
