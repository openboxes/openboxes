// Row-derived states used by the receipt status filter. Computed on the frontend from the
// live row values (see utils/receiving/receivingRowFilter).
const ShipmentItemReceiptStatus = {
  RECEIVED_MORE_THAN_SHIPPED: 'RECEIVED_MORE_THAN_SHIPPED',
  RECEIVED_LESS_THAN_SHIPPED: 'RECEIVED_LESS_THAN_SHIPPED',
  NO_QUANTITY_ENTERED: 'NO_QUANTITY_ENTERED',
  COMPLETE: 'COMPLETE',
};

export default ShipmentItemReceiptStatus;
