import React, { forwardRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';

import './style.scss';

const DateFieldInput = forwardRef(({
  value,
  placeholder,
  onClear,
  className,
  disabled,
  ...props
}, ref) => {
  const disabledProps = disabled ? {
    className: `disabled ${className}`,
  } : {
    className,
    tabIndex: 0,
    role: 'button',
  };

  return (
    <div
      {...props}
      {...disabledProps}
      ref={ref}
    >
      <span>{value || placeholder}</span>
      {!disabled
        && (
          <span className="form-element-icons-wrapper">
            {value ? <RiCloseLine onClick={onClear} /> : <RiCalendarLine />}
          </span>
        )}
    </div>
  );
});

export default DateFieldInput;

DateFieldInput.propTypes = {
  value: PropTypes.string,
  placeholder: PropTypes.string,
  onClear: PropTypes.func.isRequired,
  className: PropTypes.string,
  disabled: PropTypes.bool,
};

DateFieldInput.defaultProps = {
  value: null,
  placeholder: '',
  disabled: false,
  className: '',
};