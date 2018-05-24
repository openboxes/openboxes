import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';

const LabelField = (props) => {
  const { fieldName, fieldConfig: { attributes, getDynamicAttr } } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };

  const renderField = ({ input: { value } }) => (
    <div className="form-group">
      <span {...attr}>{value}</span>
    </div>
  );

  renderField.propTypes = {
    input: PropTypes.shape({}).isRequired,
  };

  return (
    <Field
      name={fieldName}
      component={renderField}
    />
  );
};

export default LabelField;

LabelField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
