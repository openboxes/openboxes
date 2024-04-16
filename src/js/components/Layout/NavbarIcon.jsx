import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const NavbarIcon = ({ tooltip, component, name }) => {
  const [isTooltipDisabled, setIsTooltipDisabled] = useState(false);

  return (
    <Tooltip
      html={<div className="custom-tooltip">{tooltip}</div>}
      theme="transparent"
      disabled={isTooltipDisabled}
    >
      <div data-testid="navbar-icon" aria-label={name}>
        {component({ setIsTooltipDisabled })}
      </div>
    </Tooltip>
  );
};

export default NavbarIcon;

NavbarIcon.propTypes = {
  tooltip: PropTypes.string.isRequired,
  component: PropTypes.func.isRequired,
  name: PropTypes.string.isRequired,
};
