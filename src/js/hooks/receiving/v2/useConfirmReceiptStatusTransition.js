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
// subtracted from quantityRemaining). A line selected to cancel its remaining quantity
// counts as covered too
const willBeFullyReceived = (lineItemsState, canceledReceiptItemIds) => {
  const shipmentLevelRows = denormalizeData(lineItemsState)
    .filter((row) => row
      && (row.rowType === null || row.rowType === ReceivingRowType.REPLACED));
  return shipmentLevelRows.length > 0
    && shipmentLevelRows.every((row) => row.isCompleted
      || row.quantityRemaining <= 0
      || canceledReceiptItemIds.has(row.originalReceiptItemId));
};

const hasReceivingNowQuantities = (lineItemsState) => denormalizeData(lineItemsState)
  .some((row) => (row?.quantityReceiving ?? 0) > 0);

// Status the shipment transitions into on complete receipt: a shipment becomes fully
// received once everything is covered (always the case when the location does not
// support partial receiving), a shipped one becomes partially received otherwise.
// With nothing being received the status does not change at all.
const getNextStatus = ({
  shipmentStatus,
  partialReceivingEnabled,
  shipmentItemsState,
  canceledReceiptItemIds,
}) => {
  if (shipmentStatus === ShipmentStatusCode.PARTIALLY_RECEIVED) {
    return willBeFullyReceived(shipmentItemsState, canceledReceiptItemIds)
      ? ShipmentStatusCode.RECEIVED
      : null;
  }
  if (shipmentStatus !== ShipmentStatusCode.SHIPPED
    || !hasReceivingNowQuantities(shipmentItemsState)) {
    return null;
  }
  return partialReceivingEnabled
    && !willBeFullyReceived(shipmentItemsState, canceledReceiptItemIds)
    ? ShipmentStatusCode.PARTIALLY_RECEIVED
    : ShipmentStatusCode.RECEIVED;
};

/**
 * Badge of the status the shipment transitions into on complete receipt,
 * rendered after the arrow in the check step details box.
 */
const useConfirmReceiptStatusTransition = ({
  shipmentItemsState,
  canceledReceiptItemIds,
} = {}) => {
  const translate = useTranslate();
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const { shipmentStatus } = useSelector(getReceivingShipmentDetails);

  const nextStatus = getNextStatus({
    shipmentStatus,
    partialReceivingEnabled:
      Boolean(supportedActivities?.includes(ActivityCode.PARTIAL_RECEIVING)),
    shipmentItemsState,
    canceledReceiptItemIds,
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
