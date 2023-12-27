import React from 'react';

import PropTypes from 'prop-types';

const ListHeaderWrapper = ({ children }) => (
  <div className="d-flex list-page-header">
    {children}
  </div>
);

export default ListHeaderWrapper;

ListHeaderWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
