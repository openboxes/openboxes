export const ReceivingView = {
  TABLE: 'table',
  PACKING_LIST: 'packingList',
};

const receivingViewOptions = [
  { value: ReceivingView.TABLE, label: 'react.receiving.tableView.label', defaultLabel: 'Table View' },
  { value: ReceivingView.PACKING_LIST, label: 'react.receiving.packingListView.label', defaultLabel: 'Packing List View' },
];

export default receivingViewOptions;
