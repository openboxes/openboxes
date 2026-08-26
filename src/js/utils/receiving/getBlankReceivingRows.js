import ReceivingRowType from 'consts/receivingRowType';

// Rows with no editable "receiving now" input
const NON_EDITABLE_ROW_TYPES = [ReceivingRowType.REPLACED, ReceivingRowType.TOGGLE];

/**
 * Rows the user can still enter a quantity into: plain lines and split items that are not
 * fully received yet (a completed line has its input disabled).
 */
export const getEditableReceivingRows = (lineItemsState) => (lineItemsState?.ids || [])
  .map((id) => lineItemsState.entities[id])
  // Separator entries (packing list view) have no entity - nothing to enter
  .filter((row) => row
    && !NON_EDITABLE_ROW_TYPES.includes(row.rowType)
    && !row.isCompleted);

/**
 * Editable rows the user left empty. A deliberate 0 counts as entered - the same rule the
 * quantity autofill skips rows by.
 */
const getBlankReceivingRows = (lineItemsState) => getEditableReceivingRows(lineItemsState)
  .filter((row) => row.quantityReceiving == null);

export default getBlankReceivingRows;
