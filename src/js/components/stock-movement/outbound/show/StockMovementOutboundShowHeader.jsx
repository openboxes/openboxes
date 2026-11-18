import React from 'react';

import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

import ShipmentIdentifier from 'components/stock-movement/common/ShipmentIdentifier';
import { OUTBOUND_MOVEMENT_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { mapShipmentTypes } from 'utils/option-utils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate from 'utils/Translate';

const StockMovementOutboundShowHeader = ({ stockMovement }) => {
  const { displayStatus } = stockMovement;

  return (
    <div className="d-flex list-page-header align-items-center">
      <span className="d-flex align-items-center title">
        <ShipmentIdentifier
          shipmentType={mapShipmentTypes(stockMovement.shipmentType)}
          identifier={stockMovement.identifier}
        />
        {stockMovement.description && (
          <span className="ml-2 font-weight-normal">{stockMovement.description}</span>
        )}
      </span>
      {displayStatus && (
        <span className="ml-3">
          <StatusIndicator
            variant={displayStatus.variant}
            status={displayStatus.label}
          />
        </span>
      )}
      <div className="d-flex justify-content-end buttons align-items-center ml-auto">
        <Link
          className="btn btn-outline-primary mr-2"
          to={OUTBOUND_MOVEMENT_URL.list()}
        >
          <Translate
            id="react.stockMovement.backToList.label"
            defaultMessage="Back to List"
          />
        </Link>
        <Link
          className="primary-button"
          to={STOCK_MOVEMENT_URL.editOutbound(stockMovement.id)}
        >
          <Translate
            id="react.default.button.edit.label"
            defaultMessage="Edit"
          />
        </Link>
      </div>
    </div>
  );
};

StockMovementOutboundShowHeader.propTypes = {
  stockMovement: PropTypes.shape({
    id: PropTypes.string.isRequired,
    identifier: PropTypes.string.isRequired,
    description: PropTypes.string,
    shipmentType: PropTypes.shape({}),
    displayStatus: PropTypes.shape({
      variant: PropTypes.string,
      label: PropTypes.string,
    }),
  }).isRequired,
};

export default StockMovementOutboundShowHeader;
