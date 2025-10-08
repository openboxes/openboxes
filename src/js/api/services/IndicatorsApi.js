import {
  INDICATORS_INVENTORY_ACCURACY,
  INDICATORS_INVENTORY_SHRINKAGE,
  INDICATORS_PRODUCTS_INVENTORIED,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProductsInventoried: (params = {}) =>
    apiClient.get(INDICATORS_PRODUCTS_INVENTORIED, { params }),
  getInventoryAccuracy: (params = {}) => apiClient.get(INDICATORS_INVENTORY_ACCURACY, { params }),
  getInventoryShrinkage: (params = {}) => apiClient.get(INDICATORS_INVENTORY_SHRINKAGE, { params }),
};
