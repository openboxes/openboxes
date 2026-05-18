import React from 'react';

import { RiInformationLine } from 'react-icons/ri';

import NumberCard from 'components/dashboard/NumberCard';
import tileType from 'consts/tileType';
import DoubleTile from 'utils/DoubleTile';

const IndicatorCards = (({ data }) => (
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
          showFirstValuePercentSign={card.showFirstValuePercentSign}
          formatSecondValueAsCurrency={card.formatSecondValueAsCurrency}
        />
      )))}
  </div>
));

export default IndicatorCards;
