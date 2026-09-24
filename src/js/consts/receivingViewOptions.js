export const ReceivingView = {
  TABLE: 'table',
  PACKING_LIST: 'packingList',
};

const receivingViewOptions = [
  {
    value: ReceivingView.TABLE,
    label: 'react.receiving.tableView.label',
    defaultLabel: 'Table View',
    tooltipLabel: 'react.receiving.tableView.tooltip.label',
    defaultTooltipLabel: 'Shows the shipment by item with no grouping',
  },
  {
    value: ReceivingView.PACKING_LIST,
    label: 'react.receiving.packingListView.label',
    defaultLabel: 'Packing List View',
    tooltipLabel: 'react.receiving.packingListView.tooltip.label',
    defaultTooltipLabel: 'Shows the shipment grouped by pack',
    disabledTooltipLabel: 'react.receiving.packingListView.disabled.label',
    defaultDisabledTooltipLabel: 'No packing information entered for this shipment.',
  },
];

export default receivingViewOptions;
