import getOptionalColumnsVisibility from 'utils/receiving/getOptionalColumnsVisibility';

const buildState = (rows) => ({
  entities: rows.reduce((acc, row) => {
    acc[row.rowId] = row;
    return acc;
  }, {}),
  ids: rows.map((row) => row.rowId),
});

const container = { id: 'container-1', name: 'Pallet 1' };
const recipient = { id: 'user-1', name: 'John Doe' };

describe('getOptionalColumnsVisibility', () => {
  it('hides every optional column when no row fills them', () => {
    const state = buildState([
      { rowId: 'row-1', lotNumber: null, recipient: null },
      { rowId: 'row-2' },
    ]);

    expect(getOptionalColumnsVisibility(state)).toEqual({
      showLotNumber: false,
      showExpirationDate: false,
      showRecipient: false,
      showPackLevel: false,
    });
  });

  it('hides every optional column for an empty state', () => {
    expect(getOptionalColumnsVisibility(buildState([]))).toEqual({
      showLotNumber: false,
      showExpirationDate: false,
      showRecipient: false,
      showPackLevel: false,
    });
  });

  it('shows the lot and the expiration date when a single row has a lot', () => {
    const state = buildState([
      { rowId: 'row-1' },
      { rowId: 'row-2', lotNumber: 'LOT-1', expirationDate: null },
    ]);

    const { showLotNumber, showExpirationDate } = getOptionalColumnsVisibility(state);

    expect(showLotNumber).toBe(true);
    expect(showExpirationDate).toBe(true);
  });

  it('hides the expiration date of data carrying no lot', () => {
    const state = buildState([{ rowId: 'row-1', expirationDate: '2026-01-31' }]);

    const { showLotNumber, showExpirationDate } = getOptionalColumnsVisibility(state);

    expect(showLotNumber).toBe(false);
    expect(showExpirationDate).toBe(false);
  });

  it('shows the recipient and the pack level when a single row fills them', () => {
    const state = buildState([
      { rowId: 'row-1' },
      { rowId: 'row-2', recipient, container },
    ]);

    const { showRecipient, showPackLevel } = getOptionalColumnsVisibility(state);

    expect(showRecipient).toBe(true);
    expect(showPackLevel).toBe(true);
  });

  it('counts the values of a lot added on a split item row', () => {
    const state = buildState([
      { rowId: 'row-1', lotNumber: null, rowType: 'REPLACED' },
      { rowId: 'row-2', lotNumber: 'LOT-1', rowType: 'SPLIT_ITEM' },
    ]);

    expect(getOptionalColumnsVisibility(state).showLotNumber).toBe(true);
  });

  it('counts the values of the hidden original line of a split shipment item', () => {
    const state = buildState([
      {
        rowId: 'row-1',
        originalLineItem: { rowId: 'row-original', lotNumber: 'LOT-1', recipient },
      },
    ]);

    const { showLotNumber, showRecipient } = getOptionalColumnsVisibility(state);

    expect(showLotNumber).toBe(true);
    expect(showRecipient).toBe(true);
  });

  it('skips the separator rows of the packing list view', () => {
    const separator = { isSeparator: true, id: 'separator-Pallet 1', name: 'Pallet 1' };
    const state = buildState([{ rowId: 'row-1', container }]);
    const packingListState = {
      entities: state.entities,
      ids: [separator, ...state.ids],
    };

    expect(getOptionalColumnsVisibility(packingListState)).toEqual({
      showLotNumber: false,
      showExpirationDate: false,
      showRecipient: false,
      showPackLevel: true,
    });
  });
});
