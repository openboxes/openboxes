import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import Modal from 'react-modal';
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import PropTypes from 'prop-types';

import { changeCurrentLocation } from '../../actions';
import apiClient from '../../utils/apiClient';

class LocationChooser extends Component {
  constructor(props) {
    super(props);

    this.state = {
      showModal: false,
      locations: {},
    };

    this.openModal = this.openModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
  }

  componentDidMount() {
    if (this.props.defaultTranslationsFetched) {
      this.dataFetched = true;
      this.fetchLocations();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.defaultTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchLocations();
    }
  }

  dataFetched = false;

  openModal() {
    this.setState({ showModal: true });
  }

  closeModal(location) {
    if (location) {
      this.props.changeCurrentLocation(location);
    }
    this.setState({ showModal: false });
  }

  fetchLocations() {
    const url = '/openboxes/api/locations?locationTypeCode=DEPOT&applyUserFilter=true';

    return apiClient.get(url)
      .then((response) => {
        const locations = _.groupBy(response.data.data, location => _.get(location, 'locationGroup.name') || 'No location group');

        this.setState({ locations });
      });
  }

  render() {
    return (
      <div>
        <button
          type="button"
          className="btn btn-light ml-1"
          onClick={() => this.openModal()}
        >
          {this.props.currentLocationName || 'Choose Location'}
        </button>
        <Modal
          isOpen={this.state.showModal}
          onRequestClose={() => this.closeModal()}
          className="modal-content-custom"
          shouldCloseOnOverlayClick={false}
        >
          <div>
            <div>
              <button className="btn btn-danger float-right" onClick={() => this.closeModal()}>
                <i className="fa fa-close" />
              </button>
              <h5 className="text-center">Choose Location</h5>
            </div>
            <hr />
            <Tabs>
              <TabList>
                { _.map(this.state.locations, (locations, groupName) =>
                  <Tab key={groupName}>{groupName}</Tab>) }
              </TabList>
              <div className="tabs-panel-container">
                { _.map(this.state.locations, (locations, groupName) => (
                  <TabPanel key={`tab-${groupName}`}>
                    { _.map(locations, location => (
                      <button
                        key={`${groupName}-${location.name}`}
                        onClick={() => this.closeModal(location)}
                        className="btn btn-light m-2"
                      ><span><i className="fa fa-map-marker pr-2" />{location.name}</span>
                      </button>))}
                  </TabPanel>
                )) }
              </div>
            </Tabs>
          </div>
        </Modal>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  currentLocationName: state.session.currentLocation.name,
  defaultTranslationsFetched: state.session.fetchedTranslations.default,
});

export default connect(mapStateToProps, { changeCurrentLocation })(LocationChooser);

LocationChooser.propTypes = {
  /** Function called to change the currently selected location */
  changeCurrentLocation: PropTypes.func.isRequired,
  /** Name of the currently selected location */
  currentLocationName: PropTypes.string.isRequired,
  defaultTranslationsFetched: PropTypes.bool.isRequired,
};
