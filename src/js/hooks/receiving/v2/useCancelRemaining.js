import { useCallback, useMemo, useState } from 'react';

import ReceivingRowType from 'consts/receivingRowType';

// A row can be canceled when it is the only line that was not split or the replaced row standing
// above its split lines, still has something left to receive, and its shipment item has an
// original line (isSplitItem: false) to carry the cancel - the only line the backend accepts
// the flag on.
export const isCancellableRow = (row) =>
  (row?.rowType === null || row?.rowType === ReceivingRowType.REPLACED)
  && Boolean(row.originalReceiptItemId)
  && !row.isCompleted
  && (row.quantityAvailableToReceive ?? 0) > 0;

// Ids of the original lines the given rows cancel through. Keyed by the original line, not by the
// row, because that is what the completion endpoint expects and row ids change on every reload.
export const getCancellableReceiptItemIds = (lineItemsState) => (lineItemsState?.ids || [])
  .map((id) => lineItemsState.entities[id])
  // Separator entries (packing list view) have no entity - nothing to cancel
  .filter((row) => row && isCancellableRow(row))
  .map((row) => row.originalReceiptItemId);

/**
 * Owns the "Cancel Remaining" selection of the check step
 */
const useCancelRemaining = ({ lineItemsState }) => {
  const [ids, setIds] = useState(() => new Set());

  const toggle = useCallback((receiptItemId) => {
    setIds((previous) => {
      const next = new Set(previous);
      return next.delete(receiptItemId) ? next : next.add(receiptItemId);
    });
  }, []);

  // Selects every line that still has something to cancel, limited to the rows the filter shows.
  const selectAll = () => setIds(new Set(getCancellableReceiptItemIds(lineItemsState)));

  const itemsToComplete = useMemo(
    () => [...ids].map((receiptItemId) => ({
      receiptItem: { id: receiptItemId },
      cancelRemainingQuantity: true,
    })),
    [ids],
  );

  return {
    ids,
    toggle,
    selectAll,
    itemsToComplete,
  };
};

export default useCancelRemaining;
