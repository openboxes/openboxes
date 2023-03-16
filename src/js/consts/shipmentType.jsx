import React from 'react';

import { RiFlightTakeoffLine, RiInformationLine, RiShipLine, RiSuitcaseLine, RiTruckLine } from 'react-icons/all';

const TRANSLATION_PREFIX = 'react.stockMovement.shipmentType';

const ShipmentType = {
  AIR: {
    icon: <RiFlightTakeoffLine />,
    messageId: `${TRANSLATION_PREFIX}.air.label`,
    defaultMessage: 'Air',
  },
  LAND: {
    icon: <RiTruckLine />,
    messageId: `${TRANSLATION_PREFIX}.land.label`,
    defaultMessage: 'Land',
  },
  SUITCASE: {
    icon: <RiSuitcaseLine />,
    messageId: `${TRANSLATION_PREFIX}.suitcase.label`,
    defaultMessage: 'Suitcase',
  },
  SEA: {
    icon: <RiShipLine />,
    messageId: `${TRANSLATION_PREFIX}.sea.label`,
    defaultMessage: 'Sea',
  },
  DEFAULT: {
    icon: <RiInformationLine />,
    messageId: `${TRANSLATION_PREFIX}.default.label`,
    defaultMessage: 'Default',
  },
};

export default ShipmentType;

