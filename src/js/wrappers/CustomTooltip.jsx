import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const CustomTooltip = ({
  children,
  content,
  className,
  icon: Icon,
}) => (
  // This div was added to ensure the tooltip works correctly with absolute positioning
  <div className={className}>
    <Tooltip
      delay={150}
      duration={250}
      hideDelay={50}
      html={<div className="p-2 custom-tooltip-v2">{content}</div>}
    >
      <div className="flex items-center">
        {Icon && <Icon className="mr-2" />}
        {children}
      </div>
    </Tooltip>
  </div>
);

export default CustomTooltip;

CustomTooltip.propTypes = {
  children: PropTypes.node.isRequired,
  content: PropTypes.node.isRequired,
  className: PropTypes.string,
  icon: PropTypes.elementType,
};

CustomTooltip.defaultProps = {
  className: '',
  icon: null,
};
