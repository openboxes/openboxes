import { CYCLE_COUNT_REQUESTS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  createRequest: (payload, locationId) => apiClient.post(CYCLE_COUNT_REQUESTS(locationId), payload),
  startCount: () => new Promise((resolve) => {
    setTimeout(resolve, 1000);
  }),
};
