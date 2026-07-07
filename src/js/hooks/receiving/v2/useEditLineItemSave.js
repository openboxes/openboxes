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
  getLineItems,
  onClose,
}) => {
  const dispatch = useDispatch();

  const onSave = useCallback(async () => {
    const lineItems = getLineItems();
    const payload = buildEditReceivingInfoPayload(lineItems);
    // The original row is the only one backed by an existing receipt item. When its id is no
    // longer among the form rows, the user removed it, so delete it through the batch endpoint
    // (the edit-receiving-info endpoint does not support deletes).
    const isOriginalRowRemoved = lineItem.receiptItemId
      && !lineItems.some((item) => item.receiptItemId === lineItem.receiptItemId);

    if (!payload.itemsToSave.length && !isOriginalRowRemoved) {
      onClose();
      return;
    }

    dispatch(showSpinner());
    try {
      if (isOriginalRowRemoved) {
        await receivingApi.updateItemsBatch(receiptId, {
          itemsToSave: [],
          itemsToDelete: [lineItem.receiptItemId],
        });
      }
      if (payload.itemsToSave.length) {
        await receivingApi.editReceivingInfo(receiptId, lineItem.shipmentItemId, payload);
      }
      onClose();
    } finally {
      dispatch(hideSpinner());
    }
  }, [receiptId, lineItem.shipmentItemId, lineItem.receiptItemId, getLineItems, onClose, dispatch]);

  return { onSave };
};

export default useEditLineItemSave;
