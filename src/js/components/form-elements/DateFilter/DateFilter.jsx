import React, { useState } from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';

import BaseField from 'components/form-elements/BaseField';
import Translate from 'utils/Translate';

import 'react-datepicker/dist/react-datepicker.css';
import 'components/form-elements/DateFilter/DateFilter.scss';

const CustomInput = React.forwardRef((props, ref) => {
  const {
    onClick, title, value, placeholder, onClear, defaultMessage,
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
        <span>{value || placeholder}</span>
      </span>
      <div className="date-picker__icon-wrapper">
        {
          value
            ? <button className="date-picker__icon" onClick={onClear}><RiCloseLine /></button>
            : <div className="date-picker__icon"><RiCalendarLine /></div>
        }
      </div>
    </div>
  );
});

const DateFilter = (props) => {
  const {
    value, onChange, dateFormat, placeholder, label, timeFormat, defaultMessage,
  } = props;
  const [isFocused, setIsFocused] = useState(false);

  const onChangeHandler = date => onChange(date.format(dateFormat));

  const onClear = (e) => {
    e.stopPropagation();
    onChange(null);
  };

  const onBlur = () => setIsFocused(false);

  const onFocus = () => setIsFocused(true);

  const isFocusedClass = isFocused ? 'date-picker__wrapper--focused' : '';
  const isValidClass = value ? 'date-picker__wrapper--valid' : '';

  const selectedDate = value ? moment(value, dateFormat) : null;
  const highlightedDates = [selectedDate || moment(new Date(), dateFormat)];

  return (
    <div className={`date-picker__wrapper ${isFocusedClass} ${isValidClass}`}>
      <DatePicker
        {...props}
        customInput={<CustomInput onClear={onClear} defaultMessage={defaultMessage} />}
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

const DateFilterBaseInput = props => (
  <BaseField
    {...props}
    renderInput={DateFilter}
  />);

DateFilter.defaultProps = {
  onChange: undefined,
  label: '',
  defaultMessage: '',
  placeholder: '',
  dateFormat: 'MM/DD/YYYY',
  timeFormat: 'HH:mm',
  value: null,
};

DateFilter.propTypes = {
  onChange: PropTypes.func,
  value: PropTypes.string,
  label: PropTypes.string,
  defaultMessage: PropTypes.string,
  placeholder: PropTypes.string,
  dateFormat: PropTypes.string,
  timeFormat: PropTypes.string,
};

export default DateFilterBaseInput;
