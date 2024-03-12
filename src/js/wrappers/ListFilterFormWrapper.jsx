import React from 'react';

import PropTypes from 'prop-types';

const ListFilterFormWrapper = ({ children, className }) => (
  <div className={`d-flex flex-column list-page-filters ${className}`}>
    {children}
  </div>
);

export default ListFilterFormWrapper;

ListFilterFormWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

ListFilterFormWrapper.defaultProps = {
  className: '',
};
