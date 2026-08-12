import ReceivingRowType from 'consts/receivingRowType';
import { getCancellableReceiptItemIds } from 'hooks/receiving/v2/useCancelRemaining';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  originalReceiptItemId: `original-${rowId}`,
  quantityAvailableToReceive: 10,
  isCompleted: false,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

describe('getCancellableReceiptItemIds()', () => {
  it('should collect the original receipt item of every plain row', () => {
    const state = buildState([buildRow('row-1'), buildRow('row-2')]);

    expect(getCancellableReceiptItemIds(state))
      .toEqual(['original-row-1', 'original-row-2']);
  });

  it('should collect the replaced row of a split shipment item, not its split items', () => {
    const state = buildState([
      buildRow('row-1', { rowType: ReceivingRowType.REPLACED }),
      buildRow('row-2', { rowType: ReceivingRowType.TOGGLE }),
      buildRow('row-3', { rowType: ReceivingRowType.SPLIT_ITEM }),
    ]);

    expect(getCancellableReceiptItemIds(state)).toEqual(['original-row-1']);
  });

  it('should skip completed rows and rows with nothing left to receive', () => {
    const state = buildState([
      buildRow('row-1', { isCompleted: true }),
      buildRow('row-2', { quantityAvailableToReceive: 0 }),
      buildRow('row-3', { quantityAvailableToReceive: null }),
    ]);

    expect(getCancellableReceiptItemIds(state)).toEqual([]);
  });

  it('should skip rows whose shipment item has no original line to carry the cancel', () => {
    const state = buildState([buildRow('row-1', { originalReceiptItemId: null })]);

    expect(getCancellableReceiptItemIds(state)).toEqual([]);
  });

  it('should ignore separator entries in ids (packing list view)', () => {
    const row = buildRow('row-1');
    const state = buildState([row], [{ isSeparator: true, id: 'separator-Pallet 1' }, row.rowId]);

    expect(getCancellableReceiptItemIds(state)).toEqual(['original-row-1']);
  });
});
