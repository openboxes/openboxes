import {
  STOCK_MOVEMENT_API,
  STOCK_MOVEMENT_BY_ID,
  STOCK_MOVEMENT_ROLLBACK_APPROVAL,
  STOCK_MOVEMENT_UPDATE_REQUISITION,
  STOCK_MOVEMENT_UPDATE_STATUS,
} from 'api/urls';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import RequisitionStatus from 'consts/requisitionStatus';
import apiClient from 'utils/apiClient';

export default {
  createStockMovement: (payload) => apiClient.post(STOCK_MOVEMENT_API, payload),
  updateStockMovement: (id, payload) => apiClient.post(
    STOCK_MOVEMENT_UPDATE_REQUISITION(id), payload,
  ),
  getStockMovements: (config) => apiClient.get(STOCK_MOVEMENT_API, config),
  deleteStockMovement: (id) => apiClient.delete(STOCK_MOVEMENT_BY_ID(id)),
  updateStatus: (id, status) => apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(id), { status }),
  rejectRequest: ({
    id,
    sender,
    recipient,
    comment,
  }) =>
    apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(id), {
      status: RequisitionStatus.REJECTED,
      sender,
      recipient,
      comment,
    }),
  rollbackApproval: (id) => apiClient.put(STOCK_MOVEMENT_ROLLBACK_APPROVAL(id)),
  importCsv: (stockMovementId, formData, config) =>
    apiClient.post(STOCK_MOVEMENT_URL.importCsv(stockMovementId), formData, config),
  exportCsv: (stockMovementId) =>
    apiClient.get(STOCK_MOVEMENT_URL.exportCsv(stockMovementId), { responseType: 'blob' }),
};
