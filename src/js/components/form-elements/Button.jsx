import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const Button = ({
  label, defaultLabel, disabled, variant, type, onClickAction, EndIcon,
}) => {
  const buttonClass = 'd-flex justify-content-around align-items-center gap-8';
  const variantClass = `${variant}-button`;
  const disabledClass = `${disabled ? `${variant}-button-disabled` : ''}`;
  const dropDownClass = `${variant === 'dropdown' ? 'dropdown-toggle' : ''}`;

  return (
    <button
      className={[variantClass, buttonClass, disabledClass, dropDownClass].join(' ')}
      disabled={disabled}
      type={type}
      onClick={onClickAction}
      data-toggle={variant === 'dropdown' && 'dropdown'}
      aria-haspopup={variant === 'dropdown' && 'true'}
      aria-expanded={variant === 'dropdown' && 'false'}
    >
      <React.Fragment>
        <Translate id={label} defaultMessage={defaultLabel} />
        {EndIcon && EndIcon}
      </React.Fragment>
    </button>
  );
};

export default Button;

Button.propTypes = {
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  type: PropTypes.string,
  variant: PropTypes.string,
  onClickAction: PropTypes.func,
  EndIcon: PropTypes.element,
};

Button.defaultProps = {
  disabled: false,
  type: 'button',
  variant: 'primary',
  onClickAction: undefined,
  EndIcon: null,
};
