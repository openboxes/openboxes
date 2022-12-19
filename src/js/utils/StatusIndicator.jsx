import React from 'react';

import PropTypes from 'prop-types';


const StatusIndicator = ({ status, variant }) => {
  // Replace _ with [spaces] and capitalize each word
  const statusToDisplay = status.split('_')
    .map(word => word.charAt(0) + word.toLowerCase().substring(1))
    .join(' ');

  return (
    <div data-testid="status-indicator" className="d-flex align-items-center justify-content-between">
      <div className="d-flex justify-content-between align-items-center">
        <span className={`${variant}-circle status-circle`} />
        <span className="px-1">{statusToDisplay}</span>
      </div>
    </div>
  );
};


export default StatusIndicator;

StatusIndicator.propTypes = {
  status: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
};
