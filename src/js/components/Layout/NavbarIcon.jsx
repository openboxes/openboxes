import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const NavbarIcon = ({ tooltip, component }) => {
  const [isTooltipDisabled, setIsTooltipDisabled] = useState(false);

  return (
    <Tooltip
      html={<div className="custom-tooltip">{tooltip}</div>}
      theme="transparent"
      disabled={isTooltipDisabled}
    >
      {component({ setIsTooltipDisabled })}
    </Tooltip>
  );
};

export default NavbarIcon;

NavbarIcon.propTypes = {
  tooltip: PropTypes.string.isRequired,
  component: PropTypes.func.isRequired,
};

