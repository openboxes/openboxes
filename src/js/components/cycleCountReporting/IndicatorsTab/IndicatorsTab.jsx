import React from 'react';

import PropTypes from 'prop-types';

import IndicatorNumberCards
  from 'components/cycleCountReporting/IndicatorsTab/IndicatorNumberCards';
import useIndicatorsTab from 'hooks/cycleCount/useIndicatorsTab';

import 'components/cycleCountReporting/cycleCountReporting.scss';

const IndicatorsTab = ({
  filterParams,
}) => {
  const {
    loading,
    numberCards,
  } = useIndicatorsTab({
    filterParams,
  });

  return (
    <div className="dashboard-container">
      <IndicatorNumberCards loading={loading} numberCards={numberCards} />
    </div>
  );
};

export default IndicatorsTab;

IndicatorsTab.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
