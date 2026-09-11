import {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceivingRowType from 'consts/receivingRowType';
import useReceivingAutosave from 'hooks/receiving/v2/useReceivingAutosave';
import useReceivingSaveAction from 'hooks/receiving/v2/useReceivingSaveAction';
import { createNormalizedState } from 'utils/normalizationUtils';
import getReceiptSummaryParams from 'utils/receiving/getReceiptSummaryParams';
import {
  mergeStartedReceipt,
  transformReceiptSummary,
} from 'utils/receiving/receiptSummaryRows';

// Only rows with an editable quantity input can be autofilled: plain lines (no row type)
// and split items.
const AUTOFILL_EXCLUDED_ROW_TYPES = [ReceivingRowType.REPLACED, ReceivingRowType.TOGGLE];

// A row qualifies for autofill only when it can still be received (not completed, something
// left to receive) and the user hasn't entered anything yet (0 counts as entered).
const shouldAutofillQuantity = (row) => !AUTOFILL_EXCLUDED_ROW_TYPES.includes(row.rowType)
  && !row.isCompleted
  && row.quantityReceiving == null
  && row.quantityAvailableToReceive > 0;

// Collects the "receiving now" autofill updates: one { rowId, quantityReceiving } entry per
// line that is still empty, filled with its remaining quantity. Applied through the autosave
// updateRow, so autofilled rows get queued for saving like manual edits.
export const getAutofillQuantityUpdates = (state) => (state?.ids || [])
  .map((id) => state.entities[id])
  // Separator entries (packing list view) have no entity - nothing to fill
  .filter((row) => row && shouldAutofillQuantity(row))
  .map((row) => ({ rowId: row.rowId, quantityReceiving: row.quantityAvailableToReceive }));

// How many lines of the pending receipt the summary carries, across all shipment items.
const countCurrentReceiptItems = (summary) => _.sumBy(
  Object.values(summary?.shipmentItemSummaryById ?? {}),
  (shipmentItemSummary) => (shipmentItemSummary.currentReceiptItems ?? []).length,
);

const useReceivingActions = ({ view, sort, sortOrder } = {}) => {
  const [loading, setLoading] = useState(false);
  const [receiptId, setReceiptId] = useState(null);
  // Rows as of load / last refetch. The autosave hook owns the continuously updated rows;
  // this state only seeds it (a new reference resets the hook).
  const [initialRows, setInitialRows] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const dispatch = useDispatch();
  const users = useSelector(getUsers);
  const {
    rows,
    rowsById,
    updateRow,
    updateRows,
    deleteRow,
    autosaveStatus,
    flush,
    updateRowManually,
  } = useReceivingAutosave({ initialRows, receiptId });

  const fetchSummary = async () => {
    const { data: { data: summary } } = await receivingApi.getReceiptSummary(
      shipmentId,
      getReceiptSummaryParams({ view, sort, sortOrder }),
    );
    return summary;
  };

  // Makes sure the shipment has a pending receipt this workflow can receive against, before any of
  // it is shown: one is started when there is none, and the lines of one that already exists are
  // synced - it may have been started by the old receiving workflow, which only persisted the lines
  // the user actually touched, or it may predate something that reopened a shipment item. Either
  // way the receipt's lines are folded into the summary that was read before it had them, so the
  // rows carry their receipt item ids without a reload.
  const ensureV2Receipt = async (summary) => {
    if (!summary?.pendingReceiptId) {
      const { data: { data: startedReceipt } } = await receivingApi.startReceipt(shipmentId);
      return mergeStartedReceipt(summary, startedReceipt);
    }

    const { data: { data: syncedReceipt } } = await receivingApi.syncReceiptLines(shipmentId);
    // The sync only ever adds lines, so an unchanged count means it had nothing to do and the
    // summary already carries the very same lines. Merging then buys nothing and would let the
    // receipt's own (unordered) line order reshuffle the split rows on every sort or view change.
    return countCurrentReceiptItems(summary) < (syncedReceipt?.receiptItems?.length ?? 0)
      ? mergeStartedReceipt(summary, syncedReceipt)
      : summary;
  };

  const loadReceipt = async () => {
    setLoading(true);
    try {
      // Push pending edits out before refetching (view switch, modal reload, sort change),
      // so the summary reflects them and nothing is lost when the autosave state resets.
      await flush();
      const receiptSummary = await ensureV2Receipt(await fetchSummary());
      setReceiptId(receiptSummary?.pendingReceiptId ?? null);
      setInitialRows(transformReceiptSummary(receiptSummary, view, _.keyBy(users, 'id')));
    } finally {
      setLoading(false);
    }
  };

  // Same `{ entities, ids }` shape the table and columns consumed before autosave, now
  // continuously reconciled by the hook.
  const lineItemsState = useMemo(() => ({ entities: rows, ids: rowsById }), [rows, rowsById]);

  const autofillQuantities = useCallback((state = lineItemsState) => {
    getAutofillQuantityUpdates(state)
      .forEach(({ rowId, quantityReceiving }) => updateRow(rowId, { quantityReceiving }));
  }, [lineItemsState, updateRow]);

  // Comments are persisted on their own endpoint, so unlike updateLineItem this does not mark the
  // row dirty - it only mirrors the already-saved comment so the popover prefills and the
  // create-vs-update choice stay correct without reloading the whole receipt.
  const updateLineItemComment = useCallback((rowId, comment) =>
    updateRowManually(rowId, { comment }), [updateRowManually]);

  const { onSaveAndExit } = useReceivingSaveAction({ flush });

  useEffect(() => {
    if (!shipmentId) {
      return;
    }
    loadReceipt();
  }, [shipmentId, view, sort, sortOrder]);

  useEffect(() => {
    dispatch(fetchUsers());
  }, []);

  return {
    loading,
    receiptId,
    lineItemsState,
    updateLineItem: updateRow,
    updateLineItems: updateRows,
    updateLineItemComment,
    autofillQuantities,
    // Deletes the receipt item through the autosave queue and, only on success, drops the
    // row from the local state (with grouping fix-ups).
    removeSplitItem: deleteRow,
    loadReceipt,
    onSaveAndExit,
    flush,
    autosaveStatus,
  };
};

export default useReceivingActions;
