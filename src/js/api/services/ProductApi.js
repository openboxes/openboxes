import axios from 'axios';

import { GENERIC_API, INVENTORY_ITEM, PRODUCT_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProducts: (config) => apiClient.get(PRODUCT_API, config),
  getInventoryItem: (productId, lotNumber) => axios.get(INVENTORY_ITEM(productId, lotNumber)),
  // TODO: tech debt: Replace by the product api call instead of generic
  getProduct: (id) => apiClient.get(`${GENERIC_API}/product/${id}`),
};
