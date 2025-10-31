import { VALIDATE_INVOICE_ITEM } from 'api/urls';
import { apiClientCustomResponseHandler } from 'utils/apiClient';

export default {
  validateInvoiceItem: (invoiceItem) =>
    apiClientCustomResponseHandler.post(VALIDATE_INVOICE_ITEM(invoiceItem?.id), invoiceItem),
};
