import {
  STOCK_LIST_API,
  STOCK_LIST_CLEAR,
  STOCK_LIST_CLONE,
  STOCK_LIST_DELETE,
  STOCK_LIST_PUBLISH, STOCK_LIST_UNPUBLISH,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getStockLists: config => apiClient.get(STOCK_LIST_API, config),
  deleteStockList: id => apiClient.delete(STOCK_LIST_DELETE(id)),
  clearStockList: id => apiClient.post(STOCK_LIST_CLEAR(id)),
  cloneStockList: id => apiClient.post(STOCK_LIST_CLONE(id)),
  publishStockList: id => apiClient.post(STOCK_LIST_PUBLISH(id)),
  unpublishStockList: id => apiClient.post(STOCK_LIST_UNPUBLISH(id)),
};
