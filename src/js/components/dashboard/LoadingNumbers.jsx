/* eslint-disable react/jsx-closing-tag-location */
import React from 'react';

import PropTypes from 'prop-types';
import ReactLoading from 'react-loading';

import { getRandomColor } from 'consts/dataFormat/colorMapping';

const LoadingNumbers = ({ numberOfLoadingCards }) => {
  const loadingNumbers = [];
  for (let i = 0; i < numberOfLoadingCards; i += 1) {
    loadingNumbers.push(<div className="number-card" key={i}>
      <div className="loader-div">
        <ReactLoading
          type="bubbles"
          color={getRandomColor()}
          height="100px"
          width="100px"
        />
      </div>
    </div>);
  }

  return (
    <div className="card-component">
      {loadingNumbers}
    </div>
  );
};

LoadingNumbers.propTypes = {
  numberOfLoadingCards: PropTypes.number,
};

LoadingNumbers.defaultProps = {
  numberOfLoadingCards: 4,
};

export default LoadingNumbers;
