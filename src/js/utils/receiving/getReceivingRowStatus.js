// Status of a receiving line, shared by the receiving and check step tables:
// lines flagged for cancellation show "N cancelled", completed lines "Complete",
// over-received lines "N over", fully allocated lines "Equal" and the rest "N remaining".
const getReceivingRowStatus = ({
  quantityRemaining,
  isCompleted,
  isRemainingCanceled,
  translate,
  formatNumber,
}) => {
  if (isRemainingCanceled) {
    const quantityCanceled = formatNumber(quantityRemaining);
    return {
      className: 'status-cell status-cell--canceled',
      value: translate('react.receiving.status.canceled.label', `${quantityCanceled} cancelled`, [quantityCanceled]),
    };
  }
  if (isCompleted) {
    return {
      className: 'status-cell status-cell--completed',
      value: translate('react.receiving.status.completed.label', 'Complete'),
    };
  }
  if (quantityRemaining < 0) {
    const quantityOver = formatNumber(Math.abs(quantityRemaining));
    return {
      className: 'status-cell status-cell--over',
      value: translate('react.receiving.status.over.label', `${quantityOver} over`, [quantityOver]),
    };
  }
  if (quantityRemaining === 0) {
    return {
      className: 'status-cell status-cell--equal',
      value: translate('react.receiving.status.equal.label', 'Equal'),
    };
  }
  const quantityRemainingFormatted = formatNumber(quantityRemaining);
  return {
    className: 'status-cell',
    value: translate('react.receiving.status.remaining.label', `${quantityRemainingFormatted} remaining`, [quantityRemainingFormatted]),
  };
};

export default getReceivingRowStatus;
