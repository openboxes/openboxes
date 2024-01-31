import { INVOICE_ITEM_API, VALIDATE_INVOICE_ITEM } from 'api/urls';
import { apiClientCustomResponseHandler } from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

export default {
  validateInvoiceItem: invoiceItem =>
    apiClientCustomResponseHandler.post(VALIDATE_INVOICE_ITEM(invoiceItem?.id), invoiceItem),
  downloadInvoiceLineDetails: params => exportFileFromAPI({
    url: INVOICE_ITEM_API,
    params,
  }),
};
