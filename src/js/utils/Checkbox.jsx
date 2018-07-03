import React from 'react';
import PropTypes from 'prop-types';

const Checkbox = (props) => {
  const onChange = (event) => {
    const { checked } = event.target;

    if (props.onChange) {
      props.onChange(checked);
    }
  };

  return (
    <input
      type="checkbox"
      checked={props.value}
      {...props}
      onChange={onChange}
    />);
};

export default Checkbox;

Checkbox.propTypes = {
  onChange: PropTypes.func,
  value: PropTypes.bool,
};

Checkbox.defaultProps = {
  onChange: null,
  value: null,
};
