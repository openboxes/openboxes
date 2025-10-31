import React, { useRef } from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import DateFieldInput from 'components/form-elements/v2/DateFieldInput';
import componentType from 'consts/componentType';
import { DateFormat, TimeFormat } from 'consts/timeFormat';
import useFocusOnMatch from 'hooks/useFocusOnMatch';
import useTranslate from 'hooks/useTranslate';
import InputWrapper from 'wrappers/InputWrapper';
import RootPortalWrapper from 'wrappers/RootPortalWrapper';

import 'react-datepicker/dist/react-datepicker.css';
import 'components/form-elements/DateFilter/DateFilter.scss';
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
  value,
  onChange,
  showTimeSelect,
  hideErrorMessageWrapper,
  customDateFormat,
  onChangeRaw,
  clearable,
  wrapperClassName,
  focusProps = {},
  ...fieldProps
}) => {
  const translate = useTranslate();
  const onClear = () => onChange(null);
  const onChangeHandler = (date) => {
    if (showTimeSelect) {
      onChange(date?.format(DateFormat.MMM_DD_YYYY_HH_MM_SS));
      return;
    }
    onChange(date?.format(DateFormat.MMM_DD_YYYY));
  };
  const locale = useSelector(getCurrentLocale);

  const formatDate = (dateToFormat) => {
    if (!dateToFormat) {
      return null;
    }

    const format = showTimeSelect
      ? DateFormat.MMM_DD_YYYY_HH_MM_SS
      : DateFormat.MMM_DD_YYYY;

    // The locale has to be lower cased, because moment.js accepts arguments like: 'es-mx', not
    // 'es-MX' we can't just return null if the locale is not already loaded, because the date needs
    // to have value, so we default to 'en' in that case
    const language = (locale || 'en').toLowerCase();
    // If the date is not valid in the given format, we try to parse it without format
    const date = moment(dateToFormat, format, language, true);
    if (date.isValid()) {
      return date;
    }
    // Fallback to default parsing
    return moment(dateToFormat);
  };

  const selectedDate = formatDate(value);
  const highlightedDates = [selectedDate || formatDate(new Date())];

  const placeholderText = typeof placeholder === 'object'
    ? translate(placeholder?.id, placeholder?.default)
    : placeholder;

  const datePickerRef = useRef(null);

  const getDateFormat = () => {
    if (showTimeSelect) {
      return customDateFormat ? `${customDateFormat} HH:mm:ss` : DateFormat.MMM_DD_YYYY_HH_MM_SS;
    }
    return customDateFormat || DateFormat.MMM_DD_YYYY;
  };

  useFocusOnMatch({ ...focusProps, ref: datePickerRef, type: componentType.DATE_FIELD });

  return (
    <InputWrapper
      title={title}
      required={required}
      tooltip={tooltip}
      errorMessage={errorMessage}
      button={button}
      hideErrorMessageWrapper={hideErrorMessageWrapper}
      className={wrapperClassName}
    >
      <DatePicker
        {...fieldProps}
        // Temporary workaround: using 'ar' locale causes the app to crash when selecting a date.
        // Fallback to 'en' to avoid the crash
        locale={locale === 'ar' ? 'en' : locale}
        showTimeSelect={showTimeSelect}
        customInput={<DateFieldInput onClear={onClear} clearable={clearable} />}
        className={`form-element-input ${errorMessage ? 'has-errors' : ''} ${className}`}
        dropdownMode="scroll"
        dateFormat={getDateFormat()}
        timeFormat={TimeFormat.HH_MM}
        disabled={disabled}
        timeIntervals={15}
        yearDropdownItemNumber={3}
        showYearDropdown
        scrollableYearDropdown
        disabledKeyboardNavigation
        utcOffset={0}
        placeholderText={placeholderText}
        popperContainer={RootPortalWrapper}
        selected={selectedDate}
        highlightDates={highlightedDates}
        onChange={onChangeRaw || onChangeHandler}
        onSelect={() => {
          // Close date picker on select - this is somewhat a workaround to close the datepicker
          // when using showTimeSelect, when we are expecting to close the datepicker
          // after picking the date without picking the time.
          datePickerRef.current?.setOpen(false);
        }}
        ref={(el) => {
          datePickerRef.current = el;
        }}
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
  placeholder: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      defaultMessage: PropTypes.string.isRequired,
    }),
  ]),
  className: PropTypes.string,
  value: PropTypes.string,
  onChange: PropTypes.func,
  showTimeSelect: PropTypes.bool,
  hideErrorMessageWrapper: PropTypes.bool,
  customDateFormat: PropTypes.string,
  focusProps: PropTypes.shape({
    fieldIndex: PropTypes.string,
    fieldId: PropTypes.string,
    rowIndex: PropTypes.string,
    columnId: PropTypes.string,
  }),
  onChangeRaw: PropTypes.func,
  clearable: PropTypes.bool,
  wrapperClassName: PropTypes.string,
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
  wrapperClassName: '',
  value: null,
  onChange: () => {},
  showTimeSelect: false,
  hideErrorMessageWrapper: false,
  customDateFormat: null,
  focusProps: {},
  onChangeRaw: null,
  clearable: true,
};
