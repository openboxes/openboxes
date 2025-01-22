import { PRODUCT_CLASSIFICATIONS_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProductClassifications: (facilityId) => apiClient.get(
    PRODUCT_CLASSIFICATIONS_API(facilityId),
  ),
};
