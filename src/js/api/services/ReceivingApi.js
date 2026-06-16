import { RECEIPT_BY_SHIPMENT, RECEIPT_SUMMARY_BY_SHIPMENT } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getReceipt: (shipmentId, params) =>
    apiClient.get(RECEIPT_BY_SHIPMENT(shipmentId), { params }),
  getReceiptSummary: (shipmentId, params) =>
    apiClient.get(RECEIPT_SUMMARY_BY_SHIPMENT(shipmentId), { params }),
};
