import React from 'react';

import PropTypes from 'prop-types';

const ListWrapper = ({ children }) => (
  <div className="d-flex flex-column list-page-main">
    {children}
  </div>
);

export default ListWrapper;

ListWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
