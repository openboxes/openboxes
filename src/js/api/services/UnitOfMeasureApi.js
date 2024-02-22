import { CURRENCIES_OPTIONS, UNIT_OF_MEASURE_OPTIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getCurrenciesOptions: () => apiClient.get(CURRENCIES_OPTIONS),
  getUnitOfMeasureOptions: (type) => apiClient.get(UNIT_OF_MEASURE_OPTIONS, {
    params: { type },
  }),
};
