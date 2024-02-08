import { INVOICE_API, INVOICE_ITEMS, REMOVE_INVOICE_ITEM } from 'api/urls';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

export default {
  getInvoices: config => apiClient.get(INVOICE_API, config),
  getInvoiceItems: (invoiceId, config) => apiClient.get(INVOICE_ITEMS(invoiceId), config),
  saveInvoiceItems: (invoiceId, payload) => apiClient.post(INVOICE_ITEMS(invoiceId), payload),
  removeInvoiceItem: (invoiceId) => apiClient.delete(REMOVE_INVOICE_ITEM(invoiceId)),
  downloadInvoices: params => exportFileFromAPI({
    url: INVOICE_API,
    params,
  }),
};
