import _ from 'lodash';
import React, { Component } from 'react';
import Modal from 'react-modal';
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

import apiClient from '../../utils/apiClient';

class LocationChooser extends Component {
  constructor(props) {
    super(props);

    this.state = {
      showModal: false,
      currentLocationName: '',
      locations: {},
    };

    this.openModal = this.openModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
  }

  componentDidMount() {
    this.fetchLocations();
    this.getCurrentLocation();
  }

  getCurrentLocation() {
    const url = '/openboxes/api/getSession';

    return apiClient.get(url)
      .then((response) => {
        const currentLocationName = _.get(response, 'data.data.location.name');

        this.setState({ currentLocationName });
      });
  }

  openModal() {
    this.setState({ showModal: true });
  }

  closeModal(location) {
    if (location) {
      const url = `/openboxes/api/chooseLocation/${location.id}`;

      apiClient.put(url)
        .then(() => {
          this.setState({ showModal: false, currentLocationName: location.name });
        });
    } else {
      this.setState({ showModal: false });
    }
  }

  fetchLocations() {
    const url = '/openboxes/api/locations?locationTypeCode=DEPOT';

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
          {this.state.currentLocationName || 'Choose Location'}
        </button>
        <Modal
          isOpen={this.state.showModal}
          onRequestClose={() => this.closeModal()}
          className="modal-content-custom"
          shouldCloseOnOverlayClick={false}
        >
          <div>
            <h5 className="text-center">Choose Location</h5>
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

export default LocationChooser;
