import { HANDLING_REQUIREMENTS_OPTIONS, PRIORITY_LEVEL_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getHandlingRequirementsOptions: () => apiClient.get(HANDLING_REQUIREMENTS_OPTIONS),
  getPriorityLevelOptions: () => apiClient.get(PRIORITY_LEVEL_OPTIONS),
};
