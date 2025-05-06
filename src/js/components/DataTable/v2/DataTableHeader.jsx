import React from 'react';

import { flexRender } from '@tanstack/react-table';
import PropTypes from 'prop-types';

import useColumnMeta from 'hooks/useColumnMeta';

const DataTableHeader = ({ headerGroups }) => {
  let fixedOffset = 0;

  const totalWidth = headerGroups[0]?.headers.reduce((sum, header) => {
    if (header.column.columnDef?.meta?.hide) {
      return sum;
    }
    const width = header.column.columnDef.meta?.width || 0;
    return sum + width;
  }, 0) || 0;

  return (
    <div
      className="rt-thead -header"
      style={{ width: totalWidth ? 'fit-content' : 'auto' }}
    >
      <div className="rt-tr">
        {headerGroups.map((headerGroup) =>
          headerGroup.headers.map((header) => {
            const {
              hide, width, flexWidth, fixed, className,
            } = useColumnMeta(header.column);
            if (hide) {
              return null;
            }

            const leftPosition = fixed ? fixedOffset : undefined;
            if (fixed) {
              fixedOffset += width || 0;
            }

            return (
              <div
                key={header.id}
                className={`header-cell ${className}`}
                style={{
                  flex: !width && flexWidth,
                  width: width && `${width}px`,
                  flexShrink: width && 0,
                  position: fixed && 'sticky',
                  left: leftPosition && `${leftPosition}px`,
                  zIndex: fixed && 1,
                }}
              >
                {flexRender(header.column.columnDef.header, header.getContext())}
              </div>
            );
          }))}
      </div>
    </div>
  );
};

export default DataTableHeader;

DataTableHeader.propTypes = {
  headerGroups: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
