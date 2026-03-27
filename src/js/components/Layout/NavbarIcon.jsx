import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tooltip';

const NavbarIcon = ({ tooltip, component, name }) => {
  const [isTooltipDisabled, setIsTooltipDisabled] = useState(false);
  const tooltipId = `tooltip-${name}`;

  return (
    <>
      <div
        data-testid="navbar-icon"
        aria-label={name}
        data-tooltip-id={isTooltipDisabled ? undefined : tooltipId}
        data-tooltip-content={tooltip}
      >
        {component({ setIsTooltipDisabled })}
      </div>
      <Tooltip
        id={tooltipId}
        content={tooltip}
        className="custom-tooltip"
        place="bottom"
      />
    </>
  );
};

export default NavbarIcon;

NavbarIcon.propTypes = {
  tooltip: PropTypes.string.isRequired,
  component: PropTypes.func.isRequired,
  name: PropTypes.string.isRequired,
};
