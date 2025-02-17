import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownLine, RiArrowUpLine } from 'react-icons/all';

import ArrowValueIndicatorVariant from 'consts/arrowValueIndicatorVariant';
import useTranslate from 'hooks/useTranslate';

const ArrowValueIndicator = ({ value, variant, showAbsoluteValue }) => {
  const translate = useTranslate();

  const valueToDisplay = showAbsoluteValue ? Math.abs(value) : value;

  // Variant is passed, because this component can be reused for a values
  // that are not numerical, so we cannot decide whether other types are
  // positive / negative
  if (variant === ArrowValueIndicatorVariant.POSITIVE) {
    return (
      <div>
        <RiArrowUpLine size={18} className="arrow-value-indicator--positive" />
        <span className="value-indicator value-indicator--positive">{valueToDisplay.toString()}</span>
      </div>
    );
  }

  if (variant === ArrowValueIndicatorVariant.NEGATIVE) {
    return (
      <div>
        <RiArrowDownLine size={18} className="arrow-value-indicator--negative" />
        <span className="value-indicator value-indicator--negative">{valueToDisplay.toString()}</span>
      </div>
    );
  }

  if (variant === ArrowValueIndicatorVariant.EQUAL) {
    return (
      <div className="px-2 py-1 value-indicator value-indicator--equal">
        {translate('react.cycleCount.table.equal.label', 'EQUAL')}
      </div>
    );
  }

  return (
    <div className="py-1 text-center value-indicator value-indicator--empty">
      -
    </div>
  );
};

export default ArrowValueIndicator;

ArrowValueIndicator.propTypes = {
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
  variant: PropTypes.oneOf(
    Object.keys(ArrowValueIndicatorVariant),
  ).isRequired,
  showAbsoluteValue: PropTypes.bool,
};

ArrowValueIndicator.defaultProps = {
  value: null,
  showAbsoluteValue: false,
};
