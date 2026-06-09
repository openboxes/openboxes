import { LOCATION, LOCATION_API, LOCATION_TYPES } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getLocations: (config) => apiClient(LOCATION_API, config),
  getLocationTypes: (config) => apiClient.get(LOCATION_TYPES, config),
  createLocation: (payload, params) => apiClient.post(LOCATION_API, payload, { params }),
  updateLocationAddress: (locationId, address) =>
    apiClient.post(LOCATION(locationId), { address }),
};
