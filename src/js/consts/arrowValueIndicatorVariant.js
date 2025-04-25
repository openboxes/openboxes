const arrowValueIndicatorVariant = {
  EQUAL: 'EQUAL',
  EMPTY: 'EMPTY',
  POSITIVE: 'POSITIVE',
  NEGATIVE: 'NEGATIVE',
};

export const getCycleCountDifferencesVariant = (number, value) => {
  if (Number.isNaN(number) || value === null) {
    return null;
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
