import React from 'react';

import BaseField from './BaseField';
import Checkbox from '../../utils/Checkbox';

const CheckboxField = (props) => {
  // eslint-disable-next-line react/prop-types
  const renderInput = ({ value, ...attributes }) => (
    <Checkbox
      {...attributes}
      value={value || false}
    />);

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default CheckboxField;
