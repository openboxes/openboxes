import ReceiptGroup from 'consts/receiptGroup';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import {
  mergeStartedReceipt,
  receiptGroupForView,
  transformReceiptSummary,
} from 'utils/receiving/receiptSummaryRows';

import '@testing-library/jest-dom';

const buildSummary = (id, overrides = {}) => ({
  shipmentItem: {
    id,
    quantity: 100,
    productLot: {
      product: { id: `product-${id}`, productCode: `code-${id}`, name: `Product ${id}` },
      lotNumber: `lot-${id}`,
    },
  },
  currentReceiptItems: [],
  previousReceiptItems: [],
  totalQuantityReceived: 0,
  totalQuantityCanceled: 0,
  ...overrides,
});

const buildData = (summaries, grouped) => ({
  shipmentItemSummaryById: summaries.reduce(
    (acc, summary) => ({ ...acc, [summary.shipmentItem.id]: summary }),
    {},
  ),
  shipmentItemsGrouped: grouped,
});

const entitiesInOrder = (state) => state.ids
  .filter((id) => !id.isSeparator)
  .map((id) => state.entities[id]);

describe('receiptGroupForView()', () => {
  it('should group by pack level in packing list view and by shipment item otherwise', () => {
    expect(receiptGroupForView(ReceivingView.PACKING_LIST)).toBe(ReceiptGroup.PACK_LEVEL);
    expect(receiptGroupForView(ReceivingView.TABLE)).toBe(ReceiptGroup.SHIPMENT_ITEM);
  });
});

describe('mergeStartedReceipt()', () => {
  const startedReceipt = {
    id: 'receipt-1',
    receiptItems: [
      { id: 'item-a', shipmentItemId: 'a', isSplitItem: false },
      { id: 'item-b', shipmentItemId: 'b', isSplitItem: false },
    ],
  };

  it('should fill in the pending receipt id and the created lines of each shipment item', () => {
    const data = buildData([buildSummary('a'), buildSummary('b')], { order: ['a', 'b'] });

    const merged = mergeStartedReceipt(data, startedReceipt);

    expect(merged.pendingReceiptId).toBe('receipt-1');
    expect(merged.shipmentItemSummaryById.a.currentReceiptItems)
      .toEqual([{ id: 'item-a', shipmentItemId: 'a', isSplitItem: false }]);
    expect(merged.shipmentItemSummaryById.b.currentReceiptItems)
      .toEqual([{ id: 'item-b', shipmentItemId: 'b', isSplitItem: false }]);
  });

  it('should leave shipment items without a created line empty', () => {
    const data = buildData([buildSummary('a'), buildSummary('c')], { order: ['a', 'c'] });

    const merged = mergeStartedReceipt(data, startedReceipt);

    expect(merged.shipmentItemSummaryById.c.currentReceiptItems).toEqual([]);
  });

  it('should keep previously received lines and the rest of the summary untouched', () => {
    const previousReceiptItems = [{ id: 'old-item', quantityReceived: 5 }];
    const data = buildData(
      [buildSummary('a', { previousReceiptItems, totalQuantityReceived: 5 })],
      { order: ['a'] },
    );

    const merged = mergeStartedReceipt(data, startedReceipt);

    expect(merged.shipmentItemSummaryById.a.previousReceiptItems).toEqual(previousReceiptItems);
    expect(merged.shipmentItemSummaryById.a.totalQuantityReceived).toBe(5);
    expect(merged.shipmentItemsGrouped).toEqual(data.shipmentItemsGrouped);
  });

  it('should return the summary as is when no receipt was started', () => {
    const data = buildData([buildSummary('a')], { order: ['a'] });

    expect(mergeStartedReceipt(data, null)).toBe(data);
    expect(mergeStartedReceipt(data, {})).toBe(data);
  });

  it('should build rows carrying the created receipt item ids', () => {
    const data = buildData([buildSummary('a')], { order: ['a'] });

    const state = transformReceiptSummary(
      mergeStartedReceipt(data, startedReceipt),
      ReceivingView.TABLE,
      {},
    );

    expect(entitiesInOrder(state)[0]).toMatchObject({
      receiptItemId: 'item-a',
      originalReceiptItemId: 'item-a',
    });
  });
});

