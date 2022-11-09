import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSFill, RiArrowUpSFill } from 'react-icons/ri';

const TableHeaderCell = ({
  children, className, style, toggleSort,
}) => (
  <div
    tabIndex="0"
    role="button"
    onClick={toggleSort}
    onKeyPress={() => {}}
    style={style}
    className={`rt-th ${className}`}
  >
    {children}
    <div className="sorting-arrows">
      <RiArrowUpSFill className="arrow-up" />
      <RiArrowDownSFill className="arrow-down" />
    </div>
  </div>);

TableHeaderCell.defaultProps = {
  style: undefined,
  className: undefined,
};

TableHeaderCell.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
  className: PropTypes.string,
  style: PropTypes.shape({}),
  toggleSort: PropTypes.func.isRequired,
};

export default TableHeaderCell;
