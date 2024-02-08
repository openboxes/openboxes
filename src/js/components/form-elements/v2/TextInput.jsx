import React from 'react';

import PropTypes from 'prop-types';

import InputWrapper from 'wrappers/InputWrapper';

import './style.scss';

const TextInput = ({
  title,
  tooltip,
  required,
  button,
  disabled,
  errorMessage,
  placeholder,
  ...fieldProps
}) => (
  <InputWrapper
    title={title}
    tooltip={tooltip}
    required={required}
    button={button}
    errorMessage={errorMessage}
  >
    <input
      disabled={disabled}
      className={`form-element-input ${errorMessage ? 'has-errors' : ''}`}
      placeholder={placeholder}
      {...fieldProps}
    />
  </InputWrapper>
);

export default TextInput;

TextInput.propTypes = {
  // Message which will be shown on the tooltip above the field
  tooltip: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  // Indicator whether the red asterisk has to be shown
  required: PropTypes.bool,
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
  // Text displayed within input field
  placeholder: PropTypes.string,
};

TextInput.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
  disabled: false,
  placeholder: '',
};
