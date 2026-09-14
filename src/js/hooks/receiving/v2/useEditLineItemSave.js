import { useCallback } from 'react';

import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import buildEditReceivingInfoPayload from 'utils/receiving/buildEditReceivingInfoPayload';

/**
 * Save action for the edit line item modal
 */
const useEditLineItemSave = ({
  receiptId,
  lineItem,
  initialLineItems,
  getLineItems,
  loadReceipt,
  onClose,
  confirmExpirationDateChange,
}) => {
  const dispatch = useDispatch();

  const onSave = useCallback(async () => {
    const lineItems = getLineItems();
    const remainingLineItemsIds = new Set(lineItems.map((item) => item.receiptItemId));
    // Receipt items that already existed when the modal opened but were removed from the form.
    const removedItems = initialLineItems
      .filter((item) => item.receiptItemId && !remainingLineItemsIds.has(item.receiptItemId));
    // Only split lines are ever deleted - the original line backs the cancel-remaining flow
    // on completion, so the backend refuses to delete it.
    const itemsToDelete = removedItems
      .filter((item) => item.isSplitItem)
      .map((item) => item.receiptItemId);

    // If an original receipt item is marked to be deleted, and it had quantityReceived > 0,
    // instead of removing it (backend would refuse that), update its quantity to 0.
    const originalItemsToZero = removedItems
      .filter((item) => !item.isSplitItem && item.quantityReceiving > 0);
    const payload = buildEditReceivingInfoPayload(lineItems, originalItemsToZero);

    if (!payload.itemsToSave.length && !itemsToDelete.length) {
      onClose();
      return;
    }
    // Saving a lot that is already in inventory updates its expiration date in every depot, so
    // the user confirms it first. Canceling leaves the edit modal open with the entered values.
    const isExpiryChangeConfirmed = await confirmExpirationDateChange(lineItems);
    if (!isExpiryChangeConfirmed) {
      return;
    }

    dispatch(showSpinner());
    try {
      if (itemsToDelete.length) {
        await receivingApi.updateItemsBatch(receiptId, {
          itemsToSave: [],
          itemsToDelete,
        });
      }
      if (payload.itemsToSave.length) {
        await receivingApi.editReceivingInfo(receiptId, lineItem.shipmentItemId, payload);
      }
      onClose();
      loadReceipt();
    } finally {
      dispatch(hideSpinner());
    }
  }, [
    receiptId,
    lineItem.shipmentItemId,
    initialLineItems,
    getLineItems,
    loadReceipt,
    onClose,
    dispatch,
    confirmExpirationDateChange,
  ]);

  return { onSave };
};

export default useEditLineItemSave;
