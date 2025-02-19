import queryString from 'query-string';

import {
  CYCLE_COUNT, CYCLE_COUNT_RECOUNT_START, CYCLE_COUNT_REQUESTS, CYCLE_COUNT_START,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  createRequest: (payload, locationId) => apiClient.post(CYCLE_COUNT_REQUESTS(locationId), payload),
  startCount: (payload, locationId) => apiClient.post(CYCLE_COUNT_START(locationId), payload),
  startRecount: (payload, locationId) => apiClient.post(
    CYCLE_COUNT_RECOUNT_START(locationId), payload,
  ),
  getCycleCounts: (locationId, ids) => {
    const queryParams = queryString.stringify({ id: ids });
    return apiClient.get(`${CYCLE_COUNT(locationId)}?${queryParams}`);
  },
};
