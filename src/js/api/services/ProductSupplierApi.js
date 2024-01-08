import {
  PREFERENCE_TYPE_OPTIONS,
  PRODUCT_SUPPLIER_BY_ID,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getPreferenceTypeOptions: (config) => apiClient.get(PREFERENCE_TYPE_OPTIONS, config),
  deleteProductSupplier: (id) => apiClient.delete(PRODUCT_SUPPLIER_BY_ID(id)),
};
