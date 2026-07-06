import { RECEIPT_ITEMS_BATCH, RECEIPT_START, RECEIPT_SUMMARY_BY_SHIPMENT } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getReceiptSummary: (shipmentId, params) =>
    apiClient.get(RECEIPT_SUMMARY_BY_SHIPMENT(shipmentId), { params }),
  startReceipt: (shipmentId) => apiClient.post(RECEIPT_START(shipmentId)),
  updateItemsBatch: (receiptId, payload) =>
    apiClient.post(RECEIPT_ITEMS_BATCH(receiptId), payload),
};
