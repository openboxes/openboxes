import PropTypes from 'prop-types';
import React from 'react';
import { SortableElement } from 'react-sortable-hoc';
import { Line } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import { Tooltip } from 'react-tippy';
import DragHandle from './DragHandle';
import { getColorByName } from '../../consts/dataFormat/colorMapping';
import './tablero.scss';
import { translateWithDefaultMessage } from '../../utils/Translate';

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

const ZERO = 0;

const NumberSparklineCard = ({
  cardTitle, cardInfo, color, value, goalDifference, sparklineData, translate,
}) => (
  <div className="number-div">
    <div className="number-body">
      <span className="title-card">
        {cardTitle.code ?
              translate(cardTitle.code, cardTitle.message)
             : cardTitle}
      </span>
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
    <div className="number-infos">
      <Tooltip
        html={
          <p> {cardInfo.code ? translate(cardInfo.code, cardInfo.message) : cardInfo.message} </p>
        }
        theme="transparent"
        arrow="true"
        disabled={!cardInfo}
      >
        <i className="fa fa-info-circle" />
      </Tooltip>
    </div>
    <DragHandle />
  </div>
);

const NumberCard = SortableElement(({
  cardTitle,
  cardNumber,
  cardNumberType,
  cardSubtitle,
  cardLink,
  cardDataTooltip,
  cardInfo,
  sparklineData = null,
  translate,
  currencyCode,
}) => {
  let isSparkline = false;
  if (sparklineData != null) {
    if (sparklineData.colorNumber != null) {
      isSparkline = true;
    }
  }
  const cardNumberLocale = cardNumber ? cardNumber.toLocaleString() : ZERO.toLocaleString();
  const card = !isSparkline ? (
    <Tooltip
      html={<p style={{ whiteSpace: 'pre' }}> {cardDataTooltip} </p>}
      theme="transparent"
      arrow="true"
      disabled={!cardDataTooltip}
    >
      <div className="number-div">
        <div className="number-body">
          <span className="title-card">
            {cardTitle.code ?
              translate(cardTitle.code, cardTitle.message)
             : cardTitle}
          </span>
          <span className="result-card"> {cardNumberType === 'number' ? cardNumberLocale : `${cardNumberLocale} ${currencyCode}`} </span>
          <span className="subtitle-card">
            {cardSubtitle.code ?
          _.truncate(translate(cardSubtitle.code, cardSubtitle.message), { length: 22 })
             : _.truncate(cardSubtitle, { length: 22 })}
          </span>
        </div>
        {
          cardInfo ?
            <div className="number-infos">
              <Tooltip
                html={
                  <p>
                    {cardInfo.code ? translate(cardInfo.code, cardInfo.message) : cardInfo.message}
                  </p>
                }
                theme="transparent"
                arrow="true"
              >
                <i className="fa fa-info-circle" />
              </Tooltip>
            </div>
        : null}
        <DragHandle />
      </div>
    </Tooltip>
  ) :
    (
      <NumberSparklineCard
        cardTitle={cardTitle}
        cardInfo={cardInfo}
        color={sparklineData.colorNumber.color}
        value={sparklineData.colorNumber.value}
        goalDifference={sparklineData.colorNumber.value2}
        sparklineData={sparklineData}
        translate={translate}
      />
    );

  return (
    cardLink ? <a target="_blank" rel="noopener noreferrer" href={cardLink} className="number-card">{card}</a> : <div className="number-card">{card}</div>
  );
});

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currencyCode: state.session.currencyCode,
});

export default (connect(mapStateToProps)(NumberCard));

NumberCard.defaultProps = {
  cardSubtitle: {
    code: '',
    message: '',
  },
};

NumberCard.propTypes = {
  cardTitle: PropTypes.shape({
    code: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
  }).isRequired,
  cardNumber: PropTypes.number,
  cardNumberType: PropTypes.string,
  cardSubtitle: PropTypes.shape({
    code: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
  }),
  cardLink: PropTypes.string,
  cardDataTooltip: PropTypes.string,
  cardInfo: PropTypes.shape({
    code: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
  }).isRequired,
  translate: PropTypes.func.isRequired,
  currencyCode: PropTypes.string.isRequired,
};

NumberSparklineCard.propTypes = {
  cardTitle: PropTypes.shape({
    code: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
  }).isRequired,
  cardInfo: PropTypes.shape({
    code: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
  }).isRequired,
  color: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  goalDifference: PropTypes.string.isRequired,
  sparklineData: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
};
