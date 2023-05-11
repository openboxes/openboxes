import { PRODUCT_GROUP_OPTION } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProductGroupsOptions: () => apiClient.get(PRODUCT_GROUP_OPTION),
};
