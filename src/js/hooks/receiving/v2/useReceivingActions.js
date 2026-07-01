import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import ReceiptGroup from 'consts/receiptGroup';
import { ReceivingView } from 'consts/receivingViewOptions';
import {
  createNormalizedState,
  normalizeData,
  updateNormalizedItem,
} from 'utils/normalizationUtils';
import buildReceiptItemsBatchPayload from 'utils/receiving/buildReceiptItemsBatchPayload';

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
      // Unique per-row id (a shipment item may eventually map to several rows once line
      // splitting lands), used as the normalized state key and as the rowId correlation
      // sent to / echoed back from the batch endpoint.
      rowId: _.uniqueId('row-'),
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
      // Local edit flag - only dirty rows (touched since load / last save) are sent on save.
      isDirty: false,
    };
  };

  // Build state used for table view
  const buildTableViewState = (summaryById, grouped, usersById) => {
    const lineItems = (grouped?.order || []).map((id) => buildLineItem(summaryById[id], usersById));
    return normalizeData(lineItems, 'rowId');
  };

  // Build state used for packing list.
  // The parent group (level 1) becomes a separator row, while the child group name
  // (level 2) is attached to each line item.
  const buildPackingListViewState = (summaryById, grouped, usersById) => {
    const { order = [], groups = {} } = grouped || {};

    const toLineItemRow = (id, packLevelGroup) => {
      const entity = { ...buildLineItem(summaryById[id], usersById), packLevelGroup };
      return { rowId: entity.rowId, entity };
    };

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

  // Builds the batch payload from the current line items and persists it through the receipt
  // batch endpoint. Returns the server response, or null when there's nothing to save.
  const saveItemsBatch = useCallback(async () => {
    if (!receiptId) {
      return null;
    }

    const payload = buildReceiptItemsBatchPayload(lineItemsState.entities);
    if (!payload.itemsToSave.length && !payload.itemsToDelete.length) {
      return null;
    }

    dispatch(showSpinner());
    try {
      const { data: { data } } = await receivingApi.updateItemsBatch(receiptId, payload);
      // The response echoes our rowId and returns the persisted receipt item id, so we fold it
      // back into state (matched by rowId). A subsequent save then updates the same receipt
      // item instead of creating a duplicate.
      setLineItemsState((state) => (data?.updatedLines || []).reduce(
        (acc, line) => updateNormalizedItem(acc, line.rowId, {
          receiptItemId: line.id,
          quantityReceiving: line.quantityReceived,
          isDirty: false,
        }),
        state,
      ));
      return data;
    } finally {
      dispatch(hideSpinner());
    }
  }, [receiptId, lineItemsState.entities, dispatch]);

  const onSaveAndExit = useCallback(async () => {
    await saveItemsBatch();
    window.location = STOCK_MOVEMENT_URL.show(shipmentId);
  }, [saveItemsBatch, shipmentId]);

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
    onSaveAndExit,
  };
};

export default useReceivingActions;
