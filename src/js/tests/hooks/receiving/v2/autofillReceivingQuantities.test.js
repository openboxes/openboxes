import { autofillReceivingQuantities } from 'hooks/receiving/v2/useReceivingActions';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  quantityReceiving: null,
  quantityRemaining: 10,
  isCompleted: false,
  isDirty: false,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

describe('autofillReceivingQuantities()', () => {
  it('should fill an empty row with the remaining quantity and mark it dirty', () => {
    const state = buildState([buildRow('row-1', { quantityRemaining: 7 })]);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'])
      .toEqual(expect.objectContaining({ quantityReceiving: 7, isDirty: true }));
  });

  it('should leave rows with an already entered quantity untouched, including 0', () => {
    const state = buildState([
      buildRow('row-1', { quantityReceiving: 3 }),
      buildRow('row-2', { quantityReceiving: 0 }),
    ]);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'].quantityReceiving)
      .toBe(3);
    expect(result.entities['row-2'].quantityReceiving)
      .toBe(0);
    expect(result)
      .toBe(state);
  });

  it('should leave completed rows untouched even when their quantity is empty', () => {
    const state = buildState([buildRow('row-1', { isCompleted: true })]);

    const result = autofillReceivingQuantities(state);

    expect(result)
      .toBe(state);
  });

  it('should skip rows with zero or negative remaining quantity', () => {
    const state = buildState([
      buildRow('row-1', { quantityRemaining: 0 }),
      buildRow('row-2', { quantityRemaining: -2 }),
    ]);

    const result = autofillReceivingQuantities(state);

    expect(result)
      .toBe(state);
  });

  it('should return the same state reference when no row is autofilled', () => {
    const state = buildState([
      buildRow('row-1', { quantityReceiving: 1 }),
      buildRow('row-2', { isCompleted: true }),
    ]);

    expect(autofillReceivingQuantities(state))
      .toBe(state);
  });

  it('should ignore separator entries in ids (packing list view)', () => {
    const separator = { isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' };
    const row = buildRow('row-1', { quantityRemaining: 4 });
    const state = buildState([row], [separator, 'row-1']);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'].quantityReceiving)
      .toBe(4);
    expect(result.ids)
      .toEqual([separator, 'row-1']);
    expect(Object.keys(result.entities))
      .toEqual(['row-1']);
  });
});
