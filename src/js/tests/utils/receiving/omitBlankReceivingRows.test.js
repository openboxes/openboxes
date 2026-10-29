import ReceivingRowType from 'consts/receivingRowType';
import omitBlankReceivingRows from 'utils/receiving/omitBlankReceivingRows';

import '@testing-library/jest-dom';

const buildRow = (rowId, shipmentItemId, overrides = {}) => ({
  rowId,
  shipmentItemId,
  rowType: null,
  quantityReceiving: null,
  ...overrides,
});

const buildState = (rows, ids = null) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: ids ?? rows.map((row) => row.rowId),
});

describe('omitBlankReceivingRows()', () => {
  it('should drop the lines with no quantity entered', () => {
    const state = buildState([
      buildRow('row-1', 'shipment-item-1', { quantityReceiving: 5 }),
      buildRow('row-2', 'shipment-item-2'),
    ]);

    expect(omitBlankReceivingRows(state).ids).toEqual(['row-1']);
  });

  it('should keep a line received as zero', () => {
    const state = buildState([buildRow('row-1', 'shipment-item-1', { quantityReceiving: 0 })]);

    expect(omitBlankReceivingRows(state).ids).toEqual(['row-1']);
  });

  it('should keep the whole changes group of a split item that carries a quantity', () => {
    const state = buildState([
      buildRow('row-1', 'shipment-item-1', { rowType: ReceivingRowType.REPLACED }),
      buildRow('row-2', 'shipment-item-1', { rowType: ReceivingRowType.TOGGLE }),
      buildRow('row-3', 'shipment-item-1', {
        rowType: ReceivingRowType.SPLIT_ITEM,
        quantityReceiving: 2,
      }),
    ]);

    expect(omitBlankReceivingRows(state).ids).toEqual(['row-1', 'row-2', 'row-3']);
  });

  it('should drop a changes group where nothing was entered', () => {
    const state = buildState([
      buildRow('row-1', 'shipment-item-1', { rowType: ReceivingRowType.REPLACED }),
      // The toggle row is built without a quantity field at all, not with an empty one.
      {
        rowId: 'row-2',
        shipmentItemId: 'shipment-item-1',
        rowType: ReceivingRowType.TOGGLE,
      },
      buildRow('row-3', 'shipment-item-1', { rowType: ReceivingRowType.SPLIT_ITEM }),
      buildRow('row-4', 'shipment-item-2', { quantityReceiving: 1 }),
    ]);

    expect(omitBlankReceivingRows(state).ids).toEqual(['row-4']);
  });

  it('should keep the entities so rows can still be read by their id', () => {
    const state = buildState([buildRow('row-1', 'shipment-item-1')]);

    expect(omitBlankReceivingRows(state).entities).toEqual(state.entities);
  });

  it('should drop the packing list separators of groups with no rows left', () => {
    const emptyGroup = { isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' };
    const filledGroup = { isSeparator: true, id: 'separator-Pallet 2', name: 'Pallet 2' };
    const state = buildState(
      [
        buildRow('row-1', 'shipment-item-1', { separatorId: emptyGroup.id }),
        buildRow('row-2', 'shipment-item-2', {
          separatorId: filledGroup.id,
          quantityReceiving: 4,
        }),
      ],
      [emptyGroup, 'row-1', filledGroup, 'row-2'],
    );

    expect(omitBlankReceivingRows(state).ids).toEqual([filledGroup, 'row-2']);
  });

  it('should handle an empty state', () => {
    expect(omitBlankReceivingRows({ entities: {}, ids: [] }).ids).toEqual([]);
  });
});
