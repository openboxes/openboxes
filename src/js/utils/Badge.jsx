import React from 'react';

import PropTypes from 'prop-types';

import './utils.scss';

const Badge = ({ label, variant }) => (
  <span className={`badge ${variant}`}>{label}</span>
);

Badge.propTypes = {
  label: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
};

export default Badge;
