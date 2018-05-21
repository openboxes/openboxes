import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';

import { renderField } from '../../utils/form-utils';

const BaseField = (props) => {
  const {
    fieldName,
    fieldConfig,
    renderInput,
    touched,
  } = props;
  const dynamicAttr = fieldConfig.getDynamicAttr ? fieldConfig.getDynamicAttr(props) : {};

  return (
    <Field
      name={fieldName}
      component={renderField}
      renderInput={renderInput}
      attributes={{ ...fieldConfig.attributes, ...dynamicAttr }}
      label={fieldConfig.label}
      touched={touched}
    />
  );
};

export default BaseField;

BaseField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  renderInput: PropTypes.func.isRequired,
  touched: PropTypes.bool,
};

BaseField.defaultProps = {
  touched: false,
};
