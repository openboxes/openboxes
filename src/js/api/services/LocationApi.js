import queryString from 'query-string';

import {
  INTERNAL_LOCATIONS_RECEIVING,
  LOCATION,
  LOCATION_API,
  LOCATION_TYPES,
} from 'api/urls';
import locationType from 'consts/locationType';
import apiClient from 'utils/apiClient';

export default {
  getLocations: (config) => apiClient(LOCATION_API, config),
  getLocationTypes: (config) => apiClient.get(LOCATION_TYPES, config),
  createLocation: (payload, params) => apiClient.post(LOCATION_API, payload, { params }),
  updateLocationAddress: (locationId, address) =>
    apiClient.post(LOCATION(locationId), { address }),
  getReceivingInternalLocations: (facilityId, shipmentNumber) =>
    apiClient.get(INTERNAL_LOCATIONS_RECEIVING, {
      params: {
        'location.id': facilityId,
        shipmentNumber,
        locationTypeCode: [locationType.BIN_LOCATION, locationType.INTERNAL],
      },
      paramsSerializer: (parameters) => queryString.stringify(parameters),
    }),
};
