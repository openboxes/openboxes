import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceiptGroup from 'consts/receiptGroup';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import useReceivingSaveAction from 'hooks/receiving/v2/useReceivingSaveAction';
import useRemoveSplitItem from 'hooks/receiving/v2/useRemoveSplitItem';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';
import {
  createNormalizedState,
  normalizeData,
  updateNormalizedItem,
} from 'utils/normalizationUtils';

// In packing list view we ask the API to group items by pack level so that we can
// render separator rows between groups
const receiptGroupForView = (view) =>
  (view === ReceivingView.PACKING_LIST ? ReceiptGroup.PACK_LEVEL : ReceiptGroup.SHIPMENT_ITEM);

const buildSeparatorRow = (name) => ({ isSeparator: true, id: `separator-${name}`, name });

const useReceivingActions = (view) => {
  const [loading, setLoading] = useState(false);
  const [receiptId, setReceiptId] = useState(null);
  const [lineItemsState, setLineItemsState] = useState(createNormalizedState());
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
    return {
      // Unique per-row id (a shipment item may eventually map to several rows once line
      // splitting lands), used as the normalized state key and as the rowId correlation
      // sent to / echoed back from the batch endpoint.
      rowId: _.uniqueId('row-'),
      rowType: null,
      shipmentItemId: shipmentItem.id,
      receiptItemId: receiptItem?.id ?? null,
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
      binLocation: receiptItem?.binLocation ?? shipmentItem.binLocation ?? null,
      quantityShipped: shipmentItem.quantity,
      quantityReceived: quantityPreviouslyReceived,
      previousReceiptItems,
      packSize: shipmentItem.packSize,
      unitOfMeasure: shipmentItem.unitOfMeasure,
      quantityReceiving: receiptItem?.quantityReceived ?? null,
      // Baseline quantity as of load / last successful save. A dirty row is only sent when its
      // quantity actually differs from this, so no-op edits (e.g. 3 -> 4 -> 3) are skipped.
      initialQuantityReceiving: receiptItem?.quantityReceived ?? null,
      quantityRemaining:
        shipmentItem.quantity - totalQuantityReceived - totalQuantityCanceled,
      isCompleted,
      // Local edit flag - only dirty rows (touched since load / last save) are sent on save.
      isDirty: false,
    };
  };

  // The struck-through row of a split shipment item - the split items below
  // replace it. Built without a receipt item, so it keeps the original shipment values
  // (product, lot, expiration, recipient, bin location).
  const buildReplacedEntity = (summary, usersById) => ({
    ...buildLineItem({ summary, usersById }),
    rowType: ReceivingRowType.REPLACED,
    isCompleted: false,
  });

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
    binLocation: receiptItem.binLocation,
  });

  // Rows for a single shipment item: a single editable row when it was not split,
  // or a replaced row + toggle row + one split item row per pending receipt item.
  const buildItemRows = (summary, usersById) => {
    const { currentReceiptItems = [] } = summary;

    if (currentReceiptItems.length < 2) {
      return [buildLineItem({ summary, receiptItem: currentReceiptItems[0], usersById })];
    }

    const replacedRow = buildReplacedEntity(summary, usersById);

    const splitItems = currentReceiptItems
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

    const toLineItemRow = (id, packLevelGroup) =>
      buildItemRows(summaryById[id], usersById).map((entity) => (
        { rowId: entity.rowId, entity: { ...entity, packLevelGroup } }));

    // Flatten the two-level grouping into a single ordered list of rows. Each parent group adds
    // a separator row followed by its line items.
    const rows = order.flatMap((parentName) => {
      const { order: childOrder = [], groups: childGroups = {} } = groups[parentName] || {};
      const lineItemRows = childOrder.flatMap((childName) =>
        (childGroups[childName] || []).flatMap((id) => toLineItemRow(id, childName)));
      return [{ rowId: buildSeparatorRow(parentName) }, ...lineItemRows];
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

  const loadReceipt = async () => {
    setLoading(true);
    try {
      const { data: { data: summary } } = await receivingApi.getReceiptSummary(shipmentId, {
        group: receiptGroupForView(view),
      });
      // When there's no pending receipt yet, start one
      const currentReceiptId = summary?.pendingReceiptId
        ?? (await receivingApi.startReceipt(shipmentId)).data?.data?.id;
      setReceiptId(currentReceiptId);
      setLineItemsState(transformSummary(summary, view));
    } finally {
      setLoading(false);
    }
  };

  // Updates a single line item in the normalized state without rebuilding the whole
  // collection. Stable identity (useCallback) keeps the table `meta` referentially
  // stable, so the memoized cells only re-render the line item that actually changed.
  // Every edit marks the row dirty, which is what flags it for the next batch save.
  const updateLineItem = useCallback((rowId, newData) =>
    setLineItemsState((state) => updateNormalizedItem(state, rowId, {
      ...newData,
      isDirty: true,
    })), []);

  const { removeSplitItem } = useRemoveSplitItem({ receiptId, lineItemsState, setLineItemsState });

  const { onSaveAndExit } = useReceivingSaveAction({
    receiptId,
    lineItemsState,
    setLineItemsState,
  });

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
    updateLineItem,
    removeSplitItem,
    loadReceipt,
    onSaveAndExit,
  };
};

export default useReceivingActions;
