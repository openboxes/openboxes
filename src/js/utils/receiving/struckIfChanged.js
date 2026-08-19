import ReceivingRowType from 'consts/receivingRowType';

/**
 * Cross out a cell on the replaced row when its change flag (set by buildReplacedEntity)
 * is true. `changeType` is one of 'productChanged' / 'lotChanged' / 'expirationChanged' /
 * 'recipientChanged'.
 */
const struckIfChanged = (item, changeType) => (
  item?.rowType === ReceivingRowType.REPLACED && item?.[changeType]
    ? 'receiving-table__struck'
    : ''
);

export default struckIfChanged;
