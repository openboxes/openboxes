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

const LoadingCard = () => (
  <div className="loaderDiv">
    <ReactLoading
      type="bubbles"
      color={getColor()}
      height="150px"
      width="150px"
    />
  </div>
);

export default LoadingCard;
