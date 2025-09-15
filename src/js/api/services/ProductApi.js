import axios from 'axios';
import queryString from 'query-string';

import { GENERIC_API, INVENTORY_ITEM, PRODUCT_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProducts: (config) => apiClient.get(PRODUCT_API, config),
  getInventoryItem: (productId, lotNumber) => axios.get(INVENTORY_ITEM(productId, lotNumber)),
  // TODO: tech debt: Replace by the product api call instead of generic
  getProduct: (id) => apiClient.get(`${GENERIC_API}/product/${id}`),
  getLatestInventoryCountDate: (productIds) => apiClient.get(`${PRODUCT_API}/getLatestInventoryCountDate`, {
    params: {
      productIds,
    },
    paramsSerializer: (parameters) => queryString.stringify(parameters),
  }),
  getLotNumbersByProductIds: (productIds) =>
    apiClient.get(`${PRODUCT_API}/getLotNumbersWithExpiration`, {
      params: { productId: productIds },
      paramsSerializer: (params) => queryString.stringify(params, { arrayFormat: 'repeat' }),
    }),
};
