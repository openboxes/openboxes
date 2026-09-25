import React from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { Link } from 'react-router-dom';
import { Tooltip } from 'react-tippy';

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
  customTooltipDisabled,
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
      <div className={`d-flex ${tooltipClassname}`}>
        <Tooltip
          arrow="true"
          delay="150"
          duration="250"
          hideDelay="50"
          className="text-overflow-ellipsis"
          style={tooltipForm && { width: '100%' }}
          html={tooltipLabel || value}
        >
          {cellValue}
        </Tooltip>
      </div>
    );
  }

  if (customTooltip) {
    cellValue = (
      <CustomTooltip
        content={tooltipLabel || value}
        className={tooltipClassname}
        disabled={customTooltipDisabled}
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
  customTooltipDisabled: false,
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
  /**
   * True if the cell should be wrapped in a {@link CustomTooltip}. If false, the tooltip will
   * not be rendered at all, and so setting `customTooltipDisabled=false` will have no impact.
   *
   * If you need to conditionally enable the tooltip without reloading the page, set `customTooltip`
   * to true and use `customTooltipDisabled` to dynamically control the state of the tooltip.
   */
  customTooltip: PropTypes.bool,
  /**
   * True if the {@link CustomTooltip} added by `customTooltip` should be disabled.
   *
   * Useful for cases where the tooltip will conditionally be active depending on user interaction
   * with the cell (for example, for displaying validation errors). We don't want the tooltip
   * to ever get unmounted, even if there is nothing for it to show, since unmounting it can alter
   * state, trigger a redraw, and cause the cell to lose focus.
   *
   * If `customTooltip` is false, `customTooltipDisabled` will have no effect.
   */
  customTooltipDisabled: PropTypes.bool,
};

export default TableCell;
