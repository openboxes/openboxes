import {
  STOCK_MOVEMENT_API,
  STOCK_MOVEMENT_BY_ID,
  STOCK_MOVEMENT_ROLLBACK_APPROVAL,
  STOCK_MOVEMENT_UPDATE_STATUS,
} from 'api/urls';
import RequisitionStatus from 'consts/requisitionStatus';
import apiClient from 'utils/apiClient';

export default {
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
};
