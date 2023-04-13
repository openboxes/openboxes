import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/ri';

import ShipmentType from 'consts/shipmentType';

const ShipmentIdentifier = ({ shipmentType, identifier }) => {
  const getShipmentTypeIcon = () => ShipmentType[shipmentType?.enumKey ?? 'Default']?.icon ?? <RiInformationLine />;
  return (
    <div className="d-flex align-items-center gap-8 shipment-identifier">
      {getShipmentTypeIcon()}
      {identifier}
    </div>
  );
};


export default ShipmentIdentifier;


ShipmentIdentifier.propTypes = {
  shipmentType: PropTypes.shape({}).isRequired,
  identifier: PropTypes.string.isRequired,
};
