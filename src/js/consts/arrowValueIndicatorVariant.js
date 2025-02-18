const arrowValueIndicatorVariant = {
  EQUAL: 'EQUAL',
  EMPTY: 'EMPTY',
  POSITIVE: 'POSITIVE',
  NEGATIVE: 'NEGATIVE',
};

export const getCycleCountDifferencesVariant = (number, rowId) => {
  if (rowId.includes('newRow')) {
    return arrowValueIndicatorVariant.EMPTY;
  }

  if (number > 0) {
    return arrowValueIndicatorVariant.POSITIVE;
  }

  if (number < 0) {
    return arrowValueIndicatorVariant.NEGATIVE;
  }

  return arrowValueIndicatorVariant.EQUAL;
};

export default arrowValueIndicatorVariant;
