import {
  removeNormalizedItem,
  removeNormalizedItems,
  updateNormalizedItem,
} from 'utils/normalizationUtils';

const getNextSplitItemId = (splitItemIds, removedRowId) => {
  const removedIndex = splitItemIds.indexOf(removedRowId);
  return splitItemIds[removedIndex + 1];
};

/**
 * Removes a split item row from the normalized receiving state, fixing up the split-item
 * grouping: the owning toggle row drops the id, a group reduced to a single split item
 * collapses back into a plain row, and the group lead passes on when the first split
 * item is removed.
 */
const removeSplitItemRow = (state, rowId) => {
  // The toggle row owning the removed split item in its splitItemIds.
  const toggleRowId = state.ids
    .find((id) => state.entities[id]?.splitItemIds?.includes(rowId));
  const toggle = state.entities[toggleRowId];
  const splitItemIds = toggle.splitItemIds.filter((id) => id !== rowId);
  // A single change left is no longer a group - drop the group rows and turn the
  // remaining split item back into a plain row.
  if (splitItemIds.length === 1) {
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

export default removeSplitItemRow;
