import React from 'react';

import PropTypes from 'prop-types';

import IndicatorNumberCards
  from 'components/cycleCountReporting/IndicatorsTab/IndicatorNumberCards';
import useIndicatorsTab from 'hooks/cycleCount/useIndicatorsTab';

import 'components/cycleCountReporting/cycleCountReporting.scss';

const IndicatorsTab = ({
  filterParams,
  tablePaginationProps,
}) => {
  const { serializedParams } = tablePaginationProps;
  const {
    loading,
    tiles,
  } = useIndicatorsTab({
    filterParams,
    serializedParams,
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
  tablePaginationProps: PropTypes.shape({
    paginationProps: PropTypes.shape({}).isRequired,
    offset: PropTypes.number.isRequired,
    pageSize: PropTypes.number.isRequired,
    setTotalCount: PropTypes.func.isRequired,
    serializedParams: PropTypes.number.isRequired,
  }).isRequired,
};
