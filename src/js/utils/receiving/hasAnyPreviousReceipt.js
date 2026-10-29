/**
 * Checks whether the shipment being received already has a submitted receipt, told by any of its
 * lines carrying receipt items of an earlier receipt. Reads `entities`, which holds every row of
 * the shipment even when the filter hides some of them from the table.
 */
const hasAnyPreviousReceipt = (lineItemsState) =>
  Object.values(lineItemsState.entities)
    .some((item) => item?.previousReceiptItems?.length > 0);

export default hasAnyPreviousReceipt;
