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
  return (
    <div className="double-tile">
      <div className="double-tile__container">
        <div className="double-tile__body">
          <span className="double-tile__title">
            {translate(cardTitle, cardTitleDefaultValue || cardTitle)}
          </span>
          <div className="double-tile__values">
            <div className="double-tile__value--left">
              <span className="double-tile__value-number text-center">
                {cardFirstValue}
              </span>
              <span className="double-tile__subtitle text-center">
                {translate(cardFirstSubtitle, cardDefaultFirstSubtitle)}
              </span>
            </div>
            <div className="double-tile__value--right">
              <span className="double-tile__value-number text-center">
                {formatSecondValueAsCurrency
                  ? `${cardSecondValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${currencyCode}`
                  : cardSecondValue}
              </span>
              <span className="double-tile__subtitle text-center">
                {translate(cardSecondSubtitle, cardDefaultSecondSubtitle)}
              </span>
            </div>
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
