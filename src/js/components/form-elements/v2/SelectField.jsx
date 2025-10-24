import React, { useEffect, useRef, useState } from 'react';

import PropTypes from 'prop-types';

import ProductSelect from 'components/product-select/ProductSelect';
import componentType from 'consts/componentType';
import useFocusOnMatch from 'hooks/useFocusOnMatch';
import Select from 'utils/Select';
import InputWrapper from 'wrappers/InputWrapper';

import './style.scss';

const SelectField = ({
  title,
  tooltip,
  required,
  button,
  disabled,
  errorMessage,
  placeholder,
  async,
  options,
  loadOptions,
  defaultValue,
  multiple,
  onChange,
  productSelect,
  hasErrors,
  className,
  warning,
  hideErrorMessageWrapper,
  onKeyDown,
  focusProps = {},
  creatable,
  customTooltip,
  ...fieldProps
}) => {
  const [value, setValue] = useState(defaultValue);

  useEffect(() => {
    setValue(defaultValue);
  }, [defaultValue?.id]);

  const asyncProps = async ? {
    async,
    loadOptions,
  } : {
    options,
  };

  const onChangeValue = (selectedOption) => {
    onChange?.(selectedOption);
    setValue(selectedOption);
  };

  const SelectComponent = productSelect ? ProductSelect : Select;

  const fieldRef = useRef(null);

  useFocusOnMatch({ ...focusProps, ref: fieldRef, type: componentType.SELECT_FIELD });

  return (
    <InputWrapper
      title={title}
      errorMessage={errorMessage}
      button={{ ...button, onClick: () => button.onClick(fieldProps?.value?.id ?? value) }}
      tooltip={tooltip}
      required={required}
      hideErrorMessageWrapper={hideErrorMessageWrapper}
      className="select-wrapper-container"
      customTooltip={customTooltip}
      value={fieldProps?.value?.label}
    >
      <SelectComponent
        className={`form-element-select ${className} ${errorMessage || hasErrors ? 'has-errors' : ''} ${warning ? 'has-warning' : ''}`}
        disabled={disabled}
        placeholder={placeholder}
        value={value}
        onChange={onChangeValue}
        multi={multiple}
        onKeyDown={onKeyDown}
        creatable={creatable}
        fieldRef={fieldRef}
        {...asyncProps}
        {...fieldProps}
      />
    </InputWrapper>
  );
};

export default SelectField;

SelectField.propTypes = {
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
  // The error message displayed under field
  errorMessage: PropTypes.string,
  // Text displayed within input field
  placeholder: PropTypes.string,
  // Indicator whether options will be loaded asynchronously
  async: PropTypes.bool,
  // Predefined options, not loaded asynchronously
  options: PropTypes.arrayOf(PropTypes.object),
  // Function loading options asynchronously
  loadOptions: PropTypes.func,
  // Default value of the select
  defaultValue: PropTypes.string,
  // Indicator whether we should be able to choose multiple options
  multiple: PropTypes.bool,
  // Function triggered on change
  onChange: PropTypes.func,
  productSelect: PropTypes.bool,
  // indicator whether field should be marked as invalid
  hasErrors: PropTypes.bool,
  className: PropTypes.string,
  hideErrorMessageWrapper: PropTypes.bool,
  warning: PropTypes.bool,
  onKeyDown: PropTypes.func,
  focusProps: PropTypes.shape({
    fieldIndex: PropTypes.string,
    fieldId: PropTypes.string,
    rowIndex: PropTypes.string,
    columnId: PropTypes.string,
  }),
  // boolean that enables creating new options in the dropdown
  creatable: PropTypes.bool,
  customTooltip: PropTypes.bool,
};

SelectField.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
  disabled: false,
  placeholder: '',
  async: false,
  options: [],
  loadOptions: () => [],
  defaultValue: null,
  multiple: false,
  onChange: () => {},
  productSelect: false,
  hasErrors: false,
  className: '',
  hideErrorMessageWrapper: false,
  warning: false,
  onKeyDown: null,
  focusProps: {},
  creatable: false,
  customTooltip: false,
};
