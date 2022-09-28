import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const TableCell = ({
  value, children, tooltip, tooltipLabel, link, defaultValue,
}) => {
  let cellValue = children || value || defaultValue;

  if (tooltip) {
    cellValue = (
      <Tooltip
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
        html={tooltipLabel || value}
      >
        {cellValue}
      </Tooltip>
    );
  }

  let cellElement = <div className="text-overflow-ellipsis">{cellValue}</div>;

  if (link && typeof link === 'string') {
    cellElement = <a className="text-overflow-ellipsis" href={link}>{ cellValue }</a>;
  }

  return cellElement;
};

TableCell.defaultProps = {
  defaultValue: undefined,
};

TableCell.propTypes = {
  link: PropTypes.string,
  tooltip: PropTypes.bool,
  tooltipLabel: PropTypes.string,
  children: PropTypes.element,
  defaultValue: PropTypes.string,
};


export default TableCell;
