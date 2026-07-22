import { useSelector } from 'react-redux';
import { getCurrentLocale, getReceivingShipmentDetails } from 'selectors';

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
  const {
    origin,
    destination,
    dateShipped,
    shipmentStatus,
  } = useSelector(getReceivingShipmentDetails);

  const statusBadge = SHIPMENT_STATUS_BADGES[shipmentStatus];
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
    },
    {
      label: translate('react.receiving.destination.label', 'Destination'),
      value: destination,
    },
    {
      label: translate('react.receiving.shippedOn.label', 'Shipped on'),
      value: formatDateToString({
        date: dateShipped,
        dateFormat: DateFormatDateFns.DD_MMM_YYYY,
        options: { locale: getDateFnsLocale(currentLocale) },
      }),
    },
  ];

  return { badge, fields };
};

export default useConfirmReceiptDetails;
