import React from 'react';

import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { Tooltip } from 'react-tippy';
import { RiErrorWarningLine } from 'react-icons/ri';

const TableCell = ({
  value,
  children,
  tooltip,
  tooltipLabel,
  link, reactLink,
  defaultValue,
  className,
  openLinkInNewTab,
  tdProps,
  showError,
}) => {
  let cellValue = children || value || defaultValue;
  const errorMessage = tdProps?.rest?.error;

  if (showError && errorMessage) {
    cellValue = (
      <div className="d-flex flex-row align-items-center">
        <Tooltip
          arrow="true"
          delay="150"
          duration="250"
          hideDelay="50"
          className="text-overflow-ellipsis"
          html={errorMessage}
        >
          <RiErrorWarningLine className="mr-1" />
        </Tooltip>
        {cellValue}
      </div>
    );
  }

  if (tooltip) {
    cellValue = (
      <div className="d-flex">
        <Tooltip
          arrow="true"
          delay="150"
          duration="250"
          hideDelay="50"
          className="text-overflow-ellipsis"
          html={tooltipLabel || value}
        >
          {cellValue}
        </Tooltip>
      </div>
    );
  }
  const cellErrorClasses = showError && errorMessage ? 'invalid-cell' : '';
  const elementClasses = `${className} text-overflow-ellipsis ${cellErrorClasses}`;

  let cellElement = <div className={elementClasses} data-testid="table-cell">{cellValue}</div>;

  if (link && typeof link === 'string') {
    if (reactLink) {
      cellElement = <Link className={elementClasses} to={link}>{cellValue}</Link>;
    } else {
      cellElement = (
        <a
          className={elementClasses}
          target={openLinkInNewTab ? '_blank' : undefined}
          href={link}
        >
          {cellValue}
        </a>
      );
    }
  }

  return cellElement;
};

TableCell.defaultProps = {
  defaultValue: undefined,
  className: '',
  reactLink: false,
  openLinkInNewTab: false,
  showError: false,
  tdProps: {},
};

TableCell.propTypes = {
  link: PropTypes.string,
  reactLink: PropTypes.bool,
  showError: PropTypes.bool,
  className: PropTypes.string,
  tooltip: PropTypes.bool,
  openLinkInNewTab: PropTypes.bool,
  tooltipLabel: PropTypes.string,
  tdProps: PropTypes.shape({
    rest: PropTypes.shape({
      error: PropTypes.string,
    }),
  }),
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]),
  defaultValue: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
};

export default TableCell;
