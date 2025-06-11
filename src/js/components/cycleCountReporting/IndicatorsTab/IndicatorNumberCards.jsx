import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/ri';
import { SortableContainer } from 'react-sortable-hoc';

import LoadingNumbers from 'components/dashboard/LoadingNumbers';
import NumberCard from 'components/dashboard/NumberCard';

const SortableIndicatorCards = SortableContainer(({ data }) => (
  <div className="card-component">
    {data.map((card, index) => (
      <NumberCard
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

const IndicatorNumberCards = ({
  loading,
  numberCards,
}) => (
  <div className="cards-container">
    {loading ? <LoadingNumbers /> : <SortableIndicatorCards data={numberCards} /> }
  </div>
);

IndicatorNumberCards.propTypes = {
  loading: PropTypes.bool.isRequired,
  numberCards: PropTypes.arrayOf(PropTypes.shape({
    title: PropTypes.string,
    titleDefaultValue: PropTypes.string,
    numberType: PropTypes.string,
    number: PropTypes.number,
    type: PropTypes.string,
    subtitle: PropTypes.string,
    subtitleDefaultValue: PropTypes.string,
    subtitleValue: PropTypes.string,
    info: PropTypes.string,
    infoDefaultValue: PropTypes.string,
    showPercentSign: PropTypes.bool,
  })),
};

IndicatorNumberCards.defaultProps = {
  numberCards: [],
};

export default IndicatorNumberCards;
