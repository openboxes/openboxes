import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import AddZoneModal from 'components/locations-configuration/AddZoneModal';
import EditZoneModal from 'components/locations-configuration/EditZoneModal';
import ZoneTable from 'components/locations-configuration/ZoneTable';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'react-table/react-table.css';
import 'components/locations-configuration/ZoneTable.scss';


const INITIAL_STATE = {
  zoneData: [],
};

const PAGE_ID = 'zoneAndBinLocations';

const ZONE = 'ZONE';

class ZoneAndBinLocations extends Component {
  constructor(props) {
    super(props);
    this.setShowAddZoneModal = this.setShowAddZoneModal.bind(this);
    this.fetchData = this.fetchData.bind(this);
    this.addLocation = this.addLocation.bind(this);
    this.setShowEditZoneModal = this.setShowEditZoneModal.bind(this);
    this.handleZoneEdit = this.handleZoneEdit.bind(this);
    this.editLocation = this.editLocation.bind(this);
    this.deleteLocation = this.deleteLocation.bind(this);
    this.state = {
      ...INITIAL_STATE,
      showAddZoneModal: false,
      showEditZoneModal: false,
      valuesToEdit: {},
      values: this.props.initialValues,
    };
  }


  setShowAddZoneModal() {
    this.setState({ showAddZoneModal: !this.state.showAddZoneModal });
  }

  setShowEditZoneModal() {
    this.setState({ showEditZoneModal: !this.state.showEditZoneModal });
  }

  handleZoneEdit(values) {
    this.setState({
      showEditZoneModal: !this.state.showEditZoneModal,
      valuesToEdit: { ...values },
    });
  }

  addLocation(data, entity) {
    if (entity === ZONE) {
      this.setState({ zoneData: [...this.state.zoneData, data] });
      return;
    }
    // below is prepared for bin location. It will be needed to replace zoneData with binData.
    this.setState({ zoneData: [...this.state.zoneData, data] });
  }

  editLocation(editedLocation, entity) {
    if (entity === ZONE) {
      this.setState({
        zoneData: this.state.zoneData.map((location) => {
          if (location.id === editedLocation.id) {
            return editedLocation;
          }
          return location;
        }),
      });
    }
    // below will be added setState for bin location, that's why I used that if to check entity
  }

  deleteLocation(locationId) {
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
              .then((res) => {
                if (res.status === 200) {
                  this.setState({
                    zoneData: this.state.zoneData.filter(location => location.id !== locationId),
                  });
                }
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

  fetchData(data, entity) {
    if (entity === ZONE) {
      this.setState({ zoneData: data });
      return;
    }
    // below should be bin data
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
        {this.state.showAddZoneModal &&
          <AddZoneModal
            setShowAddZoneModal={this.setShowAddZoneModal}
            locationId={this.props.initialValues.locationId}
            addLocation={this.addLocation}
          />}
        {this.state.showEditZoneModal &&
          <EditZoneModal
            setShowEditZoneModal={this.setShowEditZoneModal}
            initialValues={this.state.valuesToEdit}
            editLocation={this.editLocation}
          />}
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
              <button type="button" className="btn btn-outline-primary add-zonebin-btn" onClick={() => this.setShowAddZoneModal()}>
                <Translate id="react.locationsConfiguration.addZone.label" defaultMessage="+ Add Zone Location" />
              </button>
            </div>
            <ZoneTable
              zoneData={this.state.zoneData}
              fetchData={this.fetchData}
              currentLocationId={this.props.initialValues.locationId}
              setShowEditZoneModal={this.setShowEditZoneModal}
              handleZoneEdit={this.handleZoneEdit}
              deleteLocation={this.deleteLocation}
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
});


export default connect(mapStateToProps)(ZoneAndBinLocations);


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
};
