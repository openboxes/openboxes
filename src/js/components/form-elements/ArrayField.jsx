import React from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'react-final-form-arrays';

import FieldArrayComponent from './FieldArrayComponent';

const ArrayField = (props) => {
  const { fieldName, fieldConfig, ...otherProps } = props;

  return (
    <FieldArray
      key={fieldName}
      name={fieldName}
      component={FieldArrayComponent}
      fieldsConfig={fieldConfig}
      properties={otherProps}
    />
  );
};

export default ArrayField;

ArrayField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
    fields: PropTypes.shape({}),
  }).isRequired,
};
