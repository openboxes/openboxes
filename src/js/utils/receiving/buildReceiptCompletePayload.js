import { DateFormatDateFns } from 'consts/timeFormat';
import { formatStringToInstant } from 'utils/dateUtils';

/**
 * Builds the request body for `POST /api/receipts/v2/{receiptId}/complete`.
 *
 * Only the lines flagged for cancellation are sent - the ones left out keep their remaining
 * quantity open.
 *
 * @param {Object} params
 * @param {string} params.dateDelivered - value of the "Delivered on" field
 * @param {Array} params.itemsToComplete - [{ receiptItem: { id }, cancelRemainingQuantity }]
 * @returns {{ dateDelivered: string|null, itemsToComplete: Array }}
 */
const buildReceiptCompletePayload = ({ dateDelivered, itemsToComplete = [] } = {}) => ({
  dateDelivered: formatStringToInstant(dateDelivered, DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS),
  itemsToComplete,
});

export default buildReceiptCompletePayload;
