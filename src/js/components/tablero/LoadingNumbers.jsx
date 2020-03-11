import React from 'react';
import ReactLoading from 'react-loading';
import { getColor } from '../../consts/dataFormat/chartColors';

const LoadingNumbers = () => {
  const loadingNumbers = [];
  for (let i = 0; i < 6; i += 1) {
    loadingNumbers.push(
      <div className="numberCard" key={i}>
        <div className="loaderDiv">
          <ReactLoading
            type="bubbles"
            color={getColor()}
            height="100px"
            width="100px"
          />
        </div>
      </div>
    );
  }

  return (
    <div className="cardComponent">
      {loadingNumbers}
    </div>
  );
};


export default LoadingNumbers;