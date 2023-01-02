// PURCHASE ORDER
export const PURCHASE_ORDER_API = '/openboxes/api/purchaseOrders';
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;

// STOCK MOVEMENT
export const STOCK_MOVEMENT_API = '/openboxes/api/stockMovements';
export const STOCK_MOVEMENT_DELETE = id => `${STOCK_MOVEMENT_API}/${id}`;
export const STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS = `${STOCK_MOVEMENT_API}/pendingRequisitionItems`;
export const STOCK_MOVEMENT_INCOMING_ITEMS = `${STOCK_MOVEMENT_API}/shippedItems`;

// STOCK TRANSFER
export const STOCK_TRANSFER_API = '/openboxes/api/stockTransfers';
export const STOCK_TRANSFER_DELETE = id => `${STOCK_TRANSFER_API}/${id}`;

// INVOICE
export const INVOICE_API = '/openboxes/api/invoices';

// PRODUCT
export const PRODUCT_API = '/openboxes/api/products';

// STOCK LIST
export const STOCK_LIST_API = '/openboxes/api/stocklists';
export const STOCK_LIST_EXPORT = id => `${STOCK_LIST_API}/${id}/export`;
export const STOCK_LIST_DELETE = id => `${STOCK_LIST_API}/${id}`;
export const STOCK_LIST_CLEAR = id => `${STOCK_LIST_API}/${id}/clear`;
export const STOCK_LIST_CLONE = id => `${STOCK_LIST_API}/${id}/clone`;
export const STOCK_LIST_PUBLISH = id => `${STOCK_LIST_API}/${id}/publish`;
export const STOCK_LIST_UNPUBLISH = id => `${STOCK_LIST_API}/${id}/unpublish`;
