import React, { forwardRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';

import './style.scss';

const DateFieldInputComponent = ({
  value = null,
  placeholder = '',
  onClear,
  className = '',
  disabled = false,
  clearable,
  ...props
}, ref) => {
  const disabledProps = disabled ? {
    className: `disabled ${className} date-field-input`,
  } : {
    className: `${className} date-field-input`,
    tabIndex: 0,
    role: 'button',
  };

  const getIcon = () => {
    if (value && clearable) {
      return (
        <button
          className="border-0 bg-inherit"
          type="button"
          aria-label="clear"
          onClick={onClear}
        >
          <RiCloseLine />
        </button>
      );
    }

    return <RiCalendarLine />;
  };

  return (
    <div
      {...props}
      {...disabledProps}
      ref={ref}
    >
      <span className="date-field-input__value">{value}</span>
      {!value && <span className="date-field-input__placeholder">{placeholder}</span>}
      {!disabled
        && (
          <span className="form-element-icons-wrapper">
            {getIcon()}
          </span>
        )}
    </div>
  );
};

const DateFieldInput = forwardRef(DateFieldInputComponent);

export default DateFieldInput;

DateFieldInputComponent.propTypes = {
  value: PropTypes.string,
  placeholder: PropTypes.string,
  onClear: PropTypes.func.isRequired,
  className: PropTypes.string,
  disabled: PropTypes.bool,
  clearable: PropTypes.bool.isRequired,
};
