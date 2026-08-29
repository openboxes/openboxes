import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const DetailRow = ({ labelId, defaultLabel, value }) => {
  if (value === null || value === undefined || value === '') return null;
  return (
    <tr>
      <td className="stock-movement-show-details-label">
        <Translate id={labelId} defaultMessage={defaultLabel} />
      </td>
      <td className="stock-movement-show-details-value">{value}</td>
    </tr>
  );
};

DetailRow.propTypes = {
  labelId: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  value: PropTypes.node,
};

DetailRow.defaultProps = {
  value: null,
};

const StockMovementOutboundShowDetails = ({ stockMovement }) => (
  <div className="stock-movement-show-details">
    <h6 className="mb-3">
      <Translate
        id="react.stockMovement.details.label"
        defaultMessage="Details"
      />
    </h6>
    <table className="w-100">
      <tbody>
        <DetailRow
          labelId="react.stockMovement.identifier.label"
          defaultLabel="Identifier"
          value={stockMovement.identifier}
        />
        <DetailRow
          labelId="react.stockMovement.status.label"
          defaultLabel="Status"
          value={stockMovement.displayStatus?.label}
        />
        {stockMovement.fulfillmentStatus && (
          <DetailRow
            labelId="react.stockMovement.fulfillmentStatus.label"
            defaultLabel="Fulfillment Status"
            value={stockMovement.fulfillmentStatus.label || stockMovement.fulfillmentStatus.name}
          />
        )}
        <DetailRow
          labelId="react.stockMovement.origin.label"
          defaultLabel="Origin"
          value={stockMovement.origin?.name}
        />
        <DetailRow
          labelId="react.stockMovement.destination.label"
          defaultLabel="Destination"
          value={stockMovement.destination?.name}
        />
        <DetailRow
          labelId="react.stockMovement.stocklist.label"
          defaultLabel="Stocklist"
          value={stockMovement.stocklist?.name}
        />
        <DetailRow
          labelId="react.stockMovement.shipmentType.label"
          defaultLabel="Shipment Type"
          value={stockMovement.shipmentType?.displayName}
        />
        <DetailRow
          labelId="react.stockMovement.trackingNumber.label"
          defaultLabel="Tracking Number"
          value={stockMovement.trackingNumber}
        />
        <DetailRow
          labelId="react.stockMovement.driverName.label"
          defaultLabel="Driver Name"
          value={stockMovement.driverName}
        />
        <DetailRow
          labelId="react.stockMovement.comments.label"
          defaultLabel="Comments"
          value={stockMovement.comments}
        />
        <DetailRow
          labelId="react.stockMovement.requestedBy.label"
          defaultLabel="Requested By"
          value={stockMovement.requestedBy?.name}
        />
        <DetailRow
          labelId="react.stockMovement.dateRequested.label"
          defaultLabel="Date Requested"
          value={stockMovement.dateRequested}
        />
        <DetailRow
          labelId="react.stockMovement.column.dateCreated.label"
          defaultLabel="Date Created"
          value={stockMovement.dateCreated}
        />
        <DetailRow
          labelId="react.stockMovement.dateShipped.label"
          defaultLabel="Date Shipped"
          value={stockMovement.dateShipped}
        />
        <DetailRow
          labelId="react.stockMovement.expectedDeliveryDate.label"
          defaultLabel="Expected Delivery Date"
          value={stockMovement.expectedDeliveryDate}
        />
        <DetailRow
          labelId="react.stockMovement.column.priority.label"
          defaultLabel="Priority"
          value={stockMovement.priority}
        />
      </tbody>
    </table>
  </div>
);

StockMovementOutboundShowDetails.propTypes = {
  stockMovement: PropTypes.shape({
    identifier: PropTypes.string,
    displayStatus: PropTypes.shape({
      label: PropTypes.string,
    }),
    fulfillmentStatus: PropTypes.shape({
      name: PropTypes.string,
      label: PropTypes.string,
      variant: PropTypes.string,
    }),
    origin: PropTypes.shape({ name: PropTypes.string }),
    destination: PropTypes.shape({ name: PropTypes.string }),
    stocklist: PropTypes.shape({ name: PropTypes.string }),
    shipmentType: PropTypes.shape({ displayName: PropTypes.string }),
    trackingNumber: PropTypes.string,
    driverName: PropTypes.string,
    comments: PropTypes.string,
    requestedBy: PropTypes.shape({ name: PropTypes.string }),
    dateRequested: PropTypes.string,
    dateCreated: PropTypes.string,
    dateShipped: PropTypes.string,
    expectedDeliveryDate: PropTypes.string,
    priority: PropTypes.string,
  }).isRequired,
};

export default StockMovementOutboundShowDetails;
