import ReceivingRowType from 'consts/receivingRowType';
import { getAutofillQuantityUpdates } from 'hooks/receiving/v2/useReceivingActions';

import '@testing-library/jest-dom';

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  quantityReceiving: null,
  quantityAvailableToReceive: 10,
  isCompleted: false,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

describe('getAutofillQuantityUpdates()', () => {
  it('should fill an empty row with the available quantity', () => {
    const state = buildState([buildRow('row-1', { quantityAvailableToReceive: 7 })]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([{ rowId: 'row-1', quantityReceiving: 7 }]);
  });

  it('should refill a cleared row whose pending quantity already covers the shipment', () => {
    const state = buildState([buildRow('row-1', {
      quantityReceiving: null,
      quantityRemaining: 0,
      quantityAvailableToReceive: 10,
    })]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([{ rowId: 'row-1', quantityReceiving: 10 }]);
  });

  it('should leave rows with an already entered quantity untouched, including 0', () => {
    const state = buildState([
      buildRow('row-1', { quantityReceiving: 3 }),
      buildRow('row-2', { quantityReceiving: 0 }),
    ]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([]);
  });

  it('should leave completed rows untouched even when their quantity is empty', () => {
    const state = buildState([buildRow('row-1', { isCompleted: true })]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([]);
  });

  it('should skip rows with zero or negative available quantity', () => {
    const state = buildState([
      buildRow('row-1', { quantityAvailableToReceive: 0 }),
      buildRow('row-2', { quantityAvailableToReceive: -2 }),
    ]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([]);
  });

  it('should skip replaced and toggle rows of a split item', () => {
    const state = buildState([
      buildRow('row-1', { rowType: ReceivingRowType.REPLACED }),
      { rowType: ReceivingRowType.TOGGLE, rowId: 'row-2', splitItemIds: ['row-3'] },
    ]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([]);
  });

  it('should fill an empty split item row', () => {
    const state = buildState([buildRow('row-1', {
      rowType: ReceivingRowType.SPLIT_ITEM,
      quantityAvailableToReceive: 4,
    })]);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([{ rowId: 'row-1', quantityReceiving: 4 }]);
  });

  it('should ignore separator entries in ids (packing list view)', () => {
    const separator = { isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' };
    const row = buildRow('row-1', { quantityAvailableToReceive: 4 });
    const state = buildState([row], [separator, 'row-1']);

    expect(getAutofillQuantityUpdates(state))
      .toEqual([{ rowId: 'row-1', quantityReceiving: 4 }]);
  });
});
