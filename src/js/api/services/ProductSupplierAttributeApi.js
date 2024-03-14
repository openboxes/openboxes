import { PRODUCT_SUPPLIER_ATTRIBUTE_BATCH } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  updateAttributes: (payload) => apiClient.post(PRODUCT_SUPPLIER_ATTRIBUTE_BATCH, payload),
};
