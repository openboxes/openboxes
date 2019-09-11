/* eslint-disable react/prop-types */
import React, { Component } from 'react';
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

class DateField extends Component {
  constructor(props) {
    super(props);

    this.dateInput = null;
    this.renderInput = this.renderInput.bind(this);
  }

  renderInput({
    value, dateFormat = 'MM/DD/YYYY', timeFormat = 'HH:mm', className = '',
    arrowLeft, arrowUp, arrowRight, arrowDown, fieldRef, onTabPress, ...attributes
  }) {
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
          highlightDates={[!moment(value, dateFormat).isValid() ?
            moment(new Date(), dateFormat) : {}]}
          onChange={date => onChange(date)}
          onChangeRaw={onChangeRaw}
          onSelect={() => {
            this.dateInput.setOpen(false);
          }}
          popperContainer={CalendarContainer}
          onKeyPress={(event) => {
            if (event.which === 13 /* Enter */) {
              event.preventDefault();
            }
          }}
          onKeyDown={(event) => {
            switch (event.keyCode) {
              case 37: /* arrow left */
                if (arrowLeft && arrowLeft()) {
                  event.preventDefault();
                  this.dateInput.cancelFocusInput();
                  this.dateInput.setOpen(false);
                }
                break;
              case 38: /* arrow up */
                if (arrowUp && arrowUp()) {
                  event.preventDefault();
                  this.dateInput.cancelFocusInput();
                  this.dateInput.setOpen(false);
                }
                break;
              case 39: /* arrow right */
                if (arrowRight && arrowRight()) {
                  event.preventDefault();
                  this.dateInput.cancelFocusInput();
                  this.dateInput.setOpen(false);
                }
                break;
              case 40: /* arrow down */
                if (arrowDown && arrowDown()) {
                  event.preventDefault();
                  this.dateInput.cancelFocusInput();
                  this.dateInput.setOpen(false);
                }
                break;
              case 9: /* Tab key */
                if (onTabPress) {
                  onTabPress(event);
                }
                break;
              default:
            }
          }}
          disabledKeyboardNavigation
          popperClassName="force-on-top"
          showYearDropdown
          scrollableYearDropdown
          dateFormat={dateFormat}
          timeFormat={timeFormat}
          timeIntervals={15}
          yearDropdownItemNumber={3}
          utcOffset={0}
          ref={(el) => {
            this.dateInput = el;
            if (el && fieldRef) {
              fieldRef(el.input);
            }
          }}
        />
      </div>
    );
  }

  render() {
    return (
      <BaseField
        {...this.props}
        renderInput={this.renderInput}
      />
    );
  }
}

export default DateField;
