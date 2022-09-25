import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const TableCell = ({
  column, value, original, children, tooltip, tooltipLabel,
}) => {
  let cellElement = <div>{ children || value}</div>;

  // interpolate string by inserting values with ":" at the beginning with values from the cell
  if (column.link && typeof column.link === 'string') {
    const href = column.link
      .split('/')
      .map(it => (it[0] === ':' ? original[`${it.substring(1)}`] : it))
      .join('/');

    cellElement = <a href={href}>{ children || value}</a>;
  }

  if (column.tooltip || tooltip) {
    return (
      <Tooltip
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
        html={tooltipLabel || column.tooltipLabel || value}
      >
        {cellElement}
      </Tooltip>
    );
  }

  return cellElement;
};

TableCell.propTypes = {
  column: PropTypes.shape({
    link: PropTypes.string,
    tooltip: PropTypes.bool,
    tooltipLabel: PropTypes.string,
  }),
  original: PropTypes.shape({}),
  children: PropTypes.element,
};


export default TableCell;
