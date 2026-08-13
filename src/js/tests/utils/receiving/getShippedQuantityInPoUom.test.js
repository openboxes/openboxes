import getShippedQuantityInPoUom from 'utils/receiving/getShippedQuantityInPoUom';

// Mirrors the locale-aware formatter of the tables without pulling redux into the test.
const formatNumber = (value) => new Intl.NumberFormat('en').format(value);

describe('getShippedQuantityInPoUom', () => {
  it('converts the shipped quantity into the packs of the purchase order', () => {
    expect(getShippedQuantityInPoUom({
      item: { quantityShipped: 7500, packSize: 100, unitOfMeasure: 'PK/100' },
      formatNumber,
    })).toBe('75 PK/100');
  });

  it('keeps the quantity as is for a unit of measure of each', () => {
    expect(getShippedQuantityInPoUom({
      item: { quantityShipped: 75, packSize: 1, unitOfMeasure: 'EA/1' },
      formatNumber,
    })).toBe('75 EA/1');
  });

  it('formats the packs for the current locale', () => {
    expect(getShippedQuantityInPoUom({
      item: { quantityShipped: 7500, packSize: 1, unitOfMeasure: 'EA/1' },
      formatNumber,
    })).toBe('7,500 EA/1');
  });

  it('returns nothing for a line that does not come from a purchase order', () => {
    expect(getShippedQuantityInPoUom({
      item: { quantityShipped: 7500, packSize: null, unitOfMeasure: null },
      formatNumber,
    })).toBeNull();
  });

  it('returns nothing without a shipped quantity or an item', () => {
    expect(getShippedQuantityInPoUom({
      item: { packSize: 100, unitOfMeasure: 'PK/100' },
      formatNumber,
    })).toBeNull();
    expect(getShippedQuantityInPoUom({ item: undefined, formatNumber })).toBeNull();
  });
});
