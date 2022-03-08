import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import Alert from 'react-s-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';
import TextField from '../form-elements/TextField';
import CheckboxField from '../form-elements/CheckboxField';
import SelectField from '../form-elements/SelectField';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';
import { fetchUsers, hideSpinner, showSpinner } from '../../actions';
import { debounceUsersFetch, debounceLocationGroupsFetch, debounceOrganizationsFetch } from '../../utils/option-utils';
import { renderFormField } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';

const PAGE_ID = 'locationDetails';

function validate(values) {
  const errors = {};

  if (!values.name) {
    errors.name = 'react.default.error.requiredField.label';
  }

  if (!values.organization) {
    errors.organization = 'react.default.error.requiredField.label';
  }

  return errors;
}

const FIELDS = {
  active: {
    type: CheckboxField,
    label: 'react.locationsConfiguration.locationStatus.label',
    defaultMessage: 'Location Status',
    attributes: {
      withLabel: true,
      label: 'Active',
    },
  },
  name: {
    type: TextField,
    label: 'react.locationsConfiguration.name.label',
    defaultMessage: 'Name',
    attributes: {
      required: true,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.name.tooltip.label',
    },
  },
  locationNumber: {
    type: TextField,
    label: 'react.locationsConfiguration.locationNumber.label',
    defaultMessage: 'Location Number',
    attributes: {
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.locationNumber.tooltip.label',
    },
  },
  organization: {
    type: SelectField,
    label: 'react.locationsConfiguration.organization.label',
    defaultMessage: 'Organization',
    attributes: {
      async: true,
      required: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.organization.tooltip.label',
      options: [],
      filterOptions: options => options,
    },
    getDynamicAttr: ({ debouncedOrganizationsFetch }) => ({
      loadOptions: debouncedOrganizationsFetch,
    }),
  },
  locationGroup: {
    type: SelectField,
    label: 'react.locationsConfiguration.locationGroup.label',
    defaultMessage: 'Location Group',
    attributes: {
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.locationGroup.tooltip.label',
      options: [],
      filterOptions: options => options,
    },
    getDynamicAttr: ({ debouncedLocationGroupsFetch }) => ({
      loadOptions: debouncedLocationGroupsFetch,
    }),
  },
  manager: {
    type: SelectField,
    label: 'react.locationsConfiguration.manager.label',
    defaultMessage: 'Manager',
    attributes: {
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      labelKey: 'name',
      filterOptions: options => options,
    },
    getDynamicAttr: ({ debouncedUsersFetch }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};

class LocationDetails extends Component {
  constructor(props) {
    super(props);
    this.state = {
      setInitialValues: true,
      values: this.props.initialValues,
    };

    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);

    this.debouncedLocationGroupsFetch =
      debounceLocationGroupsFetch(this.props.debounceTime, this.props.minSearchLength);

    this.debouncedOrganizationsFetch =
      debounceOrganizationsFetch(this.props.debounceTime, this.props.minSearchLength);
  }

  componentWillReceiveProps() {
    if (this.state.setInitialValues) {
      this.setInitialValues();
    }
  }

  setInitialValues() {
    const values = {
      active: true,
    };
    this.setState({ values, setInitialValues: false });
  }

  saveLocationDetails(values) {
    if (values.name && values.organization) {
      this.props.showSpinner();

      let locationUrl = '';
      if (values.locationId) {
        locationUrl = `/openboxes/api/locations/${values.locationId}`;
      } else {
        locationUrl = '/openboxes/api/locations';
      }

      const payload = {
        name: values.name,
        active: values.active,
        locationNumber: values.locationNumber,
        'organization.id': values.organization.id,
        'locationGroup.id': _.get(values.locationGroup, 'id') || '',
        'manager.id': _.get(values.manager, 'id') || '',
      };

      apiClient.post(locationUrl, payload)
        .then((response) => {
          this.props.hideSpinner();
          Alert.success(this.props.translate('react.locationsConfiguration.alert.locationSaveCompleted.label', 'Location was successfully saved!'), { timeout: 3000 });
          const resp = response.data.data;
          this.props.nextPage({
            ...values,
            locationId: resp.id,
          });
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.createStockMovement.label', 'Could not create location')));
        });
    }
  }

  nextPage(values) {
    this.saveLocationDetails(values);
  }

  render() {
    return (
      <div className="configuration-wizard-content flex-column">
        <Form
          onSubmit={values => this.nextPage(values)}
          validate={validate}
          initialValues={this.state.values}
          render={({ form, handleSubmit, values }) => (
            <form onSubmit={handleSubmit} className="w-100">
              <div className="classic-form with-description">
                <div className="submit-buttons">
                  <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
                    <i className="fa fa-question-circle-o" aria-hidden="true" />
                    &nbsp;
                    <Translate id="react.default.button.support.label" defaultMessage="Support" />
                  </button>
                </div>
                <div className="form-title"><Translate id="react.locationsConfiguration.details.label" defaultMessage="Details" /></div>
                <div className="form-subtitle"><Translate id="react.locationsConfiguration.additionalTitle.label" defaultMessage="Fill in the details for your location. Click the question mark next to the field name to find out more." /></div>

                {_.map(
                  FIELDS,
                  (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                    active: values.active,
                    debouncedLocationGroupsFetch: this.debouncedLocationGroupsFetch,
                    debouncedOrganizationsFetch: this.debouncedOrganizationsFetch,
                    debouncedUsersFetch: this.debouncedUsersFetch,
                  }),
                )}
              </div>
              <div className="submit-buttons">
                <button type="submit" className="btn btn-outline-primary float-right btn-xs">
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  location: state.session.currentLocation,
  isSuperuser: state.session.isSuperuser,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  debounceTime: state.session.searchConfig.debounceTime,
  debouncedLocationTypesFetch: state.session.searchConfig,
  minSearchLength: state.session.searchConfig.minSearchLength,
  user: state.session.user,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(LocationDetails));

LocationDetails.propTypes = {
  initialValues: PropTypes.shape({
    active: PropTypes.bool,
    name: PropTypes.string,
    locationNumber: PropTypes.string,
    locationType: PropTypes.string,
    organization: PropTypes.string,
    locationGroup: PropTypes.string,
    manager: PropTypes.string,
  }).isRequired,
  nextPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
};
