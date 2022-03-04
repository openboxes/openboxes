import React from 'react';

import BaseField from 'components/form-elements/BaseField';
import Textarea from 'utils/Textarea';

const TextareaField = (props) => {
  const renderInput = attributes => (
    <Textarea
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

export default TextareaField;