describe('transformReceiptSummary() - table view', () => {
  it('should build one row per shipment item in the grouped order', () => {
    const data = buildData(
      [buildSummary('b'), buildSummary('a')],
      { order: ['a', 'b'] },
    );

    const state = transformReceiptSummary(data, ReceivingView.TABLE, {});

    expect(state.ids).toHaveLength(2);
    expect(entitiesInOrder(state).map((entity) => entity.shipmentItemId)).toEqual(['a', 'b']);
  });

  it('should derive the quantities of a partially received line', () => {
    const data = buildData([buildSummary('a', {
      // The receipt item mirrors the shipment item's product lot, so the line stays a plain
      // row instead of entering the changes group.
      currentReceiptItems: [{
        id: 'receipt-item',
        quantityReceived: 20,
        productLot: { product: { id: 'product-a' }, lotNumber: 'lot-a' },
      }],
      previousReceiptItems: [{ quantityReceived: 25 }, { quantityReceived: 5 }],
      totalQuantityReceived: 50,
    })], { order: ['a'] });

    const [row] = entitiesInOrder(transformReceiptSummary(data, ReceivingView.TABLE, {}));

    expect(row).toMatchObject({
      quantityShipped: 100,
      quantityReceived: 30,
      quantityReceiving: 20,
      quantityRemaining: 50,
      quantityAvailableToReceive: 70,
      isCompleted: false,
    });
  });

  it('should complete a line only when submitted receipts cover the shipment and nothing is pending', () => {
    const data = buildData([buildSummary('a', {
      previousReceiptItems: [{ quantityReceived: 60 }, { quantityCanceled: 40 }],
      totalQuantityReceived: 60,
      totalQuantityCanceled: 40,
    })], { order: ['a'] });

    const [row] = entitiesInOrder(transformReceiptSummary(data, ReceivingView.TABLE, {}));

    expect(row).toMatchObject({ isCompleted: true, quantityRemaining: 0 });
  });

  it('should build a changes group for a split shipment item', () => {
    const data = buildData([buildSummary('a', {
      currentReceiptItems: [
        { id: 'original', quantityReceived: 5 },
        { id: 'split', isSplitItem: true, quantityReceived: 3 },
      ],
      totalQuantityReceived: 8,
    })], { order: ['a'] });

    const rows = entitiesInOrder(transformReceiptSummary(data, ReceivingView.TABLE, {}));

    expect(rows.map((row) => row.rowType)).toEqual([
      ReceivingRowType.REPLACED,
      ReceivingRowType.TOGGLE,
      ReceivingRowType.SPLIT_ITEM,
      ReceivingRowType.SPLIT_ITEM,
    ]);
    const [replacedRow, toggleRow, ...splitRows] = rows;
    expect(toggleRow.replacedRowId).toBe(replacedRow.rowId);
    expect(toggleRow.splitItemIds).toEqual(splitRows.map((row) => row.rowId));
    // The replaced row carries the whole group's quantities: the remaining quantity plus
    // the pending quantities of its split items.
    expect(replacedRow).toMatchObject({
      quantityRemaining: 92,
      quantityAvailableToReceive: 100,
    });
  });
});

describe('transformReceiptSummary() - packing list view', () => {
  it('should interleave separator rows and stamp line items with their group', () => {
    const data = buildData(
      [buildSummary('a'), buildSummary('b')],
      {
        order: ['Pallet 1'],
        groups: {
          'Pallet 1': {
            order: ['Box A', 'Box B'],
            groups: { 'Box A': ['a'], 'Box B': ['b'] },
          },
        },
      },
    );

    const state = transformReceiptSummary(data, ReceivingView.PACKING_LIST, {});

    expect(state.ids).toHaveLength(3);
    expect(state.ids[0]).toEqual({ isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' });
    expect(entitiesInOrder(state)).toEqual([
      expect.objectContaining({
        shipmentItemId: 'a',
        packLevelGroup: 'Box A',
        separatorId: 'separator-Pallet 1',
      }),
      expect.objectContaining({
        shipmentItemId: 'b',
        packLevelGroup: 'Box B',
        separatorId: 'separator-Pallet 1',
      }),
    ]);
  });
});
