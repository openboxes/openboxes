import React from 'react';
import PropTypes from 'prop-types';

import BaseField from './BaseField';

const BaseHtmlField = (props) => {
  const renderInput = (input, attr) => {
    const onChange = (event) => {
      const { value } = event.target;

      if (attr.onChange) {
        attr.onChange(value);
      }

      input.onChange(value);
    };
    const attributes = { ...attr, value: input.value, onChange };

    return props.renderInput(attributes);
  };

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default BaseHtmlField;

BaseHtmlField.propTypes = {
  renderInput: PropTypes.func.isRequired,
};
