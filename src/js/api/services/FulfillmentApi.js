import { FULFILLMENT_API, FULFILLMENT_VALIDATION } from 'api/urls';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

export default {
  createOutbound: (payload) => apiClient.post(FULFILLMENT_API, payload),
  validateOutbound: (payload) =>
    apiClientCustomResponseHandler.post(FULFILLMENT_VALIDATION, payload),
};
