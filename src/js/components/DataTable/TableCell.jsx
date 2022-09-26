import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const TableCell = ({
  value, children, tooltip, tooltipLabel, link,
}) => {
  let cellElement = <div className="text-overflow-ellipsis">{ children || value}</div>;

  if (link && typeof link === 'string') {
    cellElement = <a className="text-overflow-ellipsis" href={link}>{ children || value}</a>;
  }

  if (tooltip) {
    return (
      <Tooltip
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
        html={tooltipLabel || value}
      >
        {cellElement}
      </Tooltip>
    );
  }

  return cellElement;
};

TableCell.propTypes = {
  link: PropTypes.string,
  tooltip: PropTypes.bool,
  tooltipLabel: PropTypes.string,
  children: PropTypes.element,
};


export default TableCell;
