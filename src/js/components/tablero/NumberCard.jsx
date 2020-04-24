import PropTypes from 'prop-types';
import React from 'react';
import { SortableElement } from 'react-sortable-hoc';
import DragHandle from './DragHandle';
import './tablero.scss';

/* global _ */

const NumberCard = SortableElement(({
  cardTitle, cardNumber, cardSubtitle, cardLink,
}) => {
  const card = (
    <div className="number-div">
      <div className="number-body">
        <span className="title-card"> {cardTitle} </span>
        <span className="result-card"> {cardNumber.toLocaleString()} </span>
        <span className="subtitle-card"> {_.truncate(cardSubtitle, { length: 22 })} </span>
      </div>
      <DragHandle />
    </div>
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
};
