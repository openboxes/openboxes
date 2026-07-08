const ReceivingRowType = {
  // A plain editable line of a shipment item that was not split.
  DEFAULT: 'default',
  // The struck-through row showing the original shipment values of a split shipment item.
  ORIGINAL: 'original',
  // The "N changes applied" row expanding/collapsing the split items of an original row.
  TOGGLE: 'toggle',
  // A single receipt item of an item with changes, rendered as a subRow of its toggle row.
  SPLIT_ITEM: 'splitItem',
};

export default ReceivingRowType;
