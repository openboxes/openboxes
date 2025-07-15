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

// 'shouldCheckSecondValue' was added to optionally check `secondValue`,
// since it's not always passed and shouldn't always be checked
export const getCycleCountDifferencesVariant = ({
  firstValue,
  secondValue = null,
  shouldCheckSecondValue = false,
}) => {
  if (_.isNaN(firstValue) || _.isNil(firstValue)) {
    return null;
  }

  if (shouldCheckSecondValue && _.isNil(secondValue)) {
    return null;
  }

  if (firstValue > 0) {
    return valueIndicatorVariant.POSITIVE;
  }

  if (firstValue < 0) {
    return valueIndicatorVariant.NEGATIVE;
  }

  return valueIndicatorVariant.EQUAL;
};

export default valueIndicatorVariant;
