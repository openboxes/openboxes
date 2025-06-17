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
    title: PropTypes.string,
    titleDefaultValue: PropTypes.string,
    numberType: PropTypes.string,
    number: PropTypes.number,
    type: PropTypes.string,
    subtitle: PropTypes.string,
    subtitleDefaultValue: PropTypes.string,
    subtitleValue: PropTypes.string,
    info: PropTypes.string,
    infoDefaultValue: PropTypes.string,
    showPercentSign: PropTypes.bool,
  })),
};

IndicatorNumberCards.defaultProps = {
  tiles: [],
};

export default IndicatorNumberCards;
