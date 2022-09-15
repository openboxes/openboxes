import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const ButtonPrimary = ({ label, defaultLabel, disabled }) => (
  <button
    className={`primary-button d-flex justify-content-center align-items-center ${disabled && 'primary-button-disabled'}`}
    disabled={disabled}
  >
    <Translate id={label} defaultMessage={defaultLabel} />
  </button>
);

export default ButtonPrimary;

ButtonPrimary.propTypes = {
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool.isRequired,
};
