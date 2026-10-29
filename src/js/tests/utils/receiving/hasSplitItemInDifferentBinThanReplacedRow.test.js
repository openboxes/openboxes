import hasSplitItemInDifferentBinThanReplacedRow from 'utils/receiving/hasSplitItemInDifferentBinThanReplacedRow';

const receivingBin = { id: 'bin-receiving', name: 'R-SHIP-1' };
const replacedRow = { toggleRowId: 'toggle' };

const buildEntities = (splitItems) => {
  const entities = { toggle: { splitItemIds: [] } };
  splitItems.forEach((splitItem, index) => {
    const splitItemId = `split-${index}`;
    entities.toggle.splitItemIds.push(splitItemId);
    entities[splitItemId] = splitItem;
  });
  return entities;
};

describe('hasSplitItemInDifferentBinThanReplacedRow', () => {
  it('should be false when every line receives into the given bin', () => {
    const entities = buildEntities([{ binLocation: receivingBin }, { binLocation: receivingBin }]);

    expect(hasSplitItemInDifferentBinThanReplacedRow(replacedRow, entities, receivingBin))
      .toBe(false);
  });

  it('should be true when a line receives into another bin', () => {
    const entities = buildEntities([
      { binLocation: receivingBin },
      { binLocation: { id: 'bin-other', name: 'Other bin' } },
    ]);

    expect(hasSplitItemInDifferentBinThanReplacedRow(replacedRow, entities, receivingBin))
      .toBe(true);
  });

  it('should treat a line left without a bin as different from the receiving bin', () => {
    const entities = buildEntities([{ binLocation: receivingBin }, { binLocation: null }]);

    expect(hasSplitItemInDifferentBinThanReplacedRow(replacedRow, entities, receivingBin))
      .toBe(true);
  });

  it('should be false when receiving bins are disabled, leaving nothing to cross out', () => {
    const entities = buildEntities([{ binLocation: { id: 'bin-other', name: 'Other bin' } }]);

    expect(hasSplitItemInDifferentBinThanReplacedRow(replacedRow, entities, null)).toBe(false);
  });
});
