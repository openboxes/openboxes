import React from 'react';
import DateTime from 'react-datetime';

import 'react-datetime/css/react-datetime.css';

import BaseField from './BaseField';

const DateField = (props) => {
  const renderInput = (attributes) => {
    const onChange = (date) => {
      const val = !date || typeof date === 'string' ? date : date.format(attributes.dateFormat);
      attributes.onChange(val);
    };

    return (
      <div className="date-field">
        <DateTime
          timeFormat={false}
          closeOnSelect
          {...attributes}
          onChange={date => onChange(date)}
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
