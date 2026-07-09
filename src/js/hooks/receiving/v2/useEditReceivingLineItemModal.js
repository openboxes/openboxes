import { useCallback, useState } from 'react';

import ReceivingRowType from 'consts/receivingRowType';

const useEditReceivingLineItemModal = (lineItemsState) => {
  const [isOpen, setIsOpen] = useState(false);
  const [itemId, setItemId] = useState(null);
  const openModal = useCallback((id) => {
    setItemId(id);
    setIsOpen(true);
  }, []);
  const closeModal = useCallback(() => setIsOpen(false), []);

  // Line items prefilling the edit modal form: the split items of an item with saved
  // changes (a replaced row), or the edited item itself.
  const getInitialEditModalLineItems = useCallback((rowId) => {
    const { entities } = lineItemsState;
    if (entities[rowId]?.rowType !== ReceivingRowType.REPLACED) {
      return [entities[rowId]];
    }
    // The toggle row of the group owns the split item ids.
    const toggleRow = entities[entities[rowId].toggleRowId];
    return toggleRow.splitItemIds.map((splitItemId) => entities[splitItemId]);
  }, [lineItemsState]);

  return {
    isOpen, itemId, openModal, closeModal, getInitialEditModalLineItems,
  };
};

export default useEditReceivingLineItemModal;
