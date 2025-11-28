import React from 'react';

import PropTypes from 'prop-types';

import IndicatorNumberCards
  from 'components/cycleCountReporting/IndicatorsTab/IndicatorNumberCards';
import useIndicatorsTab from 'hooks/cycleCount/useIndicatorsTab';

import 'components/cycleCountReporting/cycleCountReporting.scss';

const IndicatorsTab = ({
  filterParams,
  shouldFetch,
  setShouldFetch,
  filtersInitialized,
}) => {
  const {
    loading,
    tiles,
  } = useIndicatorsTab({
    filterParams,
    shouldFetch,
    setShouldFetch,
    filtersInitialized,
  });

  return (
    <div className="dashboard-container">
      <IndicatorNumberCards loading={loading} tiles={tiles} />
    </div>
  );
};

export default IndicatorsTab;

IndicatorsTab.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  shouldFetch: PropTypes.bool.isRequired,
  setShouldFetch: PropTypes.func.isRequired,
  filtersInitialized: PropTypes.bool.isRequired,
};
