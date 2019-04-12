import React from 'react';

import BaseField from './BaseField';
import Select from '../../utils/Select';

const SelectField = (props) => {
  // eslint-disable-next-line react/prop-types
  const renderInput = ({ className, ...attributes }) => (
    <Select
      name={attributes.id}
      {...attributes}
      className={`select-xs ${className}`}
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
