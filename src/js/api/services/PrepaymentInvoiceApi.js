import { PREPAYMENT_INVOICE_INVOICE_ITEMS } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  updateInvoiceItems: (invoiceId, payload) =>
    apiClient.post(PREPAYMENT_INVOICE_INVOICE_ITEMS(invoiceId), payload),
};
