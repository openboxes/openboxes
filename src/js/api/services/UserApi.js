import { USERS_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getUsersOptions: config => apiClient.get(USERS_OPTIONS, config),
};
