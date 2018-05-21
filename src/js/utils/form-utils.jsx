import React from 'react';
import PropTypes from 'prop-types';

export const renderFormField = (fieldConfig, fieldName, props = {}) => {
  const FieldType = fieldConfig.type;

  return (
    <FieldType
      key={fieldName}
      fieldName={fieldName}
      fieldConfig={fieldConfig}
      {...props}
    />
  );
};

export const renderField = ({
  renderInput,
  attributes: { required, hidden, ...otherAttributes },
  label: FieldLabel,
  input,
  meta: { touched, error },
}) => {
  const attr = { id: input.name, ...otherAttributes };

  const className = `form-group ${required ? 'required' : ''} ${hidden ? 'd-none' : ''} ${touched && error ? 'has-error' : ''}`;
  return (
    <div className={`padding-left-md padding-right-md ${className}`}>
      <div className="row">
        {
          typeof FieldLabel === 'string' ?
            <label htmlFor={attr.id} className="col-md-2 col-form-label text-right">{ FieldLabel }</label> :
            <FieldLabel />
        }
        {renderInput(input, attr)}
      </div>
      <div className="row">
        <div className="col-md-2" />
        <div className="help-block col-md-4" style={{ float: 'left' }}>
          { touched ? error : '' }
        </div>
      </div>
    </div>
  );
};

renderField.propTypes = {
  renderInput: PropTypes.func.isRequired,
  attributes: PropTypes.shape({}).isRequired,
  label: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func,
  ]).isRequired,
  input: PropTypes.shape({}).isRequired,
  meta: PropTypes.shape({}).isRequired,
};
