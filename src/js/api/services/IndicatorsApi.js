import {
  INDICATORS_INVENTORY_LOSS,
  INDICATORS_ITEMS_COUNTED,
  INDICATORS_NOT_FINISHED_ITEMS,
  INDICATORS_TARGET_PROGRESS,
  INDICATORS_TOTAL_COUNT,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getTotalCount: (params = {}) => apiClient.get(INDICATORS_TOTAL_COUNT, { params }),
  getItemsCounted: (params = {}) => apiClient.get(INDICATORS_ITEMS_COUNTED, { params }),
  getTargetProgress: (params = {}) => apiClient.get(INDICATORS_TARGET_PROGRESS, { params }),
  getNotFinishedItems: (params = {}) => apiClient.get(INDICATORS_NOT_FINISHED_ITEMS, { params }),
  getInventoryLoss: (params = {}) => apiClient.get(INDICATORS_INVENTORY_LOSS, { params }),
};
