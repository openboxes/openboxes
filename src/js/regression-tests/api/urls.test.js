import { urlInterceptor } from 'utils/apiClient';

// Not covered on OBAF (OBAF lacks the code): PREFERRED_BIN_LOCATIONS, the
// RECEIPT_* receiving v2 builders, AVAILABILITY_IN_ALL_DEPOTS and RECEIVING_INTERNAL_LOCATIONS.
// Adapted for OBAF: PUTAWAY_GENERATE_PDF is /putaway/... (OBAF renamed the controller path),
// and OBAF's own PUTAWAY_TASK_API, PICK_TASK_API and AUTO_ISSUANCE_TRANSACTIONS_REPORT(_CSV) are
// covered.

// urls.js reads window.CONTEXT_PATH once, when it is first imported:
// load a fresh copy per context path.
const loadUrls = (contextPath) => {
  window.CONTEXT_PATH = contextPath;
  let urls;
  jest.isolateModules(() => {
    // eslint-disable-next-line global-require
    urls = require('api/urls');
  });
  return urls;
};

describe('api/urls', () => {
  let urls;

  beforeAll(() => {
    urls = loadUrls('/openboxes');
  });

  afterAll(() => {
    delete window.CONTEXT_PATH;
  });

  describe('builders relative to the API root (the interceptor adds the context path)', () => {
    it.each([
      ['PURCHASE_ORDER_DELETE', ['po1'], '/api/purchaseOrders/po1'],
      ['PURCHASE_ORDER_ROLLBACK_ORDER', ['po1'], '/api/purchaseOrders/po1/rollback'],
      ['STOCK_MOVEMENT_BY_ID', ['sm1'], '/api/stockMovements/sm1'],
      ['STOCK_MOVEMENT_UPDATE_STATUS', ['sm1'], '/api/stockMovements/sm1/status'],
      ['STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS', ['sm1'], '/api/stockMovements/sm1/updateInventoryItems'],
      ['STOCK_MOVEMENT_UPDATE_REQUISITION', ['sm1'], '/api/stockMovements/sm1/updateRequisition'],
      ['STOCK_MOVEMENT_ROLLBACK_APPROVAL', ['sm1'], '/api/stockMovements/sm1/rollbackApproval'],
      ['STOCK_MOVEMENT_ITEMS', ['sm1'], '/api/stockMovements/sm1/stockMovementItems'],
      ['STOCK_MOVEMENT_UPDATE_ITEMS', ['sm1'], '/api/stockMovements/sm1/updateItems'],
      ['STOCK_MOVEMENT_REMOVE_ALL_ITEMS', ['sm1'], '/api/stockMovements/sm1/removeAllItems'],
      ['STOCK_MOVEMENT_STATUS', ['sm1'], '/api/stockMovements/sm1/status'],
      ['PICKLIST_ITEMS_EXPORT', ['sm1'], '/api/stockMovements/exportPickListItems/sm1'],
      ['PICKLIST_TEMPLATE_EXPORT', ['sm1'], '/api/stockMovements/picklistTemplate/sm1'],
      ['PACKLIST_TEMPLATE_EXPORT', ['sm1'], '/api/stockMovements/packlistTemplate/sm1'],
      ['PICKLIST_IMPORT', ['sm1'], '/api/stockMovements/importPickListItems/sm1'],
      ['PACKLIST_IMPORT', ['sm1'], '/api/stockMovements/importPackListItems/sm1'],
      ['STOCK_MOVEMENT_UPDATE_SHIPMENT', ['sm1'], '/api/stockMovements/sm1/updateShipment'],
      ['STOCK_MOVEMENT_UPLOAD_DOCUMENTS', ['sm1'], '/api/stockMovements/sm1/uploadDocuments'],
      ['STOCK_MOVEMENT_DOCUMENTS', ['sm1'], '/api/stockMovements/sm1/documents'],
      ['STOCK_MOVEMENT_ITEM_BY_ID', ['i1'], '/api/stockMovementItems/i1'],
      ['STOCK_MOVEMENT_ITEM_DETAILS', ['i1'], '/api/stockMovementItems/i1/details'],
      ['STOCK_MOVEMENT_ITEM_REMOVE', ['i1'], '/api/stockMovementItems/i1/removeItem'],
      ['STOCK_MOVEMENT_UPDATE_PICKLIST', ['i1'], '/api/stockMovementItems/i1/updatePicklist'],
      ['STOCK_MOVEMENT_CREATE_PICKLIST', ['i1'], '/api/stockMovementItems/i1/createPicklist'],
      ['STOCK_MOVEMENT_ITEM_REVERT_PICK', ['i1'], '/api/stockMovementItems/i1/picklistItems'],
      ['STOCK_TRANSFER_BY_ID', ['st1'], '/api/stockTransfers/st1'],
      ['STOCK_TRANSFER_REMOVE_ALL_ITEMS', ['st1'], '/api/stockTransfers/st1/removeAllItems'],
      ['STOCK_TRANSFER_ITEM_BY_ID', ['sti1'], '/api/stockTransferItems/sti1'],
      ['INVOICE_BY_ID', ['inv1'], '/api/invoices/inv1'],
      ['INVOICE_ITEMS', ['inv1'], '/api/invoices/inv1/items'],
      ['INVOICE_ITEM_CANDIDATES', ['inv1'], '/api/invoices/inv1/invoiceItemCandidates'],
      ['INVOICE_POST', ['inv1'], '/api/invoices/inv1/post'],
      ['INVOICE_SUBMIT', ['inv1'], '/api/invoices/inv1/submit'],
      ['INVOICE_ORDERS', ['inv1'], '/api/invoices/inv1/orders'],
      ['INVOICE_SHIPMENTS', ['inv1'], '/api/invoices/inv1/shipments'],
      ['REMOVE_INVOICE_ITEM', ['ii1'], '/api/invoices/ii1/removeItem'],
      ['VALIDATE_INVOICE_ITEM', ['ii1'], '/api/invoiceItems/ii1/validation'],
      ['PREPAYMENT_INVOICE_BY_ID', ['inv1'], '/api/prepaymentInvoices/inv1'],
      ['PREPAYMENT_INVOICE_INVOICE_ITEMS', ['inv1'], '/api/prepaymentInvoices/inv1/invoiceItems'],
      ['PREPAYMENT_INVOICE_ITEM_BY_ID', ['ii1'], '/api/prepaymentInvoiceItems/ii1'],
      ['STOCKLIST_EXPORT', ['sl1'], '/api/stocklists/sl1/export'],
      ['STOCKLIST_DELETE', ['sl1'], '/api/stocklists/sl1'],
      ['STOCKLIST_CLEAR', ['sl1'], '/api/stocklists/sl1/clear'],
      ['STOCKLIST_CLONE', ['sl1'], '/api/stocklists/sl1/clone'],
      ['STOCKLIST_PUBLISH', ['sl1'], '/api/stocklists/sl1/publish'],
      ['STOCKLIST_UNPUBLISH', ['sl1'], '/api/stocklists/sl1/unpublish'],
      ['LOCATION', ['loc1'], '/api/locations/loc1'],
      ['PUTAWAY_GENERATE_PDF', ['pa1'], '/putaway/generatePdf/pa1'],
      ['PUTAWAY_TASK_API', ['f1'], '/api/facilities/f1/putaway-tasks'],
      ['PICK_TASK_API', ['f1'], '/api/facilities/f1/pick-tasks'],
      ['COMBINED_SHIPMENT_ITEMS_IMPORT_TEMPLATE', ['o1'], '/api/combinedShipmentItems/importTemplate/o1'],
      ['PRODUCT_SUPPLIER_BY_ID', ['ps1'], '/api/productSuppliers/ps1'],
      ['PRODUCT_SUPPLIER_PREFERENCES_BY_ID', ['psp1'], '/api/productSupplierPreferences/psp1'],
      ['PRODUCT_CLASSIFICATIONS_API', ['f1'], '/api/facilities/f1/products/classifications'],
      ['PICKLIST_CLEAR', ['pl1'], '/api/picklists/pl1/items'],
      ['INVENTORY_API', ['f1'], '/api/facilities/f1/inventories'],
      ['REORDER_REPORT', ['f1'], '/api/facilities/f1/inventories/reorderReport'],
    ])('%s(%j) is %s', (name, args, expected) => {
      expect(urls[name](...args)).toBe(expected);
    });
  });

  describe('cycle count builders', () => {
    it.each([
      ['CYCLE_COUNT', ['f1'], '/api/facilities/f1/cycle-counts'],
      ['CYCLE_COUNT_CANDIDATES', ['f1'], '/api/facilities/f1/cycle-counts/candidates'],
      ['CYCLE_COUNT_PENDING_REQUESTS', ['f1'], '/api/facilities/f1/cycle-counts/requests/pending'],
      ['CYCLE_COUNT_REQUESTS', ['f1'], '/api/facilities/f1/cycle-counts/requests/batch'],
      ['CYCLE_COUNT_REQUESTS_BATCH', ['f1'], '/api/facilities/f1/cycle-counts/requests/batch'],
      ['CYCLE_COUNT_ITEM', ['f1', 'ci1'], '/api/facilities/f1/cycle-counts/items/ci1'],
      ['CYCLE_COUNT_ITEMS', ['f1', 'cc1'], '/api/facilities/f1/cycle-counts/cc1/items'],
      ['CYCLE_COUNT_ITEMS_BATCH', ['f1', 'cc1'], '/api/facilities/f1/cycle-counts/cc1/items/batch'],
      ['CYCLE_COUNT_ITEMS_BATCH_ROOT', ['f1'], '/api/facilities/f1/cycle-counts/items/batch'],
      ['CYCLE_COUNT_SUBMIT_COUNT', ['f1', 'cc1'], '/api/facilities/f1/cycle-counts/cc1/count'],
      ['CYCLE_COUNT_SUBMIT_RECOUNT', ['f1', 'cc1'], '/api/facilities/f1/cycle-counts/cc1/recount'],
      ['CYCLE_COUNT_ITEMS_IMPORT', ['f1'], '/api/facilities/f1/cycle-counts/items/upload/count'],
      ['CYCLE_COUNT_ITEMS_IMPORT_RECOUNT', ['f1'], '/api/facilities/f1/cycle-counts/items/upload/recount'],
    ])('%s(%j) is %s', (name, args, expected) => {
      expect(urls[name](...args)).toBe(expected);
    });

    it.each([
      ['CYCLE_COUNT_START', '/api/facilities/f1/cycle-counts/start/batch'],
      ['CYCLE_COUNT_RECOUNT_START', '/api/facilities/f1/cycle-counts/recount/start/batch'],
    ])('%s asks for a file format only when one is given', (name, base) => {
      expect(urls[name]('f1')).toBe(base);
      expect(urls[name]('f1', null)).toBe(base);
      expect(urls[name]('f1', 'pdf')).toBe(`${base}?format=pdf`);
    });

    it('CYCLE_COUNT_REFRESH_ITEMS adds the out-of-stock flag only when it is set', () => {
      const base = '/api/facilities/f1/cycle-counts/cc1/refresh';
      expect(urls.CYCLE_COUNT_REFRESH_ITEMS('f1', 'cc1')).toBe(base);
      expect(urls.CYCLE_COUNT_REFRESH_ITEMS('f1', 'cc1', false)).toBe(base);
      expect(urls.CYCLE_COUNT_REFRESH_ITEMS('f1', 'cc1', true))
        .toBe(`${base}?removeOutOfStockItemsImplicitly=true`);
    });
  });

  describe('fixed endpoints', () => {
    it.each([
      ['GENERIC_API', '/api/generic'],
      ['STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS', '/api/stockMovements/pendingRequisitionItems'],
      ['STOCK_MOVEMENT_INCOMING_ITEMS', '/api/stockMovements/shippedItems'],
      ['PACKING_LIST_TEMPLATE', '/api/stockMovements/packingList/template'],
      ['STOCK_TRANSFER_CANDIDATES', '/api/stockTransfers/candidates'],
      ['LOT_NUMBERS_WITH_EXPIRATION_DATE', '/api/products/inventoryItems/lotNumbersWithExpirationDate'],
      ['AVAILABLE_ITEMS', '/api/products/availableItems'],
      ['SHIPMENT_TYPES', '/api/generic/shipmentType'],
      ['LOCATION_TYPES', '/api/locations/locationTypes'],
      ['COMBINED_SHIPMENT_ITEMS_EXPORT_TEMPLATE', '/api/combinedShipmentItems/exportTemplate'],
      ['PRODUCT_SUPPLIER_EXPORT', '/api/productSuppliers/export'],
      ['UNIT_OF_MEASURE_OPTIONS', '/api/unitOfMeasures/options'],
      // Singular on purpose, see the comment in urls.js.
      ['CURRENCIES_OPTIONS', '/api/unitOfMeasure/currencies'],
      ['PRODUCT_SUPPLIER_PREFERENCE_BATCH', '/api/productSupplierPreferences/batch'],
      ['PRODUCT_SUPPLIER_ATTRIBUTE_BATCH', '/api/productSupplierAttributes/batch'],
      ['FULFILLMENT_VALIDATION', '/api/fulfillments/validate'],
      ['CYCLE_COUNT_SUMMARY_REPORT', '/api/reports/cycle-count-summary'],
      ['INVENTORY_AUDIT_SUMMARY_REPORT_CSV', '/api/reports/inventory-audit-summary.csv'],
      ['INVENTORY_TRANSACTIONS_SUMMARY_CSV', '/api/reports/inventory-transactions-summary.csv'],
      ['AUTO_ISSUANCE_TRANSACTIONS_REPORT', '/api/reports/auto-issuance-transactions'],
      ['AUTO_ISSUANCE_TRANSACTIONS_REPORT_CSV', '/api/reports/auto-issuance-transactions.csv'],
      ['INDICATORS_PRODUCTS_INVENTORIED', '/api/reports/indicators/productsInventoried'],
      ['INDICATORS_INVENTORY_SHRINKAGE', '/api/reports/indicators/inventoryShrinkage'],
      ['INDICATORS_INVENTORY_ACCURACY', '/api/reports/indicators/inventoryAccuracy'],
      ['EXPIRATION_HISTORY_REPORT', '/api/inventories/expirationHistoryReport'],
    ])('%s is %s', (name, expected) => {
      expect(urls[name]).toBe(expected);
    });
  });

  describe('URLs that carry the context path', () => {
    it.each([
      ['LOCATION_TEMPLATE', '/openboxes/api/locations/template'],
      ['LOCATION_IMPORT', '/openboxes/api/locations/importCsv'],
      ['SUPPORT_LINKS', '/openboxes/api/supportLinks'],
      ['HELPSCOUT_CONFIGURATION', '/openboxes/api/helpscout/configuration/'],
      ['ENABLE_LOCALIZATION', '/openboxes/user/enableLocalizationMode'],
      ['PACKING_LIST', '/openboxes/packingList'],
      ['IMPORT_PACKING_LIST', '/openboxes/packingList/upload'],
    ])('%s is %s', (name, expected) => {
      expect(urls[name]).toBe(expected);
    });

    it('follows the context path the page was served under', () => {
      const wmsUrls = loadUrls('/wms');
      window.CONTEXT_PATH = '/openboxes';

      expect(wmsUrls.LOCATION_TEMPLATE).toBe('/wms/api/locations/template');
      expect(wmsUrls.IMPORT_PACKING_LIST).toBe('/wms/packingList/upload');
      expect(wmsUrls.DISABLE_LOCALIZATION('fr')).toBe('/wms/user/disableLocalizationMode?locale=fr');
    });

    it('DISABLE_LOCALIZATION switches to the given language, or just leaves localization mode', () => {
      expect(urls.DISABLE_LOCALIZATION('es')).toBe('/openboxes/user/disableLocalizationMode?locale=es');
      expect(urls.DISABLE_LOCALIZATION()).toBe('/openboxes/user/disableLocalizationMode');
      expect(urls.DISABLE_LOCALIZATION('')).toBe('/openboxes/user/disableLocalizationMode');
    });

    it('GLOBAL_SEARCH sends the search terms to the dashboard search', () => {
      expect(urls.GLOBAL_SEARCH('ibuprofen')).toBe('/openboxes/dashboard/globalSearch?searchTerms=ibuprofen');
    });

    it('INVENTORY_ITEM resolves to the product lot endpoint once it goes through the API client', () => {
      const url = urls.INVENTORY_ITEM('p1', 'L1');

      expect(url.startsWith('/openboxes/')).toBe(true);
      expect(urlInterceptor({ url }).url).toBe('/openboxes/api/products/p1/inventoryItems/L1');
    });
  });
});
