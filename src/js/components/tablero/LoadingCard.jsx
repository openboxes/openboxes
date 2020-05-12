import React from 'react';
import ReactLoading from 'react-loading';
import { getRandomColor } from '../../consts/dataFormat/colorMapping';

const LoadingCard = () => (
  <div className="loader-div">
    <ReactLoading
      type="bubbles"
      color={getRandomColor()}
      height="150px"
      width="150px"
    />
  </div>
);

export default LoadingCard;
