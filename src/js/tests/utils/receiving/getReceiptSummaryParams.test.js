import ReceiptGroup from 'consts/receiptGroup';
import { ReceivingView } from 'consts/receivingViewOptions';
import getReceiptSummaryParams from 'utils/receiving/getReceiptSummaryParams';

import '@testing-library/jest-dom';

describe('getReceiptSummaryParams()', () => {
  it('should group by shipment item in table view', () => {
    expect(getReceiptSummaryParams({ view: ReceivingView.TABLE }))
      .toEqual({ group: ReceiptGroup.SHIPMENT_ITEM });
  });

  it('should group by pack level in packing list view', () => {
    expect(getReceiptSummaryParams({ view: ReceivingView.PACKING_LIST }))
      .toEqual({ group: ReceiptGroup.PACK_LEVEL });
  });

  it('should send an ascending sort as a plain field name', () => {
    expect(getReceiptSummaryParams({ sort: 'productCode', sortOrder: 'asc' }).sort)
      .toBe('productCode');
  });

  it('should prefix a descending sort with a minus', () => {
    expect(getReceiptSummaryParams({ sort: 'productCode', sortOrder: 'desc' }).sort)
      .toBe('-productCode');
  });

  it('should leave the sort out entirely when no column is sorted', () => {
    expect(getReceiptSummaryParams({ sortOrder: 'asc' })).not.toHaveProperty('sort');
  });
});
