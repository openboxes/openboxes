// OBAF-modified: stock transfer items carry their Reason for Shortage (reasonCode), blanked while
// the transfer is still PENDING, like the quantity.
import { extractNonCanceledItems, extractStockTransferItems, prepareRequest } from 'components/stock-transfer/utils';

const transfer = (status) => ({
  status,
  stockTransferItems: [
    {
      id: 'i1',
      quantity: 5,
      quantityOnHand: 10,
      reasonCode: 'DAMAGED',
      status: 'PENDING',
      destinationBinLocation: { id: 'b1', name: 'Bin 1' },
      destinationZone: { id: 'z1', name: 'Zone 1' },
      splitItems: [{
        id: 's1',
        quantity: 2,
        reasonCode: 'EXPIRED',
        destinationBinLocation: { id: 'b2', name: 'Bin 2' },
        destinationZone: null,
      }],
    },
    {
      id: 'i2',
      quantity: 1,
      quantityOnHand: 1,
      reasonCode: 'OTHER',
      status: 'CANCELED',
      destinationBinLocation: null,
      destinationZone: null,
      splitItems: [],
    },
  ],
});

describe('stock transfer utils (OBAF reason for shortage)', () => {
  it('keeps the reason codes of a transfer past PENDING', () => {
    const items = extractStockTransferItems(transfer('APPROVED'));

    expect(items.map(({ id, reasonCode, quantity }) => [id, reasonCode, quantity])).toEqual([
      ['i1', 'DAMAGED', 5],
      ['s1', 'EXPIRED', 2],
      ['i2', 'OTHER', 1],
    ]);
    expect(items[0].destinationBinLocation).toEqual({
      id: 'b1', name: 'Bin 1', zoneId: 'z1', zoneName: 'Zone 1',
    });
    expect(items[1]).toMatchObject({
      referenceId: 'i1',
      quantityOnHand: 10,
      destinationBinLocation: {
        id: 'b2', name: 'Bin 2', zoneId: null, zoneName: null,
      },
    });
    expect(items[2].destinationBinLocation).toEqual({
      id: null, name: null, zoneId: null, zoneName: null,
    });
  });

  it('blanks the reason codes and quantities of a PENDING transfer', () => {
    const items = extractStockTransferItems(transfer('PENDING'));

    items.forEach((item) => {
      expect(item.reasonCode).toBe('');
      expect(item.quantity).toBe('');
      expect(item.destinationBinLocation).toEqual({
        id: null, name: null, zoneId: null, zoneName: null,
      });
    });
  });

  it('drops canceled lines but keeps their reason codes elsewhere', () => {
    const items = extractNonCanceledItems(transfer('APPROVED'));
    expect(items.map(({ id, reasonCode }) => [id, reasonCode])).toEqual([
      ['i1', 'DAMAGED'],
      ['s1', 'EXPIRED'],
    ]);
  });

  it('prepareRequest regroups split lines and keeps the reason codes', () => {
    const request = prepareRequest({
      id: 'st1',
      stockTransferItems: [
        {
          id: 'i1', quantity: '', quantityOnHand: 10, reasonCode: 'DAMAGED',
        },
        {
          id: 's1', referenceId: 'i1', quantity: 2, reasonCode: 'EXPIRED',
        },
      ],
    }, 'COMPLETED');

    expect(request.status).toBe('COMPLETED');
    expect(request.stockTransferItems).toEqual([{
      id: 'i1',
      quantity: 10,
      quantityOnHand: 10,
      reasonCode: 'DAMAGED',
      splitItems: [{
        id: 's1', referenceId: 'i1', quantity: 2, reasonCode: 'EXPIRED',
      }],
    }]);
  });
});
