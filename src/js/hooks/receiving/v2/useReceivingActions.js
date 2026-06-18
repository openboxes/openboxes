import { useEffect, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import { fetchUsers } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import ReceiptGroup from 'consts/receiptGroup';

const useReceivingActions = () => {
  const [loading, setLoading] = useState(false);
  const [lineItems, setLineItems] = useState([]);
  const { shipmentId } = useParams();
  const dispatch = useDispatch();
  const users = useSelector(getUsers);

  // TODO (OBPIH-7860 Edit Modal + OBPIH-7866 Display): this is a temporary
  // solution. When line splitting is added, currentReceiptItems may have
  // more entries and each one will become its own row, so this fallback
  // to the first item will not be needed.
  // TODO (OBPIH-7857): when state normalization is added, this function
  // will be changed to return a { entities, ids } shape.
  const transformSummaryToLineItems = (data) => {
    const summaryById = data?.shipmentItemSummaryById || {};
    const orderedIds = data?.shipmentItemsGrouped?.order || [];
    const usersById = _.keyBy(users, 'id');
    return orderedIds.map((id) => {
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
      setLineItems(transformSummaryToLineItems(summary));
    } finally {
      setLoading(false);
    }
  };

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
    lineItems,
  };
};

export default useReceivingActions;
