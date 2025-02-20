import React from 'react';

import _ from 'lodash';

import useTranslate from 'hooks/useTranslate';

const groupBinLocationsByZone = (binLocations) => {
  const translate = useTranslate();
  const groupedByZone = _.groupBy(binLocations, bin => bin.zoneId || 'no-zone');

  return Object.entries(groupedByZone)
    .map(([zoneKey, bins]) => {
      const zoneName = bins[0].zoneName || translate('react.cycleCount.noZone', 'No Zone');

      return {
        id: `zone-${zoneKey}`,
        name: zoneName,
        label: <span className="zone-label">{zoneName}</span>,
        isDisabled: true,
        options: bins
          .map(bin => ({
            id: bin.id,
            name: bin.name,
            label: bin.name,
            value: bin.id,
          }))
          .sort((a, b) => a.name.localeCompare(b.name)),
      };
    })
    .sort((a, b) => {
      if (a.id === 'zone-no-zone') {
      return 1;
      }
      if (b.id === 'zone-no-zone') return -1;
      return a.name.localeCompare(b.name);
    });
};

export default groupBinLocationsByZone;
