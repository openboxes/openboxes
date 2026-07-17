import buildReceiptItemsBatchPayload from 'utils/receiving/buildReceiptItemsBatchPayload';

import '@testing-library/jest-dom';

describe('buildReceiptItemsBatchPayload()', () => {
  it('should map a new row to a create entry', () => {
    const payload = buildReceiptItemsBatchPayload([{
      rowId: 'row-1',
      shipmentItemId: 'shipment-item-1',
      receiptItemId: null,
      quantityReceiving: 5,
      binLocation: null,
    }]);

    expect(payload).toEqual({
      itemsToSave: [{
        rowId: 'row-1',
        shipmentItem: { id: 'shipment-item-1' },
        receiptItem: null,
        quantityReceiving: 5,
        binLocation: null,
      }],
      itemsToDelete: [],
    });
  });

  it('should map an already persisted row to an update entry with its ids', () => {
    const payload = buildReceiptItemsBatchPayload([{
      rowId: 'row-1',
      shipmentItemId: 'shipment-item-1',
      receiptItemId: 'receipt-item-1',
      quantityReceiving: 3,
      binLocation: { id: 'bin-1', name: 'A1' },
    }]);

    expect(payload.itemsToSave).toEqual([{
      rowId: 'row-1',
      shipmentItem: { id: 'shipment-item-1' },
      receiptItem: { id: 'receipt-item-1' },
      quantityReceiving: 3,
      binLocation: { id: 'bin-1' },
    }]);
  });

  it('should return an empty payload for no rows', () => {
    expect(buildReceiptItemsBatchPayload([]))
      .toEqual({ itemsToSave: [], itemsToDelete: [] });
    expect(buildReceiptItemsBatchPayload(undefined))
      .toEqual({ itemsToSave: [], itemsToDelete: [] });
  });
});
