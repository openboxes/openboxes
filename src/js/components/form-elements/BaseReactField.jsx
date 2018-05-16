import React from 'react';
import PropTypes from 'prop-types';

import BaseField from './BaseField';

const BaseReactField = (props) => {
  const renderInput = (input, attr) => {
    const onChange = (value) => {
      if (attr.onChange) {
        attr.onChange(value);
      }

      input.onChange(value);
    };
    const attributes = { ...attr, onChange, value: input.value };

    return props.renderInput(attributes);
  };

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default BaseReactField;

BaseReactField.propTypes = {
  renderInput: PropTypes.func.isRequired,
};
