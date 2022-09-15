import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const Button = ({
  label, defaultLabel, disabled, variant, type, onClickAction,
}) => (
  <button
    className={`${variant}-button d-flex justify-content-center align-items-center ${disabled && `${variant}-button-disabled`}`}
    disabled={disabled}
    type={type}
    onClick={onClickAction}
  >
    <Translate id={label} defaultMessage={defaultLabel} />
  </button>
);

export default Button;

Button.propTypes = {
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  type: PropTypes.string,
  variant: PropTypes.string,
  onClickAction: PropTypes.func,
};

Button.defaultProps = {
  disabled: false,
  type: 'button',
  variant: 'primary',
  onClickAction: undefined,
};
