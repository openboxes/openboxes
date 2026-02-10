/* eslint-disable react/button-has-type */
import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const Button = ({
  label,
  defaultLabel,
  disabled,
  variant,
  type,
  onClick,
  EndIcon,
  isDropdown,
  StartIcon,
  className,
  customRef,
}) => {
  const buttonClass = 'd-flex justify-content-around align-items-center gap-8';
  const variantClass = `${variant}-button`;
  const dropDownClass = `${isDropdown ? 'dropdown-toggle' : ''}`;

  return (
    <button
      className={[variantClass, buttonClass, dropDownClass, className].join(' ')}
      disabled={disabled}
      type={type}
      onClick={onClick}
      data-toggle={isDropdown && 'dropdown'}
      aria-haspopup={isDropdown && 'true'}
      aria-expanded={isDropdown && 'false'}
      ref={customRef}
    >
      <>
        {StartIcon && StartIcon}
        <Translate id={label} defaultMessage={defaultLabel} />
        {EndIcon && EndIcon}
      </>
    </button>
  );
};

export default Button;

Button.propTypes = {
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  type: PropTypes.string,
  variant: PropTypes.oneOf([
    'primary',
    'secondary',
    'transparent',
    'primary-outline',
    'grayed',
    'danger',
  ]),
  onClick: PropTypes.func,
  EndIcon: PropTypes.element,
  StartIcon: PropTypes.element,
  isDropdown: PropTypes.bool,
  className: PropTypes.string,
  customRef: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.shape({ current: PropTypes.instanceOf(Element) }),
  ]),
};

Button.defaultProps = {
  disabled: false,
  isDropdown: false,
  type: 'button',
  variant: 'primary',
  onClick: undefined,
  EndIcon: null,
  StartIcon: null,
  className: '',
  customRef: null,
};
