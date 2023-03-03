import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const Checkbox = ({
  value, indeterminate, custom, fieldRef, withLabel, label, defaultMessage, ...props
}) => {
  const onChange = (event) => {
    const { checked } = event.target;

    if (props.onChange) {
      props.onChange(checked);
    }
  };

  if (custom) {
    return (
      <div data-testid="custom-checkbox" className="custom-checkbox">
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
      <div data-testid="with-label-checkbox" className="d-flex align-items-center">
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
        <label htmlFor={props.id} style={{ margin: '0 0 0 5px' }}>
          <Translate id={label} defaultMessage={defaultMessage || label} />
        </label>
      </div>
    );
  }

  return (
    <input
      data-testid="checkbox"
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
  defaultMessage: PropTypes.string,
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
  defaultMessage: '',
};
