import React from 'react';

import PropTypes from 'prop-types';

import SortableIndicatorCards
  from 'components/cycleCountReporting/IndicatorsTab/SortableIndicatorCards';
import LoadingNumbers from 'components/dashboard/LoadingNumbers';

const IndicatorNumberCards = ({
  loading,
  tiles,
}) => (
  <div className="cards-container">
    {loading ? <LoadingNumbers /> : <SortableIndicatorCards data={tiles} /> }
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
  })),
};

IndicatorNumberCards.defaultProps = {
  tiles: [],
};

export default IndicatorNumberCards;
