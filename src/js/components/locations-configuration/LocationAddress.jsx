import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import { LOCATION } from 'api/urls';
import TextField from 'components/form-elements/TextField';
import apiClient, { mapToEmptyString } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/locations-configuration/LocationAddress.scss';

const FIELDS = {
  address: {
    type: TextField,
    label: 'address.address.label',
    defaultMessage: 'Street address',
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

const validate = (values) => Object.keys(FIELDS)
  .reduce((acc, fieldName) => {
    if (values[fieldName] && values[fieldName].length > 255) {
      return {
        ...acc,
        [fieldName]: 'react.default.error.tooLongInput.label',
      };
    }
    return acc;
  }, {});

class LocationAddress extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: this.props.initialValues,
      locationId: this.props.initialValues.locationId,
    };
  }

  componentDidMount() {
    if (this.props.match.params.locationId) {
      this.fetchLocation();
    }
  }

  fetchLocation() {
    const url = `/api/locations/${this.props.match.params.locationId}`;
    apiClient.get(url).then((response) => {
      const location = response.data.data;
      this.setState((prev) => ({
        values: {
          ...prev.values,
          address: location.address,
        },
      }));
    })
      .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.fetchingLocation', 'Could not load location data'))));
  }

  saveAddressOfLocation(values, callback) {
    this.props.showSpinner();
    const payload = mapToEmptyString(values, [null]);
    // If field was edited it is undefined and we need to send empty string
    // to remove it from location. If it is null, there is no need to send it, because
    // it will be null be default
    // We can't send empty address, because address.address is not nullable field
    const addressPayload = !_.isEmpty(payload) ? { address: payload } : {};
    apiClient.post(LOCATION(this.state.locationId), addressPayload)
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
            onSubmit={(values) => this.nextPage(values)}
            validate={validate}
            initialValues={_.get(this.state.values, 'address')}
            render={({ values, handleSubmit }) => (
              <form onSubmit={handleSubmit} className="w-100">
                <div className="classic-form with-description location-address">
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

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(LocationAddress));

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
    zoneTypeId: PropTypes.string,
    binTypeId: PropTypes.string,
  }).isRequired,
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  locationId: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({ locationId: PropTypes.string }),
  }).isRequired,
};
