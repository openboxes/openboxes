import queryString from 'query-string';

import {
  STOCK_TRANSFER_API,
  STOCK_TRANSFER_BY_ID,
  STOCK_TRANSFER_CANDIDATES,
  STOCK_TRANSFER_ITEM_BY_ID,
  STOCK_TRANSFER_REMOVE_ALL_ITEMS,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getStockTransfers: (config) => apiClient.get(STOCK_TRANSFER_API, config),
  getStockTransfer: (id) => apiClient.get(STOCK_TRANSFER_BY_ID(id)),
  updateStockTransfer: (id, payload) => apiClient.post(STOCK_TRANSFER_BY_ID(id), payload),
  deleteStockTransfer: (id) => apiClient.delete(STOCK_TRANSFER_BY_ID(id)),
  removeItem: (id) => apiClient.delete(STOCK_TRANSFER_ITEM_BY_ID(id)),
  removeAllItems: (id) => apiClient.delete(STOCK_TRANSFER_REMOVE_ALL_ITEMS(id)),
  getStockTransferCandidates: (locationId, showExpiredItemsOnly) => {
    const queryParams = queryString.stringify({
      'location.id': locationId,
      showExpiredItemsOnly,
    });
    return apiClient.get(`${STOCK_TRANSFER_CANDIDATES}?${queryParams}`);
  },
};
