import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import Modal from 'react-modal';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient, { flattenRequest } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/locations-configuration/modals/ConfigurationModal.scss';
import './AddDestinationModal.scss';

function validate(values) {
  const errors = {};

  if (!values.name) {
    errors.name = 'react.default.error.requiredField.label';
  }

  if (!values.locationType) {
    errors.locationType = 'react.default.error.requiredField.label';
  }

  // "address" field is required when any of these fields are not empty
  const addressFields = ['address', 'address2', 'city', 'stateOrProvince', 'postalCode', 'country'];
  const hasAnyAddressFields = addressFields
    .reduce((acc, addressField) => acc || !!values[addressField], false);
  if (hasAnyAddressFields && !values.address) {
    errors.address = 'react.default.error.requiredField.label';
  }
  return errors;
}

const FIELDS = {
  name: {
    type: TextField,
    label: 'react.locationsConfiguration.name.label',
    defaultMessage: 'Name',
    attributes: {
      required: true,
    },
  },
  locationType: {
    type: SelectField,
    label: 'Location Type',
    defaultMessage: 'Location Type',
    attributes: {
      className: 'multi-select',
      required: true,
      valueKey: 'id',
    },
    getDynamicAttr: ({ locationTypes }) => ({
      options: locationTypes,
    }),
  },
};

const ADDRESS_FIELDS = {
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
};

class AddDestinationModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      locationTypes: [],
    };

    this.fetchLocationTypes = this.fetchLocationTypes.bind(this);
  }

  componentDidMount() {
    this.fetchLocationTypes();
  }


  fetchLocationTypes() {
    const url = '/openboxes/api/locations/locationTypes?activityCode=DYNAMIC_CREATION';

    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        const locationTypes = _.map(resp, (locationType) => {
          const [en, fr] = _.split(locationType.name, '|fr:');
          return { ...locationType, label: this.props.locale === 'fr' && fr ? fr : en };
        });

        this.setState({ locationTypes });
      });
  }

  save(values) {
    this.props.showSpinner();
    const url = '/openboxes/api/locations?useDefaultActivities=true';

    const { name, locationType, ...address } = values;
    const payload = { name, 'locationType.id': _.get(locationType, 'id') || '' };

    apiClient.post(url, payload)
      .then((response) => {
        Alert.success(this.props.translate('react.stockMovement.success.createDestination.label', 'Destination was successfully created!'), { timeout: 3000 });
        return response.data.data;
      })
      .then((destination) => {
        this.props.onResponse(destination);

        if (!_.isEmpty(address)) {
          const addressUrl = `/openboxes/api/locations/${destination.id}`;
          return apiClient.post(addressUrl, flattenRequest({ address }));
        }
        return Promise.resolve();
      })
      .then(() => {
        this.props.hideSpinner();
        this.props.onClose();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.stockMovement.error.createDestination.label', 'Could not create destination')));
      });
  }

  render() {
    return (
      <Modal
        isOpen={this.props.isOpen}
        overlayClassName="configuration-modal-overlay"
        className="configuration-modal-content"
        shouldCloseOnOverlayClick={false}
      >
        <div>
          <h4>
            <Translate id="react.stockMovement.addDestination.label" defaultMessage="Add Destination" />
          </h4>
          <Form
            onSubmit={values => this.save(values)}
            validate={validate}
            render={({ handleSubmit, values }) =>
              (
                <form id="modalForm" onSubmit={handleSubmit}>
                  <div className="classic-form location-field-rows">
                    {_.map(
                      FIELDS,
                      (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                        values,
                        locationTypes: this.state.locationTypes,
                        testValue: this.state.testValue,
                      }),
                    )}
                    {_.map(
                      ADDRESS_FIELDS,
                      (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName),
                    )}
                  </div>
                  <div className="btn-toolbar justify-content-between pt-3">
                    <button
                      type="button"
                      className="btn btn-outline-primary ml-1"
                      onClick={() => this.props.onClose()}
                    >
                      <Translate id="default.button.cancel.label" defaultMessage="Cancel" />
                    </button>
                    <button type="submit" className="btn btn-primary align-self-end">
                      <Translate id="default.button.save.label" defaultMessage="Save" />
                    </button>
                  </div>
                </form>
              )
            }
          />
        </div>
      </Modal>
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default withRouter(connect(mapStateToProps, {
  showSpinner,
  hideSpinner,
  fetchTranslations,
})(AddDestinationModal));

AddDestinationModal.propTypes = {
  locale: PropTypes.string.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  isOpen: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  onResponse: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
