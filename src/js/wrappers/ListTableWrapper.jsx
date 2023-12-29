import React from 'react';

import PropTypes from 'prop-types';

const ListTableWrapper = ({ children }) => (
  <div className="list-page-list-section">
    {children}
  </div>
);

export default ListTableWrapper;

ListTableWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
