import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchInvoiceStatuses } from 'actions';
import StatusIndicator from 'utils/StatusIndicator';

const InvoiceStatus = ({ status, allStatuses, fetchStatuses }) => {
  // Circle is by default set as primary (blue)
  const [circle, setCircle] = useState('primary');
  const findStatusCircle = (statusProp) => {
    // Example labels: "Pending", "Posted"
    const matchedStatus = allStatuses &&
      allStatuses.length > 0 &&
      allStatuses.find(stat => stat.label === statusProp);
    if (matchedStatus && matchedStatus.variant) {
      setCircle(matchedStatus.variant);
    }
  };
  // Fetch all invoice statuses
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

  return (<StatusIndicator status={status} variant={circle} />);
};

const mapStateToProps = state => ({
  // All possible invoice statuses from store
  allStatuses: state.invoices.statuses,
});

const mapDispatchToProps = {
  fetchStatuses: fetchInvoiceStatuses,
};

export default connect(mapStateToProps, mapDispatchToProps)(InvoiceStatus);

InvoiceStatus.propTypes = {
  status: PropTypes.string.isRequired,
  allStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  fetchStatuses: PropTypes.func.isRequired,
};
