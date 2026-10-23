import React from 'react';

import PropTypes from 'prop-types';

import BadgeTransition from 'utils/BadgeTransition';

import 'utils/ItemDetails.scss';

/**
 * Generic component for displaying item details
 */
const ItemDetails = ({
  badge, fields, className, children,
}) => (
  <div className={`item-details ${className}`}>
    {badge && (
      <BadgeTransition
        current={badge.current}
        next={badge.next}
        clickable={badge.clickable}
        tooltip={badge.tooltip}
      />
    )}
    <div className="item-details__grid">
      {fields.map(({
        label, value, className: fieldClassName = '', testId,
      }) => (
        <div key={label} className={`item-details__field d-flex font-size-xs ${fieldClassName}`}>
          <span className="item-details__label font-weight-normal text-nowrap">{`${label}:`}</span>
          <span className="item-details__value font-weight-normal" data-testid={testId}>{value}</span>
        </div>
      ))}
      {children}
    </div>
  </div>
);

export default ItemDetails;

ItemDetails.propTypes = {
  badge: PropTypes.shape({
    current: PropTypes.shape({
      label: PropTypes.string.isRequired,
      variant: PropTypes.string.isRequired,
    }).isRequired,
    /** Status the badge transitions into, rendered after an arrow when provided */
    next: PropTypes.shape({
      label: PropTypes.string.isRequired,
      variant: PropTypes.string.isRequired,
    }),
    clickable: PropTypes.bool,
    tooltip: PropTypes.string,
  }),
  fields: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.node.isRequired,
    value: PropTypes.node,
    className: PropTypes.string,
    /** data-testid of the field value */
    testId: PropTypes.string,
  })).isRequired,
  className: PropTypes.string,
  /** Additional elements rendered as grid items after the fields */
  children: PropTypes.node,
};

ItemDetails.defaultProps = {
  badge: null,
  className: '',
  children: null,
};
