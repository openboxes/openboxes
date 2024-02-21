import { CURRENCIES_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getCurrenciesOptions: () => apiClient.get(CURRENCIES_OPTIONS),
};
