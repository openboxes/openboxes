import { useCallback, useState } from 'react';

import ReceivingRowType from 'consts/receivingRowType';
import hasAnyPreviousReceipt from 'utils/receiving/hasAnyPreviousReceipt';

const useEditReceivingLineItemModal = (lineItemsState) => {
  const [isOpen, setIsOpen] = useState(false);
  const [itemId, setItemId] = useState(null);
  const openModal = useCallback((id) => {
    setItemId(id);
    setIsOpen(true);
  }, []);
  const closeModal = useCallback(() => setIsOpen(false), []);

  /**
   * The intitial line items modal includes:
   *  if a row is a REPLACED row (it contains split items), the initial lines items
   *  are the original line item and all the split items
   *
   *  if a row has one split item that uses the same product code as the original line,
   *  this row is displayed in the table like a typical row, hence in the modal we would like
   *  to display this row AND the original item anyway ->
   *  (row?.originalLineItem ? [row.originalLineItem, row] : [row])
   *  Such split line (original product = split item product) carries the data of originalLineItem,
   *  so that the modal shows it
   *
   *
   */
  const getInitialEditModalLineItems = useCallback((rowId) => {
    const { entities } = lineItemsState;
    const row = entities[rowId];
    if (row?.rowType !== ReceivingRowType.REPLACED) {
      return row?.originalLineItem ? [row.originalLineItem, row] : [row];
    }

    const toggleRow = entities[row.toggleRowId];
    const splitItems = toggleRow.splitItemIds.map((splitItemId) => entities[splitItemId]);
    // If the original line has quantity > 0, it would also appear in the table, so we can take
    // the original line item data from it directly or from the toggle row.
    const originalLineItem = splitItems.find((item) => !item.isSplitItem)
      ?? toggleRow.originalLineItem;
    return [
      ...(originalLineItem ? [originalLineItem] : []),
      ...splitItems.filter((item) => item.isSplitItem),
    ];
  }, [lineItemsState]);

  const hasPreviousReceipts = hasAnyPreviousReceipt(lineItemsState);

  return {
    isOpen,
    itemId,
    openModal,
    closeModal,
    getInitialEditModalLineItems,
    hasPreviousReceipts,
  };
};

export default useEditReceivingLineItemModal;
