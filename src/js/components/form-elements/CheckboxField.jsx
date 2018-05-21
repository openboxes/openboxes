import React from 'react';

import BaseField from './BaseField';

const CheckboxField = (props) => {
  const renderInput = (input, attr) => {
    const onChange = (event) => {
      const { checked } = event.target;

      if (attr.onChange) {
        attr.onChange(checked);
      }

      input.onChange(checked);
    };
    const attributes = { ...input, ...attr, onChange };

    return (
      <input
        type="checkbox"
        checked={input.value}
        {...attributes}
      />);
  };

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default CheckboxField;
