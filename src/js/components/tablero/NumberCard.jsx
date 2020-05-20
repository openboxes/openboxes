import PropTypes from 'prop-types';
import React from 'react';
import { SortableElement } from 'react-sortable-hoc';
import { Tooltip } from 'react-tippy';
import DragHandle from './DragHandle';
import './tablero.scss';

/* global _ */

const NumberCard = SortableElement(({
  cardTitle, cardNumber, cardSubtitle, cardLink, cardDataTooltip,
}) => {
  const card = (
    <Tooltip
      html={cardDataTooltip.map(value => <p> {value}  </p>)}
      theme="transparent"
      arrow="true"
      disabled={cardDataTooltip.length > 0 ? null : 'true'}
    >
      <div className="number-div">
        <div className="number-body">
          <span className="title-card"> {cardTitle} </span>
          <span className="result-card"> {cardNumber.toLocaleString()} </span>
          <span className="subtitle-card"> {_.truncate(cardSubtitle, { length: 22 })} </span>
        </div>
        <DragHandle />
      </div>
    </Tooltip>
  );

  return (
    cardLink ? <a target="_blank" rel="noopener noreferrer" href={cardLink} className="number-card">{card}</a> : <div className="number-card">{card}</div>
  );
});

export default NumberCard;
NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardNumber: PropTypes.number.isRequired,
  cardSubtitle: PropTypes.string.isRequired,
  cardLink: PropTypes.string.isRequired,
  cardDataTooltip: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string,
    value: PropTypes.string,
  })),
};
