import React from 'react';

import PropTypes from 'prop-types';
import { BeatLoader } from 'react-spinners';

import { getRandomColor } from 'consts/dataFormat/colorMapping';

const LoadingNumbers = ({ numberOfLoadingCards = 4 }) => (
  <div className="card-component">
    {Array.from({ length: numberOfLoadingCards }, (_, index) => (
      <div className="number-card" key={index}>
        <div className="loader-div">
          <BeatLoader color={getRandomColor()} size={20} margin={5} />
        </div>
      </div>
    ))}
  </div>
);

LoadingNumbers.propTypes = {
  numberOfLoadingCards: PropTypes.number,
};

LoadingNumbers.defaultProps = {
  numberOfLoadingCards: 4,
};

export default LoadingNumbers;
