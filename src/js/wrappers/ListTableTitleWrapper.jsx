import React from 'react';

import PropTypes from 'prop-types';

const ListTableTitleWrapper = ({ children }) => (
  <div className="title-text p-3 d-flex justify-content-between align-items-center">
    {children}
  </div>
);

export default ListTableTitleWrapper;

ListTableTitleWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
