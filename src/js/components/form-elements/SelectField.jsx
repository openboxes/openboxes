import React from 'react';

import 'react-select-plus/dist/react-select-plus.css';

import BaseField from './BaseField';
import Select from '../../utils/Select';

const SelectField = (props) => {
  const renderInput = attributes => (
    <Select
      className="col-md-12 col-5"
      name={attributes.id}
      {...attributes}
    />
  );

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default SelectField;
