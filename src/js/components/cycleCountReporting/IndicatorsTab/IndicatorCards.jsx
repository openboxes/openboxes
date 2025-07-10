import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/ri';
import { SortableContainer } from 'react-sortable-hoc';

import NumberCard from 'components/dashboard/NumberCard';
import tileType from 'consts/tileType';
import DoubleTile from 'utils/DoubleTile';

const IndicatorCards = SortableContainer(({ data }) => (
  <div className="card-component">
    {data.map((card, index) =>
      (card.type === tileType.SINGLE ? (
        <NumberCard
          key={card.name}
          index={index}
          cardTitle={card.titleLabel}
          cardTitleDefaultValue={card.defaultTitle}
          cardNumberType={card.numberType}
          cardNumber={card.value}
          cardSubtitle={card.subtitleLabel}
          cardSubtitleDefaultValue={card.defaultSubtitle}
          cardSubtitleValue={card.subValue}
          cardInfo={card.infoLabel}
          cardInfoDefaultValue={card.defaultInfo}
          showPercentSign={card.showPercentSign}
          infoIcon={<RiInformationLine size={20} />}
          disabled
          hideDraghandle
          disableSubtitleEllipsis
        />
      ) : (
        <DoubleTile
          key={card.name}
          cardTitle={card.titleLabel}
          cardTitleDefaultValue={card.defaultTitle}
          cardFirstValue={card.firstValue}
          cardSecondValue={card.secondValue}
          cardFirstSubtitle={card.firstSubtitleLabel}
          cardDefaultFirstSubtitle={card.defaultFirstSubtitle}
          cardSecondSubtitle={card.secondSubtitleLabel}
          cardDefaultSecondSubtitle={card.defaultSecondSubtitle}
          cardInfo={card.infoLabel}
          cardInfoDefaultValue={card.defaultInfo}
          formatSecondValueAsCurrency
        />
      )))}
  </div>
));

export default IndicatorCards;

IndicatorCards.propTypes = {
  tiles: PropTypes.arrayOf(PropTypes.shape({
    titleLabel: PropTypes.string,
    defaultTitle: PropTypes.string,
    numberType: PropTypes.string,
    value: PropTypes.number,
    name: PropTypes.string,
    subtitleLabel: PropTypes.string,
    defaultSubtitle: PropTypes.string,
    subValue: PropTypes.string,
    infoLabel: PropTypes.string,
    defaultInfo: PropTypes.string,
    showPercentSign: PropTypes.bool,
    type: PropTypes.string,
    firstValue: PropTypes.number,
    secondValue: PropTypes.number,
    firstSubtitleLabel: PropTypes.string,
    defaultFirstSubtitle: PropTypes.string,
    secondSubtitleLabel: PropTypes.string,
    defaultSecondSubtitle: PropTypes.string,
  })),
};

IndicatorCards.defaultProps = {
  tiles: [],
};
