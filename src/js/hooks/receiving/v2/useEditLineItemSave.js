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
    // The original row is the only one backed by an existing receipt item. When its id is no
    // longer among the form rows, the user removed it, so delete it through the batch endpoint
    // (the edit-receiving-info endpoint does not support deletes).
    const initialReceiptItemIds = initialLineItems
      .map((item) => item.receiptItemId)
      .filter(Boolean);
    const itemsToDelete = initialReceiptItemIds.filter(
      (id) => !lineItems.some((item) => item.receiptItemId === id),
    );

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
