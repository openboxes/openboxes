import ReceivingRowType from 'consts/receivingRowType';
import { getCancellableReceiptItemIds, isCancellableRow } from 'hooks/receiving/v2/useCancelRemaining';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  originalReceiptItemId: `original-${rowId}`,
  quantityRemaining: 10,
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

  it('should skip rows with nothing left to cancel', () => {
    const state = buildState([
      buildRow('row-1', { isCompleted: true, quantityRemaining: 0 }),
      // Fully covered by the pending receipt - the status cell reads "Equal"
      buildRow('row-2', { quantityRemaining: 0 }),
      buildRow('row-3', { quantityRemaining: null }),
      // Over received - the status cell reads "2 over"
      buildRow('row-4', { quantityRemaining: -2 }),
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

describe('isCancellableRow()', () => {
  it('should accept a plain row with a quantity left to cancel', () => {
    expect(isCancellableRow(buildRow('row-1'))).toBe(true);
  });

  it('should accept the replaced row of a split shipment item', () => {
    expect(isCancellableRow(buildRow('row-1', { rowType: ReceivingRowType.REPLACED }))).toBe(true);
  });

  it.each([
    ['split item rows', { rowType: ReceivingRowType.SPLIT_ITEM }],
    ['toggle rows', { rowType: ReceivingRowType.TOGGLE }],
    ['rows without an original line', { originalReceiptItemId: null }],
    ['rows fully covered by the pending receipt', { quantityRemaining: 0 }],
    ['over received rows', { quantityRemaining: -2 }],
    ['rows without a remaining quantity', { quantityRemaining: null }],
  ])('should reject %s', (_label, overrides) => {
    expect(isCancellableRow(buildRow('row-1', overrides))).toBe(false);
  });

  it('should reject separator entries', () => {
    expect(isCancellableRow(undefined)).toBe(false);
  });
});
