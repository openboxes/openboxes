import React from 'react';

import PropTypes from 'prop-types';

const NotificationContent = ({ data: { message, details, detailsArray } }) => (
  <div className="notification-content">
    <span className="notification-title">{message}</span>
    {detailsArray?.length > 0 && (
      <div className="d-flex flex-column notification-details-array">
        {detailsArray
          .filter(Boolean)
          .map((detail) => <p key={detail}>{detail}</p>)}
      </div>
    )}
    {details && <span className="notification-details">{details}</span>}
  </div>
);

export default NotificationContent;

NotificationContent.propTypes = {
  data: PropTypes.shape({
    message: PropTypes.node,
    details: PropTypes.node,
    detailsArray: PropTypes.arrayOf(PropTypes.node),
  }).isRequired,
};
