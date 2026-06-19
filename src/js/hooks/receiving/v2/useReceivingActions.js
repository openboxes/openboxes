import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceiptGroup from 'consts/receiptGroup';
import {
  createNormalizedState,
  normalizeData,
  updateNormalizedItem,
} from 'utils/normalizationUtils';

const useReceivingActions = () => {
  const [loading, setLoading] = useState(false);
  const [lineItemsState, setLineItemsState] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const dispatch = useDispatch();
  const users = useSelector(getUsers);

  // TODO (OBPIH-7860 Edit Modal + OBPIH-7866 Display): this is a temporary
  // solution. When line splitting is added, currentReceiptItems may have
  // more entries and each one will become its own row, so this fallback
  // to the first item will not be needed.
  const transformSummaryToLineItems = (data) => {
    const summaryById = data?.shipmentItemSummaryById || {};
    const orderedIds = data?.shipmentItemsGrouped?.order || [];
    const usersById = _.keyBy(users, 'id');
    const lineItems = orderedIds.map((id) => {
      const {
        shipmentItem,
        currentReceiptItems = [],
        totalQuantityReceived = 0,
        totalQuantityCanceled = 0,
      } = summaryById[id];
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
        quantityReceiving: currentReceiptItem?.quantityReceived ?? null,
        quantityRemaining:
          shipmentItem.quantity - totalQuantityReceived - totalQuantityCanceled,
      };
    });

    return normalizeData(lineItems, 'shipmentItemId');
  };

  const loadReceipt = async () => {
    setLoading(true);
    try {
      const { data: { data: summary } } = await receivingApi.getReceiptSummary(shipmentId, {
        group: ReceiptGroup.SHIPMENT_ITEM,
      });
      // When there's no pending receipt yet, start one
      if (!summary?.pendingReceiptId) {
        await receivingApi.startReceipt(shipmentId);
      }
      setLineItemsState(transformSummaryToLineItems(summary));
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
  }, [shipmentId]);

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
