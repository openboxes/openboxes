import React from 'react';
import DateTime from 'react-datetime';

import 'react-datetime/css/react-datetime.css';

import BaseReactField from './BaseReactField';

const DateField = (props) => {
  const renderInput = (attributes) => {
    const onChange = (date) => {
      const val = !date || typeof date === 'string' ? date : date.format(attributes.dateFormat);
      attributes.onChange(val);
    };

    return (
      <div className="col-md-4">
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
    <BaseReactField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default DateField;
