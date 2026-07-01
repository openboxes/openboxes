import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import useTableColumnMeta from 'hooks/useTableColumnMeta';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';

/**
 * Column-aligned footer row, mirroring DataTableHeader but driven by each column's
 * `footer` definition. Reuses the header's width/pinning styles so footer cells
 * line up under their columns. Only rendered when `showFooter` is set on DataTable.
 */
const DataTableFooterRow = ({
  footerGroups,
  tableWithPinnedColumns,
  isScreenWiderThanTable,
}) => (
  <div
    className="rt-tfoot"
    style={{ width: (!isScreenWiderThanTable && tableWithPinnedColumns) ? 'fit-content' : undefined }}
  >
    <div className="rt-tr">
      {footerGroups.map((footerGroup) =>
        footerGroup.headers.map((header) => {
          const { hide, flexWidth, className } = useTableColumnMeta(header.column);
          if (hide) {
            return null;
          }

          return (
            <div
              key={header.id}
              className={`footer-cell ${className}`}
              style={{
                ...getCommonPinningStyles(
                  header.column,
                  flexWidth,
                  isScreenWiderThanTable,
                  true,
                  false,
                ),
              }}
            >
              {flexRender(header.column.columnDef.footer, header.getContext())}
            </div>
          );
        }))}
    </div>
  </div>
);

export default DataTableFooterRow;

DataTableFooterRow.propTypes = {
  footerGroups: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  tableWithPinnedColumns: PropTypes.bool,
  isScreenWiderThanTable: PropTypes.bool.isRequired,
};

DataTableFooterRow.defaultProps = {
  tableWithPinnedColumns: false,
};
