import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import ZoneTable from 'components/locations-configuration/ZoneTable';
import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'react-table/react-table.css';
import 'components/locations-configuration/ZoneTable.scss';

const INITIAL_STATE = {};

const PAGE_ID = 'zoneAndBinLocations';

class ZoneAndBinLocations extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...INITIAL_STATE,
      locationId: this.props.initialValues.locationId,
    };
  }

  nextPage() {
    this.props.nextPage({ locationId: this.state.locationId });
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
              <button type="button" className="btn btn-outline-primary add-zonebin-btn">
                <Translate id="react.locationsConfiguration.addZone.label" defaultMessage="+ Add Zone Location" />
              </button>
            </div>
            <ZoneTable
              currentLocationId={this.props.currentLocationId}
            />
          </div>
          <div className="submit-buttons d-flex justify-content-between">
            <button type="button" onClick={this.props.previousPage} className="btn btn-outline-primary float-left btn-xs">
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
  currentLocationId: state.session.currentLocation.id,
});

export default connect(mapStateToProps)(ZoneAndBinLocations);

ZoneAndBinLocations.propTypes = {
  currentLocationId: PropTypes.string.isRequired,
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
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
