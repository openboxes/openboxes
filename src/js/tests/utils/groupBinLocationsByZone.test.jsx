import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';

const mockTranslate = jest.fn((key, defaultValue) => defaultValue);

describe('getBinLocationToDisplay', () => {
  const mockBin = {
    zoneName: 'ZoneA',
    name: 'Bin123',
  };

  it('prepends zoneName to bin name when not already prepended', () => {
    const result = getBinLocationToDisplay(mockBin);
    expect(result).toBe('ZoneA: Bin123');
  });

  it('does not prepend zoneName when already included in name', () => {
    const props = {
      ...mockBin,
      name: 'ZoneA: Bin123',
    };
    const result = getBinLocationToDisplay(props);
    expect(result).toBe('ZoneA: Bin123');
  });

  it('handles missing zoneName safely', () => {
    const props = {
      zoneName: undefined,
      name: 'Bin123',
    };
    const result = getBinLocationToDisplay(props);
    expect(result).toBe('Bin123');
  });

  it('handles missing bin safely', () => {
    const result = getBinLocationToDisplay(undefined);
    expect(result).toBeUndefined();
  });
});

describe('groupBinLocationsByZone', () => {
  const mockBinLocations = [
    {
      id: '1',
      zoneId: 'zone1',
      zoneName: 'ZoneA',
      name: 'Bin1',
    },
    {
      id: '2',
      zoneId: 'zone1',
      zoneName: 'ZoneA',
      name: 'Bin2',
    },
    {
      id: '3',
      zoneId: 'zone2',
      zoneName: 'ZoneB',
      name: 'Bin3',
    },
    {
      id: '4',
      zoneId: null,
      zoneName: null,
      name: 'Bin4',
    },
  ];

  it('groups bins by zoneId and formats options correctly', () => {
    const result = groupBinLocationsByZone(mockBinLocations, mockTranslate);
    expect(result).toHaveLength(3);
    expect(result[0]).toEqual({
      id: 'zone-no-zone',
      name: 'No Zone',
      label: expect.any(Object),
      isDisabled: true,
      options: [{
        id: '4',
        name: 'Bin4',
        label: 'Bin4',
        value: '4',
      }],
    });
    expect(result[1])
      .toEqual({
        id: 'zone-zone1',
        name: 'ZoneA',
        label: expect.any(Object),
        isDisabled: true,
        options: [
          {
            id: '1',
            name: 'ZoneA: Bin1',
            label: 'ZoneA: Bin1',
            value: '1',
          },
          {
            id: '2',
            name: 'ZoneA: Bin2',
            label: 'ZoneA: Bin2',
            value: '2',
          },
        ],
      });
    expect(result[2])
      .toEqual({
        id: 'zone-zone2',
        name: 'ZoneB',
        label: expect.any(Object),
        isDisabled: true,
        options: [{
          id: '3',
          name: 'ZoneB: Bin3',
          label: 'ZoneB: Bin3',
          value: '3',
        }],
      });
  });

  it('handles empty binLocations array safely', () => {
    const result = groupBinLocationsByZone([], mockTranslate);
    expect(result).toEqual([]);
  });

  it('sorts zones alphabetically with No Zone at the start', () => {
    const result = groupBinLocationsByZone(mockBinLocations, mockTranslate);
    expect(result.map((zone) => zone.name)).toEqual(['No Zone', 'ZoneA', 'ZoneB']);
  });

  it('handles missing zoneId and zoneName safely', () => {
    const props = [{
      id: '5',
      zoneId: undefined,
      zoneName: undefined,
      name: 'Bin5',
    }];
    const result = groupBinLocationsByZone(props, mockTranslate);
    expect(result)
      .toEqual([
        {
          id: 'zone-no-zone',
          name: 'No Zone',
          label: expect.any(Object),
          isDisabled: true,
          options: [{
            id: '5',
            name: 'Bin5',
            label: 'Bin5',
            value: '5',
          }],
        },
      ]);
    expect(mockTranslate).toHaveBeenCalledWith('react.cycleCount.noZone', 'No Zone');
  });
});
