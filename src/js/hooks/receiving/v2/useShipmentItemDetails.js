import * as locales from 'date-fns/locale';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Badge and fields of the edited line item for the ItemDetails box.
 */
const useShipmentItemDetails = (lineItem) => {
  const translate = useTranslate();
  const currentLocale = useSelector(getCurrentLocale);

  const badge = {
    current: {
      label: translate('react.receiving.status.shipped.label', 'Shipped'),
      variant: 'badge--grey text-uppercase rounded',
    },
  };

  const fields = [
    {
      label: translate('react.receiving.product.label', 'Product'),
      value: lineItem?.product?.name,
    },
    {
      label: translate('react.receiving.lotSerialNo.short.label', 'Lot/SN'),
      value: lineItem?.lotNumber,
    },
    {
      label: translate('react.receiving.expiration.label', 'Expiration'),
      value: formatDateToString({
        date: lineItem?.expirationDate,
        dateFormat: DateFormatDateFns.DD_MMM_YYYY,
        options: { locale: locales[currentLocale] },
      }),
    },
    {
      label: translate('react.receiving.recipient.label', 'Recipient'),
      value: lineItem?.recipient?.name,
    },
    {
      label: translate('react.receiving.location.label', 'Location'),
      value: lineItem?.binLocation?.name,
    },
    {
      label: translate('react.receiving.shipped.label', 'Shipped'),
      value: lineItem?.quantityShipped,
    },
  ];

  return { badge, fields };
};

export default useShipmentItemDetails;
