import React from 'react';

import PropTypes from 'prop-types';

const BadgeCount = ({ count }) => (
  <span className="d-flex justify-content-center align-items-center badge-count">
    {count}
  </span>
);

export default BadgeCount;

BadgeCount.propTypes = {
  count: PropTypes.number.isRequired,
};
