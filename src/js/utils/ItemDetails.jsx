import React from 'react';

import PropTypes from 'prop-types';

import Badge from 'utils/Badge';

import 'utils/ItemDetails.scss';

/**
 * Generic component for displaying item details: an optional status badge
 * followed by a box of label/value fields.
 */
const ItemDetails = ({ badge, fields, className }) => (
  <div className={`item-details ${className}`}>
    {badge && (
      <div className="badge-container">
        <Badge label={badge.label} variant={badge.variant} />
      </div>
    )}
    <div className="item-details__grid">
      {fields.map(({ label, value }) => (
        <div key={label} className="item-details__field d-flex font-size-xs">
          <span className="item-details__label font-weight-normal text-nowrap">{`${label}:`}</span>
          <span className="item-details__value font-weight-normal">{value}</span>
        </div>
      ))}
    </div>
  </div>
);

export default ItemDetails;

ItemDetails.propTypes = {
  badge: PropTypes.shape({
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  }),
  fields: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.node.isRequired,
    value: PropTypes.node,
  })).isRequired,
  className: PropTypes.string,
};

ItemDetails.defaultProps = {
  badge: null,
  className: '',
};
