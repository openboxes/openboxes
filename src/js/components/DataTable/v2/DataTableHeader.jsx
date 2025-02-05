import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

const DataTableHeader = ({ headerGroups, columns }) => (
  <div className="rt-thead -header">
    <div className="rt-tr">
      {headerGroups
        .map((headerGroup) => (
          headerGroup.headers.map((header, index) => {
            const className = header.column.columnDef.meta?.getCellContext?.().className;
            return (
              <div style={{ flex: columns[index].flexWidth || 1 }} className={`header-cell ${className ?? ''}`} key={header.id}>
                {flexRender(header.column.columnDef.header, header.getContext())}
              </div>
            );
          })
        ))}
    </div>
  </div>
);

export default DataTableHeader;

DataTableHeader.propTypes = {
  headerGroups: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  columns: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
};
