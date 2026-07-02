import { useCallback } from 'react';

import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { updateNormalizedItem } from 'utils/normalizationUtils';
import buildReceiptItemsBatchPayload from 'utils/receiving/buildReceiptItemsBatchPayload';

const useReceivingSaveAction = ({ receiptId, lineItemsState, setLineItemsState }) => {
  const { shipmentId } = useParams();
  const dispatch = useDispatch();

  // Builds the batch payload from the current line items and persists it through the receipt
  // batch endpoint. Returns the server response, or null when there's nothing to save.
  const saveItemsBatch = useCallback(async () => {
    if (!receiptId) {
      return null;
    }

    const payload = buildReceiptItemsBatchPayload(lineItemsState.entities);
    if (!payload.itemsToSave.length && !payload.itemsToDelete.length) {
      return null;
    }

    dispatch(showSpinner());
    try {
      const { data: { data } } = await receivingApi.updateItemsBatch(receiptId, payload);
      // The response echoes our rowId and returns the persisted receipt item id, so we fold it
      // back into state (matched by rowId). A subsequent save then updates the same receipt
      // item instead of creating a duplicate.
      setLineItemsState((state) => (data?.updatedLines || []).reduce(
        (acc, line) => updateNormalizedItem(acc, line.rowId, {
          receiptItemId: line.id,
          quantityReceiving: line.quantityReceived,
          // Move the baseline forward to the persisted quantity so subsequent edits compare
          // against what's actually saved on the server.
          initialQuantityReceiving: line.quantityReceived,
          isDirty: false,
        }),
        state,
      ));
      return data;
    } finally {
      dispatch(hideSpinner());
    }
  }, [receiptId, lineItemsState.entities, dispatch, setLineItemsState]);

  const onSaveAndExit = useCallback(async () => {
    await saveItemsBatch();
    window.location = STOCK_MOVEMENT_URL.show(shipmentId);
  }, [saveItemsBatch, shipmentId]);

  return {
    saveItemsBatch,
    onSaveAndExit,
  };
};

export default useReceivingSaveAction;
