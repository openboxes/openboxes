import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import { decimalParser } from 'utils/form-utils';
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
  id,
  name,
  type,
  decimal,
  className,
  showErrorBorder,
  ...fieldProps
}) => {
  const onBlurHandler = (e) => {
    if (type === 'number') {
      const valueAsNumber = decimalParser(e.target.value, decimal);
      e.target.value = valueAsNumber;
      fieldProps.onChange?.(valueAsNumber);
    }
    fieldProps.onBlur?.(e);
  };

  const onChangeHandler = (e) => {
    switch (type) {
      case 'number': {
        const valueAsNumber = Number.isNaN(e.target.valueAsNumber)
          ? undefined
          : e.target.valueAsNumber;
        fieldProps.onChange?.(valueAsNumber);
        break;
      }
      default:
        fieldProps.onChange?.(e);
    }
  };

  const numberIncrementValue = type === 'number' && _.isNumber(decimal)
    ? 0.1 ** decimal
    : undefined;

  return (
    <InputWrapper
      title={title}
      tooltip={tooltip}
      required={required}
      button={button}
      inputId={id || name}
      errorMessage={errorMessage}
    >
      <input
        id={id || name}
        name={name}
        disabled={disabled}
        className={`form-element-input ${className} ${(errorMessage || showErrorBorder) ? 'has-errors' : ''}`}
        placeholder={placeholder}
        type={type}
        step={numberIncrementValue}
        {...fieldProps}
        onChange={onChangeHandler}
        onBlur={onBlurHandler}
      />
    </InputWrapper>
  );
};

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
  // html element id
  id: PropTypes.string,
  // html element name
  name: PropTypes.string,
  type: PropTypes.string,
  decimal: PropTypes.number,
  className: PropTypes.string,
  showErrorBorder: PropTypes.bool,
};

TextInput.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
  disabled: false,
  placeholder: '',
  id: undefined,
  name: undefined,
  type: 'text',
  decimal: undefined,
  className: '',
  showErrorBorder: false,
};
