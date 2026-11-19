import React from 'react';

import { BeatLoader } from 'react-spinners';

import { getRandomColor } from 'consts/dataFormat/colorMapping';

const LoadingCard = () => (
  <div className="loader-div">
    <BeatLoader color={getRandomColor()} size={30} margin={8} />
  </div>
);

export default LoadingCard;
