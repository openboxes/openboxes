/* eslint-disable react/jsx-closing-tag-location */

import React from 'react';
import ReactLoading from 'react-loading';

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}

function getColor() {
  const colors = [
    '#6fb98f',
    '#004445',
    '#2e5685',
    '#fcc169',
    '#cf455c',
    '#ff0000',
    '#e89da2',
    '#e0b623',
    '#444444',
  ];
  return colors[getRandomInt(0, colors.length)];
}

const LoadingNumbers = () => {
  const loadingNumbers = [];
  for (let i = 0; i < 6; i += 1) {
    loadingNumbers.push(<div className="numberCard" key={i}>
      <div className="loaderDiv">
        <ReactLoading
          type="bubbles"
          color={getColor()}
          height="100px"
          width="100px"
        />
      </div>
    </div>);
  }

  return (
    <div className="cardComponent">
      {loadingNumbers}
    </div>
  );
};


export default LoadingNumbers;
