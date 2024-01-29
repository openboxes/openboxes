import { INVOICE_API, INVOICE_ITEMS_API } from 'api/urls';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

export default {
  getInvoices: config => apiClient.get(INVOICE_API, config),
  downloadInvoices: params => exportFileFromAPI({
    url: INVOICE_API,
    params,
  }),
  downloadInvoiceLineDetails: params => exportFileFromAPI({
    url: INVOICE_ITEMS_API,
    params,
  }),
};
