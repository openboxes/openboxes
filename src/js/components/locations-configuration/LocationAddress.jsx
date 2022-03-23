import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import TextField from 'components/form-elements/TextField';
import apiClient, { flattenRequest } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/locations-configuration/LocationAddress.scss';

const PAGE_ID = 'locationAddress';

const FIELDS = {
  address: {
    type: TextField,
    label: 'address.address.label',
    defaultMessage: 'Street address',
    attributes: {
      required: true,
    },
  },
  address2: {
    type: TextField,
    label: 'address.address2.label',
    defaultMessage: 'Street address 2',
  },
  city: {
    type: TextField,
    label: 'address.city.label',
    defaultMessage: 'City',
  },
  stateOrProvince: {
    type: TextField,
    label: 'address.stateOrProvince.label',
    defaultMessage: 'State/Province',
  },
  postalCode: {
    type: TextField,
    label: 'address.postalCode.label',
    defaultMessage: 'Postal code',
  },
  country: {
    type: TextField,
    label: 'address.country.label',
    defaultMessage: 'Country',
  },
  description: {
    type: TextField,
    label: 'address.description.label',
    defaultMessage: 'Description',
  },

};

const validate = (values) => {
  const requiredFields = ['address'];
  return Object.keys(FIELDS)
    .reduce((acc, fieldName) => {
      if (!values[fieldName] && requiredFields.includes(fieldName)) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.requiredField.label',
        };
      }
      if (values[fieldName] && values[fieldName].length > 255) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.tooLongInput.label',
        };
      }
      return acc;
    }, {});
};

class LocationAddress extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: this.props.initialValues,
      locationId: this.props.initialValues.locationId,
    };
  }


  saveAddressOfLocation(values, callback) {
    const valuesAsAddressObject = {
      address: {
        ...values,
      },
    };
    this.props.showSpinner();
    const locationUrl = `/openboxes/api/locations/${this.state.locationId}`;
    apiClient.post(locationUrl, flattenRequest(valuesAsAddressObject))
      .then(() => {
        this.props.hideSpinner();
        callback({
          ...this.state.values,
          address: values,
          locationId: this.state.locationId,
        });
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.addAddress.label', 'Could not add address')));
      });
  }

  nextPage(values) {
    this.saveAddressOfLocation(values, (val) => {
      Alert.success(this.props.translate('react.locationsConfiguration.alert.addressSaveCompleted.label', 'Address was succesfully added to the location!'), { timeout: 3000 });
      this.props.nextPage(val);
    });
  }

  previousPage(values) {
    this.saveAddressOfLocation(values, this.props.previousPage);
  }

  render() {
    return (
      <div className="d-flex flex-column">
        <div className="configuration-wizard-content flex-column">
          <Form
            onSubmit={values => this.nextPage(values)}
            validate={validate}
            initialValues={_.get(this.state.values, 'address')}
            render={({ values, handleSubmit }) => (
              <form onSubmit={handleSubmit} className="w-100">
                <div className="classic-form with-description location-address">
                  <div className="submit-buttons">
                    <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
                      <i className="fa fa-question-circle-o" aria-hidden="true" />
                      &nbsp;
                      <Translate id="react.default.button.support.label" defaultMessage="Support" />
                    </button>
                  </div>
                  <div className="form-title">
                    <Translate id="address.label" defaultMessage="Address" />
                  </div>
                  <div className="form-subtitle">
                    <Translate
                      id="react.locationsConfiguration.address.additionalTitle.label"
                      defaultMessage="Enter the address for your location below. This address can be referenced in custom purchasing or shipping documentation."
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
                    <Translate id="react.default.button.next.label" defaultMessage="Next" />
                  </button>
                </div>
              </form>
            )}
          />
        </div>
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

export default connect(mapStateToProps, mapDispatchToProps)(LocationAddress);

LocationAddress.propTypes = {
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
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  locationId: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
};
