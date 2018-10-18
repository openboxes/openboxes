import React from 'react';
import DatePicker from 'react-datepicker';
import { Portal } from 'react-overlays';

import 'react-datepicker/dist/react-datepicker.css';

import BaseField from './BaseField';

// eslint-disable-next-line react/prop-types
const CalendarContainer = ({ children }) => {
  const el = document.getElementById('root');

  return (
    <Portal container={el}>
      {children}
    </Portal>
  );
};

const DateField = (props) => {
  const renderInput = (attributes) => {
    const onChange = (date) => {
      const val = !date || typeof date === 'string' ? date : date.format(attributes.dateFormat);
      attributes.onChange(val);
    };

    const onChangeRaw = (e) => {
      attributes.onChange(e.target.value);
    };

    return (
      <div className="date-field">
        <DatePicker
          className="form-control"
          {...attributes}
          onChange={date => onChange(date)}
          onChangeRaw={onChangeRaw}
          popperContainer={CalendarContainer}
          popperClassName="force-on-top"
          showYearDropdown
          scrollableYearDropdown
          timeFormat="HH:mm"
          timeIntervals={15}
          yearDropdownItemNumber={3}
        />
      </div>
    );
  };

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default DateField;
