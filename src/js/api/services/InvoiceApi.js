import { INVOICE_API, VALIDATE_INVOICE_ITEM } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getInvoices: config => apiClient.get(INVOICE_API, config),
  validateInvoiceItem: ({ invoiceItemId, quantity }) =>
    apiClient.post(VALIDATE_INVOICE_ITEM, { invoiceItemId, quantity }),
};
