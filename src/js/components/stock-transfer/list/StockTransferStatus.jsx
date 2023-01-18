import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchStockTransferStatuses } from 'actions';
import StatusIndicator from 'utils/StatusIndicator';

const StockTransferStatus = ({
  status,
  allStatuses,
  fetchStatuses,
}) => {
  // Circle is by default set as primary (blue)
  const [circle, setCircle] = useState('primary');
  const [statusLabel, setStatusLabel] = useState('');
  const findStatusCircle = (statusProp) => {
    // Example ids: "PENDING", "PRIMARY"
    const matchedStatus = allStatuses?.length > 0 &&
      allStatuses.find(stat => stat.label === statusProp);
    if (matchedStatus && matchedStatus.variant) {
      setCircle(matchedStatus.variant);
      setStatusLabel(matchedStatus.label);
    }
  };
  // Fetch all stock transfer statuses
  useEffect(() => {
    // TODO: When having full React, fetch only if not fetched yet or length is 0
    fetchStatuses();
  }, []);

  // If statuses change or status (status prop can change when filter/sort data) find circle
  useEffect(() => {
    findStatusCircle(status);
  }, [allStatuses, status]);

  return (<StatusIndicator status={statusLabel} variant={circle} />);
};

const mapStateToProps = state => ({
  // All possible stock transfer statuses from store
  allStatuses: state.stockTransfer.statuses,
  stockTransferSessionVersion: state.stockTransfer.sessionVersion,
});

const mapDispatchToProps = {
  fetchStatuses: fetchStockTransferStatuses,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockTransferStatus);

StockTransferStatus.propTypes = {
  // Status props is uppercased string e.g. "PENDING"
  status: PropTypes.string.isRequired,
  allStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  fetchStatuses: PropTypes.func.isRequired,
};
