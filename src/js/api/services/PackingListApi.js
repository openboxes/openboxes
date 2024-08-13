import { IMPORT_PACKING_LIST } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  importPackingList: (payload, config) => apiClient.post(IMPORT_PACKING_LIST, payload, config),
};
