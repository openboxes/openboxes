import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowRightLine } from 'react-icons/ri';

import Badge from 'utils/Badge';

import 'utils/utils.scss';

/**
 * Renders a status badge, optionally followed by an arrow and the badge of the
 * status it transitions into.
 */
const BadgeTransition = ({ current, next, clickable }) => (
  <div className="badge-container badge-transition">
    <Badge label={current.label} variant={current.variant} clickable={clickable} />
    {next && (
      <>
        <RiArrowRightLine size={20} className="badge-transition__arrow" />
        <Badge label={next.label} variant={next.variant} clickable={clickable} />
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
  clickable: PropTypes.bool,
};

BadgeTransition.defaultProps = {
  next: null,
  clickable: true,
};

export default BadgeTransition;
