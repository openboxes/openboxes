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
import BinTable from 'components/locations-configuration/BinTable';
import AddBinModal from 'components/locations-configuration/modals/AddBinModal';
import AddZoneModal from 'components/locations-configuration/modals/AddZoneModal';
import ImportBinModal from 'components/locations-configuration/modals/ImportBinModal';
import ZoneTable from 'components/locations-configuration/ZoneTable';
import apiClient, { flattenRequest } from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'react-table/react-table.css';
import 'components/locations-configuration/ZoneTable.scss';


const ZONE_FIELDS = {
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
  locationType: {
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

const BIN_FIELDS = {
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
  locationType: {
    type: SelectField,
    label: 'react.locationsConfiguration.binType.label',
    defaultMessage: 'Bin Type',
    attributes: {
      required: true,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ binTypes }) => ({
      options: binTypes,
    }),
  },
  zoneLocation: {
    type: SelectField,
    label: 'react.locationsConfiguration.zoneLocation.label',
    defaultMessage: 'Zone Location',
    attributes: {
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ zoneData }) => ({
      options: zoneData,
    }),
  },
};

const zoneValidate = (values) => {
  const requiredFields = ['name', 'locationType'];
  return Object.keys(ZONE_FIELDS)
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


const binValidate = (values) => {
  const requiredFields = ['name', 'locationType'];
  return Object.keys(BIN_FIELDS)
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


class ZoneAndBinLocations extends Component {
  constructor(props) {
    super(props);
    this.state = {
      zoneData: [],
      binData: [],
      values: this.props.initialValues,
      zoneTypes: [],
      binTypes: [],
    };
    this.updateZoneData = this.updateZoneData.bind(this);
    this.updateBinData = this.updateBinData.bind(this);
    this.refetchBinTable = this.refetchBinTable.bind(this);
    this.refetchZoneTable = this.refetchZoneTable.bind(this);
    this.deleteLocation = this.deleteLocation.bind(this);
    this.handleLocationEdit = this.handleLocationEdit.bind(this);
    this.refBinTable = React.createRef();
    this.refZoneTable = React.createRef();
  }

  componentDidMount() {
    this.fetchBinAndZoneTypes();
  }

  fetchBinAndZoneTypes() {
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
        const binTypes = locationTypes.filter(location => location.locationTypeCode === 'BIN_LOCATION' || location.locationTypeCode === 'INTERNAL');
        const zoneTypes = locationTypes.filter(location => location.locationTypeCode === 'ZONE');
        this.setState({ binTypes, zoneTypes });
      })
      .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.fetchingBinAndZoneTypes', 'Could not load location types'))));
  }

  handleLocationEdit(values) {
    this.props.showSpinner();
    const payload = {
      name: values.name,
      parentLocation: { id: values.parentLocation.id },
      active: values.active,
      locationType: { id: values.locationType.id },
      zone: values.zoneLocation && { id: values.zoneLocation.id },
    };

    apiClient.post(`/openboxes/api/locations/${values.id}`, flattenRequest(payload))
      .then(() => {
        this.props.hideSpinner();
        if (values.locationType.locationTypeCode === 'ZONE') {
          this.zoneEditCallback();
          return;
        }
        this.binEditCallback();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.editZone.error.label', 'Could not edit zone location')));
      });
  }

  zoneEditCallback() {
    Alert.success(this.props.translate('react.locationsConfiguration.editZone.success.label', 'Zone location has been edited successfully!'), { timeout: 3000 });
    this.refZoneTable.current.fireFetchData();
  }

  binEditCallback() {
    Alert.success(this.props.translate('react.locationsConfiguration.editBin.success.label', 'Bin location has been edited successfully!'), { timeout: 3000 });
    this.refBinTable.current.fireFetchData();
  }

  refetchBinTable() {
    this.refBinTable.current.fireFetchData();
  }

  refetchZoneTable() {
    this.refZoneTable.current.fireFetchData();
  }

  deleteLocation(location) {
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
            apiClient.delete(`/openboxes/api/locations/${location.id}`)
              .then(() => {
                if (location.locationType.locationTypeCode === 'ZONE') {
                  this.refetchZoneTable();
                  return;
                }
                this.refetchBinTable();
              })
              .catch(() => {
                if (location.locationType.locationTypeCode === 'ZONE') {
                  return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.deleteZone.error.label', 'Could not delete zone location')));
                }
                return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.deleteBin.error.label', 'Could not delete bin location')));
              });
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

  updateBinData(data) {
    this.setState({ binData: data });
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
                FIELDS={ZONE_FIELDS}
                validate={zoneValidate}
                locationId={this.props.initialValues.locationId}
                addZoneLocation={this.refetchZoneTable}
                zoneTypes={this.state.zoneTypes}
              />
            </div>
            <ZoneTable
              zoneData={this.state.zoneData}
              updateZoneData={this.updateZoneData}
              currentLocationId={this.props.initialValues.locationId}
              handleLocationEdit={this.handleLocationEdit}
              deleteLocation={this.deleteLocation}
              FIELDS={ZONE_FIELDS}
              validate={zoneValidate}
              zoneTypes={this.state.zoneTypes}
              refZoneTable={this.refZoneTable}
            />
          </div>

          <div className="classic-form with-description">
            <div className="form-title">
              <Translate id="react.locationsConfiguration.bin.label" defaultMessage="Bin Locations" />
            </div>
            <div className="form-subtitle">
              <Translate
                id="react.locationsConfiguration.bin.additionalTitle.label"
                defaultMessage="Bin locations represent a physical storage location within a depot.
                                  Inventory within the depot is tracked and picked by bin location."
              />
            </div>
            <div className="d-flex bin-buttons">
              <AddBinModal
                FIELDS={BIN_FIELDS}
                validate={binValidate}
                locationId={this.props.initialValues.locationId}
                addBinLocation={this.refetchBinTable}
                binTypes={this.state.binTypes}
                zoneData={this.state.zoneData}
              />
              <ImportBinModal
                locationId={this.props.initialValues.locationId}
                onResponse={this.refetchBinTable}
              />
              <button type="button" className="btn-xs btn btn-outline-primary add-zonebin-btn">
                <i className="fa fa-arrow-up mr-1" aria-hidden="true" />
                <Translate id="react.locationsConfiguration.exportBinLocations.label" defaultMessage="Export Bin Locations" />
              </button>
            </div>
            <BinTable
              binData={this.state.binData}
              updateBinData={this.updateBinData}
              currentLocationId={this.props.initialValues.locationId}
              handleLocationEdit={this.handleLocationEdit}
              deleteLocation={this.deleteLocation}
              FIELDS={BIN_FIELDS}
              validate={binValidate}
              binTypes={this.state.binTypes}
              refBinTable={this.refBinTable}
              zoneData={this.state.zoneData}
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
    zoneTypeId: PropTypes.string,
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
};
