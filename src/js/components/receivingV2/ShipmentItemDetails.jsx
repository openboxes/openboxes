import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';

/**
 * Read-only summary of the shipment line being edited: a status chip followed by
 * a light-blue box of product/shipment fields. Shown at the top of the edit modal.
 */
const ShipmentItemDetails = ({ details }) => {
  const translate = useTranslate();

  const fields = [
    {
      key: 'product',
      label: translate('react.receiving.product.label', 'Product'),
      value: details.product,
    },
    {
      key: 'lotNumber',
      label: translate('react.receiving.lotSerialNo.short.label', 'Lot/SN'),
      value: details.lotNumber,
    },
    {
      key: 'expiration',
      label: translate('react.receiving.expiration.label', 'Expiration'),
      value: details.expirationDate,
    },
    {
      key: 'recipient',
      label: translate('react.receiving.recipient.label', 'Recipient'),
      value: details.recipient,
    },
    {
      key: 'location',
      label: translate('react.receiving.location.label', 'Location'),
      value: details.location,
    },
    {
      key: 'shipped',
      label: translate('react.receiving.shipped.label', 'Shipped'),
      value: details.quantityShipped,
    },
  ];

  return (
    <div className="shipment-item-details">
      <div className="badge-container">
        <Badge label={details.status} variant="badge--grey" />
      </div>
      <div className="shipment-item-details__grid">
        {fields.map(({ key, label, value }) => (
          <div key={key} className="shipment-item-details__field">
            <span className="shipment-item-details__label">{`${label}:`}</span>
            <span className="shipment-item-details__value">{value}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

ShipmentItemDetails.propTypes = {
  details: PropTypes.shape({
    status: PropTypes.string,
    product: PropTypes.string,
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
    recipient: PropTypes.string,
    location: PropTypes.string,
    quantityShipped: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  }).isRequired,
};

export default ShipmentItemDetails;
