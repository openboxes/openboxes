import React from 'react';

import PropTypes from 'prop-types';

import CustomTooltip from 'wrappers/CustomTooltip';

import './utils.scss';

const Badge = ({ label, variant, tooltip }) => (
  <CustomTooltip
    content={label}
    show={tooltip}
  >
    <span className={`badge ${variant}`}>{label}</span>
  </CustomTooltip>
);

Badge.propTypes = {
  label: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
  tooltip: PropTypes.bool,
};

Badge.defaultProps = {
  tooltip: false,
};

export default Badge;
