import React, { useEffect, useRef } from 'react';

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
  hideErrorMessageWrapper,
  onKeyDown,
  fieldIndex,
  fieldId,
  focusIndex,
  focusId,
  ...fieldProps
}) => {
  const inputRef = useRef(null);

  useEffect(() => {
    if (fieldIndex === focusIndex && fieldId === focusId) {
      inputRef.current?.focus();
    }
  }, [fieldIndex, fieldId, focusIndex, focusId]);

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
      hideErrorMessageWrapper={hideErrorMessageWrapper}
    >
      <input
        ref={inputRef}
        id={id || name}
        name={name}
        disabled={disabled}
        className={`form-element-input ${className} ${(errorMessage || showErrorBorder) ? 'has-errors' : ''} ${hideErrorMessageWrapper && showErrorBorder && 'pl-4'}`}
        placeholder={placeholder}
        type={type}
        step={numberIncrementValue}
        {...fieldProps}
        onChange={onChangeHandler}
        onBlur={onBlurHandler}
        onKeyDown={onKeyDown}
      />
    </InputWrapper>
  );
};

TextInput.propTypes = {
  tooltip: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  required: PropTypes.bool,
  title: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  button: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  }),
  disabled: PropTypes.bool,
  errorMessage: PropTypes.string,
  placeholder: PropTypes.string,
  id: PropTypes.string,
  name: PropTypes.string,
  type: PropTypes.string,
  decimal: PropTypes.number,
  className: PropTypes.string,
  showErrorBorder: PropTypes.bool,
  hideErrorMessageWrapper: PropTypes.bool,
  onKeyDown: PropTypes.func,
  fieldIndex: PropTypes.string,
  fieldId: PropTypes.string,
  focusIndex: PropTypes.string,
  focusId: PropTypes.string,
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
  hideErrorMessageWrapper: false,
  onKeyDown: null,
  fieldIndex: '',
  fieldId: '',
  focusIndex: '',
  focusId: '',
};

export default TextInput;
