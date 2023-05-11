import { SHIPMENT_TYPES } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getShipmentTypes: () => apiClient.get(SHIPMENT_TYPES),
};
