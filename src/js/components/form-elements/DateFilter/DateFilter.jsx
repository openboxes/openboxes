import React, { useState } from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import BaseField from 'components/form-elements/BaseField';
import DateFormat from 'consts/dateFormat';
import Translate from 'utils/Translate';
import { formatDate, getLocaleCode } from 'utils/translation-utils';

import 'react-datepicker/dist/react-datepicker.css';
import 'components/form-elements/DateFilter/DateFilter.scss';

const CustomInput = React.forwardRef((props, ref) => {
  const {
    onClick,
    title,
    value,
    placeholder,
    onClear,
    defaultMessage,
    formatDateToDisplay,
  } = props;

  const onKeypressHandler = (event) => {
    if (event.key === 'Enter') onClick();
  };

  return (
    <div
      ref={ref}
      tabIndex="0"
      role="button"
      className="d-flex flex-row date-picker__input-wrapper"
      onClick={onClick}
      onKeyDown={onKeypressHandler}
    >
      <span className="flex-grow-1 date-picker__input">
        <Translate id={title} defaultMessage={defaultMessage} />
        <span>{formatDateToDisplay(value) || placeholder}</span>
      </span>
      <div className="date-picker__icon-wrapper">
        {
          value
            ? (
              <button aria-label="Pick date" type="button" className="date-picker__icon" onClick={onClear}>
                <RiCloseLine />
              </button>
            )
            : <div className="date-picker__icon"><RiCalendarLine /></div>
        }
      </div>
    </div>
  );
});

const DateFilter = (props) => {
  const {
    value, onChange, dateFormat, placeholder, label, timeFormat, defaultMessage,
    localizeDate, localizedDateFormat,
  } = props;
  const { localeCode, formatLocalizedDate } = useSelector((state) => ({
    localeCode: getLocaleCode(state.localize),
    formatLocalizedDate: formatDate(state.localize),
  }));
  const [isFocused, setIsFocused] = useState(false);
  const onChangeHandler = (date) => onChange(date.format(dateFormat));

  const onClear = (e) => {
    e.stopPropagation();
    onChange(null);
  };

  const formatDateToDisplay = (date) => {
    if (!date) {
      return null;
    }

    if (localizeDate && localizedDateFormat) {
      return formatLocalizedDate(date, localizedDateFormat);
    }

    return moment(date).format(dateFormat);
  };

  const onBlur = () => setIsFocused(false);

  const onFocus = () => setIsFocused(true);

  const isFocusedClass = isFocused ? 'date-picker__wrapper--focused' : '';
  const isValidClass = value ? 'date-picker__wrapper--valid' : '';

  const selectedDate = value ? moment(value, dateFormat) : null;
  const highlightedDates = [selectedDate || moment(new Date(), dateFormat)];

  const localeCodeToDisplay = localizeDate ? localeCode : null;
  return (
    <div className={`date-picker__wrapper ${isFocusedClass} ${isValidClass}`} data-testid="date-filter">
      <DatePicker
        {...props}
        customInput={(
          <CustomInput
            formatDateToDisplay={formatDateToDisplay}
            onClear={onClear}
            defaultMessage={defaultMessage}
          />
        )}
        className="date-picker__input"
        placeholderText={placeholder}
        title={label}
        highlightDates={highlightedDates}
        selected={selectedDate}
        onChange={onChangeHandler}
        onInputClick={onFocus}
        onSelect={onBlur}
        onClickOutside={onBlur}
        disabledKeyboardNavigation
        popperClassName="force-on-top"
        showYearDropdown
        locale={localeCodeToDisplay}
        scrollableYearDropdown
        dateFormat={dateFormat}
        timeFormat={timeFormat}
        timeIntervals={15}
        yearDropdownItemNumber={3}
        utcOffset={0}
      />
    </div>
  );
};

const DateFilterBaseInput = (props) => (
  <BaseField
    {...props}
    renderInput={DateFilter}
  />
);

DateFilter.defaultProps = {
  onChange: undefined,
  label: '',
  defaultMessage: '',
  placeholder: '',
  dateFormat: 'MM/DD/YYYY',
  timeFormat: 'HH:mm',
  value: null,
  localizeDate: false,
  localizedDateFormat: DateFormat.DEFAULT,
};

DateFilter.propTypes = {
  onChange: PropTypes.func,
  value: PropTypes.string,
  label: PropTypes.string,
  defaultMessage: PropTypes.string,
  placeholder: PropTypes.string,
  dateFormat: PropTypes.string,
  timeFormat: PropTypes.string,
  localizeDate: PropTypes.bool,
  localizedDateFormat: PropTypes.string,
};

export default DateFilterBaseInput;
