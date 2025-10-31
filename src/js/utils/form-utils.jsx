import React from 'react';

import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Tooltip } from 'react-tippy';

import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

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
    required, hidden, showError, subtext, tooltip, injectionData, ariaLabel, trigger = 'click', ...otherAttributes
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
  const fieldAriaLabel = ariaLabel || translate(FieldLabel, defaultMessage);

  if (arrayField) {
    return (
      <Tooltip
        title={error ? translate(`${error}`) : undefined}
        disabled={!error || !(touched || fieldTouched)}
        arrow="true"
        delay="150"
        duration="250"
        hideDelay="50"
      >
        <div className={className} data-testid="form-field" aria-label={fieldAriaLabel}>
          {renderInput(input, otherAttr)}
        </div>
      </Tooltip>
    );
  }

  return (
    <div className={`${!filterElement ? 'mt-2' : ''} ${className}`} data-testid="form-field" aria-label={fieldAriaLabel}>
      <div className={`${filterElement ? 'd-flex flex-wrap flex-1' : 'row'}`}>
        {
          typeof FieldLabel === 'string'
            ? (
              <label htmlFor={otherAttr.id} className={`${!filterElement ? 'col-md-2 col-7 col-form-label col-form-label-xs text-center  text-md-right' : ''}`}>
                {FieldLabel && <Translate id={FieldLabel} defaultMessage={defaultMessage} />}
                {otherAttr.withTooltip
                  && (
                    <Tooltip
                      interactive="true"
                      arrow="true"
                      trigger={trigger}
                      hideOnClick="true"
                      html={injectionData
                        ? ReactHtmlParser(translate(tooltip, tooltip, injectionData))
                        : translate(tooltip, tooltip)}
                    >
                      &nbsp;
                      <i className="fa fa-question-circle-o text-primary" aria-hidden="true" />
                    </Tooltip>
                  )}
              </label>
            )
            : <FieldLabel />
        }
        <div className={`form-element-container ${!filterElement ? 'col-md-4 col-7' : 'flex-1 filter-element-container'}`}>
          <CustomTooltip
            content={translate(attr?.customTooltipLabel || '')}
            show={!!attr.showCustomTooltip}
          >
            {renderInput(input, otherAttr)}
          </CustomTooltip>
        </div>
      </div>
      <div className="row" aria-label="subtext">
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

const mapStateToProps = (state) => ({
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

export const decimalParser = (value, precision) => {
  const valueAsNumber = parseFloat(value);

  if (!Number.isNaN(valueAsNumber) && _.isNumber(precision)) {
    return _.round(value, precision);
  }
  return Number.isNaN(valueAsNumber) ? undefined : valueAsNumber;
};

/**
 * Method to check if picked date is from the future
 * @return true if the date is not from the future; false if the date is from the future
 * @param pickedDate
 */
export const validateFutureDate = (pickedDate) => {
  const date = moment(pickedDate);
  const today = moment(new Date());
  return date.startOf('day').isSameOrBefore(today.startOf('day'));
};

/**
 * Method to check if first date is later or same as the second date
 * @return true if the first date is later or same as the second date
 * @param laterDate
 * @param earlierDate
 */
export const validateDateIsSameOrAfter = (laterDate, earlierDate) => {
  const laterDateParsed = moment(laterDate);
  const earlierDateParsed = moment(earlierDate);
  return laterDateParsed.startOf('day').isSameOrAfter(earlierDateParsed.startOf('day'));
};

/**
 * * Mutator function to set all values in a specified column for each entry in an array field.
 */
export const setColumnValue = ([fieldName, column, value], state, { changeValue }) =>
  changeValue(state, fieldName, (array) =>
    array.map((row) => ({
      ...row,
      [column]: value,
    })));
