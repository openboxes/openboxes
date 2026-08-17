import _ from 'lodash';

import ReceiptGroup from 'consts/receiptGroup';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import RowSaveStatus from 'consts/rowSaveStatus';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';
import {
  createNormalizedState,
  normalizeData,
} from 'utils/normalizationUtils';

// In packing list view we ask the API to group items by pack level so that we can
// render separator rows between groups
export const receiptGroupForView = (view) =>
  (view === ReceivingView.PACKING_LIST ? ReceiptGroup.PACK_LEVEL : ReceiptGroup.SHIPMENT_ITEM);

const buildSeparatorRow = (name) => ({ isSeparator: true, id: `separator-${name}`, name });

// The original line of a shipment item (isSplitItem: false), created when the receipt was
// started. Exactly one exists per receivable shipment item and it cannot be deleted, so it is
// always in the summary - even when it is filtered out of the displayed rows
// (see visibleReceiptItems).
const findOriginalReceiptItem = (currentReceiptItems) =>
  currentReceiptItems.find((receiptItem) => !receiptItem.isSplitItem);

// Base builder of a line item row:
// - a shipment item that was not split uses it directly as its only editable row,
// - the replaced and split item rows of a split item build on top of it
//   (see buildItemRows).
const buildLineItem = ({ summary, receiptItem, usersById }) => {
  const {
    shipmentItem,
    currentReceiptItems = [],
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
    // The original line of the shipment item, regardless of which line this row renders. The
    // cancel-remaining flag of the check step may only land on it (the backend rejects the flag
    // on a split line), and a row built from a split item carries the split line's own id.
    originalReceiptItemId: findOriginalReceiptItem(currentReceiptItems)?.id ?? null,
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
    // The shipment item's bin is the origin bin the stock was picked from, so it is no
    // fallback for a putaway location.
    binLocation: mapToFormSelectOption(receiptItem?.binLocation),
    // Baseline bin location as of load / last successful save, used (like
    // initialQuantityReceiving) to skip no-op edits on save.
    initialBinLocationId: receiptItem?.binLocation?.id ?? null,
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

// Two field values match when both are null/undefined or when they are strictly equal.
// A value being present on one side but not the other (cleared or added) counts as a difference.
const differsFromShipment = (receiptValue, shipmentValue) => {
  const bothEmpty = receiptValue == null && shipmentValue == null;
  return !bothEmpty && receiptValue !== shipmentValue;
};

// Detects which shipment-item field(s) a receipt item overrides. Used both to decide when
// to enter split flow (any change triggers it) and to know which cells to strike through
// on the replaced row.
const getReceiptItemChanges = (receiptItem, shipmentItem) => ({
  product: differsFromShipment(
    receiptItem?.productLot?.product?.id,
    shipmentItem.productLot?.product?.id,
  ),
  lotNumber: differsFromShipment(
    receiptItem?.productLot?.lotNumber,
    shipmentItem.productLot?.lotNumber,
  ),
  expirationDate: differsFromShipment(
    receiptItem?.productLot?.expirationDate,
    shipmentItem.productLot?.expirationDate,
  ),
  recipient: differsFromShipment(
    receiptItem?.recipient?.id,
    shipmentItem.recipientId,
  ),
});

// The struck-through row of a split shipment item - the split items below
// replace it. Built without a receipt item, so it keeps the original shipment values
// (product, lot, expiration, recipient).
const buildReplacedEntity = (summary, usersById) => {
  const lineItem = buildLineItem({ summary, usersById });
  const currentReceiptItems = summary.currentReceiptItems ?? [];
  // The pending quantities of the split items, already subtracted from quantityRemaining.
  const quantityPendingReceipt = currentReceiptItems.reduce(
    (sum, receiptItem) => sum + (receiptItem.quantityReceived ?? 0),
    0,
  );
  // A field is struck through if any of the current receipt items overrides it. A product
  // change also strikes lot and expiration, because a new product implies a new productLot.
  // Lot and expiration are assumed to be tied together, so a change in either one strikes
  // both.
  const changesPerItem = currentReceiptItems.map(
    (item) => getReceiptItemChanges(item, summary.shipmentItem),
  );
  // Checks whether any receipt item of the shipment item has the given field dirty.
  const isAnyReceiptItemOfShipmentItemDirty = (dirtyField) =>
    changesPerItem.some((itemChanges) => itemChanges[dirtyField]);
  const anyLotOrExpirationChange = isAnyReceiptItemOfShipmentItemDirty('lotNumber')
    || isAnyReceiptItemOfShipmentItemDirty('expirationDate');
  const originalReceiptItem = findOriginalReceiptItem(currentReceiptItems);
  return {
    ...lineItem,
    rowType: ReceivingRowType.REPLACED,
    // Return receipt item id of the original line to have the comments feature visible.
    receiptItemId: originalReceiptItem?.id ?? null,
    comment: originalReceiptItem?.comment ?? null,
    isCompleted: false,
    productChanged: isAnyReceiptItemOfShipmentItemDirty('product'),
    lotChanged: isAnyReceiptItemOfShipmentItemDirty('product') || anyLotOrExpirationChange,
    expirationChanged:
      isAnyReceiptItemOfShipmentItemDirty('product') || anyLotOrExpirationChange,
    recipientChanged: isAnyReceiptItemOfShipmentItemDirty('recipient'),
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

  // Any of product / lot / expiration / recipient differing from the shipment item enters
  // the changes group - as a plain line it would silently replace the shipment item's
  // value in the table instead of showing the original struck through above the new one.
  const hasReceiptItemChanges = (receiptItem) => {
    if (!receiptItem) {
      return false;
    }
    const changes = getReceiptItemChanges(receiptItem, shipmentItem);
    return changes.product || changes.lotNumber
      || changes.expirationDate || changes.recipient;
  };

  // The original line always exists in the database (it backs the cancel-remaining flow on
  // completion) but is only displayed while it carries a received quantity or is the only
  // line - an untouched or zeroed original stays hidden behind its split lines.
  const visibleReceiptItems = currentReceiptItems.filter(
    (receiptItem, index, items) => receiptItem.isSplitItem
      || (receiptItem.quantityReceived ?? 0) > 0
      || items.length === 1,
  );

  // Zeroed original line is hidden in the table, but should be always visible in the modal
  // To handle this, we store the data of originalReceiptItem either
  // in the toggle row or in the "basic" row for the case where you have one split line,
  // but with the same product as the original line product (no toggle then)
  const originalReceiptItem = findOriginalReceiptItem(currentReceiptItems);
  const hiddenOriginalLineItem = originalReceiptItem
    && !visibleReceiptItems.includes(originalReceiptItem)
    ? buildSplitItemEntity({ summary, receiptItem: originalReceiptItem, usersById })
    : null;

  if (visibleReceiptItems.length < 2 && !hasReceiptItemChanges(visibleReceiptItems[0])) {
    const receiptItem = visibleReceiptItems[0];

    // The only case where we would reach the isSplitItem=true is when you have only one split item
    // with the splitItem.product = originalItem.product
    return receiptItem?.isSplitItem
      ? [{
        ...buildSplitItemEntity({ summary, receiptItem, usersById }),
        rowType: null,
        originalLineItem: hiddenOriginalLineItem,
      }]
      : [buildLineItem({ summary, receiptItem, usersById })];
  }

  const replacedRow = buildReplacedEntity(summary, usersById);

  const splitItems = visibleReceiptItems
    .map((receiptItem) => buildSplitItemEntity({ summary, receiptItem, usersById }));

  // True when any split item changes product. Gates the arrow + product code on the
  // first row of each product group.
  const anyProductChanged = visibleReceiptItems.some((receiptItem) => differsFromShipment(
    receiptItem?.productLot?.product?.id,
    shipmentItem.productLot?.product?.id,
  ));

  // The API does not sort receipt items, so group split items by product to keep
  // each product together, then flag only the first row of each group.
  const splitItemRows = Object.values(
    _.groupBy(splitItems, (splitItem) => splitItem.product?.id),
  ).flatMap((productSplitItems) => productSplitItems.map((splitItem, index) => ({
    ...splitItem,
    isFirstSplitItem: index === 0 && anyProductChanged,
  })));

  const toggleRowId = _.uniqueId('row-');

  return [
    { ...replacedRow, toggleRowId },
    {
      rowType: ReceivingRowType.TOGGLE,
      rowId: toggleRowId,
      // Needed by the receiving filter.
      shipmentItemId: shipmentItem.id,
      replacedRowId: replacedRow.rowId,
      splitItemIds: splitItemRows.map((splitItem) => splitItem.rowId),
      originalLineItem: hiddenOriginalLineItem,
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

/**
 * Merge the lines created by the start receipt endpoint into a summary that was read before the
 * receipt existed, so the rows carry their receipt item ids right away instead of only after a
 * reload.
 */
export const mergeStartedReceipt = (summary, startedReceipt) => {
  if (!startedReceipt?.id) {
    return summary;
  }
  const itemsByShipmentItemId = _.groupBy(startedReceipt.receiptItems ?? [], 'shipmentItemId');

  return {
    ...summary,
    pendingReceiptId: startedReceipt.id,
    shipmentItemSummaryById: _.mapValues(
      summary?.shipmentItemSummaryById ?? {},
      (shipmentItemSummary, shipmentItemId) => ({
        ...shipmentItemSummary,
        currentReceiptItems: itemsByShipmentItemId[shipmentItemId] ?? [],
      }),
    ),
  };
};

// Transforms the receipt summary API response into the normalized `{ entities, ids }`
// shape needed for the table, using the builder appropriate for the current view.
export const transformReceiptSummary = (data, view, usersById) => {
  const summaryById = data?.shipmentItemSummaryById || {};
  const grouped = data?.shipmentItemsGrouped;

  if (view === ReceivingView.PACKING_LIST) {
    return buildPackingListViewState(summaryById, grouped, usersById);
  }
  return buildTableViewState(summaryById, grouped, usersById);
};
