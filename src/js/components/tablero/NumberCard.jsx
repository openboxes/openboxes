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
    <div className="numberCard">
      <span className="titleCard"> {cardTitle} </span>
      <span className="resultCard"> {cardNumber.toLocaleString()} </span>
      <span className="subtitleCard"> {truncateString(cardSubtitle, 22)} </span>
    </div>
  );

  return (
    cardLink ? <a href={cardLink}>{card}</a> : card
  );
};

export default NumberCard;
// TO DELETE when data are received from the backend
NumberCard.defaultProps = {
  cardLink: '',
};

NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardNumber: PropTypes.number.isRequired,
  cardSubtitle: PropTypes.string.isRequired,
  cardLink: PropTypes.string,
};
