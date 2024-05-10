import { PICKLIST_CLEAR } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  clearPicklist: (id) => apiClient.delete(PICKLIST_CLEAR(id)),
};
