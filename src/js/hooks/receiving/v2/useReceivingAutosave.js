import receivingApi from 'api/services/ReceivingApi';
import useAutosave from 'hooks/useAutosave';
import buildReceiptItemsBatchPayload from 'utils/receiving/buildReceiptItemsBatchPayload';
import removeSplitItemRow from 'utils/receiving/removeSplitItemRow';

// Only send rows whose quantity really differs from the baseline captured at
// load / last save, so no-op edits (e.g. 3 -> 4 -> 3) are skipped.
const shouldSaveRow = (row) => row.quantityReceiving !== row.initialQuantityReceiving;

// The response echoes our rowId and returns the saved receipt item id, so the next save
// updates the same receipt item instead of creating a duplicate. The baseline moves to
// the saved quantity.
const reconcileRow = (row, line) => ({
  receiptItemId: line.id,
  quantityReceiving: line.quantityReceived,
  initialQuantityReceiving: line.quantityReceived,
});

// A row edited while its request was running keeps the local quantity - only the
// server-assigned id is copied in.
const reconcileStaleRow = (row, line) => ({ receiptItemId: line.id });

/**
 * Receiving wiring of the generic autosave hook: batch-saves dirty line items to the pending
 * receipt and deletes split item rows through the same serial queue.
 */
const useReceivingAutosave = ({ initialRows, receiptId }) => {
  const patchFn = async (dirtyRows) => {
    const payload = buildReceiptItemsBatchPayload(dirtyRows);
    const { data: { data } } = await receivingApi.updateItemsBatch(receiptId, payload);
    return data?.updatedLines ?? [];
  };

  const deleteFn = async (row) => {
    // A row that was never saved has nothing to delete on the server.
    if (!row?.receiptItemId) {
      return;
    }
    await receivingApi.updateItemsBatch(receiptId, {
      itemsToSave: [],
      itemsToDelete: [row.receiptItemId],
    });
  };

  return useAutosave({
    initialRows,
    requests: { patchFn, deleteFn },
    rowOptions: {
      shouldSaveRow,
      reconcileRow,
      reconcileStaleRow,
      removeRowFromState: removeSplitItemRow,
    },
  });
};

export default useReceivingAutosave;
