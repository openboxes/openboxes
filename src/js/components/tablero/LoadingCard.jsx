import React from 'react';
import ReactLoading from 'react-loading';
import { getColor } from '../../consts/dataFormat/chartColors';

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
