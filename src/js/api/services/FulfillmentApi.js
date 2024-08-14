import { FULFILLMENT_API, FULFILLMENT_VALIDATION } from 'api/urls';
import { apiClientCustomResponseHandler } from 'utils/apiClient';

export default {
  createOutbound: (payload) => apiClientCustomResponseHandler.post(FULFILLMENT_API, payload),
  validateOutbound: (payload) =>
    apiClientCustomResponseHandler.post(FULFILLMENT_VALIDATION, payload),
};
