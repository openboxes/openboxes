import _ from 'lodash';

const valueIndicatorVariant = {
  // These four values are used for displaying changes in qty difference
  EQUAL: 'EQUAL',
  EMPTY: 'EMPTY',
  POSITIVE: 'POSITIVE',
  NEGATIVE: 'NEGATIVE',
  // These three values are used for alignment in cycle count reporting
  MORE: 'MORE',
  LESS: 'LESS',
  TRANSACTION: 'TRANSACTION',
};

// 'checkValue' was added to optionally check `value`,
// since it's not always passed and shouldn't always be checked
export const getCycleCountDifferencesVariant = (number, value = null, checkValue = false) => {
  if (_.isNaN(number) || _.isNil(number)) {
    return null;
  }

  if (checkValue && _.isNil(value)) {
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
