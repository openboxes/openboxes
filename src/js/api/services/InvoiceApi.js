import { INVOICE_API } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  getInvoices: config => apiClient.get(INVOICE_API, config),
};
