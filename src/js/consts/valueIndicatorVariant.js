const valueIndicatorVariant = {
  EQUAL: 'EQUAL',
  EMPTY: 'EMPTY',
  POSITIVE: 'POSITIVE',
  NEGATIVE: 'NEGATIVE',
  MORE: 'MORE',
  LESS: 'LESS',
  TRANSACTION: 'TRANSACTION',
};

export const getCycleCountDifferencesVariant = (number, value) => {
  if (Number.isNaN(number) || value === null) {
    return null;
  }

  if (number > 0) {
    return valueIndicatorVariant.POSITIVE;
  }

  if (number < 0) {
    return valueIndicatorVariant.NEGATIVE;
  }

  return valueIndicatorVariant.EQUAL;
};

export default valueIndicatorVariant;
