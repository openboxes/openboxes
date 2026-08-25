import React from 'react';

import * as locales from 'date-fns/locale';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale, getHasBinLocationSupport, getIsShipmentFromPurchaseOrder,
} from 'selectors';

import { DateFormatDateFns } from 'consts/timeFormat';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';
import getShippedQuantityInPoUom from 'utils/receiving/getShippedQuantityInPoUom';

/**
 * Badge and fields of the edited line item for the ItemDetails box.
 */
const useShipmentItemDetails = (lineItem) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();
  const currentLocale = useSelector(getCurrentLocale);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  const isShipmentFromPurchaseOrder = useSelector(getIsShipmentFromPurchaseOrder);

  // Only a purchase order has a unit of measure to convert the quantity into.
  const quantityInPoUom = isShipmentFromPurchaseOrder
    ? getShippedQuantityInPoUom({ item: lineItem, formatNumber })
    : null;
  const quantityShipped = formatNumber(lineItem?.quantityShipped);
  const shippedValue = quantityInPoUom ? (
    <>
      {quantityShipped}
      <span className="item-details__value-secondary">
        {` (${quantityInPoUom.quantity} `}
        <span className="item-details__label">{quantityInPoUom.unitOfMeasure}</span>
        )
      </span>
    </>
  ) : quantityShipped;

  const badge = {
    current: {
      label: translate('react.receiving.status.shipped.label', 'Shipped'),
      variant: 'badge--grey text-uppercase rounded',
    },
    clickable: false,
  };

  // The default grid fits six fields in two lines of three, but with both of the optional
  // fields there is a seventh, so the second line has to fit four of them instead
  const isFourColumnGrid = isShipmentFromPurchaseOrder && hasBinLocationSupport;

  const fields = [
    {
      label: translate('react.receiving.product.label', 'Product'),
      value: lineItem?.product?.name,
      className: isFourColumnGrid ? 'item-details__field--span-2' : '',
    },
    ...(isShipmentFromPurchaseOrder ? [{
      label: translate('react.receiving.supplierItemCode.label', 'Supplier Item Code'),
      value: lineItem?.supplierCode,
    }] : []),
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
      className: !isShipmentFromPurchaseOrder && !hasBinLocationSupport ? 'item-details__field--span-2' : '',
    },
    ...(hasBinLocationSupport ? [
      {
        label: translate('react.receiving.location.label', 'Location'),
        value: lineItem?.binLocation?.name,
      },
    ] : []),
    {
      label: translate('react.receiving.shipped.label', 'Shipped'),
      value: shippedValue,
    },
  ];

  return {
    badge,
    fields,
    className: isFourColumnGrid ? 'item-details--four-columns' : '',
  };
};

export default useShipmentItemDetails;
