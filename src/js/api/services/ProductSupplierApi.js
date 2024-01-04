import { PREFERENCE_TYPE_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getPreferenceTypeOptions: (config) => apiClient.get(PREFERENCE_TYPE_OPTIONS, config),
};
