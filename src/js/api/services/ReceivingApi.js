import { RECEIPT_SUMMARY_BY_SHIPMENT } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getReceiptSummary: (shipmentId, params) =>
    apiClient.get(RECEIPT_SUMMARY_BY_SHIPMENT(shipmentId), { params }),
};
