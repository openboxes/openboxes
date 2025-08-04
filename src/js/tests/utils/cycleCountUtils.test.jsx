import trimLotNumberSpaces from 'utils/cycleCountUtils';

describe('trimLotNumberSpaces', () => {
  const mockCycleCountItem = {
    id: 'ff808181985f4b620198609307eb0074',
    quantityCounted: 43,
    inventoryItem: {
      id: '09f5e1167edca0ea017efcdc20903661',
      lotNumber: 'ABC123',
      product: {
        id: 'ff80808174dd934401751d1cc01d5ff0',
        name: 'Printer Toner, Xerox',
        productCode: 'ZU857',
      },
      pricePerUnit: 150.68,
      unitOfMeasure: 'Each',
    },
  };

  it('should match snapshot for sample item', () => {
    const result = trimLotNumberSpaces(mockCycleCountItem);
    expect(result).toMatchSnapshot();
  });

  it('trims leading and trailing spaces from lotNumber', () => {
    const input = {
      ...mockCycleCountItem,
      inventoryItem: {
        ...mockCycleCountItem.inventoryItem,
        lotNumber: '   ABC123   ',
      },
    };
    const result = trimLotNumberSpaces(input);
    expect(result.inventoryItem.lotNumber).toBe('ABC123');
  });

  it('does not mutate original object', () => {
    const copy = JSON.parse(JSON.stringify(mockCycleCountItem));
    trimLotNumberSpaces(copy);
    expect(copy.inventoryItem.lotNumber).toBe('ABC123');
  });

  it('handles missing lotNumber safely', () => {
    const input = {
      ...mockCycleCountItem,
      inventoryItem: {
        ...mockCycleCountItem.inventoryItem,
        lotNumber: undefined,
      },
    };

    const result = trimLotNumberSpaces(input);
    expect(result.inventoryItem.lotNumber).toBeUndefined();
  });

  it('handles missing inventoryItem safely', () => {
    const input = {
      id: 'test',
      quantityCounted: 10,
      inventoryItem: undefined,
    };

    const result = trimLotNumberSpaces(input);
    expect(result.inventoryItem).toEqual({ lotNumber: undefined });
  });
});
