import React from 'react';

import BaseField from 'components/form-elements/BaseField';
import ColorPicker from 'utils/ColorPicker';

const ColorPickerField = (props) => {
  const renderInput = attributes => (
    <ColorPicker
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

export default ColorPickerField;
