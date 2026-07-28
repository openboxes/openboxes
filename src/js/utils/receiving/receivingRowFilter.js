import _ from 'lodash';

import ReceivingRowType from 'consts/receivingRowType';
import ShipmentItemReceiptStatus from 'consts/shipmentItemReceiptStatus';
import rowMatchesSearch from 'utils/searchRows';

// Row fields the receiving search bar matches against
const SEARCHABLE_PATHS = ['productCode', 'product.name', 'lotNumber', 'recipient.name'];

// Rows carrying the "receiving now" quantity of a shipment item. Replaced (struck through)
// and toggle (UI-only) rows are skipped.
const isCurrentRow = (row) =>
  row.rowType !== ReceivingRowType.REPLACED && row.rowType !== ReceivingRowType.TOGGLE;

// Predicate answering whether a shipment item matches any of the requested receipt status
// codes. Status is derived from the row values as of the filter apply — edits are reflected
// only on the next submit / reload.
const matchesReceiptStatus = ({ shipmentItemRows, statusCodes }) => {
  if (!statusCodes?.length) {
    return true;
  }
  const currentRows = shipmentItemRows.filter(isCurrentRow);
  const {
    quantityShipped = 0,
    quantityReceived = 0,
    previousReceiptItems = [],
    isCompleted = false,
  } = currentRows[0] ?? {};

  const quantityPreviouslyCanceled = _.sumBy(
    previousReceiptItems, (item) => item.quantityCanceled ?? 0,
  );
  const quantityReceivingNow = _.sumBy(currentRows, (row) => row.quantityReceiving ?? 0);
  const quantityRemaining = quantityShipped
    - quantityReceived
    - quantityPreviouslyCanceled
    - quantityReceivingNow;

  return statusCodes.some((code) => {
    switch (code) {
      case ShipmentItemReceiptStatus.COMPLETE:
        // isCompleted covers rows fully received by prior receipts (no live current row
        // qty to bring quantityRemaining to 0).
        return quantityRemaining === 0 || isCompleted;
      case ShipmentItemReceiptStatus.RECEIVED_MORE_THAN_SHIPPED:
        return quantityRemaining < 0;
      case ShipmentItemReceiptStatus.RECEIVED_LESS_THAN_SHIPPED:
        return quantityRemaining > 0;
      case ShipmentItemReceiptStatus.NO_QUANTITY_ENTERED:
        return quantityRemaining > 0
          && currentRows.every((row) => (row.quantityReceiving ?? 0) === 0);
      default:
        return false;
    }
  });
};

const matchesSearch = ({ shipmentItemRows, searchTerm }) =>
  !searchTerm || shipmentItemRows.some((row) =>
    rowMatchesSearch({ row, search: searchTerm, paths: SEARCHABLE_PATHS }));

const matchesFilter = ({ shipmentItemRows, receiptStatusCodes, searchTerm }) =>
  matchesSearch({ shipmentItemRows, searchTerm })
  && matchesReceiptStatus({ shipmentItemRows, statusCodes: receiptStatusCodes });

// Returns the ids of shipment items that pass the filter. We only compute this on filter
// submit or reload — not on every row edit — so the row the user is editing does not
// disappear when they change its value.
export const computeVisibleShipmentItemIds = (lineItemsState, filterParams) => {
  if (!filterParams?.searchTerm && !filterParams?.receiptStatusCodes?.length) {
    return null;
  }
  const shipmentItemGroups = _.groupBy(Object.values(lineItemsState.entities), 'shipmentItemId');
  const matchingGroups = _.pickBy(
    shipmentItemGroups,
    (rows) => matchesFilter({ shipmentItemRows: rows, ...filterParams }),
  );
  return new Set(Object.keys(matchingGroups));
};

// Rows of the same shipment item stay together (replaced + toggle + split items). Packing
// list separators of groups with no visible rows are dropped.
export const filterLineItemsState = (lineItemsState, visibleShipmentItemIds) => {
  if (!visibleShipmentItemIds) {
    return lineItemsState;
  }
  const { entities, ids } = lineItemsState;
  const isRowVisible = (row) => visibleShipmentItemIds.has(row?.shipmentItemId);
  // Separators don't have a shipmentItemId — keep a separator only if at least one of its
  // rows is still visible (each row knows its separator through `separatorId`).
  const visibleSeparatorIds = new Set(
    Object.values(entities).filter(isRowVisible).map((row) => row.separatorId),
  );

  // `ids` mixes separator objects (packing list only) with plain row ids. For a separator
  // we check the set above, for a row we check its shipmentItemId.
  const visibleIds = ids.filter((id) => (id?.isSeparator
    ? visibleSeparatorIds.has(id.id)
    : isRowVisible(entities[id])));
  return { ...lineItemsState, ids: visibleIds };
};
