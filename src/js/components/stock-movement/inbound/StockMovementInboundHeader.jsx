import React from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockMovementInboundHeader = ({ history, showMyStockMovements }) => (
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
      <Button
        defaultLabel="Create Shipment from PO"
        label="react.stockMovement.createShipmentFromPO.label"
        onClick={() => history.push('/openboxes/stockMovement/createCombinedShipments?direction=INBOUND')}
      />
      <Button
        defaultLabel="Create Stock Movement"
        label="react.stockMovement.createStockMovement.label"
        onClick={() => history.push('/openboxes/stockMovement/createInbound')}
      />
    </div>
  </div>
);

StockMovementInboundHeader.propTypes = {
  showMyStockMovements: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};

export default withRouter(StockMovementInboundHeader);
