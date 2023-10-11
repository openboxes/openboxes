const API = '/openboxes/api';
const GENERIC_API = `${API}/generic`;

// PURCHASE ORDER
export const PURCHASE_ORDER_API = `${API}/purchaseOrders`;
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;

// STOCK MOVEMENT
export const STOCK_MOVEMENT_API = `${API}/stockMovements`;
export const STOCK_MOVEMENT_DELETE = id => `${STOCK_MOVEMENT_API}/${id}`;
export const STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS = `${STOCK_MOVEMENT_API}/pendingRequisitionItems`;
export const STOCK_MOVEMENT_INCOMING_ITEMS = `${STOCK_MOVEMENT_API}/shippedItems`;
export const STOCK_MOVEMENT_UPDATE_STATUS = id => `${STOCK_MOVEMENT_API}/${id}/status`;
export const STOCK_MOVEMENT_ROLLBACK_APPROVAL = id => `${STOCK_MOVEMENT_API}/${id}/rollbackApproval`;

// STOCK TRANSFER
export const STOCK_TRANSFER_API = `${API}/stockTransfers`;
export const STOCK_TRANSFER_DELETE = id => `${STOCK_TRANSFER_API}/${id}`;

// INVOICE
export const INVOICE_API = `${API}/invoices`;

// PRODUCT
export const PRODUCT_API = `${API}/products`;

// STOCK LIST
export const STOCKLIST_API = `${API}/stocklists`;
export const STOCKLIST_EXPORT = id => `${STOCKLIST_API}/${id}/export`;
export const STOCKLIST_DELETE = id => `${STOCKLIST_API}/${id}`;
export const STOCKLIST_CLEAR = id => `${STOCKLIST_API}/${id}/clear`;
export const STOCKLIST_CLONE = id => `${STOCKLIST_API}/${id}/clone`;
export const STOCKLIST_PUBLISH = id => `${STOCKLIST_API}/${id}/publish`;
export const STOCKLIST_UNPUBLISH = id => `${STOCKLIST_API}/${id}/unpublish`;

// GL ACCOUNTS
export const GL_ACCOUNTS_OPTION = `${API}/glAccountOptions`;

// PRODUCT GROUP
export const PRODUCT_GROUP_OPTION = `${API}/productGroupOptions`;

// SHIPMENT TYPES
export const SHIPMENT_TYPES = `${GENERIC_API}/shipmentType`;

// PAYMENT TERMS
export const PAYMENT_TERMS_OPTION = `${API}/paymentTermOptions`;

// USERS
export const USERS_OPTIONS = `${API}/users`;

// LOCATIONS
export const LOCATION_API = `${API}/locations`;
export const LOCATION_TYPES = `${LOCATION_API}/locationTypes`;
