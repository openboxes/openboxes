import React from 'react';

import PropTypes from 'prop-types';

const ListFilterFormWrapper = ({ children }) => (
  <div className="d-flex flex-column list-page-filters">
    {children}
  </div>
);

export default ListFilterFormWrapper;

ListFilterFormWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
