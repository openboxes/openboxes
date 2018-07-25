import React from 'react';
import PropTypes from 'prop-types';

const Checkbox = ({ value, indeterminate, ...props }) => {
  const onChange = (event) => {
    const { checked } = event.target;

    if (props.onChange) {
      props.onChange(checked);
    }
  };

  return (
    <input
      type="checkbox"
      // eslint-disable-next-line no-param-reassign,no-return-assign
      ref={elem => elem && (elem.indeterminate = indeterminate)}
      checked={value}
      {...props}
      onChange={onChange}
    />);
};

export default Checkbox;

Checkbox.propTypes = {
  onChange: PropTypes.func,
  value: PropTypes.bool,
  indeterminate: PropTypes.bool,
};

Checkbox.defaultProps = {
  onChange: null,
  value: null,
  indeterminate: false,
};
