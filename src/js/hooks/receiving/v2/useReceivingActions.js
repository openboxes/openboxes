import { useEffect, useState } from 'react';

import { useParams } from 'react-router-dom';

import receivingApi from 'api/services/ReceivingApi';
import ReceiptResponseFormat from 'consts/receiptResponseFormat';

// Mock response matching GET /api/receiving/v2/${shipmentId}?format=DEFAULT
// TODO: remove MOCK_RECEIPT once OBPIH-7868 will be done
// TODO: packLevel is not in the ReceiptItemDto spec yet, confirm with backend
const MOCK_RECEIPT = {
  id: 'cab2b1fa9d69b73a019d6c06c9060001',
  receiptStatus: 'PENDING',
  shipment: {
    id: 'cab2b1fa9d69b73a019d6c06c9060002',
    name: 'Mock Shipment',
    shipmentNumber: 'SH-2026-0123',
    shipmentStatus: 'SHIPPED',
    isFromPurchaseOrder: true,
  },
  origin: {
    id: 'cab2b1fa9d69b73a019d6c06c9060003',
    name: 'Origin Depot',
  },
  destination: {
    id: 'cab2b1fa9d69b73a019d6c06c9060004',
    name: 'Destination Depot',
  },
  dateShipped: '2026-05-20T10:00:00Z',
  dateDelivered: null,
  requisition: { id: 'cab2b1fa9d69b73a019d6c06c9060005' },
  order: { id: 'cab2b1fa9d69b73a019d6c06c9060006' },
  description: 'Mock receipt',
  recipient: {
    id: 'cab2b1fa9d69b73a019d6c06c9060007',
    name: 'Mock Recipient',
    firstName: 'Mock',
    lastName: 'Recipient',
    email: 'mock.recipient@example.com',
    username: 'mock',
  },
  receiptItems: [
    {
      shipmentItemId: 'cab2b1fa9a5e56b0019a5ee2d5ed01bb',
      product: { id: '1', productCode: 'AS0001', name: 'Aspirin 100mg' },
      binLocation: { id: 'bin-001', name: 'Receiving Bin' },
      parentContainer: { id: 'pallet-1', name: 'Pallet 1' },
      container: { id: 'box-a1', name: 'Box A1' },
      quantityShipped: 100,
      quantityReceived: 35,
      quantityCanceled: 0,
      isFullyReceived: false,
      previousReceipts: [
        {
          receiptItemId: 'cab2b1fa9d69b73a019d6c06c9060010',
          receiptId: 'cab2b1fa9d69b73a019d6c06c9060000',
          receiptStatus: 'RECEIVED',
          lotNumber: 'LOT-001-PREV',
          expirationDate: '2027-06-30',
          quantityReceived: 15,
          quantityCanceled: 0,
        },
      ],
      currentReceiptItems: [
        {
          receiptItemId: 'cab2b1fa9d69b73a019d6c06c9060023',
          lotNumber: 'LOT-001-CURR',
          expirationDate: '2027-06-30',
          recipient: {
            id: 'r-001', name: 'John Doe', firstName: 'John', lastName: 'Doe', email: 'john@example.com', username: 'johndoe',
          },
          quantityReceived: 20,
          quantityCanceled: 0,
        },
      ],
    },
    {
      shipmentItemId: 'cab2b1fa9a5e56b0019a5ee2d5ed01cc',
      product: { id: '2', productCode: 'IV0010', name: 'IV Saline Solution 500ml' },
      binLocation: null,
      parentContainer: null,
      container: null,
      quantityShipped: 50,
      quantityReceived: 0,
      quantityCanceled: 0,
      isFullyReceived: false,
      previousReceipts: [],
      currentReceiptItems: [
        {
          receiptItemId: 'cab2b1fa9d69b73a019d6c06c9060024',
          lotNumber: null,
          expirationDate: null,
          recipient: null,
          quantityReceived: 0,
          quantityCanceled: 0,
        },
      ],
    },
    {
      shipmentItemId: 'cab2b1fa9a5e56b0019a5ee2d5ed01dd',
      product: { id: '3', productCode: 'AM0022', name: 'Amoxicillin 500mg Capsules' },
      binLocation: { id: 'bin-002', name: 'Cold Storage' },
      parentContainer: { id: 'pallet-2', name: 'Pallet 2' },
      container: { id: 'box-b1', name: 'Box B1' },
      quantityShipped: 200,
      quantityReceived: 200,
      quantityCanceled: 0,
      isFullyReceived: true,
      previousReceipts: [],
      currentReceiptItems: [
        {
          receiptItemId: 'cab2b1fa9d69b73a019d6c06c9060025',
          lotNumber: 'AM-2025-09-A',
          expirationDate: '2026-09-01',
          recipient: {
            id: 'r-002', name: 'Jane Smith', firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com', username: 'janesmith',
          },
          quantityReceived: 120,
          quantityCanceled: 0,
        },
        {
          receiptItemId: 'cab2b1fa9d69b73a019d6c06c9060026',
          lotNumber: 'AM-2025-09-B',
          expirationDate: '2026-12-15',
          recipient: {
            id: 'r-002', name: 'Jane Smith', firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com', username: 'janesmith',
          },
          quantityReceived: 80,
          quantityCanceled: 0,
        },
      ],
    },
  ],
};

const useReceivingActions = () => {
  const { shipmentId } = useParams();
  const [loading, setLoading] = useState(false);
  const [lineItems, setLineItems] = useState([]);

  // TODO: We will replace that transform when we implement the normalization table OBPIH-7857
  const transformReceiptToLineItems = (receipt) =>
    receipt.receiptItems.flatMap((shipmentItem) =>
      [...shipmentItem.previousReceipts, ...shipmentItem.currentReceiptItems].map((receiptItem) => (
        {
          receiptItemId: receiptItem.receiptItemId,
          receiptId: receiptItem.receiptId,
          receiptStatus: receiptItem.receiptStatus,
          shipmentItemId: shipmentItem.shipmentItemId,
          productCode: shipmentItem.product?.productCode,
          product: shipmentItem.product,
          parentContainer: shipmentItem.parentContainer,
          container: shipmentItem.container,
          lotNumber: receiptItem.lotNumber,
          expirationDate: receiptItem.expirationDate,
          recipient: receiptItem.recipient,
          quantityShipped: shipmentItem.quantityShipped,
          quantityReceiving: receiptItem.quantityReceived,
          quantityRemaining: shipmentItem.quantityShipped - shipmentItem.quantityReceived,
        })));

  const fetchLineItems = async () => {
    setLoading(true);
    try {
      const { data: receipt } = await receivingApi.getReceipt(shipmentId, {
        format: ReceiptResponseFormat.DEFAULT,
      });
      setLineItems(transformReceiptToLineItems(receipt));
    } catch {
      // TODO: remove fallback once OBPIH-7868 (backend read endpoint) ships
      setLineItems(transformReceiptToLineItems(MOCK_RECEIPT));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLineItems();
  }, [shipmentId]);

  return {
    loading,
    lineItems,
  };
};

export default useReceivingActions;
