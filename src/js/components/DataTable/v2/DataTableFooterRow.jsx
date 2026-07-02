import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import useTableColumnMeta from 'hooks/useTableColumnMeta';
import getCommonPinningStyles from 'utils/getCommonPinningStyles';

/**
 * Footer row  matching columns' placement.
 * Each column can have description directly under it.
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
    <div className="rt-tr d-flex">
      {footerGroups.map((footerGroup) =>
        footerGroup.headers.map((header) => {
          const { hide, flexWidth, className } = useTableColumnMeta(header.column);
          if (hide) {
            return null;
          }

          return (
            <div
              key={header.id}
              className={`footer-cell d-flex align-items-center font-weight-bold ${className}`}
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
