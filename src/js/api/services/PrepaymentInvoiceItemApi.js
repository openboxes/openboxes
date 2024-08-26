import { PREPAYMENT_INVOICE_ITEM_BY_ID } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  deletePrepaymentInvoiceItem: (invoiceItemId) =>
    apiClient.delete(PREPAYMENT_INVOICE_ITEM_BY_ID(invoiceItemId)),
};
