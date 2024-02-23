/**
 * Definitions of ENDPOINT URLs used for API calls
 * */

const API = '/api';
export const GENERIC_API = `${API}/generic`;
const { CONTEXT_PATH } = window;

// PURCHASE ORDER
export const PURCHASE_ORDER_API = `${API}/purchaseOrders`;
export const PURCHASE_ORDER_DELETE = id => `${PURCHASE_ORDER_API}/${id}`;
export const PURCHASE_ORDER_ROLLBACK_ORDER = id => `${PURCHASE_ORDER_API}/${id}/rollback`;

// STOCK MOVEMENT
export const STOCK_MOVEMENT_API = `${API}/stockMovements`;
export const STOCK_MOVEMENT_BY_ID = id => `${STOCK_MOVEMENT_API}/${id}`;
export const STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS = `${STOCK_MOVEMENT_API}/pendingRequisitionItems`;
export const STOCK_MOVEMENT_INCOMING_ITEMS = `${STOCK_MOVEMENT_API}/shippedItems`;
export const STOCK_MOVEMENT_UPDATE_STATUS = id => `${STOCK_MOVEMENT_API}/${id}/status`;
export const STOCK_MOVEMENT_ROLLBACK_APPROVAL = id => `${STOCK_MOVEMENT_API}/${id}/rollbackApproval`;
export const STOCK_MOVEMENT_ITEMS = id => `${STOCK_MOVEMENT_BY_ID(id)}/stockMovementItems`;
export const STOCK_MOVEMENT_UPDATE_ITEMS = id => `${STOCK_MOVEMENT_BY_ID(id)}/updateItems`;
export const STOCK_MOVEMENT_REMOVE_ALL_ITEMS = id => `${STOCK_MOVEMENT_BY_ID(id)}/removeAllItems`;
export const STOCK_MOVEMENT_STATUS = id => `${STOCK_MOVEMENT_BY_ID(id)}/status`;

// STOCK MOVEMENT ITEMS
export const STOCK_MOVEMENT_ITEM_API = `${API}/stockMovementItems`;
export const STOCK_MOVEMENT_ITEM_REMOVE = id => `${STOCK_MOVEMENT_ITEM_API}/${id}/removeItem`;

// STOCK TRANSFER
export const STOCK_TRANSFER_API = `${API}/stockTransfers`;
export const STOCK_TRANSFER_DELETE = id => `${STOCK_TRANSFER_API}/${id}`;

// INVOICE
export const INVOICE_API = `${API}/invoices`;

// INVOICE ITEM
export const INVOICE_ITEM_API = `${API}/invoiceItems`;
export const VALIDATE_INVOICE_ITEM = id => `${INVOICE_ITEM_API}/${id}/validation`;

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

// PREFERENCE TYPES
export const PREFERENCE_TYPE_OPTIONS = `${API}/preferenceTypeOptions`;

// RATING TYPES
export const RATING_TYPE_OPTIONS = `${API}/ratingTypeCodeOptions`;

// LOCATIONS
export const LOCATION_API = `${API}/locations`;
export const LOCATION_TYPES = `${LOCATION_API}/locationTypes`;
export const LOCATION_TEMPLATE = `${CONTEXT_PATH}${LOCATION_API}/template`;
export const LOCATION_IMPORT = `${CONTEXT_PATH}${LOCATION_API}/importCsv`;
export const LOCATION = id => `${LOCATION_API}/${id}`;

// PUTAWAY
export const PUTAWAY_GENERATE_PDF = id => `/putAway/generatePdf/${id}`;

// SUPPORT LINKS
export const SUPPORT_LINKS = `${CONTEXT_PATH}${API}/supportLinks`;

// COMBINED SHIPMENT ITEMS
export const COMBINED_SHIPMENT_ITEMS_API = `${API}/combinedShipmentItems`;
export const COMBINED_SHIPMENT_ITEMS_IMPORT_TEMPLATE = (id) => `${COMBINED_SHIPMENT_ITEMS_API}/importTemplate/${id}`;
export const COMBINED_SHIPMENT_ITEMS_EXPORT_TEMPLATE = `${COMBINED_SHIPMENT_ITEMS_API}/exportTemplate`;

export const HELPSCOUT_CONFIGURATION = `${CONTEXT_PATH}${API}/helpscout/configuration/`;

export const ENABLE_LOCALIZATION = `${CONTEXT_PATH}/user/enableLocalizationMode`;
export const DISABLE_LOCALIZATION = (languageCode) => {
  if (languageCode) {
    return `${CONTEXT_PATH}/user/disableLocalizationMode?locale=${languageCode}`;
  }
  return `${CONTEXT_PATH}/user/disableLocalizationMode`;
};

export const GLOBAL_SEARCH = term => `${CONTEXT_PATH}/dashboard/globalSearch?searchTerms=${term}`;

// PRODUCT SUPPLIER
export const PRODUCT_SUPPLIER_API = `${API}/productSuppliers`;
export const PRODUCT_SUPPLIER_BY_ID = (id) => `${PRODUCT_SUPPLIER_API}/${id}`;

// UNIT OF MEASURE
export const UNIT_OF_MEASURE_API = `${API}/unitOfMeasures`;
// Currencies don't use url in plural form, do not change it to UNIT_OF_MEASURE_API!
export const CURRENCIES_OPTIONS = `${API}/unitOfMeasure/currencies`;
