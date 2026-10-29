import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import CustomTooltip from 'wrappers/CustomTooltip';

/**
 * Wraps a component or other HTML block with a {@link CustomTooltip}.
 *
 * Primarily exists to allow callers to only conditionally include this wrapper. Needed since
 * the caller might not have access to redux, and so invoking translate would throw an error.
 *
 * @param param
 * @param param.tooltipLabel The localizable message to display
 * @param param.defaultTooltipLabel The fallback text to display if tooltipLabel does not exist
 * @param param.children The child object that the tooltip wraps
 */
const TooltipWrapper = ({
  tooltipLabel,
  defaultTooltipLabel,
  children,
}) => {
  const translate = useTranslate();
  return (
    <CustomTooltip
      content={tooltipLabel ? translate(tooltipLabel, defaultTooltipLabel) : defaultTooltipLabel}
    >
      {children}
    </CustomTooltip>
  );
};

export default TooltipWrapper;

TooltipWrapper.propTypes = {
  tooltipLabel: PropTypes.string,
  defaultTooltipLabel: PropTypes.string,
  children: PropTypes.node.isRequired,
};

TooltipWrapper.defaultProps = {
  tooltipLabel: null,
  defaultTooltipLabel: '',
};
