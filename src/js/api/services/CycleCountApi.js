import queryString from 'query-string';

import {
  CYCLE_COUNT, CYCLE_COUNT_ITEM,
  CYCLE_COUNT_ITEMS,
  CYCLE_COUNT_RECOUNT_START,
  CYCLE_COUNT_REFRESH_ITEMS,
  CYCLE_COUNT_REQUESTS,
  CYCLE_COUNT_START,
  CYCLE_COUNT_SUBMIT_COUNT,
  CYCLE_COUNT_SUBMIT_RECOUNT,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  createRequest: (payload, locationId) => apiClient.post(CYCLE_COUNT_REQUESTS(locationId), payload),
  startCount: ({
    payload, locationId, format = null, config = {},
  }) => apiClient.post(
    CYCLE_COUNT_START(locationId, format), payload, config,
  ),
  startRecount: ({
    payload, locationId, format = null, config = {},
  }) => apiClient.post(
    CYCLE_COUNT_RECOUNT_START(locationId, format), payload, config,
  ),
  getCycleCounts: (locationId, ids, format = null, config = {}) => {
    const queryParams = queryString.stringify({ id: ids, format });
    return apiClient.get(`${CYCLE_COUNT(locationId)}?${queryParams}`, config);
  },
  updateCycleCountItem: (payload, locationId, itemId) =>
    apiClient.patch(CYCLE_COUNT_ITEM(locationId, itemId), payload),
  createCycleCountItem: (payload, locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_ITEMS(locationId, cycleCountId), payload),
  deleteCycleCountItem: (locationId, itemId) =>
    apiClient.delete(CYCLE_COUNT_ITEM(locationId, itemId)),
  submitCount: (payload, locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_SUBMIT_COUNT(locationId, cycleCountId), payload),
  submitRecount: (payload, locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_SUBMIT_RECOUNT(locationId, cycleCountId), payload),
  refreshItems: (locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_REFRESH_ITEMS(locationId, cycleCountId)),
};
