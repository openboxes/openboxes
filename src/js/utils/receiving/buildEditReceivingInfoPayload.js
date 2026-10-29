import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString, formatStringToLocalDate } from 'utils/dateUtils';

// Rows the user added but never filled.
// TODO: Should be removed after implementing validation in: OBPIH-7928
const isEmptyNewRow = (item) => !item.receiptItemId
  && !item.lotNumber
  && !item.expirationDate
  && !item.recipient
  && item.quantityReceiving === '';

/**
 * A removed original line is never deleted - it is saved with its received quantity zeroed
 * instead. The entry is built from the normalized-state entity (not a form row, so its
 * expiration date is still in the API format) and echoes the persisted lot and recipient,
 * so that only the quantity changes.
 */
const toZeroedOriginalEntry = (item) => ({
  rowId: item.rowId,
  receiptItem: { id: item.receiptItemId },
  product: item.product?.id ? { id: item.product.id } : null,
  lotNumber: item.lotNumber || null,
  expirationDate: item.expirationDate
    ? formatDateToString({ date: item.expirationDate, dateFormat: DateFormatDateFns.YYYY_MM_DD })
    : null,
  recipient: item.recipient?.id ? { id: item.recipient.id } : null,
  quantityReceiving: 0,
});

/**
 * Builds the request body for the edit receiving info endpoint
 *
 * Backend contract (ReceiptEditReceivingInfoCommand / ReceiptItemEditReceivingInfoRequest):
 *  - rowId: client-side correlation id echoed back in the response.
 *  - receiptItem: { id } when updating an existing receipt item, null when creating a new one.
 *  - product + lotNumber + expirationDate: used to find or create the inventory item
 *  - recipient: { id } (nullable).
 *  - quantityReceiving: integer quantity.
 *  - binLocation: { id } (nullable, null clears the bin).
 *  - isSplitItem: marks rows split off from the original shipment item line.
 *
 * The split flag is intentionally NOT part of the payload - the backend owns it (new lines are
 * flagged as splits on creation and the flag is immutable afterwards).
 *
 * @param {Array} lineItems - form rows for saving
 * @param {Array} [originalItemsToZero] - normalized-state entities of original lines removed in the
 *   modal; they are appended as updates zeroing the received quantity instead of being deleted
 * @returns {{ itemsToSave: Array }}
 */
const buildEditReceivingInfoPayload = (lineItems, originalItemsToZero = []) => ({
  itemsToSave: [
    ...(lineItems || [])
      .filter((item) => !isEmptyNewRow(item))
      .map((item) => ({
        rowId: item.rowId,
        receiptItem: item.receiptItemId ? { id: item.receiptItemId } : null,
        product: item.product?.id ? { id: item.product.id } : null,
        lotNumber: item.lotNumber || null,
        // The edit modal date picker keeps the expiration date as a display string,
        // but the backend expects a date-only ISO format (LocalDate).
        expirationDate: formatStringToLocalDate(item.expirationDate, DateFormatDateFns.DD_MMM_YYYY),
        recipient: item.recipient?.id ? { id: item.recipient.id } : null,
        quantityReceiving: item.quantityReceiving === '' || item.quantityReceiving == null
          ? null
          : Number(item.quantityReceiving),
        binLocation: item.binLocation?.id ? { id: item.binLocation.id } : null,
      })),
    ...(originalItemsToZero || []).map(toZeroedOriginalEntry),
  ],
});

export default buildEditReceivingInfoPayload;
