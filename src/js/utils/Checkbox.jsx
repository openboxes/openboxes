import React from 'react';

import PropTypes from 'prop-types';

const Checkbox = ({
  value, indeterminate, custom, fieldRef, withLabel, label, ...props
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
          ref={fieldRef}
          checked={value}
          {...props}
          onChange={onChange}
        />
        <label htmlFor={props.id} />
      </div>
    );
  }

  if (withLabel) {
    return (
      <div className="d-flex align-items-center">
        <input
          type="checkbox"
          ref={(elem) => {
            if (elem) {
              // eslint-disable-next-line no-param-reassign
              elem.indeterminate = indeterminate;
            }
            if (fieldRef) {
              fieldRef(elem);
            }
          }}
          checked={value}
          {...props}
          onChange={onChange}
        />
        <label htmlFor={props.id} style={{ margin: '0 0 0 5px' }}>{label}</label>
      </div>
    );
  }

  return (
    <input
      type="checkbox"
      ref={(elem) => {
        if (elem) {
          // eslint-disable-next-line no-param-reassign
          elem.indeterminate = indeterminate;
        }
        if (fieldRef) {
          fieldRef(elem);
        }
      }}
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
  fieldRef: PropTypes.func,
  withLabel: PropTypes.bool,
  label: PropTypes.string,
};

Checkbox.defaultProps = {
  onChange: null,
  value: null,
  indeterminate: false,
  custom: false,
  id: '',
  fieldRef: undefined,
  withLabel: false,
  label: '',
};
