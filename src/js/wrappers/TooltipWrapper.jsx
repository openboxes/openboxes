import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const TooltipWrapper = ({
  children,
  content,
  className,
}) => (

  <div className={className}>
    <Tooltip
      delay="150"
      duration="250"
      hideDelay="50"
      html={<div className="p-2 custom-tooltip-v2">{content}</div>}
    >
      <span>{children}</span>
    </Tooltip>
  </div>
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
