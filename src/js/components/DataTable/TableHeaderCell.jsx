import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSFill, RiArrowUpSFill } from 'react-icons/ri';

const TableHeaderCell = ({
  children, className, style, toggleSort, sortable,
}) => {
  const sortableProps = {
    tabIndex: '0',
    role: 'button',
    onClick: toggleSort,
    onKeyPress: () => {},
  };

  return (
    <div
      {...(sortable ? sortableProps : {})}
      style={style}
      className={`rt-th ${className}`}
    >
      {children}
      <div className="sorting-arrows">
        <RiArrowUpSFill className="arrow-up" />
        <RiArrowDownSFill className="arrow-down" />
      </div>
    </div>
  );
};

TableHeaderCell.defaultProps = {
  style: undefined,
  className: undefined,
  sortable: false,
};

TableHeaderCell.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
  className: PropTypes.string,
  sortable: PropTypes.bool,
  style: PropTypes.shape({}),
  toggleSort: PropTypes.func.isRequired,
};

export default TableHeaderCell;
