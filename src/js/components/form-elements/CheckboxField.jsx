import React from 'react';

import BaseField from './BaseField';
import Checkbox from '../../utils/Checkbox';

const CheckboxField = (props) => {
  const renderInput = attributes => (
    <Checkbox
      {...attributes}
    />);

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default CheckboxField;
