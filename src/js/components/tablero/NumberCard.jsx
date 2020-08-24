import PropTypes from 'prop-types';
import React from 'react';
import { SortableElement } from 'react-sortable-hoc';
import { Line } from 'react-chartjs-2';
import { Tooltip } from 'react-tippy';
import DragHandle from './DragHandle';
import { getColorByName } from '../../consts/dataFormat/colorMapping';
import './tablero.scss';

/* global _ */

const options = {
  responsive: true,
  maintainAspectRatio: true,
  legend: {
    display: false,
  },
  elements: {
    line: {
      borderColor: '#000000',
      borderWidth: 1,
    },
    point: {
      radius: 0,
    },
  },
  tooltips: {
    enabled: false,
  },
  scales: {
    yAxes: [
      {
        display: false,
      },
    ],
    xAxes: [
      {
        display: false,
      },
    ],
  },
  plugins: {
    datalabels: {
      display: false,
    },
  },
};

const NumberSparklineCard = ({
  cardTitle, color, value, goalDifference, sparklineData,
}) => (
  <div className="number-div">
    <div className="number-body">
      <span className="title-card"> {cardTitle} </span>
      <div className="result-part">
        <span style={{ color: getColorByName(color, 'default') }}> {value}  </span>
        <span className="goal-difference"> {goalDifference} </span>
      </div>

      <Line
        data={sparklineData}
        options={options}
        height={25}
      />
    </div>
    <DragHandle />
  </div>
);

const NumberCard = SortableElement(({
  cardTitle,
  cardNumber,
  cardSubtitle,
  cardLink,
  cardDataTooltip,
  sparklineData = null,
}) => {
  let isSparkline = false;
  if (sparklineData != null) {
    if (sparklineData.colorNumber != null) {
      isSparkline = true;
    }
  }
  const card = !isSparkline ? (
    <Tooltip
      html={<p style={{ whiteSpace: 'pre' }}> {cardDataTooltip} </p>}
      theme="transparent"
      arrow="true"
      disabled={!cardDataTooltip}
    >
      <div className="number-div">
        <div className="number-body">
          <span className="title-card"> {cardTitle} </span>
          <span className="result-card"> {cardNumber.toLocaleString()} </span>
          <span className="subtitle-card"> {_.truncate(cardSubtitle, { length: 22 })} </span>
        </div>
        <DragHandle />
      </div>
    </Tooltip>
  ) :
    (
      <NumberSparklineCard
        cardTitle={cardTitle}
        color={sparklineData.colorNumber.color}
        value={sparklineData.colorNumber.value}
        goalDifference={sparklineData.colorNumber.value2}
        sparklineData={sparklineData}
      />
    );

  return (
    cardLink ? <a target="_blank" rel="noopener noreferrer" href={cardLink} className="number-card">{card}</a> : <div className="number-card">{card}</div>
  );
});

export default NumberCard;
NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardNumber: PropTypes.number,
  cardSubtitle: PropTypes.string,
  cardLink: PropTypes.string,
  cardDataTooltip: PropTypes.string,
};

NumberSparklineCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  color: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  goalDifference: PropTypes.string.isRequired,
  sparklineData: PropTypes.shape({}).isRequired,
};
