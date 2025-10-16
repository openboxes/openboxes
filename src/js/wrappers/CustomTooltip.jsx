import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const CustomTooltip = ({
  children,
  content,
  className,
  show,
  icon: Icon,
}) => (
  // This div was added to ensure the tooltip works correctly with absolute positioning
  show ? (
    <div className={className}>
      <Tooltip
        delay={150}
        duration={250}
        hideDelay={50}
        className="w-100"
        html={<div className={`p-2 tooltip-dark-blue ${!content && 'd-none'}`}>{content}</div>}
      >
        <div className="flex items-center">
          {Icon && <Icon className="mr-2" />}
          {children}
        </div>
      </Tooltip>
    </div>
  ) : children
);

export default CustomTooltip;

CustomTooltip.propTypes = {
  children: PropTypes.node.isRequired,
  content: PropTypes.node.isRequired,
  className: PropTypes.string,
  show: PropTypes.bool,
  icon: PropTypes.elementType,
};

CustomTooltip.defaultProps = {
  className: '',
  icon: null,
  show: true,
};
