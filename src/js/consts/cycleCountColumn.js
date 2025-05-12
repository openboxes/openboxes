const columns = {
  EXPIRATION_DATE: 'inventoryItem.expirationDate',
  QUANTITY_COUNTED: 'quantityCounted',
  QUANTITY_RECOUNTED: 'quantityRecounted',
  BIN_LOCATION: 'binLocation',
  ROOT_CAUSE: 'rootCause',
  COMMENT: 'comment',
  LOT_NUMBER: 'inventoryItem.lotNumber',
  COUNT_DIFFERENCE: 'countDifference',
  RECOUNT_DIFFERENCE: 'recountDifference',
  ACTIONS: 'actions',
  SELECTED: 'selected',
  DATE_LAST_COUNTED: 'dateLastCount',
  PRODUCT: 'product',
  CATEGORY_NAME: 'category.name',
  CATEGORY: 'category',
  INTERNAL_LOCATIONS: 'internalLocations',
  TAGS: 'tags',
  PRODUCT_CATALOGS: 'productCatalogs',
  ABC_CLASS: 'abcClass',
  QUANTITY_ON_HAND: 'quantityOnHand',
  STATUS: 'status',
  ALIGNMENT: 'alignment',
  TRANSACTION_TYPE: 'transactionType',
  // fix after getting appropriate property in response
  RECORDED: 'transactionDetails.transactionDate',
  // fix after getting appropriate property in response
  TRANSACTION_ID: 'transactionDetails.transactionNumber',
  // fix after getting appropriate property in response
  QTY_BEFORE: 'initialCount.quantityOnHand',
  QTY_AFTER: 'finalCount.quantityCounted',
  DIFFERENCE: 'difference',
  ROOT_CAUSES: 'rootCause',
  COMMENTS: 'comments',
};

export default columns;
