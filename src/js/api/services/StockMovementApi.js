import { STOCK_MOVEMENT_API, STOCK_MOVEMENT_DELETE, STOCK_MOVEMENT_UPDATE_STATUS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getStockMovements: config => apiClient.get(STOCK_MOVEMENT_API, config),
  deleteStockMovement: id => apiClient.delete(STOCK_MOVEMENT_DELETE(id)),
  updateStatus: (id, status) => apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(id), { status }),
};
