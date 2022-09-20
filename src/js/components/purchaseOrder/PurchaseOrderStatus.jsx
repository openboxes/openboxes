import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchPurchaseOrderStatuses } from 'actions';

const PurchaseOrderStatus = ({ status, allStatuses, fetchStatuses }) => {
  // Circle is by default set as primary (blue)
  const [circle, setCircle] = useState('primary');
  const findStatusCircle = (statusProp) => {
    // Example ids: "PENDING", "PRIMARY"
    const matchedStatus = allStatuses &&
      allStatuses.length > 0 &&
      allStatuses.find(stat => stat.id === statusProp);
    if (matchedStatus && matchedStatus.variant) {
      setCircle(matchedStatus.variant);
    }
  };
  // Fetch all PO statuses
  useEffect(() => {
    // If statuses not yet in store, fetch them
    if (allStatuses.length === 0) {
      fetchStatuses();
    }
  }, []);

  // If statuses change or status (status prop can change when filter/sort data) find circle
  useEffect(() => {
    findStatusCircle(status);
  }, [allStatuses, status]);

  // Capitalize only first letter of status
  const statusToDisplay = status.charAt(0) + status.toLowerCase().slice(1);

  return (
    <div className="d-flex align-items-center justify-content-between">
      <div className="d-flex justify-content-between align-items-center">
        <span className={`${circle}-circle status-circle`} />
        <span className="px-1">{statusToDisplay}</span>
      </div>
    </div>
  );
};

const mapStateToProps = state => ({
  // All possible PO statuses from store
  allStatuses: state.purchaseOrder.statuses,
});

const mapDispatchToProps = {
  fetchStatuses: fetchPurchaseOrderStatuses,
};

export default connect(mapStateToProps, mapDispatchToProps)(PurchaseOrderStatus);

PurchaseOrderStatus.propTypes = {
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
