import { STOCK_TRANSFER_API, STOCK_TRANSFER_DELETE } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getStockTransfers: config => apiClient.get(STOCK_TRANSFER_API, config),
  deleteStockTransfer: id => apiClient.delete(STOCK_TRANSFER_DELETE(id)),
};
