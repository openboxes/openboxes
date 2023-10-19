import axios from 'axios';

import { INVENTORY_ITEM, PRODUCT_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getProducts: config => apiClient.get(PRODUCT_API, config),
  getInventoryItem: (productCode, lotNumber) =>
    axios.get(INVENTORY_ITEM(productCode, lotNumber)),
};
