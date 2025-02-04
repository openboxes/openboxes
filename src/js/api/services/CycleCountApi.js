import { CYCLE_COUNT_REQUESTS, CYCLE_COUNT_START } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  createRequest: (payload, locationId) => apiClient.post(CYCLE_COUNT_REQUESTS(locationId), payload),
  startCount: (payload, locationId) => apiClient.post(CYCLE_COUNT_START(locationId), payload),
};
