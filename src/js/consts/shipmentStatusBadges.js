import ShipmentStatusCode from 'consts/shipmentStatusCode';

const BLUE_VARIANT = 'badge--blue-outline badge--no-ellipsis text-uppercase rounded';
const GREEN_VARIANT = 'badge--green-outline badge--no-ellipsis text-uppercase rounded';

const SHIPMENT_STATUS_BADGES = {
  [ShipmentStatusCode.SHIPPED]: {
    label: 'react.receiving.status.shipped.label',
    defaultLabel: 'Shipped',
    variant: BLUE_VARIANT,
  },
  [ShipmentStatusCode.PARTIALLY_RECEIVED]: {
    label: 'react.receiving.status.partiallyReceived.label',
    defaultLabel: 'Partially Received',
    variant: BLUE_VARIANT,
  },
  [ShipmentStatusCode.RECEIVED]: {
    label: 'react.receiving.status.fullyReceived.label',
    defaultLabel: 'Fully Received',
    variant: GREEN_VARIANT,
  },
};

export default SHIPMENT_STATUS_BADGES;
