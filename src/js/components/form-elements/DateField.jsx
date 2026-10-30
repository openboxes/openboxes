/* eslint-disable react/prop-types */
import React, { Component } from 'react';

import { Portal } from '@restart/ui';
import * as locales from 'date-fns/locale';
import moment from 'moment';
import DatePicker from 'react-datepicker';
import { connect } from 'react-redux';
import { getDefaultTranslationsFetched } from 'selectors';

import BaseField from 'components/form-elements/BaseField';
import DateFormat from 'consts/dateFormat';
import { convertMomentFormatToDateFns } from 'utils/dateUtils';
import { formatDate, getDateFormat, getLocaleCode } from 'utils/translation-utils';

import 'react-datepicker/dist/react-datepicker.css';

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
    arrowLeft, arrowUp, arrowRight, arrowDown, fieldRef, onTabPress, localizeDate,
    localizedDateFormat = DateFormat.COMMON, showLocalizedPlaceholder, ...attributes
  }) {
    const onChangeRaw = (e) => {
      // onChangeRaw is also called when a day is clicked in the calendar, and then the event
      // target is not the input, so the selected date is handled by onChange
      if (!(e?.target instanceof HTMLInputElement)) {
        return;
      }
      attributes.onChange(e.target.value);
    };

    const getPlaceholder = () => {
      if (localizeDate && showLocalizedPlaceholder) {
        return this.props.dateFormat(localizedDateFormat);
      }

      return attributes.placeholderText;
    };

    const getFormat = () => {
      if (localizeDate && this.props.defaultTranslationsFetched && this.props.localeCode) {
        return this.props.dateFormat(localizedDateFormat);
      }

      return dateFormat;
    };

    const onChange = (date) => {
      const val = !date || typeof date === 'string' ? date : moment(date).format(dateFormat);
      attributes.onChange(val);
    };

    const getLocale = () => {
      if (localizeDate) {
        return locales[this.props.localeCode];
      }

      return null;
    };

    return (
      <div className="date-field">
        <DatePicker
          className={`form-control form-control-xs ${className}`}
          {...attributes}
          placeholderText={getPlaceholder()}
          selected={moment(value, dateFormat).isValid() ? moment(value, dateFormat).toDate() : null}
          highlightDates={[!moment(value, dateFormat).isValid()
            ? new Date() : {}]}
          onChange={(date) => onChange(date)}
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
          popperPlacement="bottom-start"
          showYearDropdown
          scrollableYearDropdown
          dateFormat={convertMomentFormatToDateFns(getFormat())}
          locale={getLocale()}
          timeFormat={timeFormat}
          timeIntervals={15}
          yearDropdownItemNumber={3}
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

const mapStateToProps = (state) => ({
  formatDate: formatDate(state.localize),
  dateFormat: getDateFormat(state.localize),
  localeCode: getLocaleCode(state.localize),
  defaultTranslationsFetched: getDefaultTranslationsFetched(state),
});

export default connect(mapStateToProps)(DateField);
