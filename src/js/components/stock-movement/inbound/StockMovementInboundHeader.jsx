import React from 'react';

import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockMovementInboundHeader = ({ showMyStockMovements }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stockMovement.inbound.list.label" defaultMessage="Inbound Movement List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      <Button
        defaultLabel="My Stock Movements"
        variant="primary-outline"
        label="react.stockMovement.myStockMovement.label"
        onClick={showMyStockMovements}
      />
      <Link
        className="primary-button"
        to="/openboxes/stockMovement/createCombinedShipments?direction=INBOUND"
      >
        <Translate
          id="react.stockMovement.createShipmentFromPO.label"
          defaultMessage="Create Shipment from PO"
        />
      </Link>
      <Link
        className="primary-button"
        to="/openboxes/stockMovement/createInbound"
      >
        <Translate
          id="react.stockMovement.createStockMovement.label"
          defaultMessage="Create Stock Movement"
        />
      </Link>
    </div>
  </div>
);

StockMovementInboundHeader.propTypes = {
  showMyStockMovements: PropTypes.func.isRequired,
};

export default StockMovementInboundHeader;
