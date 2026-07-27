import { useSelector } from 'react-redux';
import {
  getCurrentLocationSupportedActivities,
  getReceivingShipmentDetails,
} from 'selectors';

import ActivityCode from 'consts/activityCode';
import ReceivingRowType from 'consts/receivingRowType';
import SHIPMENT_STATUS_BADGES from 'consts/shipmentStatusBadges';
import ShipmentStatusCode from 'consts/shipmentStatusCode';
import useTranslate from 'hooks/useTranslate';
import { denormalizeData } from 'utils/normalizationUtils';

// Completing the receipt makes the shipment fully received when every shipment-level
// line has nothing left to receive (pending "receiving now" quantities are already
// subtracted from quantityRemaining).
const willBeFullyReceived = (lineItemsState) => {
  const shipmentLevelRows = denormalizeData(lineItemsState)
    .filter((row) => row
      && (row.rowType === null || row.rowType === ReceivingRowType.REPLACED));
  return shipmentLevelRows.length > 0
    && shipmentLevelRows.every((row) => row.isCompleted || row.quantityRemaining <= 0);
};

const hasReceivingNowQuantities = (lineItemsState) => denormalizeData(lineItemsState)
  .some((row) => (row?.quantityReceiving ?? 0) > 0);

// Status the shipment transitions into on complete receipt: a shipment becomes fully
// received once everything is covered (always the case when the location does not
// support partial receiving), a shipped one becomes partially received otherwise.
// With nothing being received the status does not change at all.
const getNextStatus = ({ shipmentStatus, partialReceivingEnabled, lineItemsState }) => {
  if (shipmentStatus === ShipmentStatusCode.PARTIALLY_RECEIVED) {
    return willBeFullyReceived(lineItemsState) ? ShipmentStatusCode.RECEIVED : null;
  }
  if (shipmentStatus !== ShipmentStatusCode.SHIPPED
    || !hasReceivingNowQuantities(lineItemsState)) {
    return null;
  }
  return partialReceivingEnabled && !willBeFullyReceived(lineItemsState)
    ? ShipmentStatusCode.PARTIALLY_RECEIVED
    : ShipmentStatusCode.RECEIVED;
};

/**
 * Badge of the status the shipment transitions into on complete receipt,
 * rendered after the arrow in the check step details box.
 */
const useConfirmReceiptStatusTransition = ({ lineItemsState } = {}) => {
  const translate = useTranslate();
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const { shipmentStatus } = useSelector(getReceivingShipmentDetails);

  const nextStatus = getNextStatus({
    shipmentStatus,
    partialReceivingEnabled:
      Boolean(supportedActivities?.includes(ActivityCode.PARTIAL_RECEIVING)),
    lineItemsState,
  });

  const nextBadge = SHIPMENT_STATUS_BADGES[nextStatus];
  return {
    nextBadge: nextBadge && {
      label: translate(nextBadge.label, nextBadge.defaultLabel),
      variant: nextBadge.variant,
    },
  };
};

export default useConfirmReceiptStatusTransition;
