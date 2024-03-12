import { GENERIC_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  // TODO: tech debt: Replace by the product api call instead of generic
  getOrganization: (id) => apiClient.get(`${GENERIC_API}/organization/${id}`),
};
