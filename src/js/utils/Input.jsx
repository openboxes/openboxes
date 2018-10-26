import React from 'react';
import PropTypes from 'prop-types';

const Input = ({ onChange, className = '', ...props }) => {
  const handleChange = (event) => {
    const { value } = event.target;

    if (onChange) {
      onChange(value);
    }
  };

  return (
    <input
      type="text"
      onKeyPress={(event) => {
        if (event.which === 13 /* Enter */) {
          event.preventDefault();
        }
      }}
      className={`form-control form-control-xs ${className}`}
      {...props}
      onChange={handleChange}
    />
  );
};

export default Input;

Input.propTypes = {
  onChange: PropTypes.func,
  className: PropTypes.string,
};

Input.defaultProps = {
  onChange: null,
  className: '',
};
