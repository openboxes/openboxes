import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString, parseStringToDate } from 'utils/dateUtils';

/**
 * The date picker in the edit modal keeps the expiration date as a display string (dd/MMM/yyyy),
 * while the backend binds LocalDate only from ISO-like formats, so convert it to yyyy-MM-dd.
 */
const toIsoDateString = (displayDate) => {
  if (!displayDate) {
    return null;
  }
  const parsed = parseStringToDate({
    date: displayDate,
    dateOnly: true,
    options: { providedDateFormat: DateFormatDateFns.DD_MMM_YYYY },
  });
  return formatDateToString({ date: parsed, dateFormat: DateFormatDateFns.YYYY_MM_DD });
};

// Rows the user added but never filled.
// TODO: Should be removed after implementing validation in: OBPIH-7928
const isEmptyNewRow = (item) => !item.receiptItemId
  && !item.lotNumber
  && !item.expirationDate
  && !item.recipient
  && item.quantityReceiving === '';

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
 * @param {Array} lineItems - rows for saving
 * @returns {{ itemsToSave: Array }}
 */
const buildEditReceivingInfoPayload = (lineItems) => ({
  itemsToSave: (lineItems || [])
    .filter((item) => !isEmptyNewRow(item))
    .map((item) => ({
      rowId: item.rowId,
      receiptItem: item.receiptItemId ? { id: item.receiptItemId } : null,
      product: item.product?.id ? { id: item.product.id } : null,
      lotNumber: item.lotNumber || null,
      expirationDate: toIsoDateString(item.expirationDate),
      recipient: item.recipient?.id ? { id: item.recipient.id } : null,
      quantityReceiving: item.quantityReceiving === '' || item.quantityReceiving == null
        ? null
        : Number(item.quantityReceiving),
      binLocation: item.location?.id ? { id: item.location.id } : null,
      isSplitItem: Boolean(item.isSplitItem),
    })),
});

export default buildEditReceivingInfoPayload;
