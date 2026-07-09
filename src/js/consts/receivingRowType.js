// A plain editable line of a shipment item that was not split has no row type (null).
const ReceivingRowType = {
  // The struck-through row with the original shipment values, replaced by the split items below.
  REPLACED: 'replaced',
  // The row expanding/collapsing the split items of a replaced row.
  TOGGLE: 'toggle',
  // A single receipt item of an item with changes, rendered as a subRow of its toggle row.
  SPLIT_ITEM: 'splitItem',
};

export default ReceivingRowType;
