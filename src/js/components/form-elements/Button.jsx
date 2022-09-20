import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const Button = ({
  label, defaultLabel, disabled, variant, type, onClickAction, EndIcon,
}) => (
  <button
    className={`${variant}-button d-flex justify-content-around align-items-center ${disabled && `${variant}-button-disabled`} ${variant === 'dropdown' && 'dropdown-toggle'}`}
    disabled={disabled}
    type={type}
    onClick={onClickAction}
    data-toggle={variant === 'dropdown' && 'dropdown'}
    aria-haspopup={variant === 'dropdown' && 'true'}
    aria-expanded={variant === 'dropdown' && 'false'}
  >
    {EndIcon ? (
      <React.Fragment>
        <div className="px-1">
          <Translate id={label} defaultMessage={defaultLabel} />
        </div>
        <div className="px-1">
          {EndIcon}
        </div>
      </React.Fragment>
    ) :
      <Translate id={label} defaultMessage={defaultLabel} />
    }

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
  EndIcon: PropTypes.element,
};

Button.defaultProps = {
  disabled: false,
  type: 'button',
  variant: 'primary',
  onClickAction: undefined,
  EndIcon: null,
};
