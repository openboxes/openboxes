import React from 'react';
import { Tooltip } from 'react-tippy';
import PropTypes from 'prop-types';
import { Translate } from 'react-localize-redux';

import 'react-tippy/dist/tippy.css';

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
  touched: fieldTouched,
  arrayField,
  input,
  meta: { touched, error },
}) => {
  const attr = { id: input.name, ...otherAttributes };
  const className = `form-group mb-0 ${required ? 'required' : ''} ${hidden ? 'd-none' : ''} ${(touched || fieldTouched) && error ? 'has-error' : ''}`;

  if (arrayField) {
    return (
      <Tooltip
        title={<Translate id={error} />}
        disabled={!error || !(touched || fieldTouched)}
        theme="transparent"
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
      >
        <div className={className}>
          {renderInput(input, attr)}
        </div>
      </Tooltip>
    );
  }

  return (
    <div className={`mt-2 ${className}`}>
      <div className="row">
        {
          typeof FieldLabel === 'string' ?
            <label htmlFor={attr.id} className="col-md-2 col-7 col-form-label col-form-label-xs text-center text-md-right"><Translate id={FieldLabel} /></label> :
            <FieldLabel />
        }
        <div className="col-md-4 col-7">
          {renderInput(input, attr)}
        </div>
      </div>
      <div className="row">
        <div className="col-md-2" />
        <div className="help-block col-md-4" style={{ float: 'left' }}>
          { touched || fieldTouched ? <Translate id={error} /> : '' }
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
  ]),
  touched: PropTypes.bool,
  arrayField: PropTypes.bool,
  input: PropTypes.shape({}).isRequired,
  meta: PropTypes.shape({}).isRequired,
};

renderField.defaultProps = {
  touched: false,
  arrayField: false,
  label: '',
};
