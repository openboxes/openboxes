import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import CheckboxField from 'components/form-elements/CheckboxField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import AddLocationGroupModal from 'components/locations-configuration/modals/AddLocationGroupModal';
import AddOrganizationModal from 'components/locations-configuration/modals/AddOrganizationModal';
import apiClient from 'utils/apiClient';
import Checkbox from 'utils/Checkbox';
import { renderFormField } from 'utils/form-utils';
import { debounceAllOrganizationsFetch, debounceLocationGroupsFetch, debounceUsersFetch } from 'utils/option-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/locations-configuration/LocationDetails.scss';

const PAGE_ID = 'locationDetails';

function validate(values) {
  const errors = {};

  if (!values.name) {
    errors.name = 'react.default.error.requiredField.label';
  }

  if (!values.organization) {
    errors.organization = 'react.default.error.requiredField.label';
  }

  if (!values.locationType) {
    errors.locationType = 'react.default.error.requiredField.label';
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
      createNewFromModal: true,
      createNewFromModalLabel: 'Add new Organization',
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
    getDynamicAttr: ({ debouncedOrganizationsFetch, openNewOrganizationModal }) => ({
      loadOptions: debouncedOrganizationsFetch,
      newOptionModalOpen: openNewOrganizationModal,
    }),
  },
  locationGroup: {
    type: SelectField,
    label: 'react.locationsConfiguration.locationGroup.label',
    defaultMessage: 'Location Group',
    attributes: {
      createNewFromModal: true,
      createNewFromModalLabel: 'Add new Location Group',
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
    getDynamicAttr: ({ debouncedLocationGroupsFetch, openNewLocationGroupModal }) => ({
      loadOptions: debouncedLocationGroupsFetch,
      newOptionModalOpen: openNewLocationGroupModal,
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
      showOrganizationModal: false,
      showLocationGroupModal: false,
      setInitialValues: true,
      values: this.props.initialValues,
      useDefaultActivities: this.props.initialValues.useDefaultActivities !== false,
      locationTypes: [],
      supportedActivities: [],
    };
    this.openNewOrganizationModal = this.openNewOrganizationModal.bind(this);
    this.openNewLocationGroupModal = this.openNewLocationGroupModal.bind(this);
    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);

    this.debouncedLocationGroupsFetch =
      debounceLocationGroupsFetch(this.props.debounceTime, this.props.minSearchLength);

    this.debouncedOrganizationsFetch =
      debounceAllOrganizationsFetch(this.props.debounceTime, this.props.minSearchLength);
    this.setOrganization = this.setOrganization.bind(this);
    this.setLocationGroup = this.setLocationGroup.bind(this);
    this.fetchOrganization = this.fetchOrganization.bind(this);
  }

  componentDidMount() {
    this.setInitialValues();
    this.fetchLocationTypes();
    if (this.props.locConfTranslationsFetched) {
      this.dataFetched = true;
      this.fetchSupportedActivities();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!this.dataFetched && nextProps.locConfTranslationsFetched) {
      this.dataFetched = true;
      this.fetchSupportedActivities();
    }
  }

  setInitialValues() {
    if (_.isEmpty(this.props.initialValues)) {
      this.setState({
        values: {
          active: true,
        },
      });
    }
  }

  getSupportedActivities(locationType) {
    return _.chain(locationType)
      .get('supportedActivities')
      .map(value => ({ value, label: this.props.translate(`react.locationsConfiguration.ActivityCode.${value}`, value) }))
      .value();
  }

  dataFetched = false;

  fetchLocationTypes() {
    const url = '/openboxes/api/locations/locationTypes';

    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        const locationTypes = _.map(resp, (locationType) => {
          const [en, fr] = _.split(locationType.name, '|fr:');
          return { ...locationType, label: this.props.locale === 'fr' && fr ? fr : en };
        });

        if (this.state.values.locationType) {
          this.setState({ locationTypes });
        } else {
          const locationType = _.find(locationTypes, type => _.startsWith(type.name, 'Depot'));
          const supportedActivities = this.getSupportedActivities(locationType);
          this.setState({
            locationTypes, values: { ...this.state.values, locationType, supportedActivities },
          });
        }
      });
  }

  fetchSupportedActivities() {
    const url = '/openboxes/api/locations/supportedActivities';

    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        const supportedActivities = _.map(resp, value => ({ value, label: this.props.translate(`react.locationsConfiguration.ActivityCode.${value}`, value) }));
        this.setState({ supportedActivities });
      });
  }

  setOrganization({ newOrganizationId }) {
    this.fetchOrganization(newOrganizationId);
  }

  setLocationGroup({ newLocationGroupId }) {
    this.fetchLocationGroup(newLocationGroupId);
  }

  openNewOrganizationModal() {
    this.setState({ showOrganizationModal: true });
  }

  openNewLocationGroupModal() {
    this.setState({ showLocationGroupModal: true });
  }

  fetchOrganization(organizationId) {
    const url = `/openboxes/api/organizations/${organizationId}`;
    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        window.setFormValue('organization', resp.name);
      });
  }

  fetchLocationGroup(locationGroupId) {
    const url = `/openboxes/api/locationGroups/${locationGroupId}`;
    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        window.setFormValue('locationGroup', resp.name);
      });
  }

  saveLocationDetails(values) {
    if (values.name && values.organization) {
      this.props.showSpinner();

      let locationUrl = '';
      if (values.locationId) {
        locationUrl = `/openboxes/api/locations/${values.locationId}?useDefaultActivities=${this.state.useDefaultActivities}`;
      } else {
        locationUrl = `/openboxes/api/locations?useDefaultActivities=${this.state.useDefaultActivities}`;
      }

      const payload = {
        name: values.name,
        active: values.active,
        locationNumber: values.locationNumber,
        'organization.id': values.organization.id,
        'locationGroup.id': _.get(values.locationGroup, 'id') || '',
        'manager.id': _.get(values.manager, 'id') || '',
        'locationType.id': _.get(values.locationType, 'id') || '',
        supportedActivities: _.map(values.supportedActivities, val => val.value),
      };

      apiClient.post(locationUrl, payload)
        .then((response) => {
          this.props.hideSpinner();
          Alert.success(this.props.translate('react.locationsConfiguration.alert.locationSaveCompleted.label', 'Location was successfully saved!'), { timeout: 3000 });
          const resp = response.data.data;
          this.props.nextPage({
            ...values,
            locationId: resp.id,
            useDefaultActivities: this.state.useDefaultActivities,
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
          mutators={{
            setValue: ([field, value], state, { changeValue }) => {
              changeValue(state, field, () => value);
            },
            resetSupportedActivities: ([locationType], state, utils) => {
              const supportedActivities = this.getSupportedActivities(locationType);
              utils.changeValue(state, 'supportedActivities', () => supportedActivities);
            },
          }}
          render={({ form: { mutators: { resetSupportedActivities } }, handleSubmit, values }) => (
            <div>
              <AddOrganizationModal
                isOpen={this.state.showOrganizationModal}
                onClose={() => this.setState({ showOrganizationModal: false })}
                onResponse={this.setOrganization}
              />
              <AddLocationGroupModal
                isOpen={this.state.showLocationGroupModal}
                onClose={() => this.setState({ showLocationGroupModal: false })}
                onResponse={this.setLocationGroup}
              />
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
                      openNewOrganizationModal: this.openNewOrganizationModal,
                      openNewLocationGroupModal: this.openNewLocationGroupModal,
                    }),
                  )}

                  <div className="form-title"><Translate id="react.locationsConfiguration.typeAndActivities.label" defaultMessage="Location Type and Supported Activities" /></div>
                  <div className="form-subtitle">
                    <span>
                      <Translate id="react.locationsConfiguration.typeAndActivitiesDescription.label" />&nbsp;
                      <a target="_blank" rel="noopener noreferrer" href="https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1744633857/Location+Types+and+Supported+Activities">
                        <Translate id="react.locationsConfiguration.here.label" defaultMessage="here" />
                      </a>.
                    </span>
                  </div>

                  <SelectField
                    fieldName="locationType"
                    fieldConfig={{
                      label: 'react.locationsConfiguration.locationType.label',
                      defaultMessage: 'Location Type',
                      attributes: {
                        multi: true,
                        className: 'multi-select',
                        required: true,
                        valueKey: 'id',
                        withTooltip: true,
                        tooltip: 'react.locationsConfiguration.locationType.tooltip.label',
                      },
                      getDynamicAttr: ({ locationTypes }) => ({
                        options: locationTypes,
                        onChange: (val) => { resetSupportedActivities(val); },
                      }),
                    }}
                    locationTypes={this.state.locationTypes}
                  />
                  <div className="d-flex w-100 ml-1 pt-2 justify-content-between align-items-center">
                    <Checkbox
                      id="useDefaultActivities"
                      value={this.state.useDefaultActivities}
                      onChange={val => this.setState({ useDefaultActivities: val })}
                      withLabel
                      label={this.props.translate('react.locationsConfiguration.useDefaultActivities.label', 'Use default settings for Supported Activities')}
                    />
                    <button
                      type="button"
                      className="btn btn-primary btn-xs"
                      onClick={() => {
                        this.setState({ useDefaultActivities: true });
                        resetSupportedActivities(values.locationType);
                      }}
                    >
                      <span>
                        <i className="fa fa-refresh pr-2" />
                        <Translate id="react.locationsConfiguration.resetToDefault.label" defaultMessage="Reset to default settings" />
                      </span>
                    </button>
                  </div>
                  <div className="location-supported-activities">
                    <SelectField
                      fieldName="supportedActivities"
                      fieldConfig={{
                        attributes: {
                          multi: true,
                        },
                        getDynamicAttr: ({ supportedActivities, useDefaultActivities }) => ({
                          disabled: useDefaultActivities,
                          options: supportedActivities,
                        }),
                      }}
                      supportedActivities={this.state.supportedActivities}
                      useDefaultActivities={this.state.useDefaultActivities}
                    />
                  </div>
                </div>
                <div className="submit-buttons">
                  <button
                    type="submit"
                    onClick={() => {
                      if (this.state.useDefaultActivities) {
                        resetSupportedActivities(values.locationType);
                      }
                    }}
                    className="btn btn-outline-primary float-right btn-xs"
                  >
                    <Translate id="react.default.button.next.label" defaultMessage="Next" />
                  </button>
                </div>
              </form>
            </div>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  location: state.session.currentLocation,
  locale: state.session.activeLanguage,
  isSuperuser: state.session.isSuperuser,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  debounceTime: state.session.searchConfig.debounceTime,
  debouncedLocationTypesFetch: state.session.searchConfig,
  minSearchLength: state.session.searchConfig.minSearchLength,
  locConfTranslationsFetched: state.session.fetchedTranslations.locationsConfiguration,
  user: state.session.user,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(LocationDetails));

LocationDetails.propTypes = {
  initialValues: PropTypes.shape({
    active: PropTypes.bool,
    useDefaultActivities: PropTypes.bool,
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
  locConfTranslationsFetched: PropTypes.bool.isRequired,
  locale: PropTypes.string.isRequired,
};
