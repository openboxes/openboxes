import _ from 'lodash';

import { filterLineItemsState } from 'utils/receiving/receivingRowFilter';

/**
 * Drops the shipment items nothing was entered for from the check step rows.
 */
const omitBlankReceivingRows = (lineItemsState) => filterLineItemsState(
  lineItemsState,
  new Set(
    Object.values(lineItemsState?.entities || {})
      // Every row is checked, toggle rows included - those carry no quantity field at all.
      .filter((row) => !_.isNil(row?.quantityReceiving))
      .map((row) => row.shipmentItemId),
  ),
);

export default omitBlankReceivingRows;
