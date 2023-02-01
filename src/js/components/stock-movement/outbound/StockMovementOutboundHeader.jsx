import React from 'react';

import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockMovementOutboundHeader = ({ showMyStockMovements, isRequestsOpen }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      {
        isRequestsOpen
          ? (<Translate
            id="react.stockMovement.request.list.label"
            defaultMessage="Request List"
          />)
          : (<Translate
            id="react.stockMovement.outbound.list.label"
            defaultMessage="Outbound Movement List"
          />)
      }
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
        to="/openboxes/stockMovement/createOutbound"
      >
        <Translate
          id="react.stockMovement.createStockMovement.label"
          defaultMessage="Create Stock Movement"
        />
      </Link>
    </div>
  </div>
);

StockMovementOutboundHeader.propTypes = {
  showMyStockMovements: PropTypes.func.isRequired,
  isRequestsOpen: PropTypes.bool.isRequired,
};

export default StockMovementOutboundHeader;
