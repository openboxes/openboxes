import React from 'react';

import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Tooltip } from 'react-tippy';

import Translate, { translateWithDefaultMessage } from 'utils/Translate';

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

export const renderFormFields = ({
  renderInput,
  attributes: {
    required, hidden, showError, subtext, tooltip, injectionData, trigger = 'click', ...otherAttributes
  },
  label: FieldLabel,
  defaultMessage,
  touched: fieldTouched,
  arrayField,
  input,
  translate,
  meta: { touched, error },
}) => {
  const attr = { id: input.name, ...otherAttributes };
  const { filterElement, className: supplementClass, ...otherAttr } = attr;
  const filterElementClass = filterElement ? 'filter-group' : 'mb-0 mx-1 form-group';
  const requiredClass = required ? 'required' : '';
  const hiddenClass = hidden ? 'd-none' : '';
  const hasErrorClass = (touched || fieldTouched || showError) && error ? 'has-error' : '';
  const className = [supplementClass, filterElementClass, requiredClass, hiddenClass, hasErrorClass].join(' ');

  if (arrayField) {
    return (
      <Tooltip
        title={translate(`${error}`)}
        disabled={!error || !(touched || fieldTouched)}
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
      >
        <div className={className}>
          {renderInput(input, otherAttr)}
        </div>
      </Tooltip>
    );
  }

  return (
    <div className={`${!filterElement ? 'mt-2' : ''} ${className}`}>
      <div className={`${filterElement ? 'd-flex flex-wrap flex-1' : 'row'}`}>
        {
          typeof FieldLabel === 'string' ?
            <label htmlFor={otherAttr.id} className={`${!filterElement ? 'col-md-2 col-7 col-form-label col-form-label-xs text-center  text-md-right' : ''}`}>
              {FieldLabel && <Translate id={FieldLabel} defaultMessage={defaultMessage} />}
              {otherAttr.withTooltip &&
                <Tooltip
                  interactive="true"
                  arrow="true"
                  trigger={trigger}
                  hideOnClick="true"
                  html={injectionData
                    ? ReactHtmlParser(translate(tooltip, tooltip, injectionData))
                    : translate(tooltip, tooltip)
                  }
                >
                  &nbsp;
                  <i className="fa fa-question-circle-o text-primary" aria-hidden="true" />
                </Tooltip>
              }
            </label>
            :
            <FieldLabel />
        }
        <div className={`form-element-container ${!filterElement ? 'col-md-4 col-7' : 'flex-1 filter-element-container'}`}>
          {renderInput(input, otherAttr)}
        </div>
      </div>
      <div className="row">
        <div className="col-md-2 hidden" />
        <div className="help-block" style={{ float: 'left' }}>
          {
            subtext && (<div>{translate(subtext)}</div>)
          }
          {
            (error && (touched || fieldTouched || showError)) && (<div>{translate(`${error}`)}</div>)
          }
        </div>
      </div>
    </div>
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export const renderField = connect(mapStateToProps)(renderFormFields);

renderFormFields.propTypes = {
  renderInput: PropTypes.func.isRequired,
  attributes: PropTypes.shape({}).isRequired,
  label: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func,
  ]),
  defaultMessage: PropTypes.string,
  touched: PropTypes.bool,
  arrayField: PropTypes.bool,
  input: PropTypes.shape({}).isRequired,
  meta: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
};

renderFormFields.defaultProps = {
  touched: false,
  arrayField: false,
  label: '',
  defaultMessage: '',
};
