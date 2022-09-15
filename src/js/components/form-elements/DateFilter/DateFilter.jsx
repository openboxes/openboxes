/* eslint-disable react/prop-types */
import React, { useState } from 'react';

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
          props.value
            ? <button className="date-picker__icon" onClick={onClear}><RiCloseLine /></button>
            : <div className="date-picker__icon"><RiCalendarLine /></div>
        }
      </div>
    </button>
  );
});


const DateFilter = (props) => {
  const [isFocused, setIsFocused] = useState(false);
  const dateFormat = 'MM/DD/YYYY';
  const timeFormat = 'HH:mm';

  const onChange = (date) => {
    const val = !date || typeof date === 'string' ? date : date.format(dateFormat);
    props.onChange(val);
  };

  const onClear = (e) => {
    e.stopPropagation();
    onChange(null);
  };

  const onBlur = () => setIsFocused(false);

  const onFocus = () => setIsFocused(true);

  const isFocusedClass = isFocused ? 'date-picker__wrapper--focused' : '';
  const isValidClass = props.value ? 'date-picker__wrapper--valid' : '';

  return (
    <div className={`date-picker__wrapper ${isFocusedClass} ${isValidClass}`}>
      <DatePicker
        {...props}
        customInput={<CustomInput onClear={onClear} />}
        className="date-picker__input"
        placeholderText={props.placeholder}
        title={props.label}
        onChange={onChange}
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

export default DateFilter;
