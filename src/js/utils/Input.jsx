import React from 'react';
import PropTypes from 'prop-types';

const Input = (props) => {
  const onChange = (event) => {
    const { value } = event.target;

    if (props.onChange) {
      props.onChange(value);
    }
  };

  return (
    <input
      type="text"
      {...props}
      onChange={onChange}
    />
  );
};

export default Input;

Input.propTypes = {
  onChange: PropTypes.func,
};

Input.defaultProps = {
  onChange: null,
};
