package org.pih.warehouse.importer

/**
 * Enumerates the different features/data structures that support importing/exporting bulk data.
 *
 * This allows us to:
 * 1) Abstract away the class being bound to, so that the client doesn't need to work directly with class names
 * 2) Define default import/export configuration for a feature that binds to/from a Map or another non-concrete type
 * 3) Define multiple different default import/export configurations for the same data type
 */
enum BulkDataType {
    CATEGORY,
    CYCLE_COUNT_ITEM,
    CYCLE_COUNT_RECOUNT_ITEM,
    INVENTORY,
    INVENTORY_ITEM,
    INVENTORY_LEVEL,
    LOCATION,
    OUTBOUND_STOCK_MOVEMENT,
    PACKING_LIST,
    PERSON,
    PRODUCT,
    PRODUCT_ASSOCIATION,
    PRODUCT_ATTRIBUTE,
    PRODUCT_CATALOG,
    PRODUCT_CATALOG_ITEM,
    PRODUCT_GROUP,
    PRODUCT_PACKAGE,
    PRODUCT_SUPPLIER,
    PRODUCT_SYNONYM,
    PRODUCT_SUPPLIER_ATTRIBUTE,
    PRODUCT_SUPPLIER_PREFERENCE,
    PURCHASE_ORDER,
    PURCHASE_ORDER_ACTUAL_READY_DATE,
    TAG,
    USER,
    USER_LOCATION,
}
