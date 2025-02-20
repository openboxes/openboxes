import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const TooltipWrapper = ({
  children,
  content,
  className,
  ...props
}) => (
  <Tooltip
    className={`d-flex align-items-center ${className}`}
    delay={150}
    duration={250}
    hideDelay={50}
    html={<div className="custom-tooltip-v2">{content}</div>}
    {...props}
  >
    {children}
  </Tooltip>
);

export default TooltipWrapper;

TooltipWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  content: PropTypes.node.isRequired,
  className: PropTypes.string,
};

TooltipWrapper.defaultProps = {
  className: '',
};
