import { filterLineItemsState } from 'utils/receiving/receivingRowFilter';

/**
 * Drops the shipment items nothing was entered for from the check step rows.
 */
const omitBlankReceivingRows = (lineItemsState) => filterLineItemsState(
  lineItemsState,
  new Set(
    Object.values(lineItemsState?.entities || {})
      .filter((row) => row?.quantityReceiving != null)
      .map((row) => row.shipmentItemId),
  ),
);

export default omitBlankReceivingRows;
