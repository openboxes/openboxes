import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import useTableColumnMeta from 'hooks/useTableColumnMeta';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';

const DataTableHeader = ({
  headerGroups,
  tableWithPinnedColumns,
  isScreenWiderThanTable,
  isEmptyTableMessageOrLoading,
}) => (
  <div
    className="rt-thead -header"
    style={{ width: (!isScreenWiderThanTable && tableWithPinnedColumns && !isEmptyTableMessageOrLoading) ? 'fit-content' : undefined }}
  >
    <div className="rt-tr">
      {headerGroups.map((headerGroup) =>
        headerGroup.headers.map((header) => {
          const { hide, flexWidth, className } = useTableColumnMeta(header.column);
          if (hide) {
            return null;
          }

          return (
            <div
              key={header.id}
              className={`header-cell ${className}`}
              style={{
                ...getCommonPinningStyles(
                  header.column,
                  flexWidth,
                  isScreenWiderThanTable,
                  isEmptyTableMessageOrLoading,
                ),
              }}
            >
              {flexRender(header.column.columnDef.header, header.getContext())}
            </div>
          );
        }))}
    </div>
  </div>
);

export default DataTableHeader;

DataTableHeader.propTypes = {
  headerGroups: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  tableWithPinnedColumns: PropTypes.bool,
  isScreenWiderThanTable: PropTypes.bool.isRequired,
  isEmptyTableMessageOrLoading: PropTypes.bool.isRequired,
};

DataTableHeader.defaultProps = {
  tableWithPinnedColumns: false,
};
