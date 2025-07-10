import queryString from 'query-string';

import {
  CYCLE_COUNT, CYCLE_COUNT_ITEM,
  CYCLE_COUNT_ITEMS_BATCH,
  CYCLE_COUNT_ITEMS_IMPORT,
  CYCLE_COUNT_PENDING_REQUESTS,
  CYCLE_COUNT_RECOUNT_START,
  CYCLE_COUNT_REFRESH_ITEMS,
  CYCLE_COUNT_REQUESTS,
  CYCLE_COUNT_REQUESTS_BATCH,
  CYCLE_COUNT_START,
  CYCLE_COUNT_SUBMIT_COUNT,
  CYCLE_COUNT_SUBMIT_RECOUNT,
} from 'api/urls';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

export default {
  createRequest: (payload, locationId) => apiClient.post(CYCLE_COUNT_REQUESTS(locationId), payload),
  deleteRequests: (locationId, ids) => {
    const queryParams = queryString.stringify({ id: ids });
    return apiClient.delete(`${CYCLE_COUNT_REQUESTS(locationId)}?${queryParams}`);
  },
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
  getCycleCounts: (locationId, ids, sortBy) => {
    const queryParams = queryString.stringify({
      id: ids,
      sortBy,
    });
    return apiClient.get(`${CYCLE_COUNT(locationId)}?${queryParams}`);
  },
  deleteCycleCountItem: (locationId, itemId) =>
    apiClient.delete(CYCLE_COUNT_ITEM(locationId, itemId)),
  submitCount: (payload, locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_SUBMIT_COUNT(locationId, cycleCountId), payload),
  submitRecount: (payload, locationId, cycleCountId) =>
    apiClientCustomResponseHandler.post(
      CYCLE_COUNT_SUBMIT_RECOUNT(locationId, cycleCountId), payload,
    ),
  refreshItems: (locationId, cycleCountId, removeOutOfStockItemsImplicitly, countIndex) =>
    apiClient.post(CYCLE_COUNT_REFRESH_ITEMS(locationId,
      cycleCountId,
      removeOutOfStockItemsImplicitly), {}, { params: { countIndex } }),
  createCycleCountItems: (payload, locationId, cycleCountId) =>
    apiClient.post(CYCLE_COUNT_ITEMS_BATCH(locationId, cycleCountId), payload),
  updateCycleCountItems: (payload, locationId, cycleCountId) =>
    apiClient.patch(CYCLE_COUNT_ITEMS_BATCH(locationId, cycleCountId), payload),
  importCycleCountItems: (file, locationId) => {
    const formData = new FormData();
    formData.append('importFile', file);
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };
    return apiClient.post(CYCLE_COUNT_ITEMS_IMPORT(locationId), formData, config);
  },
  updateCycleCountRequests(locationId, payload) {
    return apiClient.patch(CYCLE_COUNT_REQUESTS_BATCH(locationId), payload);
  },
  getPendingRequests: ({ locationId, requestIds, max }) => {
    const queryParams = queryString.stringify({
      requestIds,
      max,
    });
    return apiClient.get(`${CYCLE_COUNT_PENDING_REQUESTS(locationId)}?${queryParams}`);
  },
};
