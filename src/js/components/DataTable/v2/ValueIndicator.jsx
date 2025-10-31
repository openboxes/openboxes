import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownLine, RiArrowUpLine } from 'react-icons/all';

import valueIndicatorVariant from 'consts/valueIndicatorVariant';
import useTranslate from 'hooks/useTranslate';

const ValueIndicator = ({
  value, variant, showAbsoluteValue, className,
}) => {
  const translate = useTranslate();

  const valueToDisplay = showAbsoluteValue ? Math.abs(value) : value;

  // Variant is passed, because this component can be reused for a values
  // that are not numerical, so we cannot decide whether other types are
  // positive / negative
  if (variant === valueIndicatorVariant.POSITIVE) {
    return (
      <div className={className}>
        <RiArrowUpLine size={18} className="arrow-value-indicator--positive" />
        <span className="value-indicator value-indicator--positive">{valueToDisplay.toString()}</span>
      </div>
    );
  }

  if (variant === valueIndicatorVariant.NEGATIVE) {
    return (
      <div className={className}>
        <RiArrowDownLine size={18} className="arrow-value-indicator--negative" />
        <span className="value-indicator value-indicator--negative">{valueToDisplay.toString()}</span>
      </div>
    );
  }

  if (variant === valueIndicatorVariant.EQUAL) {
    return (
      <div className={`${className} px-2 py-1 value-indicator value-indicator--equal`}>
        {translate('react.reactTable.equal.label', 'EQUAL')}
      </div>
    );
  }

  if (variant === valueIndicatorVariant.MORE) {
    return (
      <div className={`${className} px-2 py-1 value-indicator value-indicator--more`}>
        {translate('react.reactTable.more.label', 'MORE')}
      </div>
    );
  }

  if (variant === valueIndicatorVariant.LESS) {
    return (
      <div className={`${className} px-2 py-1 value-indicator value-indicator--less`}>
        {translate('react.reactTable.less.label', 'LESS')}
      </div>
    );
  }

  if (variant === valueIndicatorVariant.EMPTY) {
    return (
      <div className={`${className} py-1 text-center value-indicator value-indicator--empty`}>
        -
      </div>
    );
  }

  if (variant === valueIndicatorVariant.TRANSACTION) {
    return (
      <div className={`${className} px-2 py-1 value-indicator value-indicator--transaction`}>
        {value}
      </div>
    );
  }

  return null;
};

export default ValueIndicator;

ValueIndicator.propTypes = {
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
  variant: PropTypes.oneOf(
    Object.keys(valueIndicatorVariant),
  ).isRequired,
  showAbsoluteValue: PropTypes.bool,
  className: PropTypes.string,
};

ValueIndicator.defaultProps = {
  value: null,
  showAbsoluteValue: false,
  className: '',
};
