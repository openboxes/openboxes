import React from 'react';

import useTranslate from 'hooks/useTranslate';

const groupBinLocationsByZone = (binLocations) => {
  const translate = useTranslate();
  const groupedByZone = binLocations.reduce((acc, bin) => {
    const zoneKey = bin.zoneId || 'no-zone';
    const zoneName = bin.zoneName || translate('react.cycleCount.noZone', 'No Zone');

    return {
      ...acc,
      [zoneKey]: {
        zoneId: bin.zoneId,
        zoneName,
        bins: [...(acc[zoneKey]?.bins || []), {
          id: bin.id,
          name: bin.name,
          label: bin.name,
          value: bin.id,
        }],
      },
    };
  }, {});

  return (
    Object.values(groupedByZone)
      .sort((a, b) => {
        if (!a.zoneId) return 1;
        if (!b.zoneId) return -1;
        return a.zoneName.localeCompare(b.zoneName);
      })
      .map((group) => ({
        id: `zone-${group.zoneId || 'no-zone'}`,
        name: group.zoneName,
        label: <span className="zone-label">{group.zoneName}</span>,
        isDisabled: true,
        options: group.bins.sort((a, b) => a.name.localeCompare(b.name)),
      }))
  );
};

export default groupBinLocationsByZone;
