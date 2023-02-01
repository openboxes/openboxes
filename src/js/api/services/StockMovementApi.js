import { STOCK_MOVEMENT_API, STOCK_MOVEMENT_DELETE } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getStockMovements: config => apiClient.get(STOCK_MOVEMENT_API, config),
  deleteStockMovement: id => apiClient.delete(STOCK_MOVEMENT_DELETE(id)),
};
