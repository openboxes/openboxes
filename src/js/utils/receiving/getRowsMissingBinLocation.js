import { getEditableReceivingRows } from 'utils/receiving/getBlankReceivingRows';

/**
 * Editable rows that are receiving something but say nothing about where it goes. Only meaningful
 * at a location that tracks bin locations - anywhere else the rows carry no bin at all. A row
 * receiving zero moves no stock, so it needs no bin to move it into.
 */
const getRowsMissingBinLocation = (lineItemsState) => getEditableReceivingRows(lineItemsState)
  .filter((row) => Number(row.quantityReceiving) > 0 && !row.binLocation?.id);

export default getRowsMissingBinLocation;
