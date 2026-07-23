import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowRightLine } from 'react-icons/ri';

import Badge from 'utils/Badge';

import 'utils/utils.scss';

/**
 * Renders a status badge, optionally followed by an arrow and the badge of the
 * status it transitions into.
 */
const BadgeTransition = ({ current, next }) => (
  <div className="badge-container badge-transition">
    <Badge label={current.label} variant={current.variant} />
    {next && (
      <>
        <RiArrowRightLine size={20} className="badge-transition__arrow" />
        <Badge label={next.label} variant={next.variant} />
      </>
    )}
  </div>
);

const badgeShape = PropTypes.shape({
  label: PropTypes.string.isRequired,
  variant: PropTypes.string.isRequired,
});

BadgeTransition.propTypes = {
  current: badgeShape.isRequired,
  next: badgeShape,
};

BadgeTransition.defaultProps = {
  next: null,
};

export default BadgeTransition;
