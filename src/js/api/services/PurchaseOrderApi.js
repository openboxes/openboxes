import {
  PAYMENT_TERMS_OPTION,
  PURCHASE_ORDER_API,
  PURCHASE_ORDER_DELETE,
  PURCHASE_ORDER_ROLLBACK_ORDER,
} from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  deleteOrder: id => apiClient.delete(PURCHASE_ORDER_DELETE(id)),
  rollbackOrder: id => apiClient.post(PURCHASE_ORDER_ROLLBACK_ORDER(id)),
  getOrders: config => apiClient.get(PURCHASE_ORDER_API, config),
  getPaymentTerms: config => apiClient.get(PAYMENT_TERMS_OPTION, config),
};
