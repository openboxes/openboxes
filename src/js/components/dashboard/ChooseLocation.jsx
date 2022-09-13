import React, { useState } from 'react';

import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchBreadcrumbsConfig, fetchTranslations, updateBreadcrumbs } from 'actions';
import { translateWithDefaultMessage } from 'utils/Translate';
import LocationChooserModal
  from 'components/location/LocationChooser/LocationChooserModal/LocationChooserModal';
import apiClient from 'utils/apiClient';
import _ from 'lodash';


const ChooseLocation = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [locationData, setLocationData] = useState([]);

  const transformLocationListData = (data) => {
    /*
    * Transforms a simple Array of locations into an Array of objects like:
    * { organization: String, groups: [ { group: String, locations: [Array(location)] } ] }
    * */
    const locationsGroupedByOrganization = _.groupBy(data, location => _.get(location, 'organizationName'));
    return Object.entries(locationsGroupedByOrganization)
      .map(([organization, locations]) => {
        const locationsGroupedByGroup = _.groupBy(locations, location => _.get(location, 'locationGroup.name', null));
        const groups = Object.entries(locationsGroupedByGroup)
          .map(([group, locationList]) => ({ group, locations: locationList }))
          .sort((a, b) => (a.group > b.group ? 1 : -1));
        return { organization, groups };
      })
      .sort((a, b) => (a.organization > b.organization ? 1 : -1));
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

  return (<div>
    hello test
  </div>)

};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(ChooseLocation);

