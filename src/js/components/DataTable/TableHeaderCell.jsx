import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSFill, RiArrowUpSFill } from 'react-icons/ri';
import { Tooltip } from 'react-tippy';

const TableHeaderCell = ({
  children,
  className,
  style,
  toggleSort,
  sortable,
  columnId,
  dynamicClassName,
  required,
  tooltip,
  tooltipLabel,
}) => {
  const sortableProps = {
    tabIndex: '0',
    role: 'button',
    onClick: columnId ? toggleSort(columnId) : toggleSort,
    onKeyPress: () => {},
  };

  if (tooltip) {
    return (
      <Tooltip
        arrow
        delay={150}
        duration={250}
        hideDelay={50}
        position="top"
        className="text-overflow-ellipsis"
        html={tooltipLabel}
      >
        <div
          {...(sortable ? sortableProps : {})}
          style={style}
          className={`rt-th ${className} ${dynamicClassName?.(columnId)}`}
        >
          {children}
          {required && <span className="ml-1 required">&#42;</span>}
          {sortable && (
            <div className="sorting-arrows">
              <RiArrowUpSFill className="arrow-up" />
              <RiArrowDownSFill className="arrow-down" />
            </div>
          )}
        </div>
      </Tooltip>
    );
  }

  return (
    <div
      {...(sortable ? sortableProps : {})}
      style={style}
      className={`rt-th ${className} ${dynamicClassName?.(columnId)}`}
    >
      {children}
      {required && <span className="ml-1 required">&#42;</span>}
      {sortable && (
        <div className="sorting-arrows">
          <RiArrowUpSFill className="arrow-up" />
          <RiArrowDownSFill className="arrow-down" />
        </div>
      )}
    </div>
  );
};

export default TableHeaderCell;

TableHeaderCell.defaultProps = {
  style: undefined,
  className: undefined,
  sortable: false,
  required: false,
  columnId: null,
  dynamicClassName: () => {},
  toggleSort: () => {},
  tooltip: false,
  tooltipLabel: undefined,
};

TableHeaderCell.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
  className: PropTypes.string,
  sortable: PropTypes.bool,
  required: PropTypes.bool,
  style: PropTypes.shape({}),
  toggleSort: PropTypes.func,
  // When passing columnId, it is expected that toggleSort will take this as an argument.
  // It's needed for differentiate columns while sorting.
  columnId: PropTypes.string,
  dynamicClassName: PropTypes.func,
  tooltip: PropTypes.bool,
  tooltipLabel: PropTypes.string,
};
