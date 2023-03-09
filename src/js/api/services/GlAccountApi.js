import { GL_ACCOUNTS_OPTION } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getGlAccountOptions: config => apiClient.get(GL_ACCOUNTS_OPTION, config),
};
