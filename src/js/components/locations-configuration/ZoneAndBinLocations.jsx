import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import CheckboxField from 'components/form-elements/CheckboxField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import AddZoneModal from 'components/locations-configuration/AddZoneModal';
import ZoneTable from 'components/locations-configuration/ZoneTable';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'react-table/react-table.css';
import 'components/locations-configuration/ZoneTable.scss';


const FIELDS = {
  active: {
    type: CheckboxField,
    label: 'react.locationsConfiguration.addZone.status.label',
    defaultMessage: 'Status',
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
      tooltip: 'react.locationsConfiguration.addZone.name.tooltip.label',
    },
  },
  zoneType: {
    type: SelectField,
    label: 'react.locationsConfiguration.zoneType.label',
    defaultMessage: 'Zone Type',
    attributes: {
      required: true,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ zoneTypes }) => ({
      options: zoneTypes,
    }),
  },
};

const validate = (values) => {
  const requiredFields = ['name', 'zoneType'];
  return Object.keys(FIELDS)
    .reduce((acc, fieldName) => {
      if (!values[fieldName] && requiredFields.includes(fieldName)) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.requiredField.label',
        };
      }
      return acc;
    }, {});
};


const PAGE_ID = 'zoneAndBinLocations';


class ZoneAndBinLocations extends Component {
  constructor(props) {
    super(props);
    this.state = {
      zoneData: [],
      values: this.props.initialValues,
      zoneTypes: [],
    };
    this.updateZoneData = this.updateZoneData.bind(this);
    this.addZoneLocation = this.addZoneLocation.bind(this);
    this.handleZoneEdit = this.handleZoneEdit.bind(this);
    this.deleteZoneLocation = this.deleteZoneLocation.bind(this);
  }

  componentDidMount() {
    this.fetchZoneTypes();
  }

  fetchZoneTypes() {
    const url = '/openboxes/api/locations/locationTypes';
    apiClient.get(url)
      .then((response) => {
        const resp = response.data.data;
        const locationTypes = _.map(resp, (locationType) => {
          const [en, fr] = _.split(locationType.name, '|fr:');
          return {
            ...locationType,
            label: this.props.locale === 'fr' && fr ? fr : en,
          };
        });
        const zoneTypes = locationTypes.filter(location => location.locationTypeCode === 'ZONE');
        this.setState({ zoneTypes });
      })
      .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.fetchingZoneTypes', 'Could not load zone types'))));
  }

  handleZoneEdit(values) {
    this.props.showSpinner();
    apiClient.post(`/openboxes/api/locations/${values.id}`, {
      name: values.name,
      'parentLocation.id': values.parentLocation.id,
      active: values.active,
      'locationType.id': values.zoneType.id,
    })
      .then((res) => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.editZone.success.label', 'Zone location has been edited successfully!'), { timeout: 3000 });
        this.setState({
          zoneData: this.state.zoneData.map((location) => {
            if (location.id === res.data.data.id) {
              return res.data.data;
            }
            return location;
          }),
        });
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.editZone.error.label', 'Could not edit zone location')));
      });
  }

  addZoneLocation(data) {
    this.setState({ zoneData: [...this.state.zoneData, data] });
  }

  deleteZoneLocation(locationId) {
    confirmAlert({
      title: this.props.translate('react.locationsConfiguration.deleteZoneConfirm.title.label', 'Deleting a location'),
      message: this.props.translate(
        'react.locationsConfiguration.deleteZoneConfirm.subtitle.label',
        'If you press \'Yes\', this will delete the location. If you decide not to delete the location, press \'No\'',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            apiClient.delete(`/openboxes/api/locations/${locationId}`)
              .then(() => {
                this.setState({
                  zoneData: this.state.zoneData.filter(location => location.id !== locationId),
                });
              })
              .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.editZone.error.label', 'Could not edit zone location'))));
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  updateZoneData(data) {
    this.setState({ zoneData: data });
  }

  nextPage() {
    this.props.nextPage(this.state.values);
  }

  previousPage() {
    this.props.previousPage(this.state.values);
  }

  render() {
    return (
      <div className="d-flex flex-column">
        <div className="configuration-wizard-content flex-column">
          <div className="classic-form with-description">
            <div className="submit-buttons">
              <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
                <i className="fa fa-question-circle-o" aria-hidden="true" />
                &nbsp;
                <Translate id="react.default.button.support.label" defaultMessage="Support" />
              </button>
            </div>
            <div className="form-title">
              <Translate id="react.locationsConfiguration.zone.label" defaultMessage="Zone Locations" />
            </div>
            <div className="form-subtitle zone-subtitle">
              <div>
                <Translate
                  id="react.locationsConfiguration.zone.additionalTitle1.label"
                  defaultMessage="Zones are large areas within a depot encompassing multiple bin locations.
                                 They may represent different rooms or buildings within a depot space."
                />
              </div>
              <div>
                <Translate
                  id="react.locationsConfiguration.zone.additionalTitle2.label"
                  defaultMessage="Zones are optional; bin locations can be entered with or without a zone location."
                />
              </div>
            </div>
            <div className="submit-buttons">
              <AddZoneModal
                FIELDS={FIELDS}
                validate={validate}
                locationId={this.props.initialValues.locationId}
                addZoneLocation={this.addZoneLocation}
                zoneTypes={this.state.zoneTypes}
              />
            </div>
            <ZoneTable
              zoneData={this.state.zoneData}
              updateZoneData={this.updateZoneData}
              currentLocationId={this.props.initialValues.locationId}
              handleZoneEdit={this.handleZoneEdit}
              deleteZoneLocation={this.deleteZoneLocation}
              FIELDS={FIELDS}
              validate={validate}
              zoneTypes={this.state.zoneTypes}
            />
          </div>
          <div className="submit-buttons d-flex justify-content-between">
            <button type="button" onClick={() => this.previousPage()} className="btn btn-outline-primary float-left btn-xs">
              <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
            </button>
            <button type="button" onClick={() => this.nextPage()} className="btn btn-outline-primary float-left btn-xs">
              <Translate id="react.default.button.next.label" defaultMessage="Next" />
            </button>
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  locale: state.session.activeLanguage,
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
};

export default connect(mapStateToProps, mapDispatchToProps)(ZoneAndBinLocations);


ZoneAndBinLocations.propTypes = {
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
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
  locale: PropTypes.string.isRequired,
};
