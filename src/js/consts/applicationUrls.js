/**
 * Definitions of APPLICATION URLs used for redirecting to pages
 * */
import { stringifyUrl } from 'query-string';

export const CONTEXT_PATH = window.CONTEXT_PATH ?? '/openboxes';

const DASHBOARD_URL = {
  base: `${CONTEXT_PATH}/dashboard`,
};

const LOCATION_CONFIGURATION_URL = {
  base: `${CONTEXT_PATH}/locationsConfiguration`,
  create: () => `${LOCATION_CONFIGURATION_URL.base}/create`,
  edit: (id) => `${LOCATION_CONFIGURATION_URL.create()}/${id}`,
  upload: () => `${LOCATION_CONFIGURATION_URL.base}/upload`,
};

const PRODUCT_CONFIGURATION_URL = {
  base: `${CONTEXT_PATH}/productsConfiguration`,
  index: () => `${PRODUCT_CONFIGURATION_URL.base}/index`,
};

const PRODUCT_URL = {
  base: `${CONTEXT_PATH}/product`,
  list: () => `${PRODUCT_URL.base}/list`,
  create: () => `${PRODUCT_URL.base}/create`,
  edit: (id) => `${PRODUCT_URL.base}/edit/${id}`,
  importCSV: () => `${PRODUCT_URL.base}/importAsCsv`,
};

const LOCATION_URL = {
  base: `${CONTEXT_PATH}/location`,
  list: () => `${LOCATION_URL.base}/list`,
};

const STOCK_MOVEMENT_URL = {
  base: `${CONTEXT_PATH}/stockMovement`,
  list: () => `${STOCK_MOVEMENT_URL.base}/list`,
  listInbound: () => `${STOCK_MOVEMENT_URL.list()}?direction=INBOUND`,
  listOutbound: () => `${STOCK_MOVEMENT_URL.list()}?direction=OUTBOUND`,
  listRequest: () => `${STOCK_MOVEMENT_URL.list()}?direction=OUTBOUND&sourceType=ELECTRONIC`,
  createInbound: () => `${STOCK_MOVEMENT_URL.base}/createInbound`,
  createOutbound: () => `${STOCK_MOVEMENT_URL.base}/createOutbound`,
  createRequest: () => `${STOCK_MOVEMENT_URL.base}/createRequest`,
  createCombinedShipments: () => `${STOCK_MOVEMENT_URL.base}/createCombinedShipments`,
  genericEdit: (id) => `${STOCK_MOVEMENT_URL.base}/edit/${id}`,
  editInbound: (id) => `${STOCK_MOVEMENT_URL.createInbound()}/${id}`,
  editOutbound: (id) => `${STOCK_MOVEMENT_URL.createOutbound()}/${id}`,
  editRequest: (id) => `${STOCK_MOVEMENT_URL.createRequest()}/${id}`,
  editCombinedShipments: (id) => `${STOCK_MOVEMENT_URL.createCombinedShipments()}/${id}`,
  show: (id) => `${STOCK_MOVEMENT_URL.base}/show/${id}`,
  importOutbound: () => `${STOCK_MOVEMENT_URL.base}/importOutboundStockMovement`,
};

const INVOICE_URL = {
  base: `${CONTEXT_PATH}/invoice`,
  create: () => `${INVOICE_URL.base}/create`,
  edit: (id) => `${INVOICE_URL.create()}/${id}`,
  show: (id) => `${INVOICE_URL.base}/show/${id}`,
  addDocument: (id) => `${INVOICE_URL.base}/addDocument/${id}`,
};

const PUTAWAY_URL = {
  base: `${CONTEXT_PATH}/putaway`,
  create: () => `${PUTAWAY_URL.base}/create`,
  edit: (id) => `${PUTAWAY_URL.create()}/${id}`,
};

const STOCK_TRANSFER_URL = {
  base: `${CONTEXT_PATH}/stockTransfer`,
  create: () => `${STOCK_TRANSFER_URL.base}/create`,
  createOutbound: () => `${STOCK_TRANSFER_URL.base}/createOutboundReturn`,
  createInbound: () => `${STOCK_TRANSFER_URL.base}/createInboundReturn`,
  genericEdit: (id) => `${STOCK_TRANSFER_URL.base}/edit/${id}`,
  createById: (id) => `${STOCK_TRANSFER_URL.create()}/${id}`,
  edit: (id) => `${STOCK_TRANSFER_URL.base}/edit/${id}`,
  editOutbound: (id) => `${STOCK_TRANSFER_URL.createOutbound()}/${id}`,
  editInbound: (id) => `${STOCK_TRANSFER_URL.createInbound()}/${id}`,
  show: (id) => `${STOCK_TRANSFER_URL.base}/show/${id}`,
  print: (id) => `${STOCK_TRANSFER_URL.base}/print/${id}`,
};

