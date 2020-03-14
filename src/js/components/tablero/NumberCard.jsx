import React from 'react';
import PropTypes from 'prop-types';
import './tablero.scss';

function truncateString(value, length) {
  return (value.length > length) ? `${value.substr(0, length - 1)}...` : value;
}

const NumberCard = ({
  cardTitle, cardNumber, cardSubtitle, cardLink,
}) => {
  const card = (
    <div className="numberDiv">
      <span className="titleCard"> {cardTitle} </span>
      <span className="resultCard"> {cardNumber.toLocaleString()} </span>
      <span className="subtitleCard"> {truncateString(cardSubtitle, 22)} </span>
    </div>
  );

  return (
    cardLink ? <a target="_blank" rel="noopener noreferrer" href={cardLink} className="numberCard">{card}</a> : <div className="numberCard">{card}</div>
  );
};

export default NumberCard;
NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardNumber: PropTypes.number.isRequired,
  cardSubtitle: PropTypes.string,
  cardLink: PropTypes.string.isRequired,
};

NumberCard.defaultProps = {
  cardSubtitle: '',
};
