import React from 'react';

import _ from 'lodash';

/**
 * Stringify a bin location for display, prepending the bin's zone if it has one and the zone
 * isn't already prepended.
 */
const getBinLocationToDisplay = (bin) => (
  (bin?.zoneName == null || bin?.name.startsWith(`${bin?.zoneName}: `))
    ? bin?.name : `${bin?.zoneName}: ${bin?.name}`
);

const groupBinLocationsByZone = (binLocations, translate) => {
  const groupedByZone = _.groupBy(binLocations, (bin) => bin.zoneId || 'no-zone');
  return Object.entries(groupedByZone)
    .map(([zoneKey, bins]) => {
      const zoneName = bins[0].zoneName || translate('react.cycleCount.noZone', 'No Zone');

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
      if (a.id === 'no-zone') {
        return 1;
      }
      if (b.id === 'no-zone') {
        return -1;
      }
      return a.name.localeCompare(b.name);
    });
};

export {
  getBinLocationToDisplay,
  groupBinLocationsByZone,
};
