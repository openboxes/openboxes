import _ from 'lodash';
import React from 'react';
import Select from 'react-select';

import 'react-select/dist/react-select.css';

import BaseReactField from './BaseReactField';

const SelectField = (props) => {
  const renderInput = (attributes) => {
    const options = _.map(attributes.options, value => (typeof value === 'string' ? { value, label: value } : value));

    const onChange = (selectValue) => {
      if (attributes.multi) {
        const values = _.map(selectValue, val => val.value);
        attributes.onChange(values);
      } else if (selectValue !== null && selectValue !== undefined) {
        attributes.onChange(selectValue.value);
      } else {
        attributes.onChange(null);
      }
    };

    const delimiter = attributes.delimiter || ';';

    return (
      <div className="col-md-4">
        <Select
          name={attributes.id}
          {...attributes}
          options={options}
          delimiter={delimiter}
          value={attributes.multi ? _.join(attributes.value, delimiter) : attributes.value}
          onChange={onChange}
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

export default SelectField;
