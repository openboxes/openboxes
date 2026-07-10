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
}) => {
  const dispatch = useDispatch();

  const onSave = useCallback(async () => {
    const lineItems = getLineItems();
    const payload = buildEditReceivingInfoPayload(lineItems);
    const initialReceiptItemIds = initialLineItems
      .filter((item) => item.receiptItemId)
      .map((item) => item.receiptItemId);
    const remainingLineItemsIds = new Set(lineItems.map((item) => item.receiptItemId));
    // Delete the receipt items that already existed (initialReceiptItemIds) but were
    // removed from the form
    const itemsToDelete = initialReceiptItemIds.filter((id) => !remainingLineItemsIds.has(id));

    if (!payload.itemsToSave.length && !itemsToDelete.length) {
      onClose();
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
  ]);

  return { onSave };
};

export default useEditLineItemSave;
