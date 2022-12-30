// PURCHASE ORDER
export const PURCHASE_ORDER_API = '/openboxes/api/purchaseOrders';
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;
