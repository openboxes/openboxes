import ReceivingRowType from 'consts/receivingRowType';

// Crosses out a replaced row value the lines below changed, blank ones too.
const struckIfChanged = (rowType, isChanged) => (
  rowType === ReceivingRowType.REPLACED && isChanged ? 'receiving-table__struck' : ''
);

export default struckIfChanged;
