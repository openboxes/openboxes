import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getHasPartialReceivingSupport,
  getReceivingShipmentDetails,
} from 'selectors';

import SHIPMENT_STATUS_BADGES from 'consts/shipmentStatusBadges';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString, getDateFnsLocale } from 'utils/dateUtils';

/**
 * Badge and fields of the received shipment for the ItemDetails box on the check step.
 */
const useConfirmReceiptDetails = () => {
  const translate = useTranslate();
  const currentLocale = useSelector(getCurrentLocale);
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const {
    origin,
    destination,
    dateShipped,
    shipmentStatus,
  } = useSelector(getReceivingShipmentDetails);

  // A location without partial receiving support takes the shipment from shipped straight to
  // received, so there is no status progression worth badging.
  const statusBadge = hasPartialReceivingSupport ? SHIPMENT_STATUS_BADGES[shipmentStatus] : null;
  const badge = statusBadge && {
    current: {
      label: translate(statusBadge.label, statusBadge.defaultLabel),
      variant: statusBadge.variant,
    },
  };

  const fields = [
    {
      label: translate('react.receiving.origin.label', 'Origin'),
      value: origin,
      testId: 'confirm-receipt-origin',
    },
    {
      label: translate('react.receiving.shippedOn.label', 'Shipped on'),
      value: formatDateToString({
        date: dateShipped,
        dateFormat: DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS,
        options: { locale: getDateFnsLocale(currentLocale) },
      }),
      testId: 'confirm-receipt-shipped-on',
    },
    {
      label: translate('react.receiving.destination.label', 'Destination'),
      value: destination,
      testId: 'confirm-receipt-destination',
    },
  ];

  return { badge, fields };
};

export default useConfirmReceiptDetails;
