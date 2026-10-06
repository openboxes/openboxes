import React from 'react';

import PropTypes from 'prop-types';

import VerticalStripeStatus from 'consts/verticalStripeStatus';

import 'utils/VerticalStripeIndicator.scss';

/**
 * Full-height vertical stripe. Generic visual indicator.
 * Its colour is driven by the `status` prop. A null/omitted status hides the stripe.
 */
const VerticalStripeIndicator = ({ status }) => {
  if (!status) {
    return null;
  }

  return (
    <span
      className={`vertical-stripe-indicator vertical-stripe-indicator--${status}`}
      aria-hidden
    />
  );
};

export default VerticalStripeIndicator;

VerticalStripeIndicator.propTypes = {
  status: PropTypes.oneOf(Object.values(VerticalStripeStatus)),
};

VerticalStripeIndicator.defaultProps = {
  status: null,
};
