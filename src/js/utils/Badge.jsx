import React from 'react';

import PropTypes from 'prop-types';

import CustomTooltip from 'wrappers/CustomTooltip';

import './utils.scss';

const Badge = ({ label, variant, tooltip }) => {
  if (!label) {
    return null;
  }
  return (
    <CustomTooltip
      content={label}
      show={tooltip}
    >
      <span className={`badge ${variant}`} data-testid="badge">{label}</span>
    </CustomTooltip>
  );
};

Badge.propTypes = {
  label: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
  tooltip: PropTypes.bool,
};

Badge.defaultProps = {
  tooltip: false,
};

export default Badge;
