import React from 'react';
import PropTypes from 'prop-types';

const Checkbox = ({
  value, indeterminate, custom, ...props
}) => {
  const onChange = (event) => {
    const { checked } = event.target;

    if (props.onChange) {
      props.onChange(checked);
    }
  };

  if (custom) {
    return (
      <div className="custom-checkbox">
        <input
          type="checkbox"
          checked={value}
          {...props}
          onChange={onChange}
        />
        <label htmlFor={props.id} />
      </div>
    );
  }

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
  custom: PropTypes.bool,
  id: PropTypes.string,
};

Checkbox.defaultProps = {
  onChange: null,
  value: null,
  indeterminate: false,
  custom: false,
  id: '',
};
