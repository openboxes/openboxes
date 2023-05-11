import React from 'react';

import { RiFlightTakeoffLine, RiInformationLine, RiShipLine, RiSuitcaseLine, RiTruckLine } from 'react-icons/ri';


const ShipmentType = {
  AIR: {
    icon: <RiFlightTakeoffLine />,
  },
  LAND: {
    icon: <RiTruckLine />,
  },
  SUITCASE: {
    icon: <RiSuitcaseLine />,
  },
  SEA: {
    icon: <RiShipLine />,
  },
  DEFAULT: {
    icon: <RiInformationLine />,
  },
};

export default ShipmentType;

