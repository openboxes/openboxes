import React, { useId } from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tooltip';

const CustomTooltip = ({
  children,
  content,
  className,
  show,
  icon: Icon,
}) => {
  const tooltipId = useId();
  return show ? (
    <div className={className} role="tooltip">
      <div
        className="flex items-center"
        data-tooltip-id={tooltipId}
      >
        {Icon && <Icon className="mr-2" />}
        {children}
      </div>
      <Tooltip
        id={tooltipId}
        delayShow={150}
        delayHide={50}
        className="w-100"
        place="top"
      >
        <div className={`p-2 tooltip-dark-blue ${!content ? 'd-none' : ''}`}>
          {content}
        </div>
      </Tooltip>
    </div>
  ) : children;
};

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
