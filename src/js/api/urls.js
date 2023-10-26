const API = '/api';
const GENERIC_API = `${API}/generic`;
const { CONTEXT_PATH } = window;

// PURCHASE ORDER
export const PURCHASE_ORDER_API = `${API}/purchaseOrders`;
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;

// STOCK MOVEMENT
export const STOCK_MOVEMENT = `${CONTEXT_PATH}/stockMovement`;
export const STOCK_MOVEMENT_API = `${API}/stockMovements`;
export const STOCK_MOVEMENT_DELETE = id => `${STOCK_MOVEMENT_API}/${id}`;
export const STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS = `${STOCK_MOVEMENT_API}/pendingRequisitionItems`;
export const STOCK_MOVEMENT_INCOMING_ITEMS = `${STOCK_MOVEMENT_API}/shippedItems`;
export const STOCK_MOVEMENT_SHOW = id => `${STOCK_MOVEMENT}/show/${id}`;
export const STOCK_MOVEMENT_UPDATE_STATUS = id => `${STOCK_MOVEMENT_API}/${id}/status`;
export const STOCK_MOVEMENT_ROLLBACK_APPROVAL = id => `${STOCK_MOVEMENT_API}/${id}/rollbackApproval`;

// STOCK TRANSFER
export const STOCK_TRANSFER_API = `${API}/stockTransfers`;
export const STOCK_TRANSFER_DELETE = id => `${STOCK_TRANSFER_API}/${id}`;
export const STOCK_TRANSFER_PRINT = id => `${CONTEXT_PATH}/stockTransfer/print/${id}`;

// INVOICE
export const INVOICE_API = `${API}/invoices`;

// PRODUCT
export const PRODUCT_API = `${API}/products`;
export const INVENTORY_ITEM = (productCode, lotNumber) => `${CONTEXT_PATH}/${PRODUCT_API}/${productCode}/inventoryItems/${lotNumber}`;

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
export const LOCATION_TEMPLATE = `${CONTEXT_PATH}${LOCATION_API}/template`;
export const LOCATION_IMPORT = `${CONTEXT_PATH}${LOCATION_API}/importCsv`;
export const LOCATION = id => `${LOCATION_API}/${id}`;

// ORDER
export const ORDER = `${CONTEXT_PATH}/order`;
export const ORDER_SHOW = id => `${ORDER}/show/${id}`;

// REPLENISHMENT
export const REPLENISHMENT_PRINT = id => `${CONTEXT_PATH}/replenishment/print/${id}`;

// PUTAWAY
export const PUTAWAY_GENERATE_PDF = id => `/putAway/generatePdf/${id}`;

// SUPPORT LINKS
export const SUPPORT_LINKS = `${CONTEXT_PATH}${API}/supportLinks`;
