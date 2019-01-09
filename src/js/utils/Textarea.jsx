import React from 'react';
import PropTypes from 'prop-types';

const Textarea = ({
  onChange, className = '', fieldRef, ...props
}) => {
  const handleChange = (event) => {
    const { value } = event.target;

    if (onChange) {
      onChange(value);
    }
  };

  return (
    <textarea
      ref={fieldRef}
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

export default Textarea;

Textarea.propTypes = {
  onChange: PropTypes.func,
  className: PropTypes.string,
  fieldRef: PropTypes.func,
};

Textarea.defaultProps = {
  onChange: null,
  className: '',
  fieldRef: null,
};
