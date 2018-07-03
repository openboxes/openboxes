import React from 'react';

import BaseField from './BaseField';
import Input from '../../utils/Input';

const TextField = (props) => {
  const renderInput = attributes => (
    <Input
      className="form-control"
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

export default TextField;
