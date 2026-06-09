import React from 'react';

import _ from 'lodash';

import { getBinLocationToDisplay } from 'utils/form-values-utils';

const groupBinLocationsByZone = (binLocations, translate) => {
  const groupedByZone = _.groupBy(binLocations, (bin) => bin.zoneId || 'no-zone');
  return Object.entries(groupedByZone)
    .map(([zoneKey, bins]) => {
      const zoneName = bins[0].zoneName || translate?.('react.cycleCount.noZone', 'No Zone') || 'No Zone';

      return {
        id: `zone-${zoneKey}`,
        name: zoneName,
        label: <span className="zone-label">{zoneName}</span>,
        isDisabled: true,
        options: bins
          .map((bin) => ({
            id: bin.id,
            name: getBinLocationToDisplay(bin),
            label: getBinLocationToDisplay(bin),
            value: bin.id,
          })),
      };
    })
    .sort((a, b) => {
      if (a.id === 'zone-no-zone') {
        return 1;
      }
      if (b.id === 'zone-no-zone') {
        return -1;
      }

      // Just in case if any name turns out to be undefined/null
      const aName = a.name ?? '';
      const bName = b.name ?? '';

      return aName.localeCompare(bName);
    });
};

export {
  getBinLocationToDisplay,
  groupBinLocationsByZone,
};
