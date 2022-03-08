import React from 'react';

import BaseField from 'components/form-elements/BaseField';
import Input from 'utils/Input';


const TextField = (props) => {
  const renderInput = attributes => (
    <Input
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
