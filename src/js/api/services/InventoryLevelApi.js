import queryString from 'query-string';

import { PREFERRED_BIN_LOCATIONS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getPreferredBinLocations: (facilityId, productIds) =>
    apiClient.get(PREFERRED_BIN_LOCATIONS(facilityId), {
      params: { products: productIds },
      paramsSerializer: (parameters) => queryString.stringify(parameters),
    }),
};
