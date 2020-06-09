/* eslint-disable react/no-array-index-key */
import React from 'react';
import PropTypes from 'prop-types';

import { getColorByName } from '../../consts/dataFormat/colorMapping';


const NumbersRAG = ({ data }) => (
  <div className="big-gyr-indicator">
    {data.listColorNumber.map((value, index) =>
      (
        <a className="number-indicator" href={value.link} key={index}>
          <div className="value" style={{ color: getColorByName(value.color, 'default') }}>
            <span className="circle" style={{ background: getColorByName(value.color, 'light') }} />
            {value.value}
          </div>
          <div className="subtitle">{value.subtitle}</div>
        </a>
      ))
    }
  </div>
);

NumbersRAG.propTypes = {
  data: PropTypes.shape({
    listColorNumber: PropTypes.arrayOf(PropTypes.shape({
      link: PropTypes.string,
      color: PropTypes.string,
      subtitle: PropTypes.string,
      value: PropTypes.string,
    })),
  }).isRequired,
};

export default NumbersRAG;
