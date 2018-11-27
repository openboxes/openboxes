/* eslint-disable react/prop-types */
import React from 'react';
import DatePicker from 'react-datepicker';
import { Portal } from 'react-overlays';
import moment from 'moment';

import 'react-datepicker/dist/react-datepicker.css';

import BaseField from './BaseField';

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
    value, dateFormat = 'MM/DD/YYYY', timeFormat = 'HH:mm', className = '',
    arrowLeft, arrowUp, arrowRight, arrowDown, ...attributes
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
          className={`form-control form-control-xs ${className}`}
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
          onKeyDown={(event) => {
            switch (event.keyCode) {
              case 37: /* arrow left */
                if (arrowLeft) {
                  arrowLeft();
                  event.preventDefault();
                }
                break;
              case 38: /* arrow up */
                if (arrowUp) {
                  arrowUp();
                  event.preventDefault();
                }
                break;
              case 39: /* arrow right */
                if (arrowRight) {
                  arrowRight();
                  event.preventDefault();
                }
                break;
              case 40: /* arrow down */
                if (arrowDown) {
                  arrowDown();
                  event.preventDefault();
                }
                break;
              default:
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
