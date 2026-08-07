import anyLineHasOtherBin from 'utils/receiving/anyLineHasOtherBin';

const receivingBin = { id: 'bin-r', name: 'R-00001' };
const putawayBin = { id: 'bin-a', name: 'A1' };

const buildState = (rows) => ({
  entities: rows.reduce((acc, row) => {
    acc[row.rowId] = row;
    return acc;
  }, {}),
  ids: rows.map((row) => row.rowId),
});

describe('anyLineHasOtherBin', () => {
  it('returns false when every line sits in the receiving bin', () => {
    const state = buildState([
      { rowId: 'row-1', binLocation: receivingBin },
      { rowId: 'row-2', binLocation: receivingBin },
    ]);

    expect(anyLineHasOtherBin(state, receivingBin)).toBe(false);
  });

  it('returns true when a single line was put away to another bin', () => {
    const state = buildState([
      { rowId: 'row-1', binLocation: receivingBin },
      { rowId: 'row-2', binLocation: putawayBin },
    ]);

    expect(anyLineHasOtherBin(state, receivingBin)).toBe(true);
  });

  it('returns false when no line has a bin location', () => {
    const state = buildState([
      { rowId: 'row-1', binLocation: null },
      { rowId: 'row-2' },
    ]);

    expect(anyLineHasOtherBin(state, receivingBin)).toBe(false);
  });

  it('returns false for an empty state', () => {
    expect(anyLineHasOtherBin(buildState([]), receivingBin)).toBe(false);
  });

  it('counts any bin as changed when the shipment has no receiving bin', () => {
    const state = buildState([{ rowId: 'row-1', binLocation: putawayBin }]);

    expect(anyLineHasOtherBin(state, null)).toBe(true);
    expect(anyLineHasOtherBin(buildState([{ rowId: 'row-1' }]), null)).toBe(false);
  });
});
