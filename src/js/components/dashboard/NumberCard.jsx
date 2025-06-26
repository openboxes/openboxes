import React from 'react';

import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { SortableElement } from 'react-sortable-hoc';
import { Tooltip } from 'react-tippy';

import DragHandle from 'components/dashboard/DragHandle';
import { getColorByName } from 'consts/dataFormat/colorMapping';
import { translateWithDefaultMessage } from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

import 'components/dashboard/Dashboard.scss';

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
  cardTitle, cardInfo, color, value, goalDifference, sparklineData, translate, cardInfoDefaultValue,
}) => (
  <div className="number-div">
    <div className="number-body">
      <span className="title-card">
        {translate(cardTitle, cardTitle)}
      </span>
      <div className="result-part">
        <span style={{ color: getColorByName(color, 'default') }}>
          {' '}
          {value}
          {' '}
        </span>
        <span className="goal-difference">
          {' '}
          {goalDifference}
          {' '}
        </span>
      </div>

      <Line
        data={sparklineData}
        options={options}
        height={25}
      />
    </div>
    <div className="number-infos">
      <CustomTooltip
        content={translate(cardInfo, cardInfoDefaultValue || cardInfo)}
        show={cardInfo}
      >
        <i className="fa fa-info-circle" />
      </CustomTooltip>
    </div>
    <DragHandle />
  </div>
);

const NumberCard = SortableElement(({
  cardTitle,
  cardTitleDefaultValue,
  cardNumber,
  cardNumberType,
  cardSubtitle,
  cardSubtitleDefaultValue,
  cardSubtitleValue,
  cardLink,
  cardDataTooltip,
  cardInfo,
  cardInfoDefaultValue,
  sparklineData = null,
  translate,
  currencyCode,
  hideDraghandle,
  showPercentSign,
  infoIcon,
  disableSubtitleEllipsis,
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
      html={(
        <p style={{ whiteSpace: 'pre' }}>
          {' '}
          {cardDataTooltip}
          {' '}
        </p>
)}
      theme="transparent"
      arrow="true"
      disabled={!cardDataTooltip}
    >
      <div className="number-div">
        <div className="number-body">
          <span className="title-card">
            {translate(cardTitle, cardTitleDefaultValue || cardTitle)}
          </span>
          <span className="result-card">
            {' '}
            {cardNumberType === 'number' ? `${cardNumberLocale}${showPercentSign ? '%' : ''}` : `${cardNumberLocale} ${currencyCode}`}
            {' '}
          </span>
          <span className={`subtitle-card ${disableSubtitleEllipsis ? '' : ' text-overflow-ellipsis text-nowrap'}`}>
            {cardSubtitleValue}
            {' '}
            {translate(cardSubtitle, cardSubtitleDefaultValue || cardSubtitle)}
          </span>
        </div>
        {
          cardInfo
            ? (
              <div className="number-infos">
                <CustomTooltip
                  content={translate(cardInfo, cardInfoDefaultValue || cardInfo)}
                  theme="transparent"
                  arrow="true"
                >
                  {infoIcon || <i className="fa fa-info-circle" />}
                </CustomTooltip>
              </div>
            )
            : null
}
        {!hideDraghandle && <DragHandle />}
      </div>
    </Tooltip>
  )
    : (
      <NumberSparklineCard
        cardTitle={cardTitle}
        cardInfo={cardInfo}
        cardInfoDefaultValue={cardInfoDefaultValue}
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

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currencyCode: state.session.currencyCode,
});

export default (connect(mapStateToProps)(NumberCard));

NumberCard.defaultProps = {
  cardTitleDefaultValue: '',
  cardSubtitle: '',
  cardSubtitleDefaultValue: '',
  cardSubtitleValue: '',
  cardLink: '',
  cardDataTooltip: '',
  cardInfoDefaultValue: '',
  sparklineData: null,
  showPercentSign: false,
  infoIcon: null,
  disableSubtitleEllipsis: false,
};

NumberCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardTitleDefaultValue: PropTypes.string,
  cardNumber: PropTypes.number,
  cardNumberType: PropTypes.string,
  cardSubtitle: PropTypes.string,
  cardSubtitleDefaultValue: PropTypes.string,
  cardSubtitleValue: PropTypes.string,
  cardLink: PropTypes.string,
  cardDataTooltip: PropTypes.string,
  cardInfo: PropTypes.string.isRequired,
  cardInfoDefaultValue: PropTypes.string,
  translate: PropTypes.func.isRequired,
  currencyCode: PropTypes.string.isRequired,
  showPercentSign: PropTypes.bool,
  infoIcon: PropTypes.node,
  disableSubtitleEllipsis: PropTypes.bool,
};

NumberSparklineCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardInfo: PropTypes.string.isRequired,
  cardInfoDefaultValue: PropTypes.string,
  color: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  goalDifference: PropTypes.string.isRequired,
  sparklineData: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
};

NumberSparklineCard.defaultProps = {
  cardInfoDefaultValue: '',
};
