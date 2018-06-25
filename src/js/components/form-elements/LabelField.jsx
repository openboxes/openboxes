import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';

const LabelField = (props) => {
  const {
    fieldName, arrayField, fieldValue,
    fieldConfig: { attributes, getDynamicAttr, label: FieldLabel },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };
  const { formatValue, ...otherAttr } = attr;
  const className = `text-truncate ${otherAttr.className ? otherAttr.className : ''}`;

  const renderField = ({ input: { value } }) => (
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

  renderField.propTypes = {
    input: PropTypes.shape({}).isRequired,
  };

  if (arrayField) {
    return (
      <div className="form-group my-0">
        <div {...otherAttr} className={className}>
          {formatValue ? formatValue(fieldValue) : fieldValue}
        </div>
      </div>
    );
  }

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
  fieldValue: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
};

LabelField.defaultProps = {
  arrayField: false,
  fieldValue: null,
};
