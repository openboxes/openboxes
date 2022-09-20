import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';

import apiClient from 'utils/apiClient';

const PurchaseOrderStatus = ({ status }) => {
  const [statuses, setStatuses] = useState([]);
  // Circle is by default set as primary (blue)
  const [circle, setCircle] = useState('primary');
  const findStatusCircle = (statusProp) => {
    // Example ids: "PENDING", "PRIMARY"
    const matchedStatus = statuses &&
      statuses.length > 0 &&
      statuses.find(stat => stat.id === statusProp);
    if (matchedStatus && matchedStatus.variant) {
      setCircle(matchedStatus.variant);
    }
  };
  // Fetch all PO statuses
  useEffect(() => {
    apiClient.get('/openboxes/api/orderSummaryStatus')
      .then((res) => {
        setStatuses(res.data.data);
      });
  }, []);

  // If statuses change or status (status prop can change when filter/sort data) find circle
  useEffect(() => {
    findStatusCircle(status);
  }, [statuses, status]);

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

export default PurchaseOrderStatus;

PurchaseOrderStatus.propTypes = {
  // Status props is uppercased string e.g. "PENDING"
  status: PropTypes.string.isRequired,
};
