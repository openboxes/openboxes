import { LOCATION_TYPES } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getLocationTypes: config => apiClient.get(LOCATION_TYPES, config),
};