const ORDER_URL = {
  base: `${CONTEXT_PATH}/order`,
  list: () => `${ORDER_URL.base}/list`,
  create: () => `${ORDER_URL.base}/create`,
  show: (id) => `${ORDER_URL.base}/show/${id}`,
  print: (id) => `${ORDER_URL.base}/print/${id}`,
  addComment: (id) => `${ORDER_URL.base}/addComment/${id}`,
  addDocument: (id) => `${ORDER_URL.base}/addDocument/${id}`,
  placeOrder: (id) => `${ORDER_URL.base}/placeOrder/${id}`,
};

const PURCHASE_ORDER_URL = {
  base: `${CONTEXT_PATH}/purchaseOrder`,
  create: () => `${PURCHASE_ORDER_URL.base}/create`,
  edit: (id) => `${PURCHASE_ORDER_URL.base}/edit/${id}`,
  addItems: (id) => `${PURCHASE_ORDER_URL.base}/addItems/${id}`,
};

const INVENTORY_ITEM_URL = {
  base: `${CONTEXT_PATH}/inventoryItem`,
  showStockCard: (id, params = {}) => stringifyUrl({
    url: `${INVENTORY_ITEM_URL.base}/showStockCard/${id}`,
    query: { ...params },
  }),
};

const INVENTORY_URL = {
  base: `${CONTEXT_PATH}/inventory`,
  showTransaction: (id) => `${INVENTORY_URL.base}/showTransaction/${id}`,
};

const REQUISITION_TEMPLATE_URL = {
  base: `${CONTEXT_PATH}/requisitionTemplate`,
  create: () => `${REQUISITION_TEMPLATE_URL.base}/create`,
  show: (id) => `${REQUISITION_TEMPLATE_URL.base}/show/${id}`,
  edit: (id) => `${REQUISITION_TEMPLATE_URL.base}/edit/${id}`,
  batch: (id) => `${REQUISITION_TEMPLATE_URL.base}/batch/${id}`,
  editHeader: (id) => `${REQUISITION_TEMPLATE_URL.base}/editHeader/${id}`,
};

const STOCKLIST_URL = {
  base: `${CONTEXT_PATH}/stocklist`,
  pdf: (id) => `${STOCKLIST_URL.base}/renderPdf/${id}`,
  csv: (id) => `${STOCKLIST_URL.base}/generateCsv/${id}`,
};

const REPLENISHMENT_URL = {
  base: `${CONTEXT_PATH}/replenishment`,
  create: () => `${REPLENISHMENT_URL.base}/create`,
  edit: (id) => `${REPLENISHMENT_URL.create()}/${id}`,
  print: (id) => `${REPLENISHMENT_URL.base}/print/${id}`,
};

const CATEGORY_URL = {
  base: `${CONTEXT_PATH}/category`,
  tree: () => `${CATEGORY_URL.base}/tree`,
};

const PRODUCT_SUPPLIER_URL = {
  base: `${CONTEXT_PATH}/productSupplier`,
  list: () => `${PRODUCT_SUPPLIER_URL.base}/list`,
  create: () => `${PRODUCT_SUPPLIER_URL.base}/create`,
  edit: (id) => `${PRODUCT_SUPPLIER_URL.base}/create/${id}`,
  export: () => `${PRODUCT_SUPPLIER_URL.base}/export?format=xls`,
};

const CYCLE_COUNT = {
  base: `${CONTEXT_PATH}/inventory/cycleCount`,
  list: (tab) => `${CYCLE_COUNT.base}?tab=${tab}`,
  countStep: () => `${CYCLE_COUNT.base}/count`,
  resolveStep: () => `${CYCLE_COUNT.base}/resolve`,
};

export {
  CATEGORY_URL,
  CYCLE_COUNT,
  DASHBOARD_URL,
  INVENTORY_ITEM_URL,
  INVENTORY_URL,
  INVOICE_URL,
  LOCATION_CONFIGURATION_URL,
  LOCATION_URL,
  ORDER_URL,
  PRODUCT_CONFIGURATION_URL,
  PRODUCT_SUPPLIER_URL,
  PRODUCT_URL,
  PURCHASE_ORDER_URL,
  PUTAWAY_URL,
  REPLENISHMENT_URL,
  REQUISITION_TEMPLATE_URL,
  STOCK_MOVEMENT_URL,
  STOCK_TRANSFER_URL,
  STOCKLIST_URL,
};
