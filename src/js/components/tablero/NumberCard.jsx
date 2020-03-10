import React from 'react';
import PropTypes from 'prop-types';
import './tablero.scss';

function truncateString(value, length) {
  return (value.length > length) ? `${value.substr(0, length - 1)}...` : value;
}

const NumberCard = ({ cardTitle, cardNumber, cardSubtitle, cardLink }) => {
  const card = (
    <div className="numberCard">
      <span className="titleCard"> {cardTitle} </span>
      <span className="resultCard"> {cardNumber.toLocaleString()} </span>
      <span className="subtitleCard"> {truncateString(cardSubtitle, 22)} </span>
    </div>
  );
  const linkedCard = (
    <a href={cardLink} className="numberCard" target="_blank">
      <span className="titleCard"> {cardTitle} </span>
      <span className="resultCard"> {cardNumber.toLocaleString()} </span>
      <span className="subtitleCard"> {truncateString(cardSubtitle, 22)} </span>
    </a>
  );

  return (
    cardLink ? linkedCard : card
  );
};

export default NumberCard;
NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardNumber: PropTypes.number.isRequired,
  cardSubtitle: PropTypes.string,
  cardLink: PropTypes.string
};

NumberCard.defaultProps = {
  cardSubtitle: '',
};
