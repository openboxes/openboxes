import { PRODUCT_PACKAGE_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  save: (payload) => apiClient.post(PRODUCT_PACKAGE_API, payload),
};
