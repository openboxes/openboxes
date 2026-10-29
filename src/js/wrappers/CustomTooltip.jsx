import React, { useId } from 'react';

import PropTypes from 'prop-types';

import PortalTooltip from 'wrappers/PortalTooltip';

const CustomTooltip = ({
  children,
  content,
  className,
  show = true,
  icon: Icon,
  place = 'top',
  delayShow = 150,
}) => {
  const tooltipId = useId();
  return show ? (
    <div className={className} role="tooltip">
      <div
        className="flex items-center w-100"
        data-tooltip-id={tooltipId}
      >
        {Icon && <Icon className="mr-2" />}
        {children}
      </div>
      <PortalTooltip
        id={tooltipId}
        delayShow={delayShow}
        delayHide={50}
        className="custom-tooltip-popup"
        place={place}
        opacity={1}
        noArrow
      >
        <div className={`p-2 tooltip-dark-blue ${!content ? 'd-none' : ''}`}>
          {content}
        </div>
      </PortalTooltip>
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
  place: PropTypes.string,
  delayShow: PropTypes.number,
};

CustomTooltip.defaultProps = {
  className: '',
  icon: null,
  show: true,
  place: 'top',
  delayShow: 150,
};
