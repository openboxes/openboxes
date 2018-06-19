import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';

const LabelField = (props) => {
  const {
    fieldName, arrayField,
    fieldConfig: { attributes, getDynamicAttr, label: FieldLabel },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };
  const { formatValue, ...otherAttr } = attr;

  const renderField = ({ input: { value } }) => {
    if (arrayField) {
      return (
        <div className="form-group my-0">
          <span {...otherAttr}>{formatValue ? formatValue(value) : value}</span>
        </div>
      );
    }

    return (
      <div className="form-group my-0">
        <div className="row">
          {
            typeof FieldLabel === 'string' ?
              <label htmlFor={attr.id} className="col-md-2 col-form-label text-right">{ FieldLabel }</label> :
              <FieldLabel />
          }
          <div className="col-md-4 align-self-center">
            <span {...otherAttr}>{formatValue ? formatValue(value) : value}</span>
          </div>
        </div>
      </div>
    );
  };

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
  arrayField: PropTypes.bool,
};

LabelField.defaultProps = {
  arrayField: false,
};
