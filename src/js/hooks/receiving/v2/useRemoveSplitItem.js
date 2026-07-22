import { useCallback } from 'react';

import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import {
  removeNormalizedItem,
  removeNormalizedItems,
  updateNormalizedItem,
} from 'utils/normalizationUtils';

const getNextSplitItemId = (splitItemIds, removedRowId) => {
  const removedIndex = splitItemIds.indexOf(removedRowId);
  return splitItemIds[removedIndex + 1];
};

const removeSplitItemRow = (state, rowId) => {
  // The toggle row owning the removed split item in its splitItemIds.
  const toggleRowId = state.ids
    .find((id) => state.entities[id]?.splitItemIds?.includes(rowId));
  const toggle = state.entities[toggleRowId];
  const splitItemIds = toggle.splitItemIds.filter((id) => id !== rowId);
  // Removing the only split item dissolves the whole group - the replaced row turns
  // back into the plain original shipment line.
  if (splitItemIds.length === 0) {
    return updateNormalizedItem(
      removeNormalizedItems(state, [rowId, toggleRowId]),
      toggle.replacedRowId,
      { rowType: null },
    );
  }
  const remainingSplitItem = state.entities[splitItemIds[0]];
  const originalProduct = state.entities[toggle.replacedRowId]?.product;
  // A single change left is no longer a group - drop the group rows and turn the
  // remaining split item back into a plain row. A split item with a changed product
  // stays a group though, otherwise it would replace the original product instead
  // of showing it struck through.
  if (splitItemIds.length === 1 && remainingSplitItem?.product?.id === originalProduct?.id) {
    return updateNormalizedItem(
      removeNormalizedItems(state, [rowId, toggleRowId, toggle.replacedRowId]),
      splitItemIds[0],
      { rowType: null },
    );
  }
  const updatedState = updateNormalizedItem(
    removeNormalizedItem(state, rowId),
    toggleRowId,
    { splitItemIds },
  );
  if (!state.entities[rowId].isFirstSplitItem) {
    return updatedState;
  }
  // The removed split item led its product group, so the next one takes over the lead.
  return updateNormalizedItem(
    updatedState,
    getNextSplitItemId(toggle.splitItemIds, rowId),
    { isFirstSplitItem: true },
  );
};

/**
 * Removes a single split item row - deletes its receipt item through the batch
 * endpoint and, only on success, drops the row from the local state.
 */
const useRemoveSplitItem = ({ receiptId, lineItemsState, setLineItemsState }) => {
  const dispatch = useDispatch();

  const removeSplitItem = useCallback(async (rowId) => {
    const receiptItemId = lineItemsState.entities[rowId]?.receiptItemId;
    if (!receiptItemId) {
      return;
    }
    dispatch(showSpinner());
    try {
      await receivingApi.updateItemsBatch(receiptId, {
        itemsToSave: [],
        itemsToDelete: [receiptItemId],
      });
    } finally {
      dispatch(hideSpinner());
    }
    setLineItemsState((state) => removeSplitItemRow(state, rowId));
  }, [lineItemsState.entities, receiptId]);

  return { removeSplitItem };
};

export default useRemoveSplitItem;
