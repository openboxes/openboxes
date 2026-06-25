import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceiptGroup from 'consts/receiptGroup';
import { ReceivingView } from 'consts/receivingViewOptions';
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
  const [lineItemsState, setLineItemsState] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const dispatch = useDispatch();
  const users = useSelector(getUsers);

  // TODO (OBPIH-7860 Edit Modal + OBPIH-7866 Display): this is a temporary
  // solution. When line splitting is added, currentReceiptItems may have
  // more entries and each one will become its own row, so this fallback
  // to the first item will not be needed.
  const buildLineItem = (summary, usersById) => {
    const {
      shipmentItem,
      currentReceiptItems = [],
      totalQuantityReceived = 0,
      totalQuantityCanceled = 0,
      isFullyReceived = false,
    } = summary;
    const currentReceiptItem = currentReceiptItems[0];
    return {
      shipmentItemId: shipmentItem.id,
      receiptItemId: currentReceiptItem?.id ?? null,
      productCode: shipmentItem.productLot?.product?.productCode,
      product: shipmentItem.productLot?.product,
      parentContainer: shipmentItem.container?.parentContainer,
      container: shipmentItem.container && {
        id: shipmentItem.container.id,
        name: shipmentItem.container.name,
      },
      lotNumber: currentReceiptItem?.productLot?.lotNumber
        ?? shipmentItem.productLot?.lotNumber,
      expirationDate: currentReceiptItem?.productLot?.expirationDate
        ?? shipmentItem.productLot?.expirationDate,
      recipient: currentReceiptItem?.recipient
        ?? (shipmentItem.recipientId ? usersById[shipmentItem.recipientId] : null),
      quantityShipped: shipmentItem.quantity,
      packSize: shipmentItem.packSize,
      unitOfMeasure: shipmentItem.unitOfMeasure,
      quantityReceiving: currentReceiptItem?.quantityReceived ?? null,
      quantityRemaining:
        shipmentItem.quantity - totalQuantityReceived - totalQuantityCanceled,
      isFullyReceived,
    };
  };

  // Build state used for table view
  const buildTableViewState = (summaryById, grouped, usersById) => {
    const lineItems = (grouped?.order || []).map((id) => buildLineItem(summaryById[id], usersById));
    return normalizeData(lineItems, 'shipmentItemId');
  };

  // Build state used for packing list.
  // The parent group (level 1) becomes a separator row, while the child group name
  // (level 2) is attached to each line item.
  const buildPackingListViewState = (summaryById, grouped, usersById) => {
    const { order = [], groups = {} } = grouped || {};

    const toLineItemRow = (id, packLevelGroup) => ({
      rowId: id,
      entity: { ...buildLineItem(summaryById[id], usersById), packLevelGroup },
    });

    // Flatten the two-level grouping into a single ordered list of rows. Each parent group adds
    // a separator row followed by its line items.
    const rows = order.flatMap((parentName) => {
      const { order: childOrder = [], groups: childGroups = {} } = groups[parentName] || {};
      const lineItemRows = childOrder.flatMap((childName) =>
        (childGroups[childName] || []).map((id) => toLineItemRow(id, childName)));
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
      if (!summary?.pendingReceiptId) {
        await receivingApi.startReceipt(shipmentId);
      }
      setLineItemsState(transformSummary(summary, view));
    } finally {
      setLoading(false);
    }
  };

  // Updates a single line item in the normalized state without rebuilding the whole
  // collection. Stable identity (useCallback) keeps the table `meta` referentially
  // stable, so the memoized cells only re-render the line item that actually changed.
  const updateLineItem = useCallback((shipmentItemId, newData) =>
    setLineItemsState((state) => updateNormalizedItem(state, shipmentItemId, newData)), []);

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
    lineItemsState,
    updateLineItem,
  };
};

export default useReceivingActions;
