import React from 'react';
import PropTypes from 'prop-types';

const Filter = ({ filter, onChange }) => (
  <input
    onChange={event => onChange(event.target.value)}
    value={filter ? filter.value : ''}
    style={{ width: '100%' }}
    placeholder="Search"
  />
);

export default Filter;

Filter.propTypes = {
  /** Function called when filter data changes */
  onChange: PropTypes.func.isRequired,
  /** Current filter value */
  filter: PropTypes.shape({}),
};

Filter.defaultProps = {
  filter: undefined,
};
