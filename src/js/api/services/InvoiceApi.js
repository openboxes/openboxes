import {
  INVOICE_API,
  INVOICE_BY_ID,
  INVOICE_ITEM_CANDIDATES,
  INVOICE_ITEMS, INVOICE_ORDERS,
  INVOICE_POST,
  INVOICE_SHIPMENTS,
  INVOICE_SUBMIT,
  REMOVE_INVOICE_ITEM,
} from 'api/urls';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

export default {
  getInvoices: (config) => apiClient.get(INVOICE_API, config),
  getInvoice: (invoiceId, config) => apiClient.get(INVOICE_BY_ID(invoiceId), config),
  saveInvoice: (invoiceId, payload) => apiClient.post(INVOICE_BY_ID(invoiceId), payload),
  submitInvoice: (invoiceId) => apiClient.post(INVOICE_SUBMIT(invoiceId)),
  postInvoice: (invoiceId) => apiClient.post(INVOICE_POST(invoiceId)),
  getInvoiceItems: (invoiceId, config) => apiClient.get(INVOICE_ITEMS(invoiceId), config),
  saveInvoiceItems: (invoiceId, payload) => apiClient.post(INVOICE_ITEMS(invoiceId), payload),
  removeInvoiceItem: (invoiceId) => apiClient.delete(REMOVE_INVOICE_ITEM(invoiceId)),
  getInvoiceOrders: (invoiceId, config) => apiClient.get(INVOICE_ORDERS(invoiceId), config),
  getInvoiceShipments: (invoiceId, config) => apiClient.get(INVOICE_SHIPMENTS(invoiceId), config),
  saveInvoiceItemsCandidates: (invoiceId, config) =>
    apiClient.post(INVOICE_ITEM_CANDIDATES(invoiceId), config),
  downloadInvoices: (params) => exportFileFromAPI({
    url: INVOICE_API,
    params,
  }),
};
