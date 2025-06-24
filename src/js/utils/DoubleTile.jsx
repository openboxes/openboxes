import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import CustomTooltip from 'wrappers/CustomTooltip';

const DoubleTile = ({
  cardTitle,
  cardTitleDefaultValue,
  cardFirstValue,
  cardSecondValue,
  cardFirstSubtitle,
  cardDefaultFirstSubtitle,
  cardSecondSubtitle,
  cardDefaultSecondSubtitle,
  cardInfo,
  cardInfoDefaultValue,
  infoIcon,
  currencyCode,
  formatSecondValueAsCurrency,
}) => {
  const translate = useTranslate();

  const formatCurrency = (value) => {
    if (value >= 1000000) {
      return `${(value / 1000000).toFixed(3)} ${translate('react.cycleCount.indicators.million.label', 'million')} ${currencyCode}`;
    }

    return `${value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })} ${currencyCode}`;
  };

  return (
    <div className="double-tile">
      <span className="double-tile__title">
        {translate(cardTitle, cardTitleDefaultValue || cardTitle)}
      </span>
      <div className="d-flex">
        <div className="double-tile__first-value-block">
          <span className="double-tile__value-number">
            {cardFirstValue}
          </span>
          <span className="double-tile__subtitle">
            {translate(cardFirstSubtitle, cardDefaultFirstSubtitle)}
          </span>
        </div>
        <div className="double-tile__second-value-block">
          <span className="double-tile__value-number">
            {formatSecondValueAsCurrency
              ? formatCurrency(cardSecondValue)
              : cardSecondValue}
          </span>
          <span className="double-tile__subtitle">
            {translate(cardSecondSubtitle, cardDefaultSecondSubtitle)}
          </span>
        </div>
      </div>
      {cardInfo && (
        <div className="double-tile__tooltip">
          <CustomTooltip
            content={translate(cardInfo, cardInfoDefaultValue)}
          >
            {infoIcon || <i className="fa fa-info-circle" />}
          </CustomTooltip>
        </div>
      )}
    </div>
  );
};

export default DoubleTile;

DoubleTile.defaultProps = {
  infoIcon: null,
  currencyCode: 'USD',
  formatSecondValueAsCurrency: false,
};

DoubleTile.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardTitleDefaultValue: PropTypes.string.isRequired,
  cardFirstValue: PropTypes.number.isRequired,
  cardSecondValue: PropTypes.number.isRequired,
  cardFirstSubtitle: PropTypes.string.isRequired,
  cardDefaultFirstSubtitle: PropTypes.string.isRequired,
  cardSecondSubtitle: PropTypes.string.isRequired,
  cardDefaultSecondSubtitle: PropTypes.string.isRequired,
  cardInfo: PropTypes.string.isRequired,
  cardInfoDefaultValue: PropTypes.string.isRequired,
  infoIcon: PropTypes.node,
  currencyCode: PropTypes.string,
  formatSecondValueAsCurrency: PropTypes.bool,
};
