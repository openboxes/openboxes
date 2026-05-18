import React from 'react';

// import Tippy from '@tippyjs/react';
import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { Link } from 'react-router-dom';

import CustomTooltip from 'wrappers/CustomTooltip';

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
  style,
  showError,
  tooltipForm,
  tooltipClassname,
  customTooltip,
}) => {
  let cellValue = children || value || defaultValue;
  const errorMessage = tdProps?.rest?.error;

  if (showError && errorMessage) {
    cellValue = (
      <div className="d-flex flex-row align-items-center">
        {/* <Tippy */}
        {/*   arrow="true" */}
        {/*   delay="150" */}
        {/*   duration="250" */}
        {/*   hideDelay="50" */}
        {/*   className="text-overflow-ellipsis" */}
        {/*   content={errorMessage} */}
        {/* > */}
        <RiErrorWarningLine className="mr-1" />
        {/* </Tippy> */}
        {cellValue}
      </div>
    );
  }

  if (tooltip) {
    cellValue = (
      <div className={`d-flex ${tooltipClassname}`}>
        {/* <Tippy */}
        {/*   arrow="true" */}
        {/*   delay="150" */}
        {/*   duration="250" */}
        {/*   hideDelay="50" */}
        {/*   className="text-overflow-ellipsis" */}
        {/*   style={tooltipForm && { width: '100%' }} */}
        {/*   content={tooltipLabel || value} */}
        {/* > */}
        {cellValue}
        {/* </Tippy> */}
      </div>
    );
  }

  if (customTooltip) {
    cellValue = (
      <CustomTooltip
        content={tooltipLabel || value}
        className={tooltipClassname}
      >
        {cellValue}
      </CustomTooltip>
    );
  }

  const cellErrorClasses = showError && errorMessage ? 'invalid-cell' : '';
  const elementClasses = `${className} text-overflow-ellipsis ${cellErrorClasses}`;

  let cellElement = (
    <div
      className={elementClasses}
      style={style}
      data-testid="table-cell"
    >
      {cellValue}
    </div>
  );

  if (link && typeof link === 'string') {
    if (reactLink) {
      cellElement = <Link className={elementClasses} style={style} to={link}>{cellValue}</Link>;
    } else {
      cellElement = (
        <a
          className={elementClasses}
          style={style}
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
  tooltipLabel: '',
};

TableCell.propTypes = {
  link: PropTypes.string,
  reactLink: PropTypes.bool,
  showError: PropTypes.bool,
  className: PropTypes.string,
  tooltip: PropTypes.bool,
  openLinkInNewTab: PropTypes.bool,
  tooltipLabel: PropTypes.string,
  style: PropTypes.shape({}),
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
  tooltipForm: PropTypes.bool,
  tooltipClassname: PropTypes.string,
  customTooltip: PropTypes.bool,
};

export default TableCell;
