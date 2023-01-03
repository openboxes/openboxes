import { PRODUCT_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProducts: config => apiClient.get(PRODUCT_API, config),
};
