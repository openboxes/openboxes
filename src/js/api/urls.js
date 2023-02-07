// PURCHASE ORDER
export const PURCHASE_ORDER_API = '/api/purchaseOrders';
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;

// STOCK MOVEMENT
export const STOCK_MOVEMENT_API = '/api/stockMovements';
export const STOCK_MOVEMENT_DELETE = id => `${STOCK_MOVEMENT_API}/${id}`;
export const STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS = `${STOCK_MOVEMENT_API}/pendingRequisitionItems`;
export const STOCK_MOVEMENT_INCOMING_ITEMS = `${STOCK_MOVEMENT_API}/shippedItems`;

// STOCK TRANSFER
export const STOCK_TRANSFER_API = '/api/stockTransfers';
export const STOCK_TRANSFER_DELETE = id => `${STOCK_TRANSFER_API}/${id}`;

// INVOICE
export const INVOICE_API = '/api/invoices';

// PRODUCT
export const PRODUCT_API = '/api/products';

// STOCK LIST
export const STOCKLIST_API = '/api/stocklists';
export const STOCKLIST_EXPORT = id => `${STOCKLIST_API}/${id}/export`;
export const STOCKLIST_DELETE = id => `${STOCKLIST_API}/${id}`;
export const STOCKLIST_CLEAR = id => `${STOCKLIST_API}/${id}/clear`;
export const STOCKLIST_CLONE = id => `${STOCKLIST_API}/${id}/clone`;
export const STOCKLIST_PUBLISH = id => `${STOCKLIST_API}/${id}/publish`;
export const STOCKLIST_UNPUBLISH = id => `${STOCKLIST_API}/${id}/unpublish`;
