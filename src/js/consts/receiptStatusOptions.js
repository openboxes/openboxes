import ShipmentItemReceiptStatus from 'consts/shipmentItemReceiptStatus';

const receiptStatusOptions = (translate) => [
  {
    value: ShipmentItemReceiptStatus.RECEIVED_MORE_THAN_SHIPPED,
    label: translate('react.receiving.filters.receiptStatus.receivedMoreThanShipped.label', 'Received more than shipped'),
  },
  {
    value: ShipmentItemReceiptStatus.RECEIVED_LESS_THAN_SHIPPED,
    label: translate('react.receiving.filters.receiptStatus.receivedLessThanShipped.label', 'Received less than shipped'),
  },
  {
    value: ShipmentItemReceiptStatus.NO_QUANTITY_ENTERED,
    label: translate('react.receiving.filters.receiptStatus.noQuantityEntered.label', 'No quantity entered'),
  },
  {
    value: ShipmentItemReceiptStatus.COMPLETE,
    label: translate('react.receiving.filters.receiptStatus.complete.label', 'Complete'),
  },
];

export default receiptStatusOptions;
