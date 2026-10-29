import React from 'react';

import PropTypes from 'prop-types';

import CustomTooltip from 'wrappers/CustomTooltip';

import './utils.scss';

const Badge = ({
  label, variant, tooltip, clickable,
}) => {
  if (!label) {
    return null;
  }

  const className = ['badge', variant, !clickable && 'badge--not-clickable']
    .filter(Boolean)
    .join(' ');

  return (
    <CustomTooltip
      content={label}
      show={tooltip}
    >
      <span className={className} data-testid="badge">{label}</span>
    </CustomTooltip>
  );
};

Badge.propTypes = {
  label: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
  tooltip: PropTypes.bool,
  clickable: PropTypes.bool,
};

Badge.defaultProps = {
  tooltip: false,
  clickable: true,
};

export default Badge;
