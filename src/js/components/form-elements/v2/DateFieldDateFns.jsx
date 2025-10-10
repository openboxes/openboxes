import React, { useRef } from 'react';

import { format, parse } from 'date-fns';
import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker-6';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import DateFieldInput from 'components/form-elements/v2/DateFieldInput';
import componentType from 'consts/componentType';
import { DateFormatDateFns, TimeFormat } from 'consts/timeFormat';
import useFocusOnMatch from 'hooks/useFocusOnMatch';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';
import InputWrapper from 'wrappers/InputWrapper';
import RootPortalWrapper from 'wrappers/RootPortalWrapper';

import 'components/form-elements/DateFilter/DateFilter.scss';
import 'components/form-elements/v2/style.scss';
import 'react-datepicker/dist/react-datepicker.css';

const DateFieldDateFns = ({
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
  customTooltip,
  ...fieldProps
}) => {
  const translate = useTranslate();
  const onClear = async () => {
    onChange(null);
  };

  const { locale: currentLocale } = useSelector((state) => ({
    locale: getCurrentLocale(state),
  }));

  const getDateFormat = () => {
    if (showTimeSelect) {
      return customDateFormat
        ? `${customDateFormat} ${TimeFormat.HH_MM_SS}`
        : DateFormatDateFns.MMM_DD_YYYY_HH_MM_SS;
    }
    return customDateFormat || DateFormatDateFns.MMM_DD_YYYY;
  };

  const formatDate = (dateToFormat) => {
    if (!dateToFormat) {
      return null;
    }

    try {
      return parse(
        dateToFormat,
        getDateFormat(),
        new Date(),
      );
    } catch (e) {
      return null;
    }
  };

  const selectedDate = formatDate(value);
  const highlightedDates = [selectedDate || new Date()];

  const placeholderText = typeof placeholder === 'object'
    ? translate(placeholder?.id, placeholder?.defaultMessage)
    : placeholder;

  const datePickerRef = useRef(null);

  const onChangeHandler = async (date) => {
    if (!date) {
      onChange(null);
      return;
    }

    const formatted = format(date, getDateFormat());
    onChange(formatted);
  };

  const dateFnsLocale = () => {
    // Temporary workaround: using 'ar' locale causes the app to crash when selecting a date.
    // Fallback to 'en' to avoid the crash
    if (!currentLocale || ['en', 'ar'].includes(currentLocale)) {
      return locales.enUS;
    }

    return locales[currentLocale];
  };

  useFocusOnMatch({
    ...focusProps,
    ref: datePickerRef,
    type: componentType.DATE_FIELD,
  });

  return (
    <InputWrapper
      title={title}
      required={required}
      tooltip={tooltip}
      errorMessage={errorMessage}
      button={button}
      hideErrorMessageWrapper={hideErrorMessageWrapper}
      className={wrapperClassName}
      customTooltip={customTooltip}
      value={formatDateToString({
        date: value,
        dateFormat: DateFormatDateFns.DD_MMM_YYYY,
        options: { locale: dateFnsLocale() },
      })}
    >
      <DatePicker
        {...fieldProps}
        // ariaLiveMessages is a new prop for displaying a message above the date field.
        // By passing an empty object, we are hiding that message.
        ariaLiveMessages={{}}
        locale={dateFnsLocale()}
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
        placeholderText={placeholderText}
        popperContainer={RootPortalWrapper}
        popperPlacement="bottom-start"
        selected={selectedDate}
        highlightDates={highlightedDates}
        onChange={onChangeRaw || onChangeHandler}
        onSelect={() => {
          datePickerRef.current?.setOpen(false);
        }}
        ref={(el) => {
          datePickerRef.current = el;
        }}
      />
    </InputWrapper>
  );
};

export default DateFieldDateFns;

DateFieldDateFns.propTypes = {
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
  customTooltip: PropTypes.bool,
};

DateFieldDateFns.defaultProps = {
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
  customTooltip: false,
};
