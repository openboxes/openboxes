import { HANDLING_REQUIREMENTS_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getHandlingRequirementsOptions: () => apiClient.get(HANDLING_REQUIREMENTS_OPTIONS),
};
