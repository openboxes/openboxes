import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import TextField from 'components/form-elements/TextField';
import ForecastingNotsupported from 'components/locations-configuration/ForecastingNotsupported';
import SuccessMessage from 'components/locations-configuration/SuccessMessage';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/locations-configuration/Forecasting.scss';


const FIELDS = {
  expectedLeadTimeDays: {
    type: TextField,
    label: 'address.address.label',
    defaultMessage: 'Expected Lead Time Days',
    attributes: {
      type: 'number',
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.forecast.expectedLeadTime.tooltip.label',
    },
  },
  replenishmentPeriodDays: {
    type: TextField,
    label: 'address.address.label',
    defaultMessage: 'Replenishment Period Days',
    attributes: {
      type: 'number',
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.forecast.replenishmentPeriod.tooltip.label',
    },
  },
  demandTimePeriodDays: {
    type: TextField,
    label: 'address.address.label',
    defaultMessage: 'Demand Time Period Days',
    attributes: {
      type: 'number',
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.forecast.demandTime.tooltip.label',
    },
  },
};

const validate = (values) => {
  const requiredFields = [];
  const isNumeric = n => !Number.isNaN(parseFloat(n));

  return Object.keys(FIELDS)
    .reduce((acc, fieldName) => {
      if (!values[fieldName] && requiredFields.includes(fieldName)) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.requiredField.label',
        };
      }
      if (values[fieldName] && !isNumeric(values[fieldName])) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.numericField.label',
        };
      }
      if (values[fieldName] < 0) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.positiveNumberField.label',
        };
      }
      return acc;
    }, {});
};

class Forecasting extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: this.props.initialValues,
      locationId: this.props.initialValues.locationId,
      isForecastingSupported: true,
      showSuccessMessage: false,
    };
    this.setShowSuccessMessage = this.setShowSuccessMessage.bind(this);
  }

  setShowSuccessMessage() {
    this.setState({ showSuccessMessage: !this.state.showSuccessMessage });
  }

  saveForecasting(values) {
    this.props.showSpinner();
    const locationUrl = `/openboxes/api/locations/${this.state.locationId}/updateForecastingConfiguration`;
    this.props.hideSpinner();
    apiClient.post(locationUrl, values)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.alert.forecastSaveCompleted.label', 'Forecast values for the location have been set!'), { timeout: 3000 });
        this.setState({ showSuccessMessage: true });
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.forecasting.error.forecastSave.label', 'Could not set values of forecasting for this location')));
      });
  }

  previousPage(values) {
    this.props.previousPage({ ...this.state.values, forecasting: values });
  }


  render() {
    return (
      <div className="d-flex flex-column">
        <SuccessMessage
          successMessageOpen={this.state.showSuccessMessage}
          setShowSuccessMessage={this.setShowSuccessMessage}
        />
        {this.state.isForecastingSupported ?
          <div className="configuration-wizard-content flex-column">
            <Form
              onSubmit={values => this.saveForecasting(values)}
              validate={validate}
              initialValues={_.get(this.state.values, 'forecasting')}
              render={({ handleSubmit, values }) => (
                <form onSubmit={handleSubmit} className="w-100">
                  <div className="classic-form with-description forecasting">
                    <div className="form-title">
                      <Translate id="react.locationsConfiguration.forecasting.label" defaultMessage="Forecasting" />
                    </div>
                    <div className="form-subtitle">
                      <Translate
                        id="react.locationsConfiguration.forecasting.additionalTitle.label"
                        defaultMessage="On this page, you can set default values to use in forecasting for this location.
                                      These values are used in the average demand calculation and in the forecast report."
                      />
                    </div>

                    {_.map(
                    FIELDS,
                    (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName),
                  )}
                  </div>
                  <div className="submit-buttons">
                    <button type="button" onClick={() => this.previousPage(values)} className="btn btn-outline-primary float-left btn-xs">
                      <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                    </button>
                    <button type="submit" className="btn btn-outline-primary float-right btn-xs">
                      <Translate id="react.default.button.finish.label" defaultMessage="Finish" />
                    </button>
                  </div>
                </form>
            )}
            />
          </div>
        :
          <ForecastingNotsupported
            previousPage={this.props.previousPage}
          />
        }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
};

export default connect(mapStateToProps, mapDispatchToProps)(Forecasting);


Forecasting.propTypes = {
  previousPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    active: PropTypes.bool,
    name: PropTypes.string,
    locationNumber: PropTypes.string,
    locationType: PropTypes.string,
    organization: PropTypes.string,
    locationGroup: PropTypes.string,
    manager: PropTypes.string,
    locationId: PropTypes.string,
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
