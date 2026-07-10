import ReceivingRowType from 'consts/receivingRowType';
import { autofillReceivingQuantities } from 'hooks/receiving/v2/useReceivingActions';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  quantityReceiving: null,
  quantityAvailableToReceive: 10,
  isCompleted: false,
  isDirty: false,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

describe('autofillReceivingQuantities()', () => {
  it('should fill an empty row with the available quantity and mark it dirty', () => {
    const state = buildState([buildRow('row-1', { quantityAvailableToReceive: 7 })]);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'])
      .toEqual(expect.objectContaining({ quantityReceiving: 7, isDirty: true }));
  });

  it('should refill a cleared row whose pending quantity already covers the shipment', () => {
    const state = buildState([buildRow('row-1', {
      quantityReceiving: null,
      quantityRemaining: 0,
      quantityAvailableToReceive: 10,
    })]);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'])
      .toEqual(expect.objectContaining({ quantityReceiving: 10, isDirty: true }));
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

  it('should skip rows with zero or negative available quantity', () => {
    const state = buildState([
      buildRow('row-1', { quantityAvailableToReceive: 0 }),
      buildRow('row-2', { quantityAvailableToReceive: -2 }),
    ]);

    const result = autofillReceivingQuantities(state);

    expect(result)
      .toBe(state);
  });

  it('should skip replaced and toggle rows of a split item', () => {
    const state = buildState([
      buildRow('row-1', { rowType: ReceivingRowType.REPLACED }),
      { rowType: ReceivingRowType.TOGGLE, rowId: 'row-2', splitItemIds: ['row-3'] },
    ]);

    const result = autofillReceivingQuantities(state);

    expect(result)
      .toBe(state);
  });

  it('should fill an empty split item row', () => {
    const state = buildState([buildRow('row-1', {
      rowType: ReceivingRowType.SPLIT_ITEM,
      quantityAvailableToReceive: 4,
    })]);

    const result = autofillReceivingQuantities(state);

    expect(result.entities['row-1'])
      .toEqual(expect.objectContaining({ quantityReceiving: 4, isDirty: true }));
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
    const row = buildRow('row-1', { quantityAvailableToReceive: 4 });
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
