import React from 'react';

import PropTypes from 'prop-types';

const ListHeaderButtonsWrapper = ({ children }) => (
  <div className="d-flex justify-content-end buttons align-items-center">
    {children}
  </div>
);

export default ListHeaderButtonsWrapper;

ListHeaderButtonsWrapper.propTypes = {
  children: PropTypes.node.isRequired,
};
