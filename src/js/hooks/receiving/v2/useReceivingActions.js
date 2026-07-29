import {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceiptGroup from 'consts/receiptGroup';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import RowSaveStatus from 'consts/rowSaveStatus';
import useReceivingAutosave from 'hooks/receiving/v2/useReceivingAutosave';
import useReceivingSaveAction from 'hooks/receiving/v2/useReceivingSaveAction';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';
import {
  createNormalizedState,
  normalizeData,
} from 'utils/normalizationUtils';

// In packing list view we ask the API to group items by pack level so that we can
// render separator rows between groups
const receiptGroupForView = (view) =>
  (view === ReceivingView.PACKING_LIST ? ReceiptGroup.PACK_LEVEL : ReceiptGroup.SHIPMENT_ITEM);

const buildSeparatorRow = (name) => ({ isSeparator: true, id: `separator-${name}`, name });

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

const useReceivingActions = (view) => {
  const [loading, setLoading] = useState(false);
  const [receiptId, setReceiptId] = useState(null);
  // Rows as of load / last refetch. The autosave hook owns the continuously updated rows;
  // this state only seeds it (a new reference resets the hook).
  const [initialRows, setInitialRows] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const dispatch = useDispatch();
  const users = useSelector(getUsers);
  // Base builder of a line item row:
  // - a shipment item that was not split uses it directly as its only editable row,
  // - the replaced and split item rows of a split item build on top of it
  //   (see buildItemRows).
  const buildLineItem = ({ summary, receiptItem, usersById }) => {
    const {
      shipmentItem,
      previousReceiptItems = [],
      totalQuantityReceived = 0,
      totalQuantityCanceled = 0,
    } = summary;
    // Items saved on the pending receipt (currentReceiptItems) are "receiving now", not
    // received, so only submitted receipts (previousReceiptItems) count as received.
    const quantityPreviouslyReceived = previousReceiptItems.reduce(
      (sum, item) => sum + (item.quantityReceived ?? 0),
      0,
    );
    const quantityPreviouslyCanceled = previousReceiptItems.reduce(
      (sum, item) => sum + (item.quantityCanceled ?? 0),
      0,
    );
    // A line is completed only when submitted receipts cover the shipped quantity and there is
    // nothing left pending.
    const isCompleted = quantityPreviouslyReceived + quantityPreviouslyCanceled
      >= shipmentItem.quantity
      && !receiptItem;
    // A receipt item always carries its own product. The shipment item product is used
    // only for rows that have no receipt item yet.
    const product = receiptItem?.productLot?.product ?? shipmentItem.productLot?.product;
    const quantityRemaining = shipmentItem.quantity - totalQuantityReceived - totalQuantityCanceled;
    return {
      // Unique per-row id (a shipment item may eventually map to several rows once line
      // splitting lands), used as the normalized state key and as the rowId correlation
      // sent to / echoed back from the batch endpoint.
      rowId: _.uniqueId('row-'),
      rowType: null,
      shipmentItemId: shipmentItem.id,
      receiptItemId: receiptItem?.id ?? null,
      // Backend flag distinguishing the original line of a shipment item (false, created when
      // the receipt was started) from the lines split off it while receiving (true). Unlike
      // rowType (a UI grouping concept), this stays accurate across saves and reloads.
      isSplitItem: receiptItem?.isSplitItem ?? false,
      productCode: product?.productCode,
      product,
      parentContainer: shipmentItem.container?.parentContainer,
      container: shipmentItem.container && {
        id: shipmentItem.container.id,
        name: shipmentItem.container.name,
      },
      lotNumber: receiptItem?.productLot?.lotNumber
        ?? shipmentItem.productLot?.lotNumber,
      expirationDate: receiptItem?.productLot?.expirationDate
        ?? shipmentItem.productLot?.expirationDate,
      recipient: mapToFormSelectOption(receiptItem?.recipient)
        ?? (shipmentItem.recipientId ? usersById[shipmentItem.recipientId] : null),
      binLocation: mapToFormSelectOption(receiptItem?.binLocation ?? shipmentItem.binLocation),
      // Baseline bin location as of load / last successful save, used (like
      // initialQuantityReceiving) to skip no-op edits on save.
      initialBinLocationId: (receiptItem?.binLocation ?? shipmentItem.binLocation)?.id ?? null,
      quantityShipped: shipmentItem.quantity,
      quantityReceived: quantityPreviouslyReceived,
      previousReceiptItems,
      comment: receiptItem?.comment ?? null,
      packSize: shipmentItem.packSize,
      unitOfMeasure: shipmentItem.unitOfMeasure,
      quantityReceiving: receiptItem?.quantityReceived ?? null,
      // Baseline quantity as of load / last successful save. A dirty row is only sent when its
      // quantity actually differs from this, so no-op edits (e.g. 3 -> 4 -> 3) are skipped.
      initialQuantityReceiving: receiptItem?.quantityReceived ?? null,
      quantityRemaining,
      quantityAvailableToReceive: quantityRemaining + (receiptItem?.quantityReceived ?? 0),
      isCompleted,
      // Autosave status of the row (see consts/rowSaveStatus) - PENDING when edited,
      // SAVING while its batch request is in flight, ERROR when saving failed.
      saveStatus: RowSaveStatus.SAVED,
    };
  };

  // The struck-through row of a split shipment item - the split items below
  // replace it. Built without a receipt item, so it keeps the original shipment values
  // (product, lot, expiration, recipient, bin location).
  const buildReplacedEntity = (summary, usersById) => {
    const lineItem = buildLineItem({ summary, usersById });
    // The pending quantities of the split items, already subtracted from quantityRemaining.
    const quantityPendingReceipt = (summary.currentReceiptItems ?? []).reduce(
      (sum, receiptItem) => sum + (receiptItem.quantityReceived ?? 0),
      0,
    );
    return {
      ...lineItem,
      rowType: ReceivingRowType.REPLACED,
      isCompleted: false,
      // The replaced row shows the status of the whole group, so its available quantity
      // covers all its split items (buildLineItem only adds back the own receipt item,
      // which a replaced row doesn't have).
      quantityAvailableToReceive: lineItem.quantityRemaining + quantityPendingReceipt,
    };
  };

  // A split item row - one editable row per receipt item of a split shipment item.
  // A split item always comes from the edit modal, so the receipt item is the only
  // source of truth for its values - falling back to the shipment item (as
  // buildLineItem does) would show shipment values for fields saved as empty.
  const buildSplitItemEntity = ({ summary, receiptItem, usersById }) => ({
    ...buildLineItem({ summary, receiptItem, usersById }),
    rowType: ReceivingRowType.SPLIT_ITEM,
    lotNumber: receiptItem.productLot?.lotNumber,
    expirationDate: receiptItem.productLot?.expirationDate,
    recipient: mapToFormSelectOption(receiptItem.recipient),
    binLocation: mapToFormSelectOption(receiptItem.binLocation),
    initialBinLocationId: receiptItem.binLocation?.id ?? null,
  });

  // Rows for a single shipment item: a single editable row when it was not split,
  // or a replaced row + toggle row + one split item row per pending receipt item.
  const buildItemRows = (summary, usersById) => {
    const { shipmentItem, currentReceiptItems = [] } = summary;

    // A receipt item with a changed product always stays a changes group - as a plain
    // line it would replace the original product instead of showing it struck through.
    const hasChangedProduct = (receiptItem) => {
      const receiptItemProductId = receiptItem?.productLot?.product?.id;
      return receiptItemProductId != null
        && receiptItemProductId !== shipmentItem.productLot?.product?.id;
    };

    // The original line always exists in the database (it backs the cancel-remaining flow on
    // completion) but is only displayed while it carries a received quantity or is the only
    // line - an untouched or zeroed original stays hidden behind its split lines.
    const visibleReceiptItems = currentReceiptItems.filter(
      (receiptItem, index, items) => receiptItem.isSplitItem
        || (receiptItem.quantityReceived ?? 0) > 0
        || items.length === 1,
    );

    if (visibleReceiptItems.length < 2 && !hasChangedProduct(visibleReceiptItems[0])) {
      const receiptItem = visibleReceiptItems[0];
      return receiptItem?.isSplitItem
        ? [{ ...buildSplitItemEntity({ summary, receiptItem, usersById }), rowType: null }]
        : [buildLineItem({ summary, receiptItem, usersById })];
    }

    const replacedRow = buildReplacedEntity(summary, usersById);

    const splitItems = visibleReceiptItems
      .map((receiptItem) => buildSplitItemEntity({ summary, receiptItem, usersById }));

    // The API does not sort receipt items, so group split items by product to keep
    // each product together. Only the first split item of a product shows the arrow,
    // code and product cells.
    const splitItemRows = Object.values(
      _.groupBy(splitItems, (splitItem) => splitItem.product?.id),
    ).flatMap((productSplitItems) => productSplitItems.map((splitItem, index) => ({
      ...splitItem,
      isFirstSplitItem: index === 0,
    })));

    const toggleRowId = _.uniqueId('row-');

    return [
      { ...replacedRow, toggleRowId },
      {
        rowType: ReceivingRowType.TOGGLE,
        rowId: toggleRowId,
        replacedRowId: replacedRow.rowId,
        splitItemIds: splitItemRows.map((splitItem) => splitItem.rowId),
      },
      ...splitItemRows,
    ];
  };

  // Build state used for table view
  const buildTableViewState = (summaryById, grouped, usersById) => {
    const lineItems = (grouped?.order || [])
      .flatMap((id) => buildItemRows(summaryById[id], usersById));
    return normalizeData(lineItems, 'rowId');
  };

  // Build state used for packing list.
  // The parent group (level 1) becomes a separator row, while the child group name
  // (level 2) is attached to each line item.
  const buildPackingListViewState = (summaryById, grouped, usersById) => {
    const { order = [], groups = {} } = grouped || {};

    // Each row also keeps the id of its group's separator, so group-scoped actions
    // (e.g. the location autofill triggered from a separator) can find their rows.
    const toLineItemRow = (id, packLevelGroup, separatorId) =>
      buildItemRows(summaryById[id], usersById).map((entity) => (
        { rowId: entity.rowId, entity: { ...entity, packLevelGroup, separatorId } }));

    // Flatten the two-level grouping into a single ordered list of rows. Each parent group adds
    // a separator row followed by its line items.
    const rows = order.flatMap((parentName) => {
      const separatorRow = buildSeparatorRow(parentName);
      const { order: childOrder = [], groups: childGroups = {} } = groups[parentName] || {};
      const lineItemRows = childOrder.flatMap((childName) =>
        (childGroups[childName] || [])
          .flatMap((id) => toLineItemRow(id, childName, separatorRow.id)));
      return [{ rowId: separatorRow }, ...lineItemRows];
    });

    return rows.reduce((state, { rowId, entity }) => ({
      entities: entity ? { ...state.entities, [rowId]: entity } : state.entities,
      ids: [...state.ids, rowId],
    }), createNormalizedState());
  };

  // Function calling an appropriate builder based on the current view,
  // to transform the API response into the shape needed for the table.
  const transformSummary = (data, currentView) => {
    const summaryById = data?.shipmentItemSummaryById || {};
    const grouped = data?.shipmentItemsGrouped;
    const usersById = _.keyBy(users, 'id');

    if (currentView === ReceivingView.PACKING_LIST) {
      return buildPackingListViewState(summaryById, grouped, usersById);
    }
    return buildTableViewState(summaryById, grouped, usersById);
  };

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

  const loadReceipt = async () => {
    setLoading(true);
    try {
      // Push pending edits out before refetching (view switch, modal reload), so the
      // summary reflects them and nothing is lost when the autosave state resets.
      await flush();
      const { data: { data: summary } } = await receivingApi.getReceiptSummary(shipmentId, {
        group: receiptGroupForView(view),
      });
      // When there's no pending receipt yet, start one
      const currentReceiptId = summary?.pendingReceiptId
        ?? (await receivingApi.startReceipt(shipmentId)).data?.data?.id;
      setReceiptId(currentReceiptId);
      setInitialRows(transformSummary(summary, view));
    } finally {
      setLoading(false);
    }
  };

  // Same `{ entities, ids }` shape the table and columns consumed before autosave, now
  // continuously reconciled by the hook.
  const lineItemsState = useMemo(() => ({ entities: rows, ids: rowsById }), [rows, rowsById]);

  const autofillQuantities = useCallback(() => {
    getAutofillQuantityUpdates({ entities: rows, ids: rowsById })
      .forEach(({ rowId, quantityReceiving }) => updateRow(rowId, { quantityReceiving }));
  }, [rows, rowsById, updateRow]);

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
  }, [shipmentId, view]);

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
