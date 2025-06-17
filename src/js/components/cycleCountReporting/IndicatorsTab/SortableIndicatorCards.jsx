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
        cardTitle={card.title}
        cardTitleDefaultValue={card.titleDefaultValue}
        cardNumberType={card.numberType}
        cardNumber={card.number}
        cardType={card.type}
        cardSubtitle={card.subtitle}
        cardSubtitleDefaultValue={card.subtitleDefaultValue}
        cardSubtitleValue={card.subtitleValue}
        cardInfo={card.info}
        cardInfoDefaultValue={card.infoDefaultValue}
        showPercentSign={card.showPercentSign}
        infoIcon={<RiInformationLine size={20} />}
        disabled
        hideDraghandle
      />
    ))}
  </div>
));

export default SortableIndicatorCards;
