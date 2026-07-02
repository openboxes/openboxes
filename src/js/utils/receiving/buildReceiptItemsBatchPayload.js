/**
 * Builds the request body for `POST /api/receipts/v2/{receiptId}/items/batch` from the
 * normalized receiving line items (the `entities` map of { rowId -> lineItem }).
 *
 * Backend contract (ReceiptItemsBatchRequest):
 *  - itemsToSave: items to create (receiptItem == null) or update (receiptItem != null)
 *  - itemsToDelete: ids of existing receipt items to remove
 *
 * Each save entry (ReceiptItemUpsertRequest):
 *  - rowId: client-side correlation id echoed back in the response. We send the generated
 *    per-row id (not the shipmentItemId) because a single shipment item may map to several
 *    rows once line splitting lands; the echoed rowId maps the response back onto our state.
 *  - shipmentItem: { id } - the line being received against (required).
 *  - receiptItem: { id } when updating an existing receipt item, null when creating a new one.
 *  - quantityReceiving: integer quantity (nullable on the backend).
 *  - binLocation: { id } putaway bin (nullable; not captured in state yet).
 *
 * Only rows the user actually touched are sent: a row is included when it is dirty (edited
 * since load or the last save) AND its quantity actually differs from the baseline captured
 * at load / last save (initialQuantityReceiving). Untouched rows are skipped, and so are no-op
 * edits that end up back at the original value (e.g. 3 -> 4 -> 3), which merely flip isDirty.
 *
 * @param {Object} entities - normalized line items keyed by rowId
 * @returns {{ itemsToSave: Array, itemsToDelete: Array<string> }}
 */
const buildReceiptItemsBatchPayload = (entities) => {
  const itemsToSave = Object.values(entities || {})
    .filter((item) => item.isDirty && item.quantityReceiving !== item.initialQuantityReceiving)
    .map((item) => ({
      rowId: item.rowId,
      shipmentItem: { id: item.shipmentItemId },
      receiptItem: item.receiptItemId ? { id: item.receiptItemId } : null,
      quantityReceiving: item.quantityReceiving,
      binLocation: item.binLocation?.id ? { id: item.binLocation.id } : null,
    }));

  return { itemsToSave, itemsToDelete: [] };
};

export default buildReceiptItemsBatchPayload;
