import _ from 'lodash';
import React from 'react';
import Select, { Async } from 'react-select';
import PropTypes from 'prop-types';

import 'react-select/dist/react-select.css';

import BaseReactField from './BaseReactField';

const SelectField = (props) => {
  const renderInput = ({ objectValue, ...attributes }) => {
    const options = _.map(attributes.options, (value) => {
      if (typeof value === 'string') {
        return { value, label: value };
      } else if (objectValue) {
        return { ...value, value: JSON.stringify(value.value) };
      }

      return value;
    });

    const onChange = (selectValue) => {
      if (attributes.multi) {
        const values = _.map(selectValue, val => (objectValue ? JSON.parse(val.value) : val.value));
        attributes.onChange(values);
      } else if (selectValue !== null && selectValue !== undefined) {
        attributes.onChange(objectValue ? JSON.parse(selectValue.value) : selectValue.value);
      } else {
        attributes.onChange(null);
      }
    };

    const delimiter = attributes.delimiter || ';';
    let { value } = attributes;

    if (objectValue) {
      value = attributes.multi ? _.map(attributes.value, val => JSON.stringify(val))
        : JSON.stringify(attributes.value);
    }

    renderInput.propTypes = {
      objectValue: PropTypes.bool,
    };

    renderInput.defaultProps = {
      objectValue: false,
    };

    const SelectType = attributes.async ? Async : Select;

    return (
      <SelectType
        name={attributes.id}
        {...attributes}
        options={options}
        delimiter={delimiter}
        value={attributes.multi ? _.join(value, delimiter) : value}
        onChange={onChange}
      />
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
