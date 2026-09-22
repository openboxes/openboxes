import ReceivingRowType from 'consts/receivingRowType';
import getBlankReceivingRows, { getEditableReceivingRows } from 'utils/receiving/getBlankReceivingRows';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  quantityReceiving: null,
  isCompleted: false,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

const rowIds = (rows) => rows.map((row) => row.rowId);

describe('getEditableReceivingRows()', () => {
  it('should return plain lines and split items', () => {
    const state = buildState([
      buildRow('row-1'),
      buildRow('row-2', { rowType: ReceivingRowType.SPLIT_ITEM, quantityReceiving: 3 }),
    ]);

    expect(rowIds(getEditableReceivingRows(state))).toEqual(['row-1', 'row-2']);
  });

  it('should skip the rows without a quantity input', () => {
    const state = buildState([
      buildRow('row-1', { rowType: ReceivingRowType.REPLACED }),
      buildRow('row-2', { rowType: ReceivingRowType.TOGGLE }),
      buildRow('row-3'),
    ]);

    expect(rowIds(getEditableReceivingRows(state))).toEqual(['row-3']);
  });

  it('should skip completed rows', () => {
    const state = buildState([buildRow('row-1', { isCompleted: true }), buildRow('row-2')]);

    expect(rowIds(getEditableReceivingRows(state))).toEqual(['row-2']);
  });

  it('should skip packing list separators, which have no entity', () => {
    const separator = { isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' };
    const row = buildRow('row-1');
    const state = buildState([row], [separator, 'row-1']);

    expect(rowIds(getEditableReceivingRows(state))).toEqual(['row-1']);
  });

  it('should handle an empty state', () => {
    expect(getEditableReceivingRows(undefined)).toEqual([]);
    expect(getEditableReceivingRows({ entities: {}, ids: [] })).toEqual([]);
  });
});

describe('getBlankReceivingRows()', () => {
  it('should return only the rows with no quantity entered', () => {
    const state = buildState([
      buildRow('row-1'),
      buildRow('row-2', { quantityReceiving: 5 }),
      buildRow('row-3'),
    ]);

    expect(rowIds(getBlankReceivingRows(state))).toEqual(['row-1', 'row-3']);
  });

  it('should treat a deliberate 0 as entered', () => {
    const state = buildState([buildRow('row-1', { quantityReceiving: 0 })]);

    expect(getBlankReceivingRows(state)).toEqual([]);
  });

  it('should not report completed rows as blank', () => {
    const state = buildState([buildRow('row-1', { isCompleted: true })]);

    expect(getBlankReceivingRows(state)).toEqual([]);
  });
});
