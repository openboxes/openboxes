import React from 'react';
import DatePicker from 'react-datepicker';
import { Portal } from 'react-overlays';
import moment from 'moment';

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
  const renderInput = ({
    // eslint-disable-next-line react/prop-types
    value, dateFormat = 'MM/DD/YYYY', timeFormat = 'HH:mm', ...attributes
  }) => {
    const onChange = (date) => {
      const val = !date || typeof date === 'string' ? date : date.format(dateFormat);
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
          selected={moment(value, dateFormat).isValid() ? moment(value, dateFormat) : null}
          onChange={date => onChange(date)}
          onChangeRaw={onChangeRaw}
          popperContainer={CalendarContainer}
          onKeyPress={(event) => {
            if (event.which === 13 /* Enter */) {
              event.preventDefault();
            }
          }}
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

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default DateField;
