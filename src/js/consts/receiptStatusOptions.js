const receiptStatusOptions = (translate) => [
  {
    value: 'QUANTITY_REMAINING',
    label: translate('react.receiving.filters.receiptStatus.quantityRemaining.label', 'Quantity remaining'),
  },
  {
    value: 'OVER_RECEIVED',
    label: translate('react.receiving.filters.receiptStatus.overReceived.label', 'Over received'),
  },
  {
    value: 'NO_QUANTITY_ENTERED',
    label: translate('react.receiving.filters.receiptStatus.noQuantityEntered.label', 'No quantity entered'),
  },
  {
    value: 'COMPLETE',
    label: translate('react.receiving.filters.receiptStatus.complete.label', 'Complete / Fully Received'),
  },
];

export default receiptStatusOptions;
