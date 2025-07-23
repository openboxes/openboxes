import React from 'react';

import PropTypes from 'prop-types';

import IndicatorCards
  from 'components/cycleCountReporting/IndicatorsTab/IndicatorCards';
import LoadingNumbers from 'components/dashboard/LoadingNumbers';

const IndicatorNumberCards = ({
  loading,
  tiles,
}) => (
  <div className="cards-container">
    {loading ? <LoadingNumbers numberOfLoadingCards={3} /> : <IndicatorCards data={tiles} />}
  </div>
);

IndicatorNumberCards.propTypes = {
  loading: PropTypes.bool.isRequired,
  tiles: PropTypes.arrayOf(PropTypes.shape({
    titleLabel: PropTypes.string,
    defaultTitle: PropTypes.string,
    numberType: PropTypes.string,
    value: PropTypes.number,
    name: PropTypes.string,
    subtitleLabel: PropTypes.string,
    defaultSubtitle: PropTypes.string,
    subValue: PropTypes.string,
    infoLabel: PropTypes.string,
    defaultInfo: PropTypes.string,
    showPercentSign: PropTypes.bool,
    type: PropTypes.string,
    firstValue: PropTypes.number,
    secondValue: PropTypes.number,
    firstSubtitleLabel: PropTypes.string,
    defaultFirstSubtitle: PropTypes.string,
    secondSubtitleLabel: PropTypes.string,
    defaultSecondSubtitle: PropTypes.string,
    showFirstValuePercentSign: PropTypes.bool,
    formatSecondValueAsCurrency: PropTypes.bool,
  })),
};

IndicatorNumberCards.defaultProps = {
  tiles: [],
};

export default IndicatorNumberCards;
