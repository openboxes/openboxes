import React from 'react';

import PropTypes from 'prop-types';

import 'utils/VerticalStripeIndicator.scss';

/**
 * Full-height vertical stripe. Generic visual indicator.
 * It can be displayed via the `display` prop.
 */
const VerticalStripeIndicator = ({ display }) => {
  if (!display) {
    return null;
  }

  return <span className="vertical-stripe-indicator" aria-hidden />;
};

export default VerticalStripeIndicator;

VerticalStripeIndicator.propTypes = {
  display: PropTypes.bool,
};

VerticalStripeIndicator.defaultProps = {
  display: false,
};
