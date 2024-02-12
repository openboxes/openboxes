import React, { useState } from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';

import DateFieldInput from 'components/form-elements/v2/DateFieldInput';
import { DateFormat, TimeFormat } from 'consts/timeFormat';
import InputWrapper from 'wrappers/InputWrapper';

import './style.scss';

const DateField = ({
  title,
  required,
  tooltip,
  disabled,
  errorMessage,
  placeholder,
  button,
  className,
  defaultValue,
  ...fieldProps
}) => {
  const [date, setDate] = useState(defaultValue);

  const onChange = (pickedDate) => setDate(moment(pickedDate, DateFormat.MM_DD_YYYY));

  const onClear = () => setDate(null);

  return (
    <InputWrapper
      title={title}
      required={required}
      tooltip={tooltip}
      errorMessage={errorMessage}
      button={button}
    >
      <DatePicker
        customInput={<DateFieldInput onClear={onClear} />}
        className={`form-element-input ${errorMessage ? 'has-errors' : ''} ${className}`}
        dropdownMode="scroll"
        onChange={onChange}
        dateFormat={DateFormat.MM_DD_YYYY}
        timeFormat={TimeFormat.HH_MM}
        disabled={disabled}
        selected={date}
        timeIntervals={15}
        yearDropdownItemNumber={3}
        showYearDropdown
        scrollableYearDropdown
        utcOffset={0}
        placeholderText={placeholder}
        {...fieldProps}
      />
    </InputWrapper>
  );
};

export default DateField;

DateField.propTypes = {
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
  className: PropTypes.string,
  defaultValue: PropTypes.string,
};

DateField.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
  disabled: false,
  placeholder: '',
  className: '',
  defaultValue: null,
};