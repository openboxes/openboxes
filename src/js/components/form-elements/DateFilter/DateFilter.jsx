import React, { useState } from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';

import 'react-datepicker/dist/react-datepicker.css';
import 'components/form-elements/DateFilter/DateFilter.scss';

const CustomInput = React.forwardRef((props, ref) => {
  const {
    onClick, title, value, placeholder, onClear,
  } = props;
  return (
    <button
      ref={ref}
      className="d-flex flex-row date-picker__input-wrapper"
      onClick={onClick}
    >
      <span className="flex-grow-1 date-picker__input">
        <span>{title}</span>
        <span>{value || placeholder}</span>
      </span>
      <div className="date-picker__icon-wrapper">
        {
          value
            ? <button className="date-picker__icon" onClick={onClear}><RiCloseLine /></button>
            : <div className="date-picker__icon"><RiCalendarLine /></div>
        }
      </div>
    </button>
  );
});


const DateFilter = (props) => {
  const {
    value, onChange, dateFormat, placeholder, label, timeFormat,
  } = props;
  const [isFocused, setIsFocused] = useState(false);

  const onChangeHandler = (date) => {
    const val = !date || typeof date === 'string' ? date : date.format(dateFormat);
    onChange(val);
  };

  const onClear = (e) => {
    e.stopPropagation();
    onChange(null);
  };

  const onBlur = () => setIsFocused(false);

  const onFocus = () => setIsFocused(true);

  const isFocusedClass = isFocused ? 'date-picker__wrapper--focused' : '';
  const isValidClass = value ? 'date-picker__wrapper--valid' : '';

  const selectedDates = [value ? moment(value, dateFormat) : moment(new Date(), dateFormat)];

  return (
    <div className={`date-picker__wrapper ${isFocusedClass} ${isValidClass}`}>
      <DatePicker
        {...props}
        customInput={<CustomInput onClear={onClear} />}
        className="date-picker__input"
        placeholderText={placeholder}
        title={label}
        highlightDates={selectedDates}
        onChange={onChangeHandler}
        onInputClick={onFocus}
        onSelect={onBlur}
        onClickOutside={onBlur}
        disabledKeyboardNavigation
        popperClassName="force-on-top"
        showYearDropdown
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

DateFilter.defaultProps = {
  onChange: undefined,
  label: '',
  placeholder: '',
  dateFormat: 'MM/DD/YYYY',
  timeFormat: 'HH:mm',
};

DateFilter.propTypes = {
  onChange: PropTypes.func,
  value: PropTypes.string.isRequired,
  label: PropTypes.string,
  placeholder: PropTypes.string,
  dateFormat: PropTypes.string,
  timeFormat: PropTypes.string,

};

export default DateFilter;
