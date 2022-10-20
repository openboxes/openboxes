import React, { useEffect, useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import {
  changeCurrentLocation,
  fetchMenuConfig,
  fetchSessionInfo,
} from 'actions';
import LocationChooserButton
  from 'components/location/LocationChooser/LocationChooserButton/LocationChooserButton';
import LocationChooserModal
  from 'components/location/LocationChooser/LocationChooserModal/LocationChooserModal';
import apiClient from 'utils/apiClient';

const LocationChooser = (props) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [locationData, setLocationData] = useState([]);

  const transformLocationListData = (data) => {
    /*
    * Transforms a simple Array of locations into an Array of objects like:
    * { organization: String, groups: [ { group: String, locations: [Array(location)] } ] }
    * sort by organization name and group name
    * null organizations and location groups are sorted as last
    * */
    const noOrganizationTag = 'NO_ORGANIZATION';
    const noGroupTag = 'NO_GROUP';
    const locationsGroupedByOrganization = _.groupBy(data, location =>
      _.get(location, 'organizationName', noOrganizationTag) || noOrganizationTag);
    return Object.entries(locationsGroupedByOrganization)
      .map(([organization, locations]) => {
        const locationsGroupedByGroup = _.groupBy(locations, location =>
          _.get(location, 'locationGroup.name', noGroupTag) || noGroupTag);
        const groups = Object.entries(locationsGroupedByGroup)
          .map(([group, locationList]) => ({ group, locations: locationList }))
          .sort((a, b) => {
            if (a.group === noGroupTag) return 1;
            if (b.group === noGroupTag) return -1;
            return a.group > b.group ? 1 : -1;
          });
        return { organization, groups };
      })
      .sort((a, b) => {
        if (a.organization === noOrganizationTag) return 1;
        if (b.organization === noOrganizationTag) return -1;
        return a.organization > b.organization ? 1 : -1;
      });
  };

  const fetchLocations = () => {
    const url = '/openboxes/api/locations';
    const params = {
      locationChooser: true,
      applyUserFilter: true,
      locationTypeCode: 'DEPOT',
      activityCodes: 'MANAGE_INVENTORY',
    };
    setIsLoading(true);
    return apiClient.get(url, { params })
      .then((response) => {
        const { data } = response.data;
        const locations = transformLocationListData(data);
        setLocationData(locations);
      }).finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchLocations();
  }, []);

  const selectLocation = (location) => {
    props.changeCurrentLocation(location).then(() => {
      props.fetchMenuConfig();
      props.fetchSessionInfo();
    });
    setIsOpen(false);
  };

  const toggleModalHandler = () => {
    setIsOpen(isOpenModal => !isOpenModal);
  };

  return (
    <React.Fragment>
      <LocationChooserButton
        onToggle={toggleModalHandler}
        location={props.currentLocation}
        envTag={props.logoLabel}
      />
      <LocationChooserModal
        isLoading={isLoading}
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        locations={locationData}
        onSelectLocation={selectLocation}
      />
    </React.Fragment>
  );
};

const mapStateToProps = state => ({
  currentLocation: state.session.currentLocation,
  defaultTranslationsFetched: state.session.fetchedTranslations.default,
  logoLabel: state.session.logoLabel,
});

LocationChooser.propTypes = {
  changeCurrentLocation: PropTypes.func.isRequired,
  fetchSessionInfo: PropTypes.func.isRequired,
  fetchMenuConfig: PropTypes.func.isRequired,
  currentLocation: PropTypes.shape({
    name: PropTypes.string,
    backgroundColor: PropTypes.string,
  }).isRequired,
  logoLabel: PropTypes.string.isRequired,
};

export default connect(mapStateToProps, {
  changeCurrentLocation,
  fetchSessionInfo,
  fetchMenuConfig,
})(LocationChooser);
