import React from 'react';

import PropTypes from 'prop-types';

import InputWrapper from 'wrappers/InputWrapper';

import './style.scss';

const Checkbox = ({
  title,
  tooltip,
  button,
  disabled,
  errorMessage,
  labelPosition,
  id,
  name,
  noWrapper,
  indeterminate,
  className,
  ...fieldProps
}) => {
  const checkbox = (
    <input
      id={id || name}
      name={name}
      type="checkbox"
      disabled={disabled}
      className={`checkbox-v2 ${errorMessage ? 'has-errors' : ''} ${className}`}
      {...fieldProps}
      checked={fieldProps.value}
      ref={(checkboxRef) => {
        if (checkboxRef) {
          // eslint-disable-next-line no-param-reassign
          checkboxRef.indeterminate = indeterminate;
        }
      }}
    />
  );

  return (
    noWrapper ? checkbox : (
      <InputWrapper
        title={title}
        tooltip={tooltip}
        button={button}
        errorMessage={errorMessage}
        inputId={id || name}
        labelPosition={labelPosition}
      >
        <div className="form-element-checkbox">
          {checkbox}
        </div>
      </InputWrapper>
    )
  );
};

export default Checkbox;

Checkbox.propTypes = {
  // Message which will be shown on the tooltip above the field
  tooltip: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  // Title displayed above the field
  title: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  // Button on the right side above the input
  button: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  }),
  // Indicator whether the field should be disabled
  disabled: PropTypes.bool,
  // If the errorMessage is not empty then the field is bordered
  // and the message is displayed under the input
  errorMessage: PropTypes.string,
  // position of input label
  labelPosition: PropTypes.oneOf(['top', 'bottom', 'left', 'right']),
  // html element id
  id: PropTypes.string,
  // html element name
  name: PropTypes.string,
  // Indicator whether we want to remove the wrapper from the checkbox
  // it is needed to remove additional padding
  noWrapper: PropTypes.bool,
  indeterminate: PropTypes.bool,
  className: PropTypes.string,
};

Checkbox.defaultProps = {
  tooltip: null,
  title: null,
  button: null,
  errorMessage: null,
  disabled: false,
  id: undefined,
  name: undefined,
  labelPosition: 'right',
  noWrapper: false,
  indeterminate: false,
  className: '',
};
