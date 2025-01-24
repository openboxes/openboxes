import React from 'react';

import PropTypes from 'prop-types';
import { Field } from 'react-final-form';
import { Tooltip } from 'react-tippy';

import Translate from 'utils/Translate';

import 'react-tippy/dist/tippy.css';

const LabelField = (props) => {
  const {
    fieldName, arrayField, fieldValue,
    fieldConfig: {
      attributes, getDynamicAttr, label: FieldLabel, defaultMessage,
    },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };
  const {
    formatValue, color, tooltipValue, showValueTooltip, url, numberField, ...otherAttr
  } = attr;
  const className = `text-truncate ${otherAttr.className ? otherAttr.className : ''} ${numberField ? 'text-right mr-2' : ''}`;

  if (otherAttr.cellClassName) {
    delete otherAttr.cellClassName;
  }

  const renderField = ({ input: { value } }) => (
    <div className="form-group my-0" data-testid="label-field" aria-label={defaultMessage}>
      <div className="row">
        {
            typeof FieldLabel === 'string'
              ? (
                <label htmlFor={attr.id} className="col-md-2 col-form-label col-form-label-xs text-right">
                  <Translate id={FieldLabel} defaultMessage={defaultMessage} />
                </label>
              )
              : <FieldLabel />
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
    const formattedValue = formatValue ? formatValue(fieldValue) : fieldValue;
    return (
      <div className="form-group my-0" data-testid="label-field" aria-label={defaultMessage}>
        <Tooltip
          html={tooltipValue || (<div className="text-truncate">{formattedValue}</div>)}
          disabled={!showValueTooltip}
          theme="dark"
          delay="150"
          duration="250"
          hideDelay="50"
          arrow
        >
          {url
            ? (
              <div {...otherAttr} className={`font-size-xs ${className}`}>
                <a
                  href={url}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>{formattedValue}</span>
                </a>
              </div>
            )
            : (
              <div style={{ color }} {...otherAttr} className={`font-size-xs ${className}`}>
                {formattedValue}
              </div>
            )}
        </Tooltip>
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
  numberField: PropTypes.bool,
};

LabelField.defaultProps = {
  arrayField: false,
  fieldValue: null,
  numberField: false,
};
