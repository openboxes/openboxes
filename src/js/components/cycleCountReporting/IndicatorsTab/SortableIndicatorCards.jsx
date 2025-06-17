import React from 'react';

import { RiInformationLine } from 'react-icons/ri';
import { SortableContainer } from 'react-sortable-hoc';

import NumberCard from 'components/dashboard/NumberCard';

const SortableIndicatorCards = SortableContainer(({ data }) => (
  <div className="card-component">
    {data.map((card, index) => (
      <NumberCard
        key={card.type}
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
      />
    ))}
  </div>
));

export default SortableIndicatorCards;
