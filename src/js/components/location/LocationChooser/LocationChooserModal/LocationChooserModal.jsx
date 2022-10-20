import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine } from 'react-icons/ri';
import Modal from 'react-modal';
import { Tab, TabList, TabPanel, Tabs } from 'react-tabs';

import LocationButton
  from 'components/location/LocationChooser/LocationChooserModal/LocationButton';
import Spinner from 'components/spinner/Spinner';
import Translate from 'utils/Translate';

import 'components/location/LocationChooser/LocationChooserModal/LocationChooserModal.scss';


Modal.setAppElement('#root');

const LocationChooserModal = ({
  locations, onClose, isLoading, isOpen, onSelectLocation,
}) => {
  const renderGroupedLocations = groupedLocations => (
    <div
      key={`${groupedLocations.group}-group`}
      className="location-chooser__modal__group d-flex flex-column mb-4"
    >
      <h3 className="location-chooser__modal__group-title mb-3">
        {groupedLocations.group !== 'NO_GROUP'
          ? groupedLocations.group
          : (
            <Translate
              id="react.dashboard.noLocationGroup.label"
              defaultMessage="No location group"
            />)}
      </h3>
      <div className="location-chooser__modal__group-container d-flex flex-wrap flex-row">
        {groupedLocations.locations.map(location =>
          (<LocationButton
            key={`${location.name}-location`}
            onClick={onSelectLocation}
            location={location}
          />))}
      </div>
    </div>);

  const renderOrganizationTabs = locationGroups => (
    <Tabs className="react-tabs">
      <TabList className="react-tabs__tab-list m-0 list-unstyled scrollbar">
        {locations.map(({ organization }) => (
          <Tab
            key={`${organization}-org-tab`}
            className="react-tabs__tab border-0 rounded-0 px-3 py-2"
          >
            {organization !== 'NO_ORGANIZATION'
              ? organization
              : (
                <Translate
                  id="react.dashboard.noOrganization.label"
                  defaultMessage="No organization"
                />)}
          </Tab>))}
      </TabList>
      <div>
        {locationGroups.map(({
          organization,
          groups,
        }) => (
          <TabPanel
            className="react-tabs__tab-panel scrollbar w-100"
            key={`${organization}-org-panel`}
          >
            {groups.map(renderGroupedLocations)}
          </TabPanel>))}
      </div>
    </Tabs>);

  return (
    <Modal
      isOpen={isOpen}
      shouldCloseOnOverlayClick
      onRequestClose={onClose}
      portalClassName="location-chooser__modal"
    >
      <header
        className="location-chooser__modal__header d-flex flex-row justify-content-between align-items-center"
      >
        <h1 className="location-chooser__modal__header__title m-0">
          <Translate
            id="react.dashboard.chooseLocation.label"
            defaultMessage="Choose Location"
          />
        </h1>
        <button
          onClick={onClose}
          type="button"
          className="location-chooser__modal__header__close-button"
        >
          <RiCloseLine />
        </button>
      </header>
      <section
        className="location-chooser__modal__content d-flex flex-grow-1 justify-content-center"
      >
        {isLoading ? <Spinner /> : renderOrganizationTabs(locations)}
      </section>
    </Modal>);
};
LocationChooserModal.defaultProps = {
  isLoading: false,
  onSelectLocation: undefined,
};

LocationChooserModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  isLoading: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  onSelectLocation: PropTypes.func,
  locations: PropTypes.arrayOf(PropTypes.shape({
    organization: PropTypes.string,
    groups: PropTypes.arrayOf(PropTypes.shape({
      group: PropTypes.string,
      locations: PropTypes.arrayOf(PropTypes.shape({})),
    })),
  })).isRequired,
};

export default LocationChooserModal;
