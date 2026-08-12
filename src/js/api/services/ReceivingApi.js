import {
  RECEIPT_COMPLETE,
  RECEIPT_ITEM_COMMENTS,
  RECEIPT_ITEMS_BATCH,
  RECEIPT_ITEMS_BY_SHIPMENT_ITEM,
  RECEIPT_START,
  RECEIPT_SUMMARY_BY_SHIPMENT,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getReceiptSummary: (shipmentId, params) =>
    apiClient.get(RECEIPT_SUMMARY_BY_SHIPMENT(shipmentId), { params }),
  startReceipt: (shipmentId) => apiClient.post(RECEIPT_START(shipmentId)),
  updateItemsBatch: (receiptId, payload) =>
    apiClient.post(RECEIPT_ITEMS_BATCH(receiptId), payload),
  completeReceipt: (receiptId, payload) =>
    apiClient.post(RECEIPT_COMPLETE(receiptId), payload),
  editReceivingInfo: (receiptId, shipmentItemId, payload) =>
    apiClient.post(RECEIPT_ITEMS_BY_SHIPMENT_ITEM(receiptId, shipmentItemId), payload),
  createReceiptItemComment: (receiptItemId, payload) =>
    apiClient.post(RECEIPT_ITEM_COMMENTS(receiptItemId), payload),
  updateReceiptItemComment: (receiptItemId, payload) =>
    apiClient.put(RECEIPT_ITEM_COMMENTS(receiptItemId), payload),
};
