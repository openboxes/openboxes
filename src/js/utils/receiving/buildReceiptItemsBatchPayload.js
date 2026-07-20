/**
 * Builds the request body for `POST /api/receipts/v2/{receiptId}/items/batch` from the
 * dirty receiving line items collected by the autosave hook (rows edited since load or
 * the last save, no-op edits already filtered out by shouldSaveRow).
 *
 * Backend contract (ReceiptItemsBatchRequest):
 *  - itemsToSave: items to create (receiptItem == null) or update (receiptItem != null)
 *  - itemsToDelete: ids of existing receipt items to remove (deletes flow through the
 *    autosave deleteFn as separate requests, so this stays empty here)
 *
 * Each save entry (ReceiptItemUpsertRequest):
 *  - rowId: client-side correlation id echoed back in the response, used to match each
 *    returned line with the local row it belongs to.
 *  - shipmentItem: { id } - the line being received against (required).
 *  - receiptItem: { id } when updating an existing receipt item, null when creating one.
 *  - quantityReceiving: integer quantity (nullable on the backend).
 *  - binLocation: { id } putaway bin (nullable).
 *
 * @param {Array} dirtyRows - line items to save
 * @returns {{ itemsToSave: Array, itemsToDelete: Array<string> }}
 */
const buildReceiptItemsBatchPayload = (dirtyRows) => ({
  itemsToSave: (dirtyRows || []).map((item) => ({
    rowId: item.rowId,
    shipmentItem: { id: item.shipmentItemId },
    receiptItem: item.receiptItemId ? { id: item.receiptItemId } : null,
    quantityReceiving: item.quantityReceiving,
    binLocation: item.binLocation?.id ? { id: item.binLocation.id } : null,
  })),
  itemsToDelete: [],
});

export default buildReceiptItemsBatchPayload;
